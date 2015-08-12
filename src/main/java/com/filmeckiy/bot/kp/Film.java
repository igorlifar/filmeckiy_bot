package com.filmeckiy.bot.kp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author egor
 */
public class Film {
    private static final Logger logger = LogManager.getLogger(Film.class);
    String title;
    String year;
    String director;
    String description;
    double rating;
    List<String> actors;
    List<String> genres;
    List<String> countries;

}
