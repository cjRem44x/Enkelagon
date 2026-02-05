package com.enkelagon.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a chess game with state, move history, and game status.
 */
public class Game {

    public enum Status {
        IN_PROGRESS,
        WHITE_WINS_CHECKMATE,
        BLACK_WINS_CHECKMATE,
        STALEMATE,
        DRAW_FIFTY_MOVE,
        DRAW_THREEFOLD,
        DRAW_INSUFFICIENT_MATERIAL,
        DRAW_AGREEMENT,
        WHITE_RESIGNS,
        BLACK_RESIGNS
    }

    private Board board;
    private final List<Move> moveHistory;
    private final List<String> fenHistory;
    private Status status;
    private String whitePlayer;
    private String blackPlayer;
    private String event;
    private String site;
    private String date;

    public Game() {
        this.board = new Board();
        this.moveHistory = new ArrayList<>();
        this.fenHistory = new ArrayList<>();
        this.status = Status.IN_PROGRESS;
        this.whitePlayer = "Human";
        this.blackPlayer = "Stockfish";
        this.event = "Casual Game";
        this.site = "Enkelagon";
        this.date = java.time.LocalDate.now().toString();
        fenHistory.add(board.toFen());
    }

    public Board getBoard() {
        return board;
    }

    public List<Move> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }

    public int getMoveCount() {
        return moveHistory.size();
    }

    public Move getLastMove() {
        return moveHistory.isEmpty() ? null : moveHistory.get(moveHistory.size() - 1);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isGameOver() {
        return status != Status.IN_PROGRESS;
    }

    public boolean isWhiteToMove() {
        return board.isWhiteToMove();
    }

    public String getWhitePlayer() {
        return whitePlayer;
    }

    public void setWhitePlayer(String whitePlayer) {
        this.whitePlayer = whitePlayer;
    }

    public String getBlackPlayer() {
        return blackPlayer;
    }

    public void setBlackPlayer(String blackPlayer) {
        this.blackPlayer = blackPlayer;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Makes a move on the board and records it in history.
     */
    public void makeMove(Move move) {
        board.makeMove(move);
        moveHistory.add(move);
        fenHistory.add(board.toFen());
    }

    /**
     * Undoes the last move if possible.
     */
    public Move undoMove() {
        if (moveHistory.isEmpty()) {
            return null;
        }

        Move lastMove = moveHistory.remove(moveHistory.size() - 1);
        fenHistory.remove(fenHistory.size() - 1);

        // Reload board from the previous FEN
        String previousFen = fenHistory.get(fenHistory.size() - 1);
        board.loadFromFen(previousFen);

        if (status != Status.IN_PROGRESS) {
            status = Status.IN_PROGRESS;
        }

        return lastMove;
    }

    /**
     * Resets the game to initial position.
     */
    public void reset() {
        board.reset();
        moveHistory.clear();
        fenHistory.clear();
        fenHistory.add(board.toFen());
        status = Status.IN_PROGRESS;
    }

    /**
     * Loads a game from FEN.
     */
    public void loadFromFen(String fen) {
        board.loadFromFen(fen);
        moveHistory.clear();
        fenHistory.clear();
        fenHistory.add(fen);
        status = Status.IN_PROGRESS;
    }

    /**
     * Gets the current position's FEN.
     */
    public String getCurrentFen() {
        return board.toFen();
    }

    /**
     * Returns the move history in algebraic notation for display.
     */
    public List<String> getMoveHistoryAlgebraic() {
        List<String> algebraic = new ArrayList<>();
        for (Move move : moveHistory) {
            algebraic.add(move.toAlgebraic());
        }
        return algebraic;
    }

    /**
     * Returns formatted move history for PGN (numbered move pairs).
     */
    public String getFormattedMoveHistory() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < moveHistory.size(); i++) {
            if (i % 2 == 0) {
                sb.append((i / 2 + 1)).append(". ");
            }
            sb.append(moveHistory.get(i).toAlgebraic());
            if (i % 2 == 0 && i < moveHistory.size() - 1) {
                sb.append(" ");
            } else if (i % 2 == 1) {
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    /**
     * Gets the result string for PGN.
     */
    public String getResultString() {
        return switch (status) {
            case WHITE_WINS_CHECKMATE, BLACK_RESIGNS -> "1-0";
            case BLACK_WINS_CHECKMATE, WHITE_RESIGNS -> "0-1";
            case STALEMATE, DRAW_FIFTY_MOVE, DRAW_THREEFOLD,
                 DRAW_INSUFFICIENT_MATERIAL, DRAW_AGREEMENT -> "1/2-1/2";
            case IN_PROGRESS -> "*";
        };
    }

    /**
     * Checks for threefold repetition.
     */
    public boolean isThreefoldRepetition() {
        if (fenHistory.size() < 5) return false;

        String currentPosition = extractPositionFromFen(getCurrentFen());
        int count = 0;
        for (String fen : fenHistory) {
            if (extractPositionFromFen(fen).equals(currentPosition)) {
                count++;
                if (count >= 3) return true;
            }
        }
        return false;
    }

    private String extractPositionFromFen(String fen) {
        // Extract just the position, active color, castling, and en passant (not clocks)
        String[] parts = fen.split(" ");
        if (parts.length >= 4) {
            return parts[0] + " " + parts[1] + " " + parts[2] + " " + parts[3];
        }
        return fen;
    }

    /**
     * Checks for fifty-move rule.
     */
    public boolean isFiftyMoveRule() {
        return board.getHalfmoveClock() >= 100;
    }
}
