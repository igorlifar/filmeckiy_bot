package com.filmeckiy.bot;

import com.filmeckiy.bot.kp.KpClient;
import com.filmeckiy.bot.kp.MongoUtils;
import com.mongodb.client.FindIterable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

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
            String url = doc.getString("_id");
            if (doc.getString("text").contains("с вашего IP-адреса поступило необычно много запросов")) {
                logger.info("Found banned page in cache: {}", url);

                MongoUtils.getClient()
                        .getDatabase(MongoUtils.DB_NAME)
                        .getCollection("pages_cache")
                        .deleteOne(new Document("_id", url));

                kpClient.addToQueue(url);
            }
        }
    }
}
