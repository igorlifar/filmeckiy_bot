package com.filmeckiy.bot.kp;

import com.filmeckiy.bot.utils.Option;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.util.HashSet;

/**
 * @author egor
 */
public class KpClientTest {
    Logger logger = LogManager.getLogger(KpClientTest.class);

    @Test
    public void testGetText() {
        KpClient client = new KpClient();
        String text = client.getText("http://kinopoisk.ru");
       // System.out.println(text);
    }

    @Test
    public void testGetCached() {
        KpClient client = new KpClient();

        String cachedText = client.getCachedOrUpdate("http://www.kinopoisk.ru/film/407636/");

        logger.info("Text size: {}", cachedText.length());

        Elements elements = Jsoup.parse(cachedText).select("a[href^=\"/film/\"]");

        HashSet<Integer> ids = new HashSet<>();
        elements.forEach(e -> ids.add(Integer.parseInt(e.attr("href").split("/")[2])));

        logger.info(ids);
    }

    @Test
    public void testQueue() {
        KpClient kpClient = new KpClient();

        kpClient.addToQueue("http://www.kinopoisk.ru/film/407636/");

        kpClient.addToQueue("http://www.kinopoisk.ru/film/258687/");
        kpClient.addToQueue("http://www.kinopoisk.ru/film/258687/");

        kpClient.addToQueue("http://www.kinopoisk.ru/film/468466/");

        logger.info(kpClient.popFromQueue());
        logger.info(kpClient.popFromQueue());
        logger.info(kpClient.popFromQueue());
    }
}