package com.filmeckiy.bot;

import com.filmeckiy.bot.kp.MongoUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import java.util.ArrayList;

/**
 * @author egor
 */
public class Main4 {
    private static final Logger logger = LogManager.getLogger(Main4.class);

    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>();

        list.add(10);
        list.add(20);
        list.add(30);

        MongoUtils.getClient().getDatabase("filmeckiy_bot").getCollection("bfs_queue").insertOne(new Document("_id", 0).append("queue", list));
    }
}
