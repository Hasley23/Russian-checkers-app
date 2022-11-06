package com.esause.russiancheckers;

class Cell {
    private boolean isQueen;
    public enum State {WHITE, BLACK, BLANK}
    private State mState;

    /**
     * Creates a new cell
     * @param mState current state
     */
    Cell(State mState) {
        this.mState = mState;
        isQueen = false;
    }

    /**
     * @return current state
     */
    final State GetState() { return this.mState; }

    /**
     * State setter
     * @param mState state to set
     */
    void SetState(State mState) { this.mState = mState; }

    /**
     * @return queen state
     */
    final boolean GetIsQueen() { return this.isQueen; }

    /**
     * Queen state setter
     * @param isQueen queen state to set
     */
    void SetIsQueen(boolean isQueen) { this.isQueen = isQueen; }
}
