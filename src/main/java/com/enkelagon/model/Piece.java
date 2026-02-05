package com.enkelagon.model;

/**
 * Represents a chess piece with its type and color.
 */
public enum Piece {
    WHITE_KING('K', true, "king"),
    WHITE_QUEEN('Q', true, "queen"),
    WHITE_ROOK('R', true, "rook"),
    WHITE_BISHOP('B', true, "bishop"),
    WHITE_KNIGHT('N', true, "knight"),
    WHITE_PAWN('P', true, "pawn"),
    BLACK_KING('k', false, "king"),
    BLACK_QUEEN('q', false, "queen"),
    BLACK_ROOK('r', false, "rook"),
    BLACK_BISHOP('b', false, "bishop"),
    BLACK_KNIGHT('n', false, "knight"),
    BLACK_PAWN('p', false, "pawn");

    private final char fenChar;
    private final boolean white;
    private final String pieceName;

    Piece(char fenChar, boolean white, String pieceName) {
        this.fenChar = fenChar;
        this.white = white;
        this.pieceName = pieceName;
    }

    public char getFenChar() {
        return fenChar;
    }

    public boolean isWhite() {
        return white;
    }

    public boolean isBlack() {
        return !white;
    }

    public String getPieceName() {
        return pieceName;
    }

    public String getColorName() {
        return white ? "white" : "black";
    }

    public String getImageFileName() {
        return getColorName() + "-" + pieceName + ".png";
    }

    public static Piece fromFenChar(char c) {
        for (Piece p : values()) {
            if (p.fenChar == c) {
                return p;
            }
        }
        return null;
    }

    public static Piece of(String pieceName, boolean white) {
        for (Piece p : values()) {
            if (p.pieceName.equals(pieceName) && p.white == white) {
                return p;
            }
        }
        return null;
    }
}
