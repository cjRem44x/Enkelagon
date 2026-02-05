package com.enkelagon.ui;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * Dialog for starting a new game with color selection.
 */
public class NewGameDialog extends JDialog {

    public enum ColorChoice {
        WHITE, BLACK, RANDOM
    }

    private ColorChoice selectedColor = ColorChoice.WHITE;
    private boolean confirmed = false;
    private final ThemeManager theme;

    public NewGameDialog(Frame parent) {
        super(parent, "New Game", true);
        this.theme = ThemeManager.getInstance();

        setSize(400, 300);
        setLocationRelativeTo(parent);
        setResizable(false);
        setUndecorated(true);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Dark background with red border glow
                g2d.setColor(new Color(10, 10, 10));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Red glow border
                for (int i = 0; i < 5; i++) {
                    g2d.setColor(new Color(150, 0, 0, 60 - i * 10));
                    g2d.setStroke(new BasicStroke(2));
                    g2d.drawRoundRect(i, i, getWidth() - i * 2 - 1, getHeight() - i * 2 - 1, 20, 20);
                }

                g2d.dispose();
            }
        };
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // Title
        JLabel titleLabel = new JLabel("Choose Your Color", SwingConstants.CENTER);
        titleLabel.setFont(theme.getUIFont().deriveFont(Font.BOLD, 22f));
        titleLabel.setForeground(new Color(220, 50, 50));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Color buttons panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        buttonPanel.setOpaque(false);

        JToggleButton whiteBtn = createColorButton("WHITE", "Play as White", Color.WHITE, new Color(40, 40, 40));
        JToggleButton blackBtn = createColorButton("BLACK", "Play as Black", new Color(30, 30, 30), Color.WHITE);
        JToggleButton randomBtn = createColorButton("RANDOM", "Random Side", new Color(150, 0, 0), Color.WHITE);

        ButtonGroup group = new ButtonGroup();
        group.add(whiteBtn);
        group.add(blackBtn);
        group.add(randomBtn);

        whiteBtn.setSelected(true);

        whiteBtn.addActionListener(e -> selectedColor = ColorChoice.WHITE);
        blackBtn.addActionListener(e -> selectedColor = ColorChoice.BLACK);
        randomBtn.addActionListener(e -> selectedColor = ColorChoice.RANDOM);

        buttonPanel.add(whiteBtn);
        buttonPanel.add(randomBtn);
        buttonPanel.add(blackBtn);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // Start button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setOpaque(false);

        JButton startBtn = createActionButton("Start Game");
        startBtn.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        JButton cancelBtn = createActionButton("Cancel");
        cancelBtn.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        bottomPanel.add(startBtn);
        bottomPanel.add(cancelBtn);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JToggleButton createColorButton(String text, String tooltip, Color bgColor, Color fgColor) {
        JToggleButton btn = new JToggleButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Background
                if (isSelected()) {
                    // Selected state - red glow
                    g2d.setColor(new Color(100, 0, 0));
                    g2d.fillRoundRect(0, 0, w, h, 15, 15);
                    g2d.setColor(new Color(180, 0, 0));
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawRoundRect(2, 2, w - 4, h - 4, 12, 12);
                } else {
                    g2d.setColor(new Color(30, 30, 30));
                    g2d.fillRoundRect(0, 0, w, h, 15, 15);
                }

                // Inner color square
                int margin = 15;
                g2d.setColor(bgColor);
                g2d.fillRoundRect(margin, margin, w - margin * 2, h - margin * 2 - 25, 10, 10);

                // Border for inner square
                g2d.setColor(new Color(100, 100, 100));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(margin, margin, w - margin * 2, h - margin * 2 - 25, 10, 10);

                // Text
                g2d.setColor(isSelected() ? Color.WHITE : new Color(180, 180, 180));
                g2d.setFont(theme.getUIFont().deriveFont(Font.BOLD, 12f));
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (w - fm.stringWidth(text)) / 2;
                int textY = h - 12;
                g2d.drawString(text, textX, textY);

                g2d.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(100, 120));
        btn.setToolTipText(tooltip);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    private JButton createActionButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Background
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(150, 0, 0));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(100, 0, 0));
                } else {
                    g2d.setColor(new Color(50, 50, 50));
                }
                g2d.fillRoundRect(0, 0, w, h, 10, 10);

                // Border
                g2d.setColor(new Color(150, 0, 0));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, w - 1, h - 1, 10, 10);

                // Text
                g2d.setColor(Color.WHITE);
                g2d.setFont(theme.getUIFont().deriveFont(Font.BOLD, 14f));
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (w - fm.stringWidth(text)) / 2;
                int textY = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(text, textX, textY);

                g2d.dispose();
            }
        };

        btn.setPreferredSize(new Dimension(120, 40));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return btn;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public ColorChoice getSelectedColor() {
        return selectedColor;
    }

    /**
     * Resolves RANDOM to either WHITE or BLACK.
     */
    public boolean playAsWhite() {
        if (selectedColor == ColorChoice.RANDOM) {
            return new Random().nextBoolean();
        }
        return selectedColor == ColorChoice.WHITE;
    }

    /**
     * Shows the dialog and returns whether the user confirmed.
     */
    public static Result showDialog(Frame parent) {
        NewGameDialog dialog = new NewGameDialog(parent);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            return new Result(true, dialog.playAsWhite());
        }
        return new Result(false, true);
    }

    public static class Result {
        public final boolean confirmed;
        public final boolean playAsWhite;

        public Result(boolean confirmed, boolean playAsWhite) {
            this.confirmed = confirmed;
            this.playAsWhite = playAsWhite;
        }
    }
}
