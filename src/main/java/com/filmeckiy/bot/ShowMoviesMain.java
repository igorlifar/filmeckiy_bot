package com.filmeckiy.bot;

import java.util.ArrayList;
import java.util.List;

import com.filmeckiy.bot.kp.MongoUtils;
import com.mongodb.client.FindIterable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
            String titleEnglish = parsed.select("#headerFilm span[itemprop=\"alternativeHeadline\"]").text();

            String year = "";
            String slogan = "";
            String description = "";
            String country = "";
            String director = "";
            String genres1 = "";
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
                if (e.select("td").first().text().equals("жанр")) {
                    genres1 = e.select("td").get(1).text();
                }
            }
            String[] split = genres1.split(",");
            List<String> genres = new ArrayList<>();
            for (String genre : split) {
                if (!genre.trim().equals("... слова")) {
                    genres.add(genre.trim());
                }
            }
            String[] split1 = country.split(",");
            List<String> countries = new ArrayList<>();
            for (String country1 : split1) {
                countries.add(country1.trim());
            }
            description = parsed.select("div.block_left_padtop div.brand_words[itemprop=\"description\"]").text();
            List<String> actors = new ArrayList<>();
            Elements uls = parsed.select("#actorList ul");
            if (uls.size() != 0) {
                for (Element a : uls.get(0).select("li")) {
                    if (!a.text().equals("...")) {
                        actors.add(a.text());
                    }
                }
            }
//            double kpRating = Double.parseDouble(rating);

            logger.info("{}:", url);
            logger.info("{}", title);
            logger.info("{}", titleEnglish);
            logger.info("{}", slogan);
            logger.info("{}", year);
            logger.info(countries);
            logger.info("{}", director);
            logger.info("{}", actors);
            logger.info(genres);
            logger.info("Kinopoisk rating: {}", 0.1);
            logger.info("Description:");
            logger.info("{}", description);
        }
    }
}
