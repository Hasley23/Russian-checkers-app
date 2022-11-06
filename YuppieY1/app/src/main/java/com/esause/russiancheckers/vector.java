package com.esause.russiancheckers;

class vector {
    private position first;
    private position second;

    /**
     * Creates a new move
     * @param first start position
     * @param second end position
     */
    vector(position first, position second){
        this.first = first;
        this.second = second;
    }

    /**
     * @return start position
     */
    final position getFirst(){
        return first;
    }

    /**
     * @return end position
     */
    final position getSecond(){
        return second;
    }
}
