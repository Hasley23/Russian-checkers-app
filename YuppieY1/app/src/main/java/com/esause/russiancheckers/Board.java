package com.esause.russiancheckers;

import java.util.ArrayList;

// esause | 2019

class Board {
    final static int BOARD_SIZE = 8;                    // Checkers board size
    public enum MoveEventResult {
        MOVE_NORMAL,
        MOVE_WITH_ELIMINATION,
        MOVE_PROHIBITED,
        GIVEN_UP,
        MOVE_QUEEN}

    private int blackCount = 12;                        // initial count
    private int whiteCount = 12;
    private int blackQueenCount = 0;                    // queen count
    private int whiteQueenCount = 0;
    private ArrayList<position> murderedPieces;         // turkish move stuff

    private Cell[][] CellsState;

    /**
     * @return turkish move stuff
     */
    ArrayList<position> getMurderedPieces(){
        return murderedPieces;
    }

    /**
     * Creates a new game board
     * @param CellsState states array
     * @param blackCount black pieces count
     * @param whiteCount white pieces count
     * @param blackQueenCount black queen count
     * @param whiteQueenCount white queen count
     * @param murderedPieces turkish move stuff
     */
    Board(
            Cell[][] CellsState,
            int blackCount,
            int whiteCount,
            int blackQueenCount,
            int whiteQueenCount,
            ArrayList<position> murderedPieces) {

        this.CellsState = new Cell[BOARD_SIZE][BOARD_SIZE];

        // array init
        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                this.CellsState[i][j] = new Cell(CellsState[i][j].GetState());
                this.CellsState[i][j].SetIsQueen(CellsState[i][j].GetIsQueen());
            }
        }

        this.murderedPieces = murderedPieces;
        this.blackCount = blackCount;
        this.whiteCount = whiteCount;
        this.blackQueenCount = blackQueenCount;
        this.whiteQueenCount = whiteQueenCount;
    }

    /**
     * Creates a new empty Board object
     */
    Board(){
        CellsState = new Cell[BOARD_SIZE][BOARD_SIZE];
        this.murderedPieces = new ArrayList<>();
    }

    /**
     * @return states array
     */
    Cell[][] getCellsState(){
        return CellsState;
    }

    /**
     * @return white pieces count
     */
    int getNumWhite() {
        return whiteCount;
    }

    /**
     * @return white queen count
     */
    int getNumWhiteQueen() {
        return whiteQueenCount;
    }

    /**
     * @return black pieces count
     */
    int getNumBlack() {
        return blackCount;
    }

    /**
     * @return black queen count
     */
    int getNumBlackQueen() {
        return blackQueenCount;
    }

    /**
     * @return evaluation for black player
     */
    int evaluateBlack(){
        return blackCount - blackQueenCount + 3 * blackQueenCount -
                (whiteCount - whiteQueenCount + 3 * whiteQueenCount);
    }

    /**
     * @return evaluation for white player
     */
    int evaluateWhite() {
        return whiteCount - whiteQueenCount + 3 * whiteQueenCount -
                (blackCount - blackQueenCount + 3 * blackQueenCount);
    }

    /**
     * Use this to place pieces on the board
     * @param whiteOnTop changes the arrangement of colors on the board
     */
    void prepareBoard(boolean whiteOnTop){
        Cell filler1 = new Cell(Cell.State.WHITE);
        Cell filler2 = new Cell(Cell.State.BLACK);

        // Choose color on top
        if (!whiteOnTop) {
            filler1.SetState(Cell.State.BLACK);
            filler2.SetState(Cell.State.WHITE);
        }


        // Top down container initializing
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                Cell currentCellState = new Cell(Cell.State.BLANK);

                if (i == 0 && j % 2 != 0)
                {
                    // Odd cells on the 1st line
                    currentCellState = filler1;
                }
                else if (i == 1 && j % 2 == 0)
                {
                    // Even cells on the 2nd line
                    currentCellState = filler1;
                }
                else if (i == 2 && j % 2 != 0)
                {
                    // Odd cells on the 3rd line
                    currentCellState = filler1;
                }
                else if (i == 5 && j % 2 == 0)
                {
                    // Even cells on the 6th line
                    currentCellState = filler2;
                }
                else if (i == 6 && j % 2 != 0)
                {
                    // Odd cells on the 7th line
                    currentCellState = filler2;
                }
                else if (i == 7 && j % 2 == 0)
                {
                    // Even cells on the 8th line
                    currentCellState = filler2;
                }

                CellsState[i][j] = currentCellState;
            }
        }
    }


    /**
     * Makes an attempt to make a move
     * @param start position
     * @param end position
     * @param direction current move direction
     * @param playerState current player's color
     * @param fightExpected second move boolean
     * @return result of attempt to make a move
     */
    MoveEventResult MovePiece(
            final position start,
            final position end,
            boolean direction,
            Cell.State playerState,
            boolean fightExpected){

        MoveEventResult moveResult = CheckMove(start, end, direction, playerState);

        if (fightExpected && moveResult != MoveEventResult.MOVE_WITH_ELIMINATION){
            if (moveResult != MoveEventResult.MOVE_QUEEN){
                return MoveEventResult.MOVE_PROHIBITED;
            }
        }

        switch (moveResult){
            case MOVE_NORMAL:
                CellsState[end.getY()][end.getX()].
                        SetState(CellsState[start.getY()][start.getX()].GetState());
                if (CellsState[start.getY()][start.getX()].GetIsQueen()) {
                    CellsState[end.getY()][end.getX()].SetIsQueen(true);
                }
                CellsState[start.getY()][start.getX()] = new Cell(Cell.State.BLANK);

                // Check for queen transformation event
                if ((end.getY() == 7 && direction) || (end.getY() == 0 && !direction))
                {
                    CellsState[end.getY()][end.getX()].SetIsQueen(true);
                    if (CellsState[end.getY()][end.getX()].GetState() == Cell.State.BLACK){
                        blackQueenCount++;
                    }
                    else {
                        whiteQueenCount++;
                    }
                }
                murderedPieces.clear();
                break;
            case MOVE_WITH_ELIMINATION:
                CellsState[end.getY()][end.getX()].
                        SetState(CellsState[start.getY()][start.getX()].GetState());
                if (CellsState[start.getY()][start.getX()].GetIsQueen()) {
                    CellsState[end.getY()][end.getX()].SetIsQueen(true);
                }
                CellsState[start.getY()][start.getX()] = new Cell(Cell.State.BLANK);

                // Set blank for intermediate coordinate (enemy elimination)
                if (CellsState[(start.getY() + end.getY()) / 2][(start.getX() + end.getX()) / 2].
                        GetIsQueen())
                {
                    if (CellsState[
                            (start.getY() + end.getY()) / 2][
                                    (start.getX() + end.getX()) / 2].
                            GetState() == Cell.State.BLACK) {
                        blackQueenCount--;
                    }
                    else
                        whiteQueenCount--;
                }
                murderedPieces.add(
                        new position(
                                (start.getY() + end.getY()) / 2,
                                (start.getX() + end.getX()) / 2));
                CellsState[(start.getY() + end.getY()) / 2][(start.getX() + end.getX()) / 2] =
                        new Cell(Cell.State.BLANK);

                if ((end.getY() == 7 && direction) || (end.getY() == 0 && !direction))
                {
                    // This result will not calculate second move
                    if (!CellsState[end.getY()][end.getX()].GetIsQueen()){
                        //moveResult = MoveEventResult.MOVE_NORMAL;
                        CellsState[end.getY()][end.getX()].SetIsQueen(true);
                        if (CellsState[end.getY()][end.getX()].GetState() == Cell.State.BLACK){
                            blackQueenCount++;
                        }
                        else {
                            whiteQueenCount++;
                        }
                    }
                    //Log.d("Board_class", "Obey! New Queen there!");
                }

                if (CellsState[end.getY()][end.getX()].GetState() == Cell.State.BLACK) {
                    whiteCount--;
                }
                else {
                    blackCount--;
                }
                //Log.d("Board_class", "Piece moved with result: MOVE_WITH_ELIMINATION");
                break;
            case MOVE_QUEEN:
                CellsState[end.getY()][end.getX()].SetState(
                        CellsState[start.getY()][start.getX()].GetState());
                if (CellsState[start.getY()][start.getX()].GetIsQueen()) {
                    CellsState[end.getY()][end.getX()].SetIsQueen(true);
                    CellsState[start.getY()][start.getX()].SetIsQueen(false);
                }

                int i = start.getY();
                int j = start.getX();
                CellsState[i][j] = new Cell(Cell.State.BLANK);
                CellsState[i][j].SetIsQueen(false);
                if (end.getX() > start.getX())
                    j++;
                else j--;
                if (end.getY() > start.getY())
                    i++;
                else i--;

                while (i != end.getY() && j != end.getX()) {
                    if (CellsState[i][j].GetState() != Cell.State.BLANK)
                    {
                        if (CellsState[end.getY()][end.getX()].GetState() == Cell.State.BLACK) {
                            whiteCount--;
                            if (CellsState[i][j].GetIsQueen())
                                whiteQueenCount--;
                        }
                        else {
                            blackCount--;
                            if (CellsState[i][j].GetIsQueen())
                                blackQueenCount--;
                        }
                        murderedPieces.add(new position(i, j));
                        CellsState[i][j] = new Cell(Cell.State.BLANK);
                        CellsState[i][j].SetIsQueen(false);
                    }
                    if (end.getX() > start.getX())
                        j++;
                    else j--;
                    if (end.getY() > start.getY())
                        i++;
                    else i--;
                }
                break;
            default:
                //murderedPieces.clear();
                break;
        }

        return moveResult;
    }

    /**
     * Passes only the right moves
     * @param start position
     * @param end position
     * @param direction current move direction
     * @param playerState current player's color
     * @return result of the move verification
     */
    private MoveEventResult CheckMove(
            final position start,
            final position end,
            boolean direction,
            Cell.State playerState) {
        MoveEventResult result = MoveEventResult.MOVE_PROHIBITED;

        // Calculate difference
        final int dY = end.getY() - start.getY();
        final int dX = end.getX() - start.getX();

        // Given up player event
        if (start.getY() == 0 && start.getX() == 0 && end.getY() == 0 && end.getX() == 0)
        {
            result = MoveEventResult.GIVEN_UP;
        }
        else if (end.getY() >= 0 && end.getY() < BOARD_SIZE &&
                end.getX() >= 0 && end.getX() < BOARD_SIZE) {
            Cell.State targetCellState = CellsState[end.getY()][end.getX()].GetState();

            if (targetCellState == Cell.State.BLANK && playerState ==
                    CellsState[start.getY()][start.getX()].GetState()){
                // When difference more than 1 (fight chance)
                if (Math.abs(dX) == 2 && Math.abs(dY) == 2 &&
                        !CellsState[start.getY()][start.getX()].GetIsQueen()) {
                    // Intermediate coordinate
                    int y = (start.getY() + end.getY()) / 2;
                    int x = (start.getX() + end.getX()) / 2;
                    // Check if victim is on the way
                    Cell.State victimCellState = CellsState[y][x].GetState();
                    Cell.State startCellState = CellsState[start.getY()][start.getX()].GetState();
                    if (targetCellState != victimCellState && startCellState != victimCellState) {
                        for (position p : murderedPieces){
                            if (p.getX() == x && p.getY() == y) {
                                return MoveEventResult.MOVE_PROHIBITED;
                            }
                        }
                        result = MoveEventResult.MOVE_WITH_ELIMINATION;
                        //Log.d("Board_class", "Detected MOVE_WITH_ELIMINATION event!");
                    }
                }
                // Normal move event
                else if (((Math.abs(dX) == 1 && dY == 1 && direction) ||
                        (Math.abs(dX) == 1 && dY == -1 && !direction))) {
                    result = MoveEventResult.MOVE_NORMAL;
                    //Log.d("Board_class", "Detected MOVE_NORMAL event!");
                }
                // Queen move
                else if ((Math.abs(dX) == 1 && Math.abs(dY) == 1) &&
                        CellsState[start.getY()][start.getX()].GetIsQueen()) {
                    result = MoveEventResult.MOVE_NORMAL;
                    //Log.d("Board_class", "Detected QUEEN_NORMAL_MOVE event!");
                } else if ((Math.abs(dX) >= 2 && Math.abs(dX) <= 7 && Math.abs(dY) == Math.abs(dX))
                        && CellsState[start.getY()][start.getX()].GetIsQueen()) {
                    Cell.State startCellState = CellsState[start.getY()][start.getX()].GetState();

                    int i = start.getY();
                    int j = start.getX();
                    boolean isCombat = false;
                    boolean wasCombat = false;

                    do {
                        if(CellsState[i][j].GetState() != startCellState &&
                                CellsState[i][j].GetState() != Cell.State.BLANK) {
                            wasCombat = true;
                            isCombat = true;
                        }
                        for (position p : murderedPieces){
                            if (p.getX() == j && p.getY() == i) {
                                return MoveEventResult.MOVE_PROHIBITED;
                            }
                        }
                        if (end.getX() > start.getX())
                            j++;
                        else j--;
                        if (end.getY() > start.getY())
                            i++;
                        else i--;

                        if (isCombat) {
                            for (position p : murderedPieces){
                                if (p.getX() == j && p.getY() == i) {
                                    return MoveEventResult.MOVE_PROHIBITED;
                                }
                            }
                            if (CellsState[i][j].GetState() != Cell.State.BLANK) {
                                break;
                            }
                            else isCombat = false;
                        }
                    } while (i != end.getY() && j != end.getX() &&
                            CellsState[i][j].GetState() != startCellState);

                    if (i == end.getY() && j == end.getX()) {
                        if (wasCombat){
                            result = MoveEventResult.MOVE_QUEEN;
                        }
                        else result = MoveEventResult.MOVE_NORMAL;
                    }
                }
            }
        }

        return result;
    }

    /**
     * MoveEventResult filter
     * @param direction current move direction
     * @param playerState current player's color
     * @return all possible fights for current player's color
     */
    ArrayList<vector> LookForFights (boolean direction, Cell.State playerState){
        ArrayList<vector> result = new ArrayList<>();
        int count = 0;

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if ((j % 2 == 0 && i % 2 != 0) || (j % 2 != 0 && i % 2 == 0)){
                    result.addAll(LookForFight(direction, playerState, new position(i, j)));
                    if (CellsState[i][j].GetState() == playerState){
                        count++;
                    }
                    if ( count == 12)
                        break;
                }
            }
        }

        return result;
    }

    /**
     * MoveEventResult filter
     * @param direction current move direction
     * @param playerState current player's color
     * @param start position
     * @return all possible fights for current piece
     */
    ArrayList<vector> LookForFight (boolean direction, Cell.State playerState, position start) {
        /* Use this to find possible fights */
        // declare possible fights array
        ArrayList<vector> result = new ArrayList<>();
        int y, x;


        // find fight for regular piece
        if (CellsState[start.getY()][start.getX()].GetState() ==
                playerState && !CellsState[start.getY()][start.getX()].GetIsQueen()) {
            if (CheckMove(new position(start.getY(), start.getX()),
                    new position(start.getY() - 2, start.getX() - 2), direction, playerState)
                    == MoveEventResult.MOVE_WITH_ELIMINATION)
                {
                    result.add(new vector(new position(start.getY(), start.getX()),
                            new position(start.getY() - 2, start.getX() - 2)));
                }
                if (CheckMove(new position(start.getY(), start.getX()),
                        new position(start.getY() + 2, start.getX() + 2), direction, playerState)
                        == MoveEventResult.MOVE_WITH_ELIMINATION)
                {
                    result.add(new vector(new position(start.getY(), start.getX()),
                            new position(start.getY() + 2, start.getX() + 2)));
                }
                if (CheckMove(new position(start.getY(), start.getX()),
                        new position(start.getY() + 2, start.getX() - 2), direction, playerState)
                        == MoveEventResult.MOVE_WITH_ELIMINATION)
                {
                    result.add(new vector(new position(start.getY(), start.getX()),
                            new position(start.getY() + 2, start.getX() - 2)));
                }
                if (CheckMove(new position(start.getY(), start.getX()),
                        new position(start.getY() - 2, start.getX() + 2), direction, playerState)
                        == MoveEventResult.MOVE_WITH_ELIMINATION)
                {
                    result.add(new vector(new position(start.getY(), start.getX()),
                            new position(start.getY() - 2, start.getX() + 2)));
            }
        }
        // find fight for Queen piece
        if (CellsState[start.getY()][start.getX()].GetState() ==
                playerState && CellsState[start.getY()][start.getX()].GetIsQueen()) {
            y = start.getY() + 1;
            x = start.getX() + 1;

            while (y < BOARD_SIZE && y >= 0 && x < BOARD_SIZE && x >= 0){
                if (CheckMove(new position(start.getY(), start.getX()), new position(y, x),
                        direction, playerState) == MoveEventResult.MOVE_QUEEN){
                    result.add(new vector(
                            new position(start.getY(), start.getX()), new position(y, x)));
                    break;
                }
                y++; x++;
            }

            y = start.getY() + 1;
            x = start.getX() - 1;
            while (y < BOARD_SIZE && y >= 0 && x < BOARD_SIZE && x >= 0){
                if (CheckMove(new position(start.getY(), start.getX()), new position(y, x),
                        direction, playerState) == MoveEventResult.MOVE_QUEEN){
                    result.add(new vector(
                            new position(start.getY(), start.getX()), new position(y, x)));
                    break;
                }
                y++; x--;
            }

            y = start.getY() - 1;
            x = start.getX() - 1;
            while (y < BOARD_SIZE && y >= 0 && x < BOARD_SIZE && x >= 0){
                if (CheckMove(new position(start.getY(), start.getX()), new position(y, x),
                        direction, playerState) == MoveEventResult.MOVE_QUEEN){
                    result.add(new vector(
                            new position(start.getY(), start.getX()), new position(y, x)));
                    break;
                }
                y--; x--;
            }
            y = start.getY() - 1;
            x = start.getX() + 1;
            while (y < BOARD_SIZE && y >= 0 && x < BOARD_SIZE && x >= 0){
                if (CheckMove(new position(start.getY(), start.getX()), new position(y, x),
                        direction, playerState) == MoveEventResult.MOVE_QUEEN){
                    result.add(new vector(
                            new position(start.getY(), start.getX()), new position(y, x)));
                    break;
                }
                y--; x++;
            }
        }
        return result;
    }

    /**
     * Provides all possible moves to aiPlayer
     * @param aiState AI cell state
     * @param direction current move direction
     * @return all possible moves
     */
    ArrayList<vector> getAllMovesForPlayer(Cell.State aiState, boolean direction) {
        ArrayList<vector> moves = new ArrayList<>(LookForFights(direction, aiState));

        if (moves.size() == 0){
            int count = 0;
            for (int i = 0; i < BOARD_SIZE; i++) {
                for (int j = 0; j < BOARD_SIZE; j++) {
                    Cell.State state = CellsState[i][j].GetState();
                    if (state == aiState) {
                        moves.addAll(getAllMovesForCell(aiState, new position(i, j), direction));
                        count++;
                    }
                    if (count == 12) {
                        return moves;
                    }
                }
            }
        }
        return moves;
    }

    /**
     * MoveEventResult filter
     * @param playerState current player's color
     * @param start position
     * @param direction current move direction
     * @return all possible normal moves
     */
    private ArrayList<vector> getAllMovesForCell(Cell.State playerState,
                                                 position start,
                                                 boolean direction) {
        int y, x;
        ArrayList<vector> result = new ArrayList<>();
        // find fight for regular piece
        if (CellsState[start.getY()][start.getX()].GetState() == playerState &&
                !CellsState[start.getY()][start.getX()].GetIsQueen()) {
            if (CheckMove(new position(start.getY(), start.getX()),
                    new position(start.getY() - 1, start.getX() - 1), direction, playerState)
                    == MoveEventResult.MOVE_NORMAL)
            {
                result.add(new vector(new position(start.getY(), start.getX()),
                        new position(start.getY() - 1, start.getX() - 1)));
            }
            if (CheckMove(new position(start.getY(), start.getX()),
                    new position(start.getY() + 1, start.getX() + 1), direction, playerState)
                    == MoveEventResult.MOVE_NORMAL)
            {
                result.add(new vector(new position(start.getY(), start.getX()),
                        new position(start.getY() + 1, start.getX() + 1)));
            }
            if (CheckMove(new position(start.getY(), start.getX()),
                    new position(start.getY() + 1, start.getX() - 1), direction, playerState)
                    == MoveEventResult.MOVE_NORMAL)
            {
                result.add(new vector(new position(start.getY(), start.getX()),
                        new position(start.getY() + 1, start.getX() - 1)));
            }
            if (CheckMove(new position(start.getY(), start.getX()),
                    new position(start.getY() - 1, start.getX() + 1), direction, playerState)
                    == MoveEventResult.MOVE_NORMAL)
            {
                result.add(new vector(new position(start.getY(), start.getX()),
                        new position(start.getY() - 1, start.getX() + 1)));
            }
        }
        // find fight for Queen piece
        if (CellsState[start.getY()][start.getX()].GetState() == playerState &&
                CellsState[start.getY()][start.getX()].GetIsQueen()) {
            y = start.getY() + 1;
            x = start.getX() + 1;

            while (y < BOARD_SIZE && y >= 0 && x < BOARD_SIZE && x >= 0){
                if (CheckMove(new position(start.getY(), start.getX()),
                        new position(y, x), direction, playerState) == MoveEventResult.MOVE_NORMAL){
                    result.add(new vector(
                            new position(start.getY(), start.getX()), new position(y, x)));
                    break;
                }
                y++; x++;
            }

            y = start.getY() + 1;
            x = start.getX() - 1;
            while (y < BOARD_SIZE && y >= 0 && x < BOARD_SIZE && x >= 0){
                if (CheckMove(new position(start.getY(), start.getX()), new position(y, x),
                        direction, playerState) == MoveEventResult.MOVE_NORMAL){
                    result.add(new vector(
                            new position(start.getY(), start.getX()), new position(y, x)));
                    break;
                }
                y++; x--;
            }

            y = start.getY() - 1;
            x = start.getX() - 1;
            while (y < BOARD_SIZE && y >= 0 && x < BOARD_SIZE && x >= 0){
                if (CheckMove(new position(start.getY(), start.getX()),
                        new position(y, x), direction, playerState) == MoveEventResult.MOVE_NORMAL){
                    result.add(new vector(
                            new position(start.getY(), start.getX()), new position(y, x)));
                    break;
                }
                y--; x--;
            }
            y = start.getY() - 1;
            x = start.getX() + 1;
            while (y < BOARD_SIZE && y >= 0 && x < BOARD_SIZE && x >= 0){
                if (CheckMove(new position(start.getY(), start.getX()), new position(y, x),
                        direction, playerState) == MoveEventResult.MOVE_NORMAL){
                    result.add(new vector(new position(start.getY(),
                            start.getX()), new position(y, x)));
                    break;
                }
                y--; x++;
            }
        }
        return result;
    }
}
