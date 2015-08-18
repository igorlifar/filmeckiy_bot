package com.filmeckiy.bot;

import com.filmeckiy.bot.kp.Film;
import com.filmeckiy.bot.kp.MongoUtils;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

/**
 * @author egor
 */
public class ParseMoviesMain {
    private static final Logger logger = LogManager.getLogger(ParseMoviesMain.class);

    public static void main(String[] args) {
        MongoCollection<Document> collection = MongoUtils
                .getClient()
                .getDatabase(MongoUtils.DB_NAME)
                .getCollection("movies");

        FindIterable<Document> documents = MongoUtils
                .getClient()
                .getDatabase(MongoUtils.DB_NAME)
                .getCollection("pages_cache").find();

        for (Document document : documents) {
            String url = document.getString("_id");
            if (!url.contains("film")) {
                continue;
            }

            Film film = Film.fromText(url, document.getString("text"));
            Film.addFilmToMongo(film, collection);

            logger.info("added film {}", url);
        }
    }
}
