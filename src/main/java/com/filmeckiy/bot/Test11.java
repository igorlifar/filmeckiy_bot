package com.filmeckiy.bot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author egor
 */
public class Test11 {
    private static final Logger logger = LogManager.getLogger(Test11.class);

    public static void main(String[] args) {
        logger.info("http://kinopoisk.ru/name/12312/".split("/", 4)[2]);
        logger.info("http://kinopoisk.ru/name/12312/".split("/", 4)[3]);
    }
}
