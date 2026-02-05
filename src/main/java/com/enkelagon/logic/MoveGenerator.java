package com.enkelagon.logic;

import com.enkelagon.model.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Generates legal moves for the current position.
 * Uses Stockfish for authoritative move generation.
 */
public class MoveGenerator {

    /**
     * Parses legal moves from Stockfish's response to "go perft 1" or similar.
     * Expected format: "a2a3: 1\na2a4: 1\n..." or just "a2a3 a2a4 ..."
     */
    public Set<String> parseLegalMoves(String stockfishOutput) {
        Set<String> moves = new HashSet<>();

        if (stockfishOutput == null || stockfishOutput.isEmpty()) {
            return moves;
        }

        String[] lines = stockfishOutput.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Handle perft format: "e2e4: 1"
            if (line.contains(":")) {
                String move = line.split(":")[0].trim();
                if (isValidUciMove(move)) {
                    moves.add(move);
                }
            }
            // Handle space-separated format
            else {
                String[] parts = line.split("\\s+");
                for (String part : parts) {
                    if (isValidUciMove(part)) {
                        moves.add(part);
                    }
                }
            }
        }

        return moves;
    }

    /**
     * Validates UCI move format (e.g., "e2e4", "e7e8q").
     */
    private boolean isValidUciMove(String move) {
        if (move == null || move.length() < 4 || move.length() > 5) {
            return false;
        }

        char fromFile = move.charAt(0);
        char fromRank = move.charAt(1);
        char toFile = move.charAt(2);
        char toRank = move.charAt(3);

        if (fromFile < 'a' || fromFile > 'h') return false;
        if (fromRank < '1' || fromRank > '8') return false;
        if (toFile < 'a' || toFile > 'h') return false;
        if (toRank < '1' || toRank > '8') return false;

        // Promotion piece
        if (move.length() == 5) {
            char promo = Character.toLowerCase(move.charAt(4));
            if (promo != 'q' && promo != 'r' && promo != 'b' && promo != 'n') {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets all legal moves from a specific square.
     */
    public Set<String> getMovesFromSquare(Set<String> allMoves, Position from) {
        Set<String> moves = new HashSet<>();
        String fromStr = from.toAlgebraic();

        for (String move : allMoves) {
            if (move.startsWith(fromStr)) {
                moves.add(move);
            }
        }

        return moves;
    }

    /**
     * Gets all destination squares for moves from a specific square.
     */
    public Set<Position> getDestinationsFromSquare(Set<String> allMoves, Position from) {
        Set<Position> destinations = new HashSet<>();
        String fromStr = from.toAlgebraic();

        for (String move : allMoves) {
            if (move.startsWith(fromStr)) {
                String toStr = move.substring(2, 4);
                try {
                    destinations.add(Position.fromAlgebraic(toStr));
                } catch (IllegalArgumentException e) {
                    // Invalid position, skip
                }
            }
        }

        return destinations;
    }

    /**
     * Generates pseudo-legal pawn moves (for basic UI hints).
     * Does not account for pins, checks, etc.
     */
    public Set<Position> getPawnMoves(Board board, Position from) {
        Set<Position> moves = new HashSet<>();
        Piece pawn = board.getPieceAt(from);

        if (pawn == null) return moves;

        int direction = pawn.isWhite() ? 1 : -1;
        int startRank = pawn.isWhite() ? 1 : 6;

        // Single push
        int newRank = from.getRank() + direction;
        if (newRank >= 0 && newRank <= 7) {
            Position single = new Position(from.getFile(), newRank);
            if (board.getPieceAt(single) == null) {
                moves.add(single);

                // Double push from starting position
                if (from.getRank() == startRank) {
                    Position doublePush = new Position(from.getFile(), from.getRank() + 2 * direction);
                    if (board.getPieceAt(doublePush) == null) {
                        moves.add(doublePush);
                    }
                }
            }
        }

        // Captures
        for (int fileDelta : new int[]{-1, 1}) {
            int newFile = from.getFile() + fileDelta;
            if (newFile >= 0 && newFile <= 7 && newRank >= 0 && newRank <= 7) {
                Position capture = new Position(newFile, newRank);
                Piece target = board.getPieceAt(capture);

                // Regular capture
                if (target != null && target.isWhite() != pawn.isWhite()) {
                    moves.add(capture);
                }

                // En passant
                Position epTarget = board.getEnPassantTarget();
                if (epTarget != null && capture.equals(epTarget)) {
                    moves.add(capture);
                }
            }
        }

        return moves;
    }
}
