package com.enkelagon.model;

/**
 * Represents a chess move.
 */
public class Move {
    private final Position from;
    private final Position to;
    private final Piece piece;
    private final Piece capturedPiece;
    private final Piece promotionPiece;
    private final boolean castling;
    private final boolean enPassant;
    private final boolean check;
    private final boolean checkmate;

    private Move(Builder builder) {
        this.from = builder.from;
        this.to = builder.to;
        this.piece = builder.piece;
        this.capturedPiece = builder.capturedPiece;
        this.promotionPiece = builder.promotionPiece;
        this.castling = builder.castling;
        this.enPassant = builder.enPassant;
        this.check = builder.check;
        this.checkmate = builder.checkmate;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public Piece getPiece() {
        return piece;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public Piece getPromotionPiece() {
        return promotionPiece;
    }

    public boolean isCapture() {
        return capturedPiece != null;
    }

    public boolean isPromotion() {
        return promotionPiece != null;
    }

    public boolean isCastling() {
        return castling;
    }

    public boolean isEnPassant() {
        return enPassant;
    }

    public boolean isCheck() {
        return check;
    }

    public boolean isCheckmate() {
        return checkmate;
    }

    public boolean isKingsideCastling() {
        return castling && to.getFile() > from.getFile();
    }

    public boolean isQueensideCastling() {
        return castling && to.getFile() < from.getFile();
    }

    /**
     * Returns the UCI notation for this move (e.g., "e2e4", "e7e8q").
     */
    public String toUci() {
        String uci = from.toAlgebraic() + to.toAlgebraic();
        if (promotionPiece != null) {
            uci += Character.toLowerCase(promotionPiece.getFenChar());
        }
        return uci;
    }

    /**
     * Creates a Move from UCI notation.
     */
    public static Move fromUci(String uci, Board board) {
        if (uci == null || uci.length() < 4) {
            throw new IllegalArgumentException("Invalid UCI notation: " + uci);
        }

        Position from = Position.fromAlgebraic(uci.substring(0, 2));
        Position to = Position.fromAlgebraic(uci.substring(2, 4));
        Piece piece = board.getPieceAt(from);
        Piece capturedPiece = board.getPieceAt(to);

        Builder builder = new Builder(from, to, piece)
                .capturedPiece(capturedPiece);

        // Check for promotion
        if (uci.length() == 5) {
            char promoChar = uci.charAt(4);
            if (piece != null && piece.isWhite()) {
                promoChar = Character.toUpperCase(promoChar);
            }
            builder.promotionPiece(Piece.fromFenChar(promoChar));
        }

        // Check for castling
        if (piece != null && (piece == Piece.WHITE_KING || piece == Piece.BLACK_KING)) {
            int fileDiff = Math.abs(to.getFile() - from.getFile());
            if (fileDiff == 2) {
                builder.castling(true);
            }
        }

        // Check for en passant
        if (piece != null && (piece == Piece.WHITE_PAWN || piece == Piece.BLACK_PAWN)) {
            if (from.getFile() != to.getFile() && capturedPiece == null) {
                builder.enPassant(true);
                // The captured pawn is on the same rank as the from position
                Position epCapture = new Position(to.getFile(), from.getRank());
                builder.capturedPiece(board.getPieceAt(epCapture));
            }
        }

        return builder.build();
    }

    /**
     * Returns algebraic notation for this move.
     */
    public String toAlgebraic() {
        if (castling) {
            return isKingsideCastling() ? "O-O" : "O-O-O";
        }

        StringBuilder sb = new StringBuilder();

        // Piece letter (not for pawns)
        if (piece != null) {
            char pieceChar = Character.toUpperCase(piece.getFenChar());
            if (pieceChar != 'P') {
                sb.append(pieceChar);
            }
        }

        // Capture symbol
        if (isCapture() || isEnPassant()) {
            if (piece != null && (piece == Piece.WHITE_PAWN || piece == Piece.BLACK_PAWN)) {
                sb.append(from.getFileChar());
            }
            sb.append('x');
        }

        // Destination square
        sb.append(to.toAlgebraic());

        // Promotion
        if (promotionPiece != null) {
            sb.append('=');
            sb.append(Character.toUpperCase(promotionPiece.getFenChar()));
        }

        // Check/checkmate
        if (checkmate) {
            sb.append('#');
        } else if (check) {
            sb.append('+');
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return toUci();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Move move = (Move) obj;
        return from.equals(move.from) && to.equals(move.to) &&
               (promotionPiece == move.promotionPiece);
    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        if (promotionPiece != null) {
            result = 31 * result + promotionPiece.hashCode();
        }
        return result;
    }

    public static class Builder {
        private final Position from;
        private final Position to;
        private Piece piece;
        private Piece capturedPiece;
        private Piece promotionPiece;
        private boolean castling;
        private boolean enPassant;
        private boolean check;
        private boolean checkmate;

        public Builder(Position from, Position to, Piece piece) {
            this.from = from;
            this.to = to;
            this.piece = piece;
        }

        public Builder capturedPiece(Piece capturedPiece) {
            this.capturedPiece = capturedPiece;
            return this;
        }

        public Builder promotionPiece(Piece promotionPiece) {
            this.promotionPiece = promotionPiece;
            return this;
        }

        public Builder castling(boolean castling) {
            this.castling = castling;
            return this;
        }

        public Builder enPassant(boolean enPassant) {
            this.enPassant = enPassant;
            return this;
        }

        public Builder check(boolean check) {
            this.check = check;
            return this;
        }

        public Builder checkmate(boolean checkmate) {
            this.checkmate = checkmate;
            return this;
        }

        public Move build() {
            return new Move(this);
        }
    }
}
