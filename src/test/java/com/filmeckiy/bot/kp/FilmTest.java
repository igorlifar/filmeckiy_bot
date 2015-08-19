package com.filmeckiy.bot.kp;

import com.filmeckiy.bot.utils.Option;
import org.junit.Test;

import java.util.ArrayList;

/**
 * @author egor
 */
public class FilmTest {
    @Test
    public void test1() {
        Film film = new Film(
                12134, "korolo levtrhones", Option.some("2015"),
                Option.some("Abrams"), Option.some("Filmec klevii"),
                Option.some("winter is coming"), Option.some(10.0),
                ratingCountInt, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        Film.addFilmtoMongo(film);
        Film.getMoviefromMongo(12134);
    }
}