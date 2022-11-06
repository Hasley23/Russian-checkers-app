package com.esause.russiancheckers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class DecisionTree {
    private int nodeScore;
    private ArrayList<DecisionTree> children;
    private vector move;

    /**
     * Creates a new tree node
     * @param move node move
     * @param nodeScore node score
     * @param children node children
     */
    DecisionTree(vector move, int nodeScore, DecisionTree ... children) {
        this.children = new ArrayList<>(Arrays.asList(children));
        this.nodeScore = nodeScore;
        this.move = move;
    }

    /**
     * @return node move
     */
    vector getMove() {
        return move;
    }

    /**
     * @return node score
     */
    int getNodeScore() {
        return nodeScore;
    }

    /**
     * @return node children
     */
    List<DecisionTree> getChildren() {
        return children;
    }

    /**
     * Node score setter
     * @param newScore node score
     */
    void setNodeScore(int newScore) {
        this.nodeScore = newScore;
    }

    /**
     * @return first child of a tree node
     */
    DecisionTree getChild() {
        return children.get(0);
    }

    /**
     * Use this to add a child node to tree
     * @param child a child node
     */
    void addChild(DecisionTree child) {
        children.add(child);
    }

}
