package com.filmeckiy.bot;

import com.filmeckiy.bot.kp.KpClient;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.util.HashSet;

/**
 * @author lifar
 */
public class Main3 {
    private static final Logger logger = LogManager.getLogger(Main3.class);

    public static void main(String[] args) {
        KpClient client = new KpClient();

        String cachedText = client.getCachedOrUpdate("http://www.kinopoisk.ru/film/407636/");

        logger.info("Text size: {}", cachedText.length());

        Elements elements = Jsoup.parse(cachedText).select("a[href^=\"/film/\"]");

        HashSet<Integer> ids = new HashSet<>();
        elements.forEach(e -> ids.add(Integer.parseInt(e.attr("href").split("/")[2])));

//        new Document("123", new Document)

        ids.forEach(id -> client.getCachedOrUpdate("http://www.kinopoisk.ru/film/" + String.valueOf(id) + "/"));
    }
}
