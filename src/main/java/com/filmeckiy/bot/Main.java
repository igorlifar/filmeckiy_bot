package com.filmeckiy.bot;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.filmeckiy.bot.kp.Film;
import com.filmeckiy.bot.kp.MongoUtils;
import com.mongodb.client.FindIterable;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

/**
 * @author lifar
 */
public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static class Tuple<T1, T2> {
        private final T1 _1;
        private final T2 _2;

        public Tuple(T1 _1, T2 _2) {
            this._1 = _1;
            this._2 = _2;
        }

        public static class Cmp<T1 extends Comparable<T1>, T2> implements Comparator<Tuple<T1, T2>> {
            @Override
            public int compare(Tuple<T1, T2> o1, Tuple<T1, T2> o2) {
                return o1._1.compareTo(o2._1);
            }
        }
    }


    public static int equals(List<String> a, List<String> b) {
        int n = a.size();
        int m = b.size();
        int l = 0;
        int k = 0;
        for (int i = 0; i < n; i++) {
            while (l < m) {
                int maxk = (Math.min(a.get(i).length(), b.get(i).length()) + 2) / 4;
                if (StringUtils.nop(a.get(i), b.get(l), maxk) >= Math.min(a.get(i).length(), b.get(i).length()))  {
                    break;
                }
                l++;
            }
            if (l != m) {
                k++;
            }
            l++;
        }
        return k;
    }


    public static void main(String[] args) {
        logger.info("Lol, privetik");

        FindIterable<Document> documents = MongoUtils.getClient()
                .getDatabase(MongoUtils.DB_NAME)
                .getCollection("movies")
                .find();

        List<Film> movies = new ArrayList<>();

        for (org.bson.Document doc : documents) {
            try {
                Film filmec = Film.getMoviefromDocument(doc);
                if (filmec.year.isDefined() && !filmec.year.get().equals("-") && Long.parseLong(filmec.year.get()) >= 1975) {
                    movies.add(filmec);
                }
            } catch (Exception e) {
                logger.error("Failed on film: {} {}", doc.toJson());
                continue;
            }
        }
        logger.info("Read {} movies", movies.size());

        long previousMaxUpdateId = 0;

        while (true) {
            CloseableHttpClient client = HttpClients.createDefault();

            HttpGet get = new HttpGet(
                    "https://api.telegram.org/bot98005573:AAG-tn1xzJQkt3h1adyM3mAzAL9loIY2ruk/getUpdates?offset="
                            + String.valueOf(previousMaxUpdateId + 1));

            try {
                CloseableHttpResponse result = client.execute(get);
                logger.info(result.getStatusLine());

                HttpEntity entity = result.getEntity();

                StringWriter stringWriter = new StringWriter();
                IOUtils.copy(entity.getContent(), stringWriter);

                logger.info(stringWriter.toString());

                ObjectMapper om = new ObjectMapper();
                JsonNode root = om.readTree(stringWriter.toString());

                logger.info("OK: {}", root.path("ok").booleanValue());

                long newMaxUpdateId = 0;
                for (int i = 0; i < root.path("result").size(); i++) {
                    JsonNode update = root.path("result").get(i);

                    long updateId = update.path("update_id").longValue();

                    if (updateId <= previousMaxUpdateId) {
                        continue;
                    }

                    newMaxUpdateId = Math.max(updateId, newMaxUpdateId);

                    long from = update.path("message").path("from").path("id").longValue();
                    String text = update.path("message").path("text").textValue();

                    if (text.equals("/cancel")) {
                        continue;
                    }

                    ArrayList<Tuple<Integer, Film>> tupleList = new ArrayList<>();

                    int maxScore = 0;
                    Film res = null;
                    for (Film film : movies) {
                        int score = equals(
                                StringUtils.main(text),
                                StringUtils.main(film.title + " " + film.year.getOrElse("")));

                        tupleList.add(new Tuple<>(score, film));

                        if (score > maxScore) {
                            maxScore = score;
                            res = film;
                        }
                    }

                    tupleList.sort(new Tuple.Cmp<>());

                    logger.info("Query: {}, Title: {}, Dist: {}", text, res, maxScore);
                    String newText = Film.getText(res);


                    logger.info("Update id: {}, from: {}, text: {}", updateId, from, text);


                    ObjectNode objectNode = om.createObjectNode();
                    ArrayNode arrayNode = om.createArrayNode();

                    for (int j = 0; j < 2; j++) {
                        arrayNode.add(om
                                .createArrayNode()
                                .add(getSuggestText(tupleList.get(tupleList.size() - 1 - j * 2)))
                                .add(getSuggestText(tupleList.get(tupleList.size() - 1 - (j * 2 + 1)))));
                    }
                    arrayNode.add(om.createArrayNode().add("/cancel"));

                    objectNode.set("keyboard", arrayNode);
                    objectNode.put("one_time_keyboard", true);

                    URIBuilder uriBuilder = new URIBuilder();
                    uriBuilder
                            .setScheme("https")
                            .setHost("api.telegram.org/")
                            .setPath("bot98005573:AAG-tn1xzJQkt3h1adyM3mAzAL9loIY2ruk/sendMessage")
                            .setParameter("chat_id", String.valueOf(from))
                            .setParameter("text", newText)
                            .setParameter("reply_markup", objectNode.toString());

                    URI uri;
                    try {
                        uri = uriBuilder.build();
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                    logger.info(uri.toString());
                    HttpGet httpGet = new HttpGet(uri);

                    CloseableHttpResponse execute = client.execute(httpGet);
                    logger.info(execute.getStatusLine());

                    execute.close();
                }

                if (newMaxUpdateId != 0) {
                    previousMaxUpdateId = newMaxUpdateId;
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static String getSuggestText(Tuple<Integer, Film> film1) {
        return film1._2.title + " " + film1._2.year.getOrElse("");
    }
}
