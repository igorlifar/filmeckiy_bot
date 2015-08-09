package com.filmeckiy.bot;

/**
 * @author lifar
 */
public class NotEmptyNode extends Node {
    public final int x;
    public final int y;

    private Node left;
    private Node right;

    public NotEmptyNode(int x, int y) {
        this.x = x;
        this.y = y;

        this.left = EmptyNode.instance;
        this.right = EmptyNode.instance;
    }

    public NotEmptyNode(int x, int y, Node left, Node right) {
        this.x = x;
        this.y = y;
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    @Override
    public int size() {
        return this.left.size() + this.right.size() + 1;
    }
}
