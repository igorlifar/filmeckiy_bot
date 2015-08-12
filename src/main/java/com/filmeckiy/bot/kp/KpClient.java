package com.filmeckiy.bot.kp;

import com.filmeckiy.bot.utils.Option;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Projections;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import javax.print.Doc;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author egor
 */
public class KpClient {
    private static final Logger logger = LogManager.getLogger(KpClient.class);

    private final CloseableHttpClient httpClient;
    private final MongoClient mongoClient;

    private final HashSet<String> urlsInQueue;
    private final HashSet<String> urlsInCache;

    public KpClient() {
        this.httpClient = HttpClients.createDefault();
        this.mongoClient = MongoUtils.getClient();

        this.urlsInCache = new HashSet<>();
        this.urlsInQueue = new HashSet<>();

        for (Document doc : mongoClient.getDatabase(MongoUtils.DB_NAME).getCollection("pages_cache").find().projection(Projections.include("_id"))) {
            this.urlsInCache.add(doc.getString("_id"));
        }
        logger.info("Urls in cache: {}", this.urlsInCache.size());

        for (Document doc : mongoClient.getDatabase(MongoUtils.DB_NAME).getCollection("bfs_queue").find().projection(Projections.include("_id"))) {
            this.urlsInQueue.add(doc.getString("_id"));
        }
        logger.info("Urls in queue: {}", this.urlsInQueue.size());
    }

    public String getText(String url) {
        logger.info("Going to execute request: {}", url);
        HttpGet request = new HttpGet(url);
        request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        request.setHeader(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/600.4.10 " +
                        "(KHTML, like Gecko) Version/8.0.4 Safari/600.4.10");

        HttpEntity entity;
        StringWriter stringWriter = new StringWriter();

        try (CloseableHttpResponse response = httpClient.execute(request)) {
            entity = response.getEntity();
            logger.info("Status line: {}", response.getStatusLine());
            IOUtils.copy(new InputStreamReader(entity.getContent(), "windows-1251"), stringWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String result = stringWriter.toString();

        if (result.contains("с вашего IP-адреса поступило необычно много запросов")) {
            logger.warn("NAS ZABANILI");
            throw new RuntimeException("zabanili");
        }

        logger.info("Text size: {}", result.length());
        return result;
    }

    private void updateHtmlInMongo(String url, String text) {
        logger.info("Going to update cached text for url {}", url);
        Document document = new Document()
                .append("_id", url)
                .append("text", text);

        mongoClient
                .getDatabase(MongoUtils.DB_NAME)
                .getCollection("pages_cache")
                .insertOne(document);

        this.urlsInCache.add(url);
    }

    public Option<String> getCachedText(String url) {
        FindIterable<Document> documents = mongoClient
                .getDatabase(MongoUtils.DB_NAME)
                .getCollection("pages_cache")
                .find(new Document().append("_id", url));

        ArrayList<Document> list = new ArrayList<>();
        for (Document doc : documents) {
            list.add(doc);
        }

        if (list.size() == 0) {
            return Option.none();
        }

        if (list.size() == 1) {
            return Option.some(list.get(0).get("text").toString());
        }

        throw new RuntimeException("WTF??");
    }

    public String getCachedOrUpdate(String url) {
        logger.info("Looking for text for url {}", url);
        Option<String> cached = getCachedText(url);
        if (cached.isDefined()) {
            logger.info("Found text in cache");
            return cached.get();
        }

        String text = getText(url);
        updateHtmlInMongo(url, text);
        return text;
    }

    public void addToQueue(String url) {
        MongoCollection<Document> queueCollection = mongoClient
                .getDatabase(MongoUtils.DB_NAME)
                .getCollection("bfs_queue");

        logger.info("Going to insert url {} to queue", url);
//        long count = queueCollection.count(new Document("_id", url));
//        if (count != 0) {
        if (this.urlsInQueue.contains(url)) {
            logger.info("Url {} is already in queue", url);
            return;
        }

//        long countPages = mongoClient
//                .getDatabase(MongoUtils.DB_NAME)
//                .getCollection("pages_cache")
//                .count(new Document("_id", url));
//
//        if (countPages != 0) {
        if (this.urlsInCache.contains(url)) {
            logger.info("Url {} is already in pages cache");
            return;
        }

        queueCollection.insertOne(new Document("_id", url).append("timestamp", System.currentTimeMillis()));
        logger.info("Added url {} to queue", url);

        this.urlsInQueue.add(url);
    }

    public Option<String> popFromQueue() {
        MongoCollection<Document> queueCollection = mongoClient
                .getDatabase(MongoUtils.DB_NAME)
                .getCollection("bfs_queue");

        FindIterable<Document> iter = queueCollection.find().sort(new Document("timestamp", 1)).limit(1);

        ArrayList<Document> documents = new ArrayList<>();
        for (Document doc : iter) {
            documents.add(doc);
        }

        if (documents.size() == 0) {
            logger.info("Found 0 urls in queue");
            return Option.none();
        }

        String url = documents.get(0).get("_id").toString();
        logger.info("Found url {}", url);

        logger.info("Removing url {} from queue", url);
        queueCollection.deleteOne(new Document("_id", url));
        this.urlsInQueue.remove(url);
        return Option.some(url);
    }

    public List<String> getMovieUrls(String url) {
        String text = getCachedOrUpdate(url);

        Elements elements = Jsoup.parse(text).select("a[href^=\"/film/\"]");

        HashSet<Integer> ids = new HashSet<>();
        elements.forEach(e -> ids.add(Integer.parseInt(e.attr("href").split("/")[2])));

        ArrayList<String> result = new ArrayList<>();
        ids.forEach(id -> result.add(String.format("http://www.kinopoisk.ru/film/%d/", id)));
        return result;
    }
}
