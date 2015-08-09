package com.filmeckiy.bot;

import org.junit.Test;

/**
 * @author lifar
 */
public class ATest {

    @Test
    public void test() {
        A.d = 10;
        A.setF(33);

        A a = new A();
        a.a = 20;
        a.setC(44);

        System.out.println(a.a);
        System.out.println(a.getC());

        System.out.println(A.d);
        System.out.println(A.getF());
    }

    @Test
    public void test2() {
        NotEmptyNode a = new NotEmptyNode(10, 33);
        EmptyNode b = EmptyNode.instance;

        Node c = a;
        Node d = b;


        System.out.println(a.isEmpty());
        System.out.println(b.isEmpty());
        System.out.println(c.isEmpty());
        System.out.println(d.isEmpty());



        a = (NotEmptyNode) c;
//        a = (NotEmptyNode) d;

//        c.get();
//        d.get();


    }

}
