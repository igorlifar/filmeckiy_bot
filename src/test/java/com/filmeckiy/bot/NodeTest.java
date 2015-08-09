package com.filmeckiy.bot;

import java.util.Random;

import org.junit.Test;

/**
 * @author lifar
 */
public class NodeTest {
    @Test
    public void test1() {
        Random random = new Random(228);

        Node root = EmptyNode.instance;
        for (int i = 0; i < 10; i++) {
            root = root.merge(new NotEmptyNode(i, random.nextInt()));

            System.out.println(root.size());
        }

        Node.SplitResult split = root.split(5);
    }

}
