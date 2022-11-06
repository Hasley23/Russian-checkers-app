package com.esause.russiancheckers;

import java.util.ArrayList;

class aiPlayer {
    private Cell.State aiState;
    private vector bestMove;
    private int INFINITY = 12;    // player's pieces count
    private int DEPTH = 4;        // MiniMax depth

    /**
     * Creates a new aiPlayer
     * @param state the state of the AI cells
     */
    aiPlayer(Cell.State state) {
        aiState = state;
    }

    /**
     * @return the state of the AI
     */
    Cell.State getAiState() {
        return aiState;
    }

    /**
     * Main AI function
     * @param board the current state of the game
     * @param direction AI player's direction
     * @return the AI move
     */
    vector calculateAiMove(Board board, boolean direction) {
        bestMove = new vector(new position(0,0), new position(0,0));
        Board temp = copyBoard(board);
        DecisionTree tree = generateDecisionTree(temp, aiState, null, direction, DEPTH);
        alphaBetaPruning(-INFINITY, INFINITY, DEPTH, tree);
        return bestMove;
    }

    /**
     * Generates a decision tree at a given depth
     * Use this before AlphaBetaPruning()
     * @param board the state of a board
     * @param state the state of player's cells
     * @param curMove current move
     * @param direction player's direction
     * @param depth MiniMax depth
     * @return all possible moves based on board state
     */
    private DecisionTree generateDecisionTree (Board board,
                                               Cell.State state,
                                               vector curMove,
                                               boolean direction,
                                               int depth) {

        if (depth == 0) return new DecisionTree(curMove, Evaluate(board, state));

        ArrayList<vector> moves = board.getAllMovesForPlayer(state, direction);

        DecisionTree Tree = new DecisionTree(curMove, Evaluate(board, state));

        for (vector move : moves) {
            Board temp = copyBoard(board);
            temp.MovePiece(move.getFirst(), move.getSecond(), direction, state, false);
            temp.getMurderedPieces().clear();
            Tree.addChild(generateDecisionTree(
                    temp, state == Cell.State.BLACK ? Cell.State.WHITE : Cell.State.BLACK,
                    move, !direction, depth-1));
        }

        return Tree;
    }

    /**
     * Alpha-Beta pruning implementation
     * Use this before generateDecisionTree()
     * @param alpha first maximum
     * @param beta second maximum
     * @param depth MiniMax depth
     * @param decisionTree current tree node
     * @return best score
     */
    private int alphaBetaPruning(int alpha, int beta, int depth, DecisionTree decisionTree){
        if (depth == 0) return decisionTree.getNodeScore();
        int score = -INFINITY;

        if (depth == DEPTH){
            bestMove = decisionTree.getChild().getMove();
        }

        for (DecisionTree child : decisionTree.getChildren()) {
            int temp = alphaBetaPruning(-beta, -alpha, depth - 1, child);

            if (temp > score) {
                if (depth == DEPTH){
                    bestMove = child.getMove();
                }
                score = temp;
            }
            if (score > alpha) {
                alpha = score;
            }
            if (alpha >= beta) {
                break;
            }
        }

        decisionTree.setNodeScore(score);
        return score;
    }

    /**
     * @return new same as input Board object
     */
    private Board copyBoard(Board board) {
        return new Board(
                board.getCellsState(),
                board.getNumBlack(),
                board.getNumWhite(),
                board.getNumBlackQueen(),
                board.getNumWhiteQueen(),
                board.getMurderedPieces());
    }

    /**
     * @return evaluation of a board
     */
    private int Evaluate(Board board, Cell.State state) {
        if (state == Cell.State.BLACK) {
            return board.evaluateBlack();
        }
        else if (state == Cell.State.WHITE) {
            return board.evaluateWhite();
        }
        else {
            return 0;
        }
    }

}
