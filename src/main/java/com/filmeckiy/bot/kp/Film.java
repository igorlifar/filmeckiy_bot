package com.filmeckiy.bot.kp;

import java.util.ArrayList;
import java.util.List;

import com.filmeckiy.bot.utils.Option;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author egor
 */
public class Film {
    private static final Logger logger = LogManager.getLogger(Film.class);

    public final long id;
    public final String title;

    public final Option<String> year;
    public final Option<String> director;
    public final Option<String> description;
    public final Option<String> slogan;
    public final Option<Double> kpRating;
    public final Option<Integer> ratingCount;

    public final List<String> actors;
    public final List<String> genres;
    public final List<String> countries;

    public Film(
            long id,
            String title,
            Option<String> year,
            Option<String> director,
            Option<String> description,
            Option<String> slogan,
            Option<Double> kpRating,
            Option<Integer> ratingCountInt,
            List<String> actors,
            List<String> genres,
            List<String> countries)
    {
        this.id = id;
        this.title = title;
        this.year = year;
        this.director = director;
        this.description = description;
        this.slogan = slogan;
        this.kpRating = kpRating;
        this.ratingCount = ratingCountInt;
        this.actors = actors;
        this.genres = genres;
        this.countries = countries;
    }

    public static Film fromText(String url, String text) {
        Document parsed = Jsoup.parse(text);

        long id = Long.parseLong(url.split("/")[4]);
        String title = parsed.select("#headerFilm h1.moviename-big").text();
        String titleMusor = parsed.select("#headerFilm h1.moviename-big span").text();
        title = title.substring(0, title.length() - titleMusor.length()).trim();

        String rating = parsed.select("#block_rating span.rating_ball").text();
        String ratingCount = parsed.select("#block_rating span.ratingCount").text();

        Option<String> year = Option.none();
        Option<String> slogan = Option.none();
        Option<String> description = Option.none();
        String country = "";
        Option<String> director = Option.none();
        String genres1 = "";
        for (Element e : parsed.select("#infoTable tr")) {
            if (e.select("td").first().text().equals("год") && e.select("td").get(1).select("a").size() != 0) {
                year = Option.some(e.select("td").get(1).select("a").get(0).text());
            }
            if (e.select("td").first().text().equals("слоган")) {
                slogan =  Option.some(e.select("td").get(1).text());
            }
            if (e.select("td").first().text().equals("страна")) {
                country =  e.select("td").get(1).text();
            }
            if (e.select("td").first().text().equals("режиссер")) {
                director =  Option.some(e.select("td").get(1).text());
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
        String sss = parsed.select("div.block_left_padtop div.brand_words[itemprop=\"description\"]").text();
        description = (sss.equals("") ? Option.none(): Option.some(sss));
        List<String> actors = new ArrayList<>();
        Elements uls = parsed.select("#actorList ul");
        if (uls.size() != 0) {
            for (Element a : uls.get(0).select("li")) {
                if (!a.text().equals("...")) {
                    actors.add(a.text());
                }
            }
        }

        Option<Double> kpRating = rating.equals("")
                ? Option.none()
                : Option.some(Double.parseDouble(rating));

        Option<Integer> ratingCountInt = ratingCount.equals("")
                ? Option.none()
                : Option.some(Integer.parseInt(ratingCount.replaceAll(" ", "")));

        return new Film(
                id, title, year, director, description, slogan, kpRating, ratingCountInt, actors, genres, countries);
    }

    public static void addFilmtoMongo(Film film) {
        MongoCollection<org.bson.Document> collection = MongoUtils
                .getClient()
                .getDatabase(MongoUtils.DB_NAME)
                .getCollection("movies");

        addFilmToMongo(film, collection);
    }

    private static String drawRating(double rating) {
       String x1 = "\uD83C\uDF15";
        String x2 = "\uD83C\uDF16";
        String x3 = "\uD83C\uDF17";
        String x4 = "\uD83C\uDF18";
        String x5 = "\uD83C\uDF11";

        String result = "";

        rating *= 2;

        int times = -1;
        while (true) {
            times += 1;

            if (rating >= 2.0) {
                result += x1;
                rating -= 2.0;
                continue;
            }

            if (rating >= 1.5) {
                result += x2;
                rating -= 1.5;
                continue;
            }

            if (rating >= 1.0) {
                result += x3;
                rating -= 1.0;
                continue;
            }

            if (rating >= 0.5) {
                result += x4;
                rating -= 0.5;
                continue;
            }

            break;
        }

        while (times < 10) {
            result += x5;
            times += 1;
        }

        return result;
    }

    public static String getText(Film film) {
        if (film == null) {
            return "NULL";
        }
        String ans = "";
        String countriesLine = film.countries.isEmpty() ? "" : ", " + formatStringList(film.countries);
        ans += String.format("%s\n", film.title);
        ans += String.format("%s%s\n", film.year.getOrElse("?"), countriesLine);
        if (!film.genres.isEmpty()) {
            ans += String.format("%s\n", formatStringList(film.genres));
        }

        if (film.kpRating.isDefined()) {
            ans += String.format("%.1f %s\n\n", film.kpRating.get(), drawRating(film.kpRating.get()));
        }

        if (film.slogan.isDefined()) {
            ans += String.format("%s\n\n", film.slogan.get());
        }

        if (film.director.isDefined()) {
            ans += String.format("Режиссер: %s\n", film.director.get());
        }

        if (!film.actors.isEmpty()) {
            ans += String.format("В ролях: %s\n\n", formatStringList(take(film.actors, 5)));
        }

        ans += film.description.getOrElse("");
        ans += "\n\n";

        ans += String.format("http://www.kinopoisk.ru/film/%d/", film.id);
        return ans;
    }

    private static String formatStringList(List<String> list) {
        String actorsLine = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            actorsLine += String.format(", %s", list.get(i));
        }
        return actorsLine;
    }

    public static <T> List<T> take(List<T> list, int k) {
        ArrayList<T> result = new ArrayList<>();
        for (int i = 0; i < Math.min(k, list.size()); i++) {
            result.add(list.get(i));
        }

        return result;
    }

    public static void addFilmToMongo(Film film, MongoCollection<org.bson.Document> collection) {
        org.bson.Document document = new org.bson.Document()
                .append("_id", film.id)
                .append("title", film.title);

        if (film.director.isDefined()) {
            document = document.append("director", film.director.get());
        }
        if (film.slogan.isDefined()) {
            document = document.append("slogan", film.slogan.get());
        }
        if (film.year.isDefined()) {
            document = document.append("year", film.year.get());
        }
        if (film.description.isDefined()) {
            document = document.append("description", film.description.get());
        }
        if (film.kpRating.isDefined()) {
            document = document.append("kpRating", film.kpRating.get());
        }
        if (film.ratingCount.isDefined()) {
            document = document.append("ratingCount", film.ratingCount.get());
        }
        document = document.append("actors", film.actors);
        document = document.append("genres", film.genres);
        document = document.append("countries", film.countries);


        org.bson.Document res = collection
                .findOneAndReplace(new org.bson.Document("_id", film.id), document);

        if (res == null) {
            collection.insertOne(document);
        }
    }

    public static Film getMoviefromDocument(org.bson.Document doc) {
        long id = doc.getLong("_id");
        String title = doc.getString("title");
        String director = doc.getString("director");
        Option<String> directorO = director == null ? Option.none() : Option.some(director);
        String slogan = doc.getString("slogan");
        Option<String> sloganO = slogan == null ? Option.none() : Option.some(slogan);
        String year = doc.getString("year");
        Option<String> yearO = year == null ? Option.none() : Option.some(year);
        String description = doc.getString("description");
        Option<String> descriptionO = description == null ? Option.none() : Option.some(description);
        Double kpRating = doc.getDouble("kpRating");
        Option<Double> kpRatingO = kpRating == null ? Option.none() : Option.some(kpRating);
        Integer ratingCount = doc.getInteger("ratingCount");
        Option<Integer> ratingCountO = ratingCount == null ? Option.none() : Option.some(ratingCount);
        List<String> actors = new ArrayList<>();
        for (int i = 0; i < doc.get("actors", List.class).size(); i++) {
            actors.add(doc.get("actors", List.class).get(i).toString());
        }
        List<String> genres = new ArrayList<>();
        for (int i = 0; i < doc.get("genres", List.class).size(); i++) {
            genres.add(doc.get("genres", List.class).get(i).toString());
        }
        List<String> countries = new ArrayList<>();
        for (int i = 0; i < doc.get("countries", List.class).size(); i++) {
            countries.add(doc.get("countries", List.class).get(i).toString());
        }
        return new Film(id, title, yearO,
                directorO, descriptionO, sloganO, kpRatingO, ratingCountO, actors, genres, countries);
    }

    public static Option<Film> getMoviefromMongo(long id) {
        FindIterable<org.bson.Document> documents = MongoUtils.getClient()
                .getDatabase(MongoUtils.DB_NAME)
                .getCollection("movies")
                .find(new org.bson.Document("_id", id));
        org.bson.Document doc = documents.first();
        if (doc == null) {
            return Option.none();
        }
        logger.info(doc);
        return Option.some(getMoviefromDocument(doc));
    }
}
