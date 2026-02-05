package com.enkelagon.ui;

import com.enkelagon.engine.StockfishEngine;

import javax.swing.*;
import java.awt.*;

/**
 * Panel displaying engine analysis and evaluation.
 */
public class AnalysisPanel extends JPanel {

    private final ThemeManager theme;

    private final JLabel evalLabel;
    private final JProgressBar evalBar;
    private final JLabel depthLabel;
    private final JLabel bestMoveLabel;
    private final JTextArea pvTextArea;
    private final JLabel npsLabel;

    private int currentEval = 0;
    private boolean isMate = false;
    private int mateIn = 0;

    public AnalysisPanel() {
        this.theme = ThemeManager.getInstance();

        setLayout(new BorderLayout(5, 5));
        setBackground(theme.getBackgroundColor());
        setBorder(theme.createBorder("Engine Analysis"));

        // Top section - evaluation
        JPanel evalPanel = createEvalPanel();
        add(evalPanel, BorderLayout.NORTH);

        // Center section - principal variation
        pvTextArea = new JTextArea(4, 20);
        pvTextArea.setFont(theme.getAnalysisFont());
        pvTextArea.setBackground(new Color(30, 30, 30));
        pvTextArea.setForeground(theme.getForegroundColor());
        pvTextArea.setEditable(false);
        pvTextArea.setLineWrap(true);
        pvTextArea.setWrapStyleWord(true);

        JScrollPane pvScroll = new JScrollPane(pvTextArea);
        pvScroll.setBorder(BorderFactory.createEmptyBorder());
        pvScroll.setBackground(theme.getBackgroundColor());
        add(pvScroll, BorderLayout.CENTER);

        // Bottom section - stats
        JPanel statsPanel = createStatsPanel();
        add(statsPanel, BorderLayout.SOUTH);

        // Labels initialized in panels
        evalLabel = (JLabel) ((JPanel) evalPanel.getComponent(0)).getComponent(0);
        evalBar = (JProgressBar) evalPanel.getComponent(1);
        depthLabel = (JLabel) statsPanel.getComponent(0);
        npsLabel = (JLabel) statsPanel.getComponent(1);
        bestMoveLabel = (JLabel) statsPanel.getComponent(2);

        setPreferredSize(new Dimension(220, 200));
    }

    private JPanel createEvalPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(theme.getBackgroundColor());

        // Eval display
        JPanel evalDisplay = new JPanel(new FlowLayout(FlowLayout.CENTER));
        evalDisplay.setBackground(theme.getBackgroundColor());

        JLabel label = new JLabel("0.00");
        label.setFont(theme.getAnalysisFont().deriveFont(Font.BOLD, 24f));
        label.setForeground(theme.getForegroundColor());
        evalDisplay.add(label);

        panel.add(evalDisplay, BorderLayout.NORTH);

        // Eval bar
        JProgressBar bar = new JProgressBar(-1000, 1000);
        bar.setValue(0);
        bar.setBackground(new Color(40, 40, 40));
        bar.setForeground(theme.getForegroundColor());
        bar.setBorderPainted(false);
        bar.setPreferredSize(new Dimension(200, 20));

        panel.add(bar, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 2, 2));
        panel.setBackground(theme.getBackgroundColor());

        JLabel depth = new JLabel("Depth: -");
        depth.setFont(theme.getAnalysisFont());
        depth.setForeground(theme.getSecondaryColor());
        panel.add(depth);

        JLabel nps = new JLabel("NPS: -");
        nps.setFont(theme.getAnalysisFont());
        nps.setForeground(theme.getSecondaryColor());
        panel.add(nps);

        JLabel bestMove = new JLabel("Best: -");
        bestMove.setFont(theme.getAnalysisFont());
        bestMove.setForeground(theme.getAccentColor());
        panel.add(bestMove);

        return panel;
    }

    /**
     * Updates the analysis display with new info.
     */
    public void updateAnalysis(StockfishEngine.AnalysisInfo info) {
        if (info == null) return;

        SwingUtilities.invokeLater(() -> {
            // Update evaluation
            if (info.isMate) {
                isMate = true;
                mateIn = info.mateIn;
                String mateStr = info.mateIn > 0 ? "M" + info.mateIn : "-M" + Math.abs(info.mateIn);
                evalLabel.setText(mateStr);
                evalBar.setValue(info.mateIn > 0 ? 1000 : -1000);
            } else {
                isMate = false;
                currentEval = info.score;
                double evalValue = info.score / 100.0;
                String evalStr = String.format("%+.2f", evalValue);
                evalLabel.setText(evalStr);

                // Clamp for display
                int barValue = Math.max(-1000, Math.min(1000, info.score));
                evalBar.setValue(barValue);
            }

            // Update color based on evaluation
            if (currentEval > 100 || (isMate && mateIn > 0)) {
                evalLabel.setForeground(Color.WHITE);
            } else if (currentEval < -100 || (isMate && mateIn < 0)) {
                evalLabel.setForeground(theme.getSecondaryColor());
            } else {
                evalLabel.setForeground(theme.getForegroundColor());
            }

            // Update depth
            depthLabel.setText("Depth: " + info.depth + "/" + info.selectiveDepth);

            // Update NPS
            if (info.nps > 0) {
                String npsStr = formatNps(info.nps);
                npsLabel.setText("NPS: " + npsStr);
            }

            // Update best move
            if (info.bestMove != null) {
                bestMoveLabel.setText("Best: " + info.bestMove);
            }

            // Update principal variation
            if (info.principalVariation != null && info.principalVariation.length > 0) {
                StringBuilder pv = new StringBuilder();
                for (int i = 0; i < Math.min(info.principalVariation.length, 10); i++) {
                    pv.append(info.principalVariation[i]).append(" ");
                }
                pvTextArea.setText(pv.toString().trim());
            }
        });
    }

    private String formatNps(int nps) {
        if (nps >= 1000000) {
            return String.format("%.1fM", nps / 1000000.0);
        } else if (nps >= 1000) {
            return String.format("%.1fK", nps / 1000.0);
        }
        return String.valueOf(nps);
    }

    /**
     * Clears the analysis display.
     */
    public void clear() {
        SwingUtilities.invokeLater(() -> {
            evalLabel.setText("0.00");
            evalLabel.setForeground(theme.getForegroundColor());
            evalBar.setValue(0);
            depthLabel.setText("Depth: -");
            npsLabel.setText("NPS: -");
            bestMoveLabel.setText("Best: -");
            pvTextArea.setText("");
            currentEval = 0;
            isMate = false;
        });
    }

    /**
     * Sets the best move display.
     */
    public void setBestMove(String move) {
        SwingUtilities.invokeLater(() -> {
            bestMoveLabel.setText("Best: " + (move != null ? move : "-"));
        });
    }

    /**
     * Sets analyzing state.
     */
    public void setAnalyzing(boolean analyzing) {
        SwingUtilities.invokeLater(() -> {
            if (analyzing) {
                depthLabel.setText("Depth: analyzing...");
            }
        });
    }

    /**
     * Gets the current evaluation in centipawns.
     */
    public int getCurrentEval() {
        return currentEval;
    }

    /**
     * Checks if current position is a forced mate.
     */
    public boolean isMateEval() {
        return isMate;
    }
}
