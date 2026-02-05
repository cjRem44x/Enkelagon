package com.enkelagon.model;

/**
 * Represents the chess board state.
 */
public class Board {
    public static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private final Piece[][] squares; // [row][col], row 0 = rank 8
    private boolean whiteToMove;
    private boolean whiteKingsideCastle;
    private boolean whiteQueensideCastle;
    private boolean blackKingsideCastle;
    private boolean blackQueensideCastle;
    private Position enPassantTarget;
    private int halfmoveClock;
    private int fullmoveNumber;

    public Board() {
        this.squares = new Piece[8][8];
        reset();
    }

    public Board(Board other) {
        this.squares = new Piece[8][8];
        for (int row = 0; row < 8; row++) {
            System.arraycopy(other.squares[row], 0, this.squares[row], 0, 8);
        }
        this.whiteToMove = other.whiteToMove;
        this.whiteKingsideCastle = other.whiteKingsideCastle;
        this.whiteQueensideCastle = other.whiteQueensideCastle;
        this.blackKingsideCastle = other.blackKingsideCastle;
        this.blackQueensideCastle = other.blackQueensideCastle;
        this.enPassantTarget = other.enPassantTarget;
        this.halfmoveClock = other.halfmoveClock;
        this.fullmoveNumber = other.fullmoveNumber;
    }

    public void reset() {
        loadFromFen(STARTING_FEN);
    }

    public void clear() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col] = null;
            }
        }
        whiteToMove = true;
        whiteKingsideCastle = false;
        whiteQueensideCastle = false;
        blackKingsideCastle = false;
        blackQueensideCastle = false;
        enPassantTarget = null;
        halfmoveClock = 0;
        fullmoveNumber = 1;
    }

    public Piece getPieceAt(Position pos) {
        return squares[pos.getArrayRow()][pos.getArrayCol()];
    }

    public Piece getPieceAt(int row, int col) {
        return squares[row][col];
    }

    public void setPieceAt(Position pos, Piece piece) {
        squares[pos.getArrayRow()][pos.getArrayCol()] = piece;
    }

    public void setPieceAt(int row, int col, Piece piece) {
        squares[row][col] = piece;
    }

    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    public void setWhiteToMove(boolean whiteToMove) {
        this.whiteToMove = whiteToMove;
    }

    public boolean canWhiteCastleKingside() {
        return whiteKingsideCastle;
    }

    public boolean canWhiteCastleQueenside() {
        return whiteQueensideCastle;
    }

    public boolean canBlackCastleKingside() {
        return blackKingsideCastle;
    }

    public boolean canBlackCastleQueenside() {
        return blackQueensideCastle;
    }

    public Position getEnPassantTarget() {
        return enPassantTarget;
    }

    public int getHalfmoveClock() {
        return halfmoveClock;
    }

    public int getFullmoveNumber() {
        return fullmoveNumber;
    }

    /**
     * Applies a move to the board. Does not validate legality.
     */
    public void makeMove(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece piece = getPieceAt(from);

        // Remove piece from source
        setPieceAt(from, null);

        // Handle en passant capture
        if (move.isEnPassant()) {
            Position capturedPawnPos = new Position(to.getFile(), from.getRank());
            setPieceAt(capturedPawnPos, null);
        }

        // Handle castling - move the rook
        if (move.isCastling()) {
            if (move.isKingsideCastling()) {
                Position rookFrom = new Position(7, from.getRank());
                Position rookTo = new Position(5, from.getRank());
                Piece rook = getPieceAt(rookFrom);
                setPieceAt(rookFrom, null);
                setPieceAt(rookTo, rook);
            } else {
                Position rookFrom = new Position(0, from.getRank());
                Position rookTo = new Position(3, from.getRank());
                Piece rook = getPieceAt(rookFrom);
                setPieceAt(rookFrom, null);
                setPieceAt(rookTo, rook);
            }
        }

        // Place piece at destination (or promoted piece)
        if (move.isPromotion()) {
            setPieceAt(to, move.getPromotionPiece());
        } else {
            setPieceAt(to, piece);
        }

        // Update castling rights
        updateCastlingRights(move, piece);

        // Update en passant target
        updateEnPassantTarget(move, piece);

        // Update clocks
        if (piece == Piece.WHITE_PAWN || piece == Piece.BLACK_PAWN || move.isCapture()) {
            halfmoveClock = 0;
        } else {
            halfmoveClock++;
        }

        if (!whiteToMove) {
            fullmoveNumber++;
        }

        // Switch turn
        whiteToMove = !whiteToMove;
    }

    private void updateCastlingRights(Move move, Piece piece) {
        Position from = move.getFrom();
        Position to = move.getTo();

        // King moves lose all castling rights for that side
        if (piece == Piece.WHITE_KING) {
            whiteKingsideCastle = false;
            whiteQueensideCastle = false;
        } else if (piece == Piece.BLACK_KING) {
            blackKingsideCastle = false;
            blackQueensideCastle = false;
        }

        // Rook moves or captures lose specific castling rights
        if (from.equals(new Position(0, 0)) || to.equals(new Position(0, 0))) {
            blackQueensideCastle = false;
        }
        if (from.equals(new Position(7, 0)) || to.equals(new Position(7, 0))) {
            blackKingsideCastle = false;
        }
        if (from.equals(new Position(0, 7)) || to.equals(new Position(0, 7))) {
            whiteQueensideCastle = false;
        }
        if (from.equals(new Position(7, 7)) || to.equals(new Position(7, 7))) {
            whiteKingsideCastle = false;
        }
    }

    private void updateEnPassantTarget(Move move, Piece piece) {
        // Check for double pawn push
        if ((piece == Piece.WHITE_PAWN || piece == Piece.BLACK_PAWN) &&
                Math.abs(move.getTo().getRank() - move.getFrom().getRank()) == 2) {
            int epRank = (move.getFrom().getRank() + move.getTo().getRank()) / 2;
            enPassantTarget = new Position(move.getFrom().getFile(), epRank);
        } else {
            enPassantTarget = null;
        }
    }

    /**
     * Finds the position of the king for the given color.
     */
    public Position findKing(boolean white) {
        Piece targetKing = white ? Piece.WHITE_KING : Piece.BLACK_KING;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (squares[row][col] == targetKing) {
                    return Position.fromArrayIndices(row, col);
                }
            }
        }
        return null;
    }

    /**
     * Loads board state from a FEN string.
     */
    public void loadFromFen(String fen) {
        clear();
        String[] parts = fen.split(" ");

        // Piece placement
        String[] ranks = parts[0].split("/");
        for (int row = 0; row < 8; row++) {
            int col = 0;
            for (char c : ranks[row].toCharArray()) {
                if (Character.isDigit(c)) {
                    col += Character.getNumericValue(c);
                } else {
                    squares[row][col] = Piece.fromFenChar(c);
                    col++;
                }
            }
        }

        // Active color
        if (parts.length > 1) {
            whiteToMove = parts[1].equals("w");
        }

        // Castling rights
        if (parts.length > 2) {
            String castling = parts[2];
            whiteKingsideCastle = castling.contains("K");
            whiteQueensideCastle = castling.contains("Q");
            blackKingsideCastle = castling.contains("k");
            blackQueensideCastle = castling.contains("q");
        }

        // En passant target
        if (parts.length > 3 && !parts[3].equals("-")) {
            enPassantTarget = Position.fromAlgebraic(parts[3]);
        }

        // Halfmove clock
        if (parts.length > 4) {
            halfmoveClock = Integer.parseInt(parts[4]);
        }

        // Fullmove number
        if (parts.length > 5) {
            fullmoveNumber = Integer.parseInt(parts[5]);
        }
    }

    /**
     * Generates the FEN string for the current board state.
     */
    public String toFen() {
        StringBuilder sb = new StringBuilder();

        // Piece placement
        for (int row = 0; row < 8; row++) {
            int emptyCount = 0;
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                if (piece == null) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        sb.append(emptyCount);
                        emptyCount = 0;
                    }
                    sb.append(piece.getFenChar());
                }
            }
            if (emptyCount > 0) {
                sb.append(emptyCount);
            }
            if (row < 7) {
                sb.append('/');
            }
        }

        sb.append(' ');

        // Active color
        sb.append(whiteToMove ? 'w' : 'b');
        sb.append(' ');

        // Castling rights
        StringBuilder castling = new StringBuilder();
        if (whiteKingsideCastle) castling.append('K');
        if (whiteQueensideCastle) castling.append('Q');
        if (blackKingsideCastle) castling.append('k');
        if (blackQueensideCastle) castling.append('q');
        sb.append(castling.length() > 0 ? castling : "-");
        sb.append(' ');

        // En passant target
        sb.append(enPassantTarget != null ? enPassantTarget.toAlgebraic() : "-");
        sb.append(' ');

        // Halfmove clock
        sb.append(halfmoveClock);
        sb.append(' ');

        // Fullmove number
        sb.append(fullmoveNumber);

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  a b c d e f g h\n");
        for (int row = 0; row < 8; row++) {
            sb.append(8 - row).append(" ");
            for (int col = 0; col < 8; col++) {
                Piece piece = squares[row][col];
                sb.append(piece != null ? piece.getFenChar() : '.');
                sb.append(' ');
            }
            sb.append(8 - row).append("\n");
        }
        sb.append("  a b c d e f g h\n");
        return sb.toString();
    }
}
