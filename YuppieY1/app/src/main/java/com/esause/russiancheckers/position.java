package com.esause.russiancheckers;

class position {
    private int x;
    private int y;

    /**
     * Creates a new board position
     * @param y row
     * @param x column
     */
    position(int y, int x) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return column
     */
    final int getX(){
        return x;
    }

    /**
     * @return row
     */
    final int getY(){
        return y;
    }
}
