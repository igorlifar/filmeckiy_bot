package com.filmeckiy.bot;

/**
 * @author lifar
 */
public abstract class Node {
    public abstract boolean isEmpty();
    public abstract int size();

    public NotEmptyNode get() {
        if (isEmpty()) {
            throw new RuntimeException("This node is empty!!");
        }

        return (NotEmptyNode) this;
    }

    public Node merge(Node other) {
        if (isEmpty()) {
            return other;
        }

        if (other.isEmpty()) {
            return this;
        }

        NotEmptyNode thisNode = this.get();
        NotEmptyNode otherNode = other.get();

        if (thisNode.y <= otherNode.y) {
            Node rightNode = thisNode.getRight().merge(otherNode);
            return new NotEmptyNode(
                    thisNode.x,
                    thisNode.y,
                    thisNode.getLeft(),
                    rightNode);
        }   else {
            Node leftNode = otherNode.getLeft().merge(thisNode);
            return new NotEmptyNode(
                    otherNode.x,
                    otherNode.y,
                    leftNode,
                    otherNode.getRight());
        }

    }

    public SplitResult split(int x) {
        if (this.isEmpty()) {
            return new SplitResult(EmptyNode.instance, EmptyNode.instance);
        }
        NotEmptyNode me = this.get();
        if (me.x < x) {
            SplitResult splitResult = me.getRight().split(x);
            return new SplitResult(
                    new NotEmptyNode(me.x, me.y, me.getLeft(), splitResult.left),
                    splitResult.right);

        }  else {
            SplitResult splitResult = me.getLeft().split(x);
            return new SplitResult(
                    splitResult.left,
                    new NotEmptyNode(me.x, me.y, splitResult.right, me.getRight()));

        }
    }

    public static class SplitResult {
        public final Node left;
        public final Node right;

        public SplitResult(Node left, Node right) {
            this.left = left;
            this.right = right;
        }
    }
}
