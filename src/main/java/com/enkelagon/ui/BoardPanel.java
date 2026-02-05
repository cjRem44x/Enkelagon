package com.enkelagon.ui;

import com.enkelagon.config.ConfigManager;
import com.enkelagon.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The chess board panel with drag-drop support.
 */
public class BoardPanel extends JPanel {

    private final SquarePanel[][] squares;
    private final ThemeManager theme;
    private final ConfigManager config;

    private Board board;
    private Position selectedSquare;
    private Position dragStartSquare;
    private Set<String> legalMoves;

    private BiConsumer<Position, Position> moveCallback;
    private Consumer<Position> squareClickCallback;

    // Drag state
    private boolean dragging;
    private Point dragPoint;
    private Piece dragPiece;

    private boolean flipped;

    public BoardPanel() {
        this.squares = new SquarePanel[8][8];
        this.theme = ThemeManager.getInstance();
        this.config = ConfigManager.getInstance();
        this.legalMoves = Set.of();
        this.flipped = false;

        setLayout(new GridLayout(8, 8, 0, 0));
        setPreferredSize(new Dimension(880, 880));  // Bigger board
        setMinimumSize(new Dimension(640, 640));
        setBackground(new Color(0, 0, 0, 0));  // Transparent
        setOpaque(false);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(150, 0, 0), 3),
                BorderFactory.createLineBorder(new Color(80, 0, 0), 2)
        ));

        initializeSquares();
        setupMouseListeners();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw outer glow effect
        int width = getWidth();
        int height = getHeight();

        // Red glow layers
        for (int i = 0; i < 8; i++) {
            int alpha = 30 - i * 3;
            if (alpha > 0) {
                g2d.setColor(new Color(180, 0, 0, alpha));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect(-i - 1, -i - 1, width + i * 2 + 1, height + i * 2 + 1);
            }
        }

        g2d.dispose();
    }

    private void initializeSquares() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = Position.fromArrayIndices(row, col);
                SquarePanel square = new SquarePanel(pos);
                squares[row][col] = square;
                add(square);
            }
        }
    }

    private void setupMouseListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    private void handleMousePressed(MouseEvent e) {
        SquarePanel square = getSquareAt(e.getPoint());
        if (square == null) return;

        Position clickedPos = square.getPosition();
        Piece piece = square.getPiece();

        // If a piece is clicked and it's the right color
        if (piece != null && board != null && piece.isWhite() == board.isWhiteToMove()) {
            selectedSquare = clickedPos;
            dragStartSquare = clickedPos;
            dragPiece = piece;
            dragging = true;
            dragPoint = e.getPoint();

            // Highlight legal moves from this square
            highlightLegalMoves(clickedPos);
            square.setSelected(true);
        }

        if (squareClickCallback != null) {
            squareClickCallback.accept(clickedPos);
        }

        repaint();
    }

    private void handleMouseReleased(MouseEvent e) {
        if (dragging && dragStartSquare != null) {
            SquarePanel targetSquare = getSquareAt(e.getPoint());

            if (targetSquare != null) {
                Position targetPos = targetSquare.getPosition();

                if (!targetPos.equals(dragStartSquare) && moveCallback != null) {
                    moveCallback.accept(dragStartSquare, targetPos);
                }
            }

            dragging = false;
            dragPiece = null;
            dragStartSquare = null;
            clearHighlights();
            repaint();
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        if (dragging) {
            dragPoint = e.getPoint();
            repaint();
        }
    }

    private SquarePanel getSquareAt(Point point) {
        int squareSize = getWidth() / 8;
        int col = point.x / squareSize;
        int row = point.y / squareSize;

        if (flipped) {
            row = 7 - row;
            col = 7 - col;
        }

        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            return squares[row][col];
        }
        return null;
    }

    private void highlightLegalMoves(Position from) {
        if (legalMoves == null || !config.isHighlightLegalMoves()) return;

        String fromStr = from.toAlgebraic();
        for (String move : legalMoves) {
            if (move.startsWith(fromStr)) {
                String toStr = move.substring(2, 4);
                try {
                    Position to = Position.fromAlgebraic(toStr);
                    squares[to.getArrayRow()][to.getArrayCol()].setLegalMoveTarget(true);
                } catch (IllegalArgumentException e) {
                    // Invalid position, skip
                }
            }
        }
    }

    public void clearHighlights() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].clearHighlights();
            }
        }
        selectedSquare = null;
    }

    public void setBoard(Board board) {
        this.board = board;
        updatePieces();
    }

    public void updatePieces() {
        if (board == null) return;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                squares[row][col].setPiece(piece);
            }
        }
        repaint();
    }

    public void setLegalMoves(Set<String> legalMoves) {
        this.legalMoves = legalMoves != null ? legalMoves : Set.of();
    }

    public void highlightLastMove(Move move) {
        clearHighlights();
        if (move != null && config.isHighlightLastMove()) {
            Position from = move.getFrom();
            Position to = move.getTo();
            squares[from.getArrayRow()][from.getArrayCol()].setLastMoveSquare(true);
            squares[to.getArrayRow()][to.getArrayCol()].setLastMoveSquare(true);
        }
    }

    public void highlightCheck(Position kingPos) {
        if (kingPos != null) {
            squares[kingPos.getArrayRow()][kingPos.getArrayCol()].setInCheck(true);
        }
    }

    /**
     * Highlights a suggested move from the engine.
     */
    public void highlightSuggestedMove(String uciMove) {
        clearSuggestion();
        if (uciMove == null || uciMove.length() < 4) return;

        try {
            Position from = Position.fromAlgebraic(uciMove.substring(0, 2));
            Position to = Position.fromAlgebraic(uciMove.substring(2, 4));

            squares[from.getArrayRow()][from.getArrayCol()].setSuggestedMoveFrom(true);
            squares[to.getArrayRow()][to.getArrayCol()].setSuggestedMoveTo(true);
        } catch (IllegalArgumentException e) {
            // Invalid move format
        }
    }

    /**
     * Clears any suggested move highlighting.
     */
    public void clearSuggestion() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].clearSuggestion();
            }
        }
    }

    public void setMoveCallback(BiConsumer<Position, Position> callback) {
        this.moveCallback = callback;
    }

    public void setSquareClickCallback(Consumer<Position> callback) {
        this.squareClickCallback = callback;
    }

    public void flipBoard() {
        this.flipped = !flipped;
        removeAll();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int displayRow = flipped ? 7 - row : row;
                int displayCol = flipped ? 7 - col : col;
                add(squares[displayRow][displayCol]);
            }
        }

        revalidate();
        repaint();
    }

    public boolean isFlipped() {
        return flipped;
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);

        // Draw dragged piece on top
        if (dragging && dragPiece != null && dragPoint != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int squareSize = getWidth() / 8;
            int pieceSize = (int) (squareSize * 0.85);
            int x = dragPoint.x - pieceSize / 2;
            int y = dragPoint.y - pieceSize / 2;

            // Semi-transparent while dragging
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
            PieceRenderer.getInstance().drawPiece(g2d, dragPiece, x, y, pieceSize);

            g2d.dispose();
        }
    }

    /**
     * Shows a dialog to choose promotion piece.
     */
    public Piece showPromotionDialog(boolean isWhite) {
        Piece[] pieces = isWhite ?
                new Piece[]{Piece.WHITE_QUEEN, Piece.WHITE_ROOK, Piece.WHITE_BISHOP, Piece.WHITE_KNIGHT} :
                new Piece[]{Piece.BLACK_QUEEN, Piece.BLACK_ROOK, Piece.BLACK_BISHOP, Piece.BLACK_KNIGHT};

        String[] options = {"Queen", "Rook", "Bishop", "Knight"};

        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setBackground(theme.getBackgroundColor());

        ButtonGroup group = new ButtonGroup();
        JToggleButton[] buttons = new JToggleButton[4];

        for (int i = 0; i < 4; i++) {
            ImageIcon icon = PieceRenderer.getInstance().getIcon(pieces[i], 60);
            buttons[i] = new JToggleButton(icon);
            buttons[i].setBackground(theme.getBackgroundColor());
            buttons[i].setFocusPainted(false);
            group.add(buttons[i]);
            panel.add(buttons[i]);
        }

        buttons[0].setSelected(true);

        int result = JOptionPane.showConfirmDialog(this, panel, "Promote Pawn",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            for (int i = 0; i < 4; i++) {
                if (buttons[i].isSelected()) {
                    return pieces[i];
                }
            }
        }

        return pieces[0]; // Default to queen
    }
}
