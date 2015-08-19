package com.filmeckiy.bot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

/**
 * @author egor
 */
public class MainTest {

    @Test
    public void test() {
        System.out.println(StringUtils.levenshtein("гарри поттер и дары смерти", "Карта смерти"));
        System.out.println(StringUtils.levenshtein("гарри поттер и дары смерти", "Гарри Поттер и Дары Смерти: Часть I"));

        ObjectMapper om = new ObjectMapper();

        ObjectNode objectNode = om.createObjectNode();
        ArrayNode arrayNode = om.createArrayNode();
        ArrayNode arrayNode2 = om.createArrayNode();
        arrayNode2.add("movie1");
        arrayNode2.add("movie2");

        arrayNode.add(arrayNode2);
        objectNode.set("keyboard", arrayNode);
        objectNode.put("one_time_keyboard", true);

        System.out.println(objectNode.toString());



    }
}