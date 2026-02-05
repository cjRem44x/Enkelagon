package com.enkelagon.logic;

import com.enkelagon.model.*;

import java.util.Set;

/**
 * Validates chess moves. For complex validation, delegates to Stockfish.
 */
public class MoveValidator {

    private Set<String> legalMoves;

    public MoveValidator() {
        this.legalMoves = Set.of();
    }

    /**
     * Sets the legal moves from Stockfish's response.
     */
    public void setLegalMoves(Set<String> legalMoves) {
        this.legalMoves = legalMoves;
    }

    /**
     * Checks if a move is legal based on the current legal move set.
     */
    public boolean isLegalMove(Move move) {
        return legalMoves.contains(move.toUci());
    }

    /**
     * Checks if a UCI move string is legal.
     */
    public boolean isLegalMove(String uciMove) {
        return legalMoves.contains(uciMove);
    }

    /**
     * Basic validation before checking with engine.
     * Returns false for obviously illegal moves.
     */
    public boolean basicValidation(Board board, Position from, Position to) {
        Piece piece = board.getPieceAt(from);

        // Must have a piece to move
        if (piece == null) {
            return false;
        }

        // Must be correct color's turn
        if (piece.isWhite() != board.isWhiteToMove()) {
            return false;
        }

        // Cannot capture own piece
        Piece targetPiece = board.getPieceAt(to);
        if (targetPiece != null && targetPiece.isWhite() == piece.isWhite()) {
            return false;
        }

        // Cannot move to same square
        if (from.equals(to)) {
            return false;
        }

        return true;
    }

    /**
     * Checks if a move is a potential promotion move.
     */
    public boolean isPromotionMove(Board board, Position from, Position to) {
        Piece piece = board.getPieceAt(from);
        if (piece == null) {
            return false;
        }

        if (piece == Piece.WHITE_PAWN && to.getRank() == 7) {
            return true;
        }
        if (piece == Piece.BLACK_PAWN && to.getRank() == 0) {
            return true;
        }

        return false;
    }

    /**
     * Gets the available promotion pieces for the current player.
     */
    public Piece[] getPromotionPieces(boolean white) {
        if (white) {
            return new Piece[]{
                    Piece.WHITE_QUEEN,
                    Piece.WHITE_ROOK,
                    Piece.WHITE_BISHOP,
                    Piece.WHITE_KNIGHT
            };
        } else {
            return new Piece[]{
                    Piece.BLACK_QUEEN,
                    Piece.BLACK_ROOK,
                    Piece.BLACK_BISHOP,
                    Piece.BLACK_KNIGHT
            };
        }
    }

    /**
     * Gets the number of legal moves available.
     */
    public int getLegalMoveCount() {
        return legalMoves.size();
    }

    /**
     * Checks if there are any legal moves.
     */
    public boolean hasLegalMoves() {
        return !legalMoves.isEmpty();
    }
}
