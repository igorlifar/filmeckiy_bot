package com.filmeckiy.bot;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

/**
 * @author lifar
 */
public class Main3 {
    private static final Logger logger = LogManager.getLogger(Main3.class);

    public static void main(String[] args) {
        MongoClientOptions mongoOptions = MongoClientOptions.builder()
                .connectionsPerHost(50)
                .connectTimeout(120000)
                .socketTimeout(120000)
                .maxWaitTime(120000)
                .threadsAllowedToBlockForConnectionMultiplier(20)
                .writeConcern(new WriteConcern(1, 2500, false))
                .readPreference(ReadPreference.primaryPreferred())
                .build();

        MongoClient client = new MongoClient("server.indieworkshop.ru", mongoOptions);

        MongoDatabase db = client.getDatabase("filmeckiy_bot");

        db.createCollection("movies");

        Document object = new Document("foo", "bar").append("key", "val").append("_id", 10);

        db.getCollection("movies").insertOne(object);
    }
}
