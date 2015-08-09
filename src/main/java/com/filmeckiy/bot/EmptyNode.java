package com.filmeckiy.bot;

/**
 * @author lifar
 */
public class EmptyNode extends Node {
    public static final EmptyNode instance = new EmptyNode();

    private EmptyNode() {

    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public int size() {
        return 0;
    }
}
