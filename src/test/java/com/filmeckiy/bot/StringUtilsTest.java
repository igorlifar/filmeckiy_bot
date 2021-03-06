package com.filmeckiy.bot;

import org.junit.Test;

import java.util.List;

/**
 * @author egor
 */
public class StringUtilsTest {

    @Test
    public void testMain() throws Exception {
        String s = "Yарри Поттер дАры:часть 2,!";
        System.out.println(s);
        List<String> test = StringUtils.main(s);
        for (int i = 0; i < test.size(); i++) {
            System.out.println("token " + i + ": " + test.get(i));
        }

        System.out.println(StringUtils.nop("гарри", "Гарnи", 0));
        System.out.println(StringUtils.nop("гарри", "Гарnи", 1));
        System.out.println(StringUtils.nop("гарри", "Гарnи", 2));


        System.out.println("equals: " + Main.equals1(
                StringUtils.main("гарри поттер дары часть 2"),
                StringUtils.main("Гарри поттер и дары смерти: часть II")));

        List<String> tokens1 = StringUtils.main("квентин тарантино 2012");
        List<String> tokens2 = StringUtils.main("2012");
        System.out.println(tokens1);
        System.out.println(tokens2);
        System.out.println("equals 2: " + Main.equals1(
                tokens1,
                tokens2));

        System.out.println("177 474".replaceAll(" ", ""));
    }
}