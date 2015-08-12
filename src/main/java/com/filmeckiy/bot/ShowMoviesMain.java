package com.filmeckiy.bot;

import com.filmeckiy.bot.kp.KpClient;
import com.filmeckiy.bot.kp.MongoUtils;
import com.mongodb.client.FindIterable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import javax.print.Doc;

/**
 * @author egor
 */
public class ShowMoviesMain {
    private static final Logger logger = LogManager.getLogger(ShowMoviesMain.class);

    public static void main(String[] args) {
        FindIterable<Document> pages = MongoUtils.getClient()
                .getDatabase(MongoUtils.DB_NAME)
                .getCollection("pages_cache")
                .find();

        for (Document doc : pages) {
            String url = doc.getString("_id");

            if (!url.contains("film")) {
                continue;
            }

            org.jsoup.nodes.Document parsed = Jsoup.parse(doc.getString("text"));

            String title = parsed.select("#headerFilm h1.moviename-big").text();
            String rating = parsed.select("#block_rating span.rating_ball").text();

            String year = "";
            String slogan = "";
            String description = "";
            String country = "";
            String director = "";
            for (Element e : parsed.select("#infoTable tr")) {
                if (e.select("td").first().text().equals("год")) {
                    year = e.select("td").get(1).text();
                }
                if (e.select("td").first().text().equals("слоган")) {
                    slogan = e.select("td").get(1).text();
                }
                if (e.select("td").first().text().equals("страна")) {
                    country = e.select("td").get(1).text();
                }
                if (e.select("td").first().text().equals("режиссер")) {
                    director = e.select("td").get(1).text();
                }
            }

            description = parsed.select("div.block_left_padtop div.brand_words").text();

            logger.info("{}:", url);
            logger.info("{}", title);
            logger.info("{}", slogan);
            logger.info("{}", year);
            logger.info("{}", country);
            logger.info("{}", director);
            logger.info("Kinopoisk rating: {}", rating);
            logger.info("Description:");
            logger.info("{}", description);
        }
    }
}
