package com.filmeckiy.bot;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public static int levenshtein(String a, String b) {
        int[][] m = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                m[i][j] = 1000000;
            }
        }
        m[0][0] = 0;
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (a.length() == i && b.length() == j) {
                    continue;
                }
                if (a.length() == i) {
                    m[i][j + 1] = Math.min(m[i][j + 1], m[i][j] + 1);
                    continue;
                }
                if (b.length() == j) {
                    m[i + 1][j] = Math.min(m[i + 1][j], m[i][j] + 1);
                    continue;
                }
                m[i + 1][j + 1] = Math.min(m[i + 1][j + 1], m[i][j] + 2);
                if (a.charAt(i) == b.charAt(j)) {
                    m[i + 1][j + 1] = Math.min(m[i + 1][j + 1], m[i][j]);
                }
                m[i + 1][j] = Math.min(m[i + 1][j], m[i][j] + 1);
                m[i][j + 1] = Math.min(m[i][j + 1], m[i][j] + 1);
            }
        }
        return m[a.length()][b.length()];
    }

    public static void main(String[] args) {
        logger.info("Lol, privetik");

        HashMap<String, Film> titleToFilm = new HashMap<>();

        FindIterable<Document> documents = MongoUtils.getClient()
                .getDatabase(MongoUtils.DB_NAME)
                .getCollection("movies")
                .find();

        List<Film> movies = new ArrayList<>();

        for (org.bson.Document doc : documents) {
            Film filmec = Film.getMoviefromDocument(doc);
//            titleToFilm.put(filmec.title, filmec);
            movies.add(filmec);
        }
        logger.info("Read {} movies", titleToFilm.size());

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

                    int minDist = 1000;
                    Film res = null;
                    for (Film film : movies) {
                        int dist = levenshtein(text, film.title);

                        if (dist < minDist) {
                            minDist = dist;
                            res = film;
                        }
                    }

                    String newText = Film.getText(res);


                    logger.info("Update id: {}, from: {}, text: {}", updateId, from, text);

                    URIBuilder uriBuilder = new URIBuilder();
                    uriBuilder
                            .setScheme("https")
                            .setHost("api.telegram.org/")
                            .setPath("bot98005573:AAG-tn1xzJQkt3h1adyM3mAzAL9loIY2ruk/sendMessage")
                            .setParameter("chat_id", String.valueOf(from))
                            .setParameter("text", newText);

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
}
