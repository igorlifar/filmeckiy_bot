package com.filmeckiy.bot;

import com.filmeckiy.bot.kp.KpClient;
import com.filmeckiy.bot.utils.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * @author egor
 */
public class KpCrawlMain {
    private static final Logger logger = LogManager.getLogger(KpCrawlMain.class);

    public static void main(String[] args) {
        KpClient kpClient = new KpClient();

        kpClient.addToQueue("http://www.kinopoisk.ru/top/");

        Option<String> next = kpClient.popFromQueue();
        logger.info("Next: {}", next);
        while (next.isDefined()) {
            List<String> movieUrls;
            try {
                movieUrls = kpClient.getMovieUrls(next.get());
            } catch (Exception e) {
                kpClient.addToQueue(next.get());

                throw e;
            }

            logger.info("Found {} movie urls", movieUrls.size());
            for (String movieUrl : movieUrls) {
                kpClient.addToQueue(movieUrl);
            }

            next = kpClient.popFromQueue();
        }
    }
}
