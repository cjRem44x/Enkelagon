package com.enkelagon.ui;

import com.enkelagon.model.Piece;
import com.enkelagon.model.Position;

import javax.swing.*;
import java.awt.*;

/**
 * Represents a single square on the chess board.
 */
public class SquarePanel extends JPanel {

    private final Position position;
    private final boolean isLight;
    private Piece piece;

    private boolean highlighted;
    private boolean legalMoveTarget;
    private boolean lastMoveSquare;
    private boolean inCheck;
    private boolean selected;
    private boolean suggestedMoveFrom;
    private boolean suggestedMoveTo;

    private final ThemeManager theme;
    private final PieceRenderer pieceRenderer;

    private static final int TILE_OPACITY = 178;  // 70% opacity

    public SquarePanel(Position position) {
        this.position = position;
        this.isLight = position.isLightSquare();
        this.theme = ThemeManager.getInstance();
        this.pieceRenderer = PieceRenderer.getInstance();

        setOpaque(false);  // Allow transparency
        setPreferredSize(new Dimension(110, 110));  // Bigger squares
    }

    public Position getPosition() {
        return position;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
        repaint();
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        repaint();
    }

    public boolean isLegalMoveTarget() {
        return legalMoveTarget;
    }

    public void setLegalMoveTarget(boolean legalMoveTarget) {
        this.legalMoveTarget = legalMoveTarget;
        repaint();
    }

    public boolean isLastMoveSquare() {
        return lastMoveSquare;
    }

    public void setLastMoveSquare(boolean lastMoveSquare) {
        this.lastMoveSquare = lastMoveSquare;
        repaint();
    }

    public boolean isInCheck() {
        return inCheck;
    }

    public void setInCheck(boolean inCheck) {
        this.inCheck = inCheck;
        repaint();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        repaint();
    }

    public boolean isSuggestedMoveFrom() {
        return suggestedMoveFrom;
    }

    public void setSuggestedMoveFrom(boolean suggestedMoveFrom) {
        this.suggestedMoveFrom = suggestedMoveFrom;
        repaint();
    }

    public boolean isSuggestedMoveTo() {
        return suggestedMoveTo;
    }

    public void setSuggestedMoveTo(boolean suggestedMoveTo) {
        this.suggestedMoveTo = suggestedMoveTo;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int width = getWidth();
        int height = getHeight();

        // Determine background color based on state - DARKER with transparency
        Color baseColor;
        Color borderColor = null;
        int borderWidth = 0;

        if (inCheck) {
            baseColor = new Color(180, 30, 30);
            borderColor = new Color(255, 50, 50);
            borderWidth = 3;
        } else if (selected || highlighted) {
            baseColor = new Color(120, 20, 20);
            borderColor = new Color(255, 100, 100);
            borderWidth = 2;
        } else if (lastMoveSquare) {
            baseColor = new Color(80, 20, 20);
            borderColor = new Color(200, 50, 50, 150);
            borderWidth = 2;
        } else {
            // Darker base colors - dark squares have subtle red tint mixed with black
            baseColor = isLight ? new Color(35, 35, 40) : new Color(28, 10, 12);
        }

        // Apply 80% opacity to the base color
        Color bgColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), TILE_OPACITY);

        // Fill background with gradient for depth (with transparency)
        Color gradTop = new Color(
                Math.min(255, (int)(baseColor.getRed() * 1.2)),
                Math.min(255, (int)(baseColor.getGreen() * 1.2)),
                Math.min(255, (int)(baseColor.getBlue() * 1.2)),
                TILE_OPACITY
        );
        Color gradBottom = new Color(
                (int)(baseColor.getRed() * 0.7),
                (int)(baseColor.getGreen() * 0.7),
                (int)(baseColor.getBlue() * 0.7),
                TILE_OPACITY
        );
        GradientPaint gradient = new GradientPaint(0, 0, gradTop, width, height, gradBottom);
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, width, height);

        // Add subtle inner highlight (top-left)
        g2d.setColor(new Color(255, 255, 255, 15));
        g2d.fillRect(0, 0, width, 1);
        g2d.fillRect(0, 0, 1, height);

        // Add inner shadow (bottom-right)
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.fillRect(0, height - 1, width, 1);
        g2d.fillRect(width - 1, 0, 1, height);

        // Draw glowing border for special states
        if (borderColor != null && borderWidth > 0) {
            // Outer glow
            for (int i = borderWidth; i > 0; i--) {
                int alpha = (int) (100 * ((float) i / borderWidth));
                g2d.setColor(new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), alpha));
                g2d.setStroke(new BasicStroke(i * 2));
                g2d.drawRect(i, i, width - i * 2, height - i * 2);
            }
            // Inner border
            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(borderWidth));
            g2d.drawRect(borderWidth / 2, borderWidth / 2, width - borderWidth, height - borderWidth);
        }

        // Draw legal move indicator
        if (legalMoveTarget) {
            Color legalColor = theme.getLegalMoveColor();

            if (piece == null) {
                // Draw glowing dot for empty square
                int dotSize = width / 4;
                int dotX = (width - dotSize) / 2;
                int dotY = (height - dotSize) / 2;

                // Glow effect
                for (int i = 3; i > 0; i--) {
                    int glowSize = dotSize + i * 6;
                    int glowX = (width - glowSize) / 2;
                    int glowY = (height - glowSize) / 2;
                    g2d.setColor(new Color(legalColor.getRed(), legalColor.getGreen(), legalColor.getBlue(), 30 * i));
                    g2d.fillOval(glowX, glowY, glowSize, glowSize);
                }

                g2d.setColor(new Color(legalColor.getRed(), legalColor.getGreen(), legalColor.getBlue(), 180));
                g2d.fillOval(dotX, dotY, dotSize, dotSize);
            } else {
                // Draw glowing ring for capture target
                for (int i = 3; i > 0; i--) {
                    g2d.setColor(new Color(255, 50, 50, 40 * i));
                    g2d.setStroke(new BasicStroke(3 + i * 2));
                    g2d.drawOval(5, 5, width - 10, height - 10);
                }
                g2d.setColor(new Color(255, 80, 80, 200));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawOval(5, 5, width - 10, height - 10);
            }
        }

        // Draw suggested move highlight (green/cyan glow)
        if (suggestedMoveFrom || suggestedMoveTo) {
            Color hintColor = new Color(0, 200, 150);  // Cyan-green

            // Pulsing glow effect
            for (int i = 5; i > 0; i--) {
                int alpha = 25 * i;
                g2d.setColor(new Color(hintColor.getRed(), hintColor.getGreen(), hintColor.getBlue(), alpha));
                g2d.setStroke(new BasicStroke(i * 2));
                g2d.drawRoundRect(i * 2, i * 2, width - i * 4, height - i * 4, 8, 8);
            }

            // Inner bright border
            g2d.setColor(new Color(0, 255, 180, 200));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(4, 4, width - 8, height - 8, 6, 6);

            // Arrow indicator for "from" square
            if (suggestedMoveFrom) {
                g2d.setColor(new Color(0, 255, 180, 150));
                int arrowSize = width / 5;
                int cx = width / 2;
                int cy = height / 2;
                g2d.fillOval(cx - arrowSize / 2, cy - arrowSize / 2, arrowSize, arrowSize);
            }

            // Target indicator for "to" square
            if (suggestedMoveTo) {
                g2d.setColor(new Color(0, 255, 180, 120));
                g2d.setStroke(new BasicStroke(4));
                int margin = 12;
                g2d.drawOval(margin, margin, width - margin * 2, height - margin * 2);
            }
        }

        // Draw piece
        if (piece != null) {
            pieceRenderer.drawPieceCentered(g2d, piece, 0, 0, Math.min(width, height));
        }

        g2d.dispose();
    }

    private Color brighten(Color c, float factor) {
        int r = Math.min(255, (int) (c.getRed() * factor));
        int g = Math.min(255, (int) (c.getGreen() * factor));
        int b = Math.min(255, (int) (c.getBlue() * factor));
        return new Color(r, g, b);
    }

    private Color darken(Color c, float factor) {
        int r = (int) (c.getRed() * factor);
        int g = (int) (c.getGreen() * factor);
        int b = (int) (c.getBlue() * factor);
        return new Color(r, g, b);
    }

    public void clearHighlights() {
        highlighted = false;
        legalMoveTarget = false;
        lastMoveSquare = false;
        inCheck = false;
        selected = false;
        suggestedMoveFrom = false;
        suggestedMoveTo = false;
        repaint();
    }

    public void clearSuggestion() {
        suggestedMoveFrom = false;
        suggestedMoveTo = false;
        repaint();
    }
}
