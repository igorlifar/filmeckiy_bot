package com.filmeckiy.bot;

import com.filmeckiy.bot.kp.KpClient;
import com.filmeckiy.bot.kp.MongoUtils;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
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

        MongoCollection<Document> pagesCacheCollection = MongoUtils.getClient()
                .getDatabase(MongoUtils.DB_NAME)
                .getCollection("pages_cache");

        for (Document doc : pagesCacheCollection.find()) {
            int length = doc.getString("text").length();

            if (length < 500) {
                String url = doc.getString("_id");
                logger.info("Found small text: {} {}", url, length);

                pagesCacheCollection.deleteOne(new Document("_id", url));
            }
        }

        KpClient kpClient = new KpClient();
        for (Document doc : pagesCacheCollection
                .find()) {
            List<String> urls = kpClient.getBfsUrls(doc.getString("_id"));
            urls.forEach(kpClient::addToQueue);
        }
    }
}
