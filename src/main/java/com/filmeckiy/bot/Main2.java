package com.filmeckiy.bot;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * @author lifar
 */
public class Main2 {
    private static final Logger logger = LogManager.getLogger(Main2.class);

    public static void main(String[] args) {


        try {
            HttpGet request = new HttpGet("http://www.kinopoisk.ru/film/678549/");
            request.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            request.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/600.4.10 (KHTML, like Gecko) Version/8.0.4 Safari/600.4.10");

            CloseableHttpClient client = HttpClients.createDefault();

            HttpEntity entity = client.execute(request).getEntity();

            StringWriter stringWriter = new StringWriter();
            IOUtils.copy(new InputStreamReader(entity.getContent(), "windows-1251"), stringWriter);

            Document document = Jsoup.parse(stringWriter.toString());

            logger.info(document.text());
            logger.info(document.select("#headerFilm h1.moviename-big").text());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
