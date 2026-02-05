package com.enkelagon.logic;

import com.enkelagon.model.Board;
import com.enkelagon.model.Piece;
import com.enkelagon.model.Position;

/**
 * Utility class for FEN string parsing and generation.
 */
public class FenParser {

    private FenParser() {
        // Utility class
    }

    /**
     * Validates a FEN string.
     */
    public static boolean isValidFen(String fen) {
        if (fen == null || fen.isEmpty()) {
            return false;
        }

        String[] parts = fen.split(" ");
        if (parts.length < 1) {
            return false;
        }

        // Validate piece placement
        String[] ranks = parts[0].split("/");
        if (ranks.length != 8) {
            return false;
        }

        for (String rank : ranks) {
            int squares = 0;
            for (char c : rank.toCharArray()) {
                if (Character.isDigit(c)) {
                    squares += Character.getNumericValue(c);
                } else if (Piece.fromFenChar(c) != null) {
                    squares++;
                } else {
                    return false;
                }
            }
            if (squares != 8) {
                return false;
            }
        }

        // Validate active color
        if (parts.length > 1 && !parts[1].equals("w") && !parts[1].equals("b")) {
            return false;
        }

        // Validate castling
        if (parts.length > 2) {
            String castling = parts[2];
            if (!castling.equals("-") && !castling.matches("^[KQkq]+$")) {
                return false;
            }
        }

        // Validate en passant
        if (parts.length > 3) {
            String ep = parts[3];
            if (!ep.equals("-") && !ep.matches("^[a-h][36]$")) {
                return false;
            }
        }

        return true;
    }

    /**
     * Creates a Board from a FEN string.
     */
    public static Board parse(String fen) {
        Board board = new Board();
        board.loadFromFen(fen);
        return board;
    }

    /**
     * Generates a FEN string from a Board.
     */
    public static String generate(Board board) {
        return board.toFen();
    }

    /**
     * Returns just the position part of a FEN (useful for repetition detection).
     */
    public static String getPositionKey(String fen) {
        String[] parts = fen.split(" ");
        if (parts.length >= 4) {
            return parts[0] + " " + parts[1] + " " + parts[2] + " " + parts[3];
        }
        return parts[0];
    }

    /**
     * Gets the piece at a specific square from a FEN string.
     */
    public static Piece getPieceAt(String fen, Position pos) {
        String[] parts = fen.split(" ");
        String[] ranks = parts[0].split("/");

        int rank = 7 - pos.getRank(); // FEN starts from rank 8
        String rankStr = ranks[rank];

        int file = 0;
        for (char c : rankStr.toCharArray()) {
            if (Character.isDigit(c)) {
                int empty = Character.getNumericValue(c);
                if (pos.getFile() < file + empty) {
                    return null;
                }
                file += empty;
            } else {
                if (file == pos.getFile()) {
                    return Piece.fromFenChar(c);
                }
                file++;
            }
        }

        return null;
    }
}
