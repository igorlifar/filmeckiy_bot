package com.filmeckiy.bot;

import com.filmeckiy.bot.kp.KpClient;
import com.filmeckiy.bot.kp.MongoUtils;
import com.mongodb.client.FindIterable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.util.List;

/**
 * @author egor
 */
public class FixPagesCacheMain {
    private static final Logger logger = LogManager.getLogger(FixPagesCacheMain.class);

    public static void main(String[] args) {
        KpClient kpClient = new KpClient();

        FindIterable<Document> iter = MongoUtils.getClient()
                .getDatabase(MongoUtils.DB_NAME)
                .getCollection("pages_cache")
                .find();

        for (Document doc : iter) {
            List<String> urls = kpClient.getBfsUrls(doc.getString("_id"));
            urls.forEach(kpClient::addToQueue);
        }
    }
}
