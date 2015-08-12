package com.filmeckiy.bot.kp;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author egor
 */
public class MongoUtils {
    private static final Logger logger = LogManager.getLogger(MongoUtils.class);

    public static final String DB_NAME = "filmeckiy_bot";

    public static MongoClient getClient() {
        MongoClientOptions mongoOptions = MongoClientOptions.builder()
                .connectionsPerHost(50)
                .connectTimeout(120000)
                .socketTimeout(120000)
                .maxWaitTime(120000)
                .threadsAllowedToBlockForConnectionMultiplier(20)
                .writeConcern(new WriteConcern(1, 2500, false))
                .readPreference(ReadPreference.primaryPreferred())
                .build();

        return new MongoClient("server.indieworkshop.ru", mongoOptions);
    }
}
