package com.enkelagon.model;

/**
 * Represents a square position on the chess board.
 * File is a-h (0-7), Rank is 1-8 (0-7 internally).
 */
public class Position {
    private final int file; // 0-7 (a-h)
    private final int rank; // 0-7 (1-8)

    public Position(int file, int rank) {
        if (file < 0 || file > 7 || rank < 0 || rank > 7) {
            throw new IllegalArgumentException("Invalid position: file=" + file + ", rank=" + rank);
        }
        this.file = file;
        this.rank = rank;
    }

    public int getFile() {
        return file;
    }

    public int getRank() {
        return rank;
    }

    public char getFileChar() {
        return (char) ('a' + file);
    }

    public int getRankNumber() {
        return rank + 1;
    }

    /**
     * Returns the algebraic notation for this position (e.g., "e4").
     */
    public String toAlgebraic() {
        return "" + getFileChar() + getRankNumber();
    }

    /**
     * Creates a Position from algebraic notation (e.g., "e4").
     */
    public static Position fromAlgebraic(String algebraic) {
        if (algebraic == null || algebraic.length() != 2) {
            throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
        }
        char fileChar = Character.toLowerCase(algebraic.charAt(0));
        char rankChar = algebraic.charAt(1);

        int file = fileChar - 'a';
        int rank = rankChar - '1';

        return new Position(file, rank);
    }

    /**
     * Creates a Position from array indices (row 0 = rank 8, row 7 = rank 1).
     */
    public static Position fromArrayIndices(int row, int col) {
        return new Position(col, 7 - row);
    }

    /**
     * Gets the array row index for this position (0 = rank 8, 7 = rank 1).
     */
    public int getArrayRow() {
        return 7 - rank;
    }

    /**
     * Gets the array column index for this position.
     */
    public int getArrayCol() {
        return file;
    }

    public boolean isLightSquare() {
        return (file + rank) % 2 == 1;
    }

    public boolean isDarkSquare() {
        return (file + rank) % 2 == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return file == position.file && rank == position.rank;
    }

    @Override
    public int hashCode() {
        return 31 * file + rank;
    }

    @Override
    public String toString() {
        return toAlgebraic();
    }
}
