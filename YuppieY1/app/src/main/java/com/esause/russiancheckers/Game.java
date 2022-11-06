package com.esause.russiancheckers;

import android.content.Context;
import java.util.ArrayList;
import java.util.Arrays;

class Game {
    public enum Player { NONE, BLACK, WHITE, DRAFT}

    private int iWhiteScore;
    private int iBlackScore;
    private int movesToDraft = 0;
    private int queenMovesToDraft = 0;
    private int endingMovesToDraft = 0;
    private int endingMovesToDraft2 = 0;
    private Player pLastPlayer;
    private boolean bIsSurrender;
    private boolean direction;
    private boolean fightExpected;
    private boolean possibleSecondMove;
    private boolean playWithAI;
    private boolean isOnlyQueenMoves = false;
    private aiPlayer ai;
    private Board mBoard;
    private ArrayList<vector> fightExpectedPositions;
    private ArrayList<vector> aiMoveStreak;

    private int tempWhiteQueens = 0;
    private int tempBlackQueens = 0;
    private int tempWhite = 12;
    private int tempBlack = 12;

    private ArrayList<Cell[][]> draftBoardList;

    /**
     * @return positions for highlighting
     */
    ArrayList<vector> getFightExpectedPositions(){
        return fightExpectedPositions;
    }

    /**
     * @return the need for highlighting
     */
    boolean getFightExpected() {
        return fightExpected;
    }

    /**
     * @return current white score
     */
    int getIWhiteScore(){
        return iWhiteScore;
    }

    /**
     * @return current black score
     */
    int getIBlackScore() {
        return iBlackScore;
    }

    /**
     * @return the need for a second move
     */
    boolean getPossibleSecondMove(){
        return possibleSecondMove;
    }

    /**
     * Init board and place pieces
     * @param whiteOnTop changes the arrangement of colors on the board
     */
    private void prepareMap(boolean whiteOnTop) {
        mBoard = new Board();
        mBoard.prepareBoard(whiteOnTop);
    }

    /**
     * @return current board
     */
    Board getMBoard(){
        return mBoard;
    }

    /**
     * Creates a new game core
     * @param iWhiteScore white score
     * @param iBlackScore black score
     * @param pLastPlayer last player (must be black for a new game)
     * @param bIsSurrender rudiment
     * @param direction move direction
     * @param playWithAI AI switch
     */
    Game(int iWhiteScore,
         int iBlackScore,
         Player pLastPlayer,
         boolean bIsSurrender,
         boolean direction,
         boolean playWithAI){

        this.iWhiteScore = iWhiteScore;
        this.iBlackScore = iBlackScore;
        this.pLastPlayer = pLastPlayer;
        this.direction = direction;
        this.bIsSurrender = bIsSurrender;
        this.fightExpectedPositions = new ArrayList<>();
        this.fightExpected = false;
        this.possibleSecondMove = false;
        this.playWithAI = playWithAI;
        this.aiMoveStreak = new ArrayList<>();
        prepareMap(direction);
        if (this.playWithAI) {
            if (direction){
                ai = new aiPlayer(Cell.State.WHITE);
                MainAction(null);
            }
            else
                ai = new aiPlayer(Cell.State.BLACK);
        }
        this.draftBoardList = new ArrayList<>();
        this.draftBoardList.add(mBoard.getCellsState());
    }

    /**
     * Winner check based on game rules
     * @return winner player state
     */
    Player GetWinner(){
        Player winner = checkRules();
        if (winner != Player.NONE) {
            return winner;
        }

        if (checkArrays()) {
            return Player.DRAFT;
        }

        if(bIsSurrender) {
            if(pLastPlayer == Player.WHITE){
                winner = Player.BLACK;
            }
            else if (pLastPlayer == Player.BLACK){
                winner = Player.WHITE;
            }
        }
        else if(iWhiteScore == 12){
            winner = Player.WHITE;
        }
        else if(iBlackScore == 12){
            winner = Player.BLACK;
        }

        return winner;
    }

    /**
     * @return current player's cell state
     */
    private Cell.State getPlayerCellState() {
        return pLastPlayer != Player.WHITE ? Cell.State.WHITE : Cell.State.BLACK;
    }

    /**
     * Allows the player to make a move
     * @param newMove player's move
     * @return result of trying to make a move
     */
    boolean MainAction(vector newMove)
    {
        boolean result = false;
        Board.MoveEventResult moveResult;
        if (newMove != null) {
            // check is game over
            if (GetWinner() == Player.NONE)
            {
                //if (mBoard.getMurderedPieces() != null)

                moveResult = mBoard.MovePiece(newMove.getFirst(), newMove.getSecond(),
                            direction, getPlayerCellState(), fightExpected);

                // Check if ff (fast finish)
                if (moveResult == Board.MoveEventResult.GIVEN_UP)
                {
                    bIsSurrender = true;
                }

                // Parse move result
                if (moveResult == Board.MoveEventResult.MOVE_WITH_ELIMINATION ||
                        moveResult == Board.MoveEventResult.MOVE_QUEEN)
                {
                    // Update score
                    UpdateScore();
                }

                if (moveResult != Board.MoveEventResult.MOVE_PROHIBITED) {
                    draftBoardList.add(copyArray(mBoard.getCellsState()));
                    result = true;
                    if (moveResult != Board.MoveEventResult.MOVE_NORMAL){
                        fightExpectedPositions = mBoard.LookForFight(
                                direction,
                                getPlayerCellState(),
                                newMove.getSecond());
                        isOnlyQueenMoves = false;
                        queenMovesToDraft = 0;
                    }
                    else {
                        if(mBoard.getCellsState()[
                                newMove.getSecond().getY()][
                                newMove.getSecond().getX()].GetIsQueen()){
                            isOnlyQueenMoves = true;
                        } else {
                            isOnlyQueenMoves = false;
                            queenMovesToDraft = 0;
                        }
                        fightExpectedPositions.clear();
                    }
                    if (fightExpectedPositions.size() == 0)
                    {
                        SwitchPlayer();
                        direction = !direction;
                        fightExpectedPositions = mBoard.LookForFights(
                                direction,
                                getPlayerCellState());
                        fightExpected = fightExpectedPositions.size() != 0;
                        possibleSecondMove = false;
                        UpdateScore();
                        mBoard.getMurderedPieces().clear();

                        ///There was ai func
                    }
                    else possibleSecondMove = true;
                }
            }
        }
        else if (playWithAI) {
            //aiMoveStreak = new ArrayList<>();
            vector aiMove = ai.calculateAiMove(mBoard, direction);
            //aiMoveStreak.add(aiMove);
            mBoard.MovePiece(
                    aiMove.getFirst(),
                    aiMove.getSecond(),
                    direction,
                    ai.getAiState(),
                    false);
            SwitchPlayer();
            direction = !direction;
        }
        return result;
    }

    /**
     * Game rules stuff
     * @return winner player state
     */
    private Player checkRules(){

        ArrayList<vector> al = mBoard.getAllMovesForPlayer(getPlayerCellState(), direction);
        if (al.size() == 0) {
            return pLastPlayer == Player.WHITE ? Player.WHITE : Player.BLACK;
        }

        if (pLastPlayer == Player.WHITE &&
                mBoard.getNumWhiteQueen() == 1 &&
                mBoard.getNumBlackQueen() >= 3 ||
                pLastPlayer == Player.BLACK &&
                        mBoard.getNumBlackQueen() == 1 &&
                        mBoard.getNumWhiteQueen() >= 3){
            movesToDraft++;
        } else {
            movesToDraft = 0;
        }

        if (isOnlyQueenMoves) {
            queenMovesToDraft++;
        } else {
            queenMovesToDraft = 0;
        }

        int ending = mBoard.getNumWhite() + mBoard.getNumBlack();

        if (tempBlack == mBoard.getNumBlack() &&
                tempWhite == mBoard.getNumWhite() &&
                tempBlackQueens == mBoard.getNumBlackQueen() &&
                tempWhiteQueens == mBoard.getNumWhiteQueen()){
            endingMovesToDraft2++;
        } else endingMovesToDraft2 = 0;

        if ((ending == 2 || ending == 3) && endingMovesToDraft2 == 5) {
            return Player.DRAFT;
        }

        if ((ending == 4 || ending == 5) && endingMovesToDraft2 == 30) {
            return Player.DRAFT;
        }

        if ((ending == 6 || ending == 7) && endingMovesToDraft2 == 60) {
            return Player.DRAFT;
        }

        tempWhiteQueens = mBoard.getNumWhiteQueen();
        tempWhite = mBoard.getNumWhite();
        tempBlackQueens = mBoard.getNumBlackQueen();
        tempBlack = mBoard.getNumBlack();

        if (mBoard.getNumWhiteQueen() == 1 &&
                mBoard.getNumWhite() == 1 && mBoard.getNumBlack() == 3 ||
                mBoard.getNumBlackQueen() == 1 && mBoard.getNumBlack() == 1 &&
                        mBoard.getNumWhite() == 3) {
            boolean isBigRoad = false;
            for (int i = 0; i < Board.BOARD_SIZE; i++) {
                if (mBoard.getCellsState()[7 - i][i].GetIsQueen()) {
                    isBigRoad = true;
                    break;
                }
            }

            if (isBigRoad) {
                endingMovesToDraft++;
            } else {
                endingMovesToDraft = 0;
            }
        } else endingMovesToDraft = 0;

        if (endingMovesToDraft == 5) {
            return Player.DRAFT;
        }

        // 15 moves rules
        if (movesToDraft == 15 || queenMovesToDraft == 15) {
            return Player.DRAFT;
        }

        return Player.NONE;
    }

    /**
     * Allows the AI to make a move
     * @return AI calculated move
     */
    ArrayList<vector> aiMovement() {
        Board.MoveEventResult moveResult;

        if (playWithAI && GetWinner() == Player.NONE) {
            aiMoveStreak = new ArrayList<>();
            vector aiMove = ai.calculateAiMove(mBoard, direction);

            moveResult = mBoard.MovePiece(
                    aiMove.getFirst(),
                    aiMove.getSecond(),
                    direction,
                    ai.getAiState(),
                    false);
            aiMoveStreak.add(aiMove);

            if(moveResult == Board.MoveEventResult.MOVE_NORMAL) {
                if(mBoard.getCellsState()[
                        aiMove.getSecond().getY()][
                        aiMove.getSecond().getX()].GetIsQueen()){
                    isOnlyQueenMoves = true;
                } else {
                    isOnlyQueenMoves = false;
                    queenMovesToDraft = 0;
                }
            } else {
                isOnlyQueenMoves = false;
                queenMovesToDraft = 0;
            }

            position temp = aiMove.getSecond();
            // if second move event
            while (moveResult == Board.MoveEventResult.MOVE_WITH_ELIMINATION ||
                    moveResult == Board.MoveEventResult.MOVE_QUEEN) {
                ArrayList<vector> possibleFights = mBoard.LookForFight(
                        direction,
                        ai.getAiState(),
                        temp);
                if (possibleFights.size() != 0) {
                    vector aiTemp = ai.calculateAiMove(mBoard, direction);
                    boolean independent = false;
                    if (temp == aiTemp.getFirst()) {
                        independent = true;
                    }

                    if (!independent){
                        moveResult = mBoard.MovePiece(
                                temp,
                                possibleFights.get(0).getSecond(),
                                direction,
                                ai.getAiState(),
                                true);
                        aiMoveStreak.add(new vector(
                                temp,
                                possibleFights.get(0).getSecond()));
                        temp = possibleFights.get(0).getSecond();
                    }
                    else {
                        moveResult = mBoard.MovePiece(
                                aiTemp.getFirst(),
                                aiTemp.getSecond(),
                                direction,
                                ai.getAiState(),
                                true);
                        aiMoveStreak.add(
                                new vector(aiTemp.getFirst(), aiTemp.getSecond()));
                        temp = aiTemp.getSecond();
                    }

                    if(moveResult != Board.MoveEventResult.MOVE_PROHIBITED) {
                        if(mBoard.getCellsState()[
                                temp.getY()][
                                temp.getX()].GetIsQueen()){
                            isOnlyQueenMoves = true;
                        } else {
                            isOnlyQueenMoves = false;
                            queenMovesToDraft = 0;
                        }
                    }
                } else moveResult = Board.MoveEventResult.MOVE_PROHIBITED;
            }
            mBoard.getMurderedPieces().clear();

            draftBoardList.add(copyArray(mBoard.getCellsState()));

            UpdateScore();
            SwitchPlayer();
            direction = !direction;
            fightExpectedPositions = mBoard.LookForFights(
                    direction,
                    getPlayerCellState());
            fightExpected = fightExpectedPositions.size() != 0;
        } else {
            return null;
        }

        return aiMoveStreak;
    }

    /**
     * Check equal boards
     * @return true if equals
     */
    private boolean checkArrays(){
        for (int i = 0; i < draftBoardList.size(); i++) {
            int count = 0;
            for (int j = 0; j < draftBoardList.size(); j++) {
                if (Arrays.deepEquals(draftBoardList.get(j), draftBoardList.get(i))){
                    count++;
                }
                if (count >= 3) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * not pointer copy
     * @param cells source array
     * @return new array
     */
    private Cell[][] copyArray(Cell[][] cells) {
        Cell[][] temp = new Cell[Board.BOARD_SIZE][Board.BOARD_SIZE];
        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            temp[i] = Arrays.copyOf(cells[i], cells.length);
        }
        return temp;
    }

    /**
     * Changes the active player
     */
    private void SwitchPlayer(){
        if(pLastPlayer == Player.WHITE){
            pLastPlayer = Player.BLACK;
        }
        else {
            pLastPlayer = Player.WHITE;
        }
    }

    /**
     * Updates game score
     */
    private void UpdateScore(){
        iWhiteScore = 12 - mBoard.getNumBlack();
        iBlackScore = 12 - mBoard.getNumWhite();
    }

    /**
     * @param context activity context
     * @return current player string
     */
    final String GetCurrentPlayer(Context context){
        if (pLastPlayer == Player.WHITE)
        {
            return context.getString(R.string.player_black);
        }
        else
        {
            return context.getString(R.string.player_white);
        }
    }
}
