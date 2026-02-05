package com.enkelagon.logic;

import com.enkelagon.model.Game;
import com.enkelagon.model.Move;
import com.enkelagon.model.Board;
import com.enkelagon.model.Position;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles PGN (Portable Game Notation) file read/write operations.
 */
public class PgnHandler {

    private static final Pattern TAG_PATTERN = Pattern.compile("\\[(\\w+)\\s+\"([^\"]*)\"\\]");
    private static final Pattern MOVE_NUMBER_PATTERN = Pattern.compile("(\\d+)\\.+");

    /**
     * Exports a game to PGN format.
     */
    public String exportToPgn(Game game) {
        StringBuilder pgn = new StringBuilder();

        // Standard tags
        pgn.append("[Event \"").append(game.getEvent()).append("\"]\n");
        pgn.append("[Site \"").append(game.getSite()).append("\"]\n");
        pgn.append("[Date \"").append(game.getDate()).append("\"]\n");
        pgn.append("[Round \"?\"]\n");
        pgn.append("[White \"").append(game.getWhitePlayer()).append("\"]\n");
        pgn.append("[Black \"").append(game.getBlackPlayer()).append("\"]\n");
        pgn.append("[Result \"").append(game.getResultString()).append("\"]\n");

        // FEN if not starting position
        String startFen = game.getCurrentFen();
        if (!Board.STARTING_FEN.equals(startFen) && game.getMoveCount() == 0) {
            pgn.append("[FEN \"").append(startFen).append("\"]\n");
            pgn.append("[SetUp \"1\"]\n");
        }

        // Save UCI moves for reliable loading (custom tag)
        if (game.getMoveCount() > 0) {
            StringBuilder uciMoves = new StringBuilder();
            for (Move move : game.getMoveHistory()) {
                if (uciMoves.length() > 0) uciMoves.append(" ");
                uciMoves.append(move.toUci());
            }
            pgn.append("[UCIMoves \"").append(uciMoves).append("\"]\n");
        }

        pgn.append("\n");

        // Moves
        pgn.append(formatMoves(game));

        // Result
        pgn.append(" ").append(game.getResultString());

        return pgn.toString();
    }

    /**
     * Formats moves in PGN notation.
     */
    private String formatMoves(Game game) {
        StringBuilder sb = new StringBuilder();
        List<Move> moves = game.getMoveHistory();

        int lineLength = 0;
        for (int i = 0; i < moves.size(); i++) {
            String moveStr;
            if (i % 2 == 0) {
                // White's move
                moveStr = (i / 2 + 1) + ". " + moves.get(i).toAlgebraic();
            } else {
                // Black's move
                moveStr = moves.get(i).toAlgebraic();
            }

            // Add space between moves
            if (sb.length() > 0 && !sb.toString().endsWith("\n")) {
                sb.append(" ");
                lineLength++;
            }

            // Wrap lines at ~80 characters
            if (lineLength + moveStr.length() > 80) {
                sb.append("\n");
                lineLength = 0;
            }

            sb.append(moveStr);
            lineLength += moveStr.length();
        }

        return sb.toString();
    }

    /**
     * Saves a game to a PGN file.
     */
    public void saveToFile(Game game, Path filePath) throws IOException {
        String pgn = exportToPgn(game);
        Files.writeString(filePath, pgn);
    }

    /**
     * Loads a game from a PGN file.
     * Note: This is a simplified parser that handles basic PGN.
     */
    public Game loadFromFile(Path filePath) throws IOException {
        String content = Files.readString(filePath);
        return parsePgn(content);
    }

    /**
     * Parses a PGN string into a Game object.
     */
    public Game parsePgn(String pgn) {
        Game game = new Game();

        // Parse tags
        Matcher tagMatcher = TAG_PATTERN.matcher(pgn);
        String fen = null;
        String uciMoves = null;

        while (tagMatcher.find()) {
            String tagName = tagMatcher.group(1);
            String tagValue = tagMatcher.group(2);

            switch (tagName) {
                case "Event" -> game.setEvent(tagValue);
                case "Site" -> game.setSite(tagValue);
                case "Date" -> game.setDate(tagValue);
                case "White" -> game.setWhitePlayer(tagValue);
                case "Black" -> game.setBlackPlayer(tagValue);
                case "FEN" -> fen = tagValue;
                case "UCIMoves" -> uciMoves = tagValue;
            }
        }

        // Set starting position if FEN tag present
        if (fen != null) {
            game.loadFromFen(fen);
        }

        // Replay UCI moves if present
        if (uciMoves != null && !uciMoves.trim().isEmpty()) {
            String[] moves = uciMoves.trim().split("\\s+");
            for (String uciMove : moves) {
                if (uciMove.length() >= 4) {
                    try {
                        Move move = Move.fromUci(uciMove, game.getBoard());
                        game.makeMove(move);
                    } catch (Exception e) {
                        // Skip invalid moves silently
                    }
                }
            }
        }

        return game;
    }

    /**
     * Parses move text and returns a list of algebraic move strings.
     */
    public List<String> parseMoveText(String moveText) {
        List<String> moves = new ArrayList<>();

        // Remove comments
        moveText = moveText.replaceAll("\\{[^}]*\\}", "");
        moveText = moveText.replaceAll("\\([^)]*\\)", "");

        // Remove result
        moveText = moveText.replaceAll("1-0|0-1|1/2-1/2|\\*", "");

        // Remove move numbers
        moveText = moveText.replaceAll("\\d+\\.+", "");

        // Split by whitespace
        String[] tokens = moveText.trim().split("\\s+");
        for (String token : tokens) {
            token = token.trim();
            if (!token.isEmpty() && !token.equals("...")) {
                // Clean up check/checkmate symbols for parsing
                moves.add(token);
            }
        }

        return moves;
    }

    /**
     * Validates a PGN file.
     */
    public boolean isValidPgn(String pgn) {
        // Must have at least some tags
        if (!pgn.contains("[")) {
            return false;
        }

        // Must have Event tag at minimum
        return TAG_PATTERN.matcher(pgn).find();
    }
}
