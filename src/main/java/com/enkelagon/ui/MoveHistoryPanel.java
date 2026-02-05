package com.enkelagon.ui;

import com.enkelagon.model.Move;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel displaying the move history in algebraic notation.
 */
public class MoveHistoryPanel extends JPanel {

    private final ThemeManager theme;
    private final DefaultTableModel tableModel;
    private final JTable moveTable;
    private final JScrollPane scrollPane;

    public MoveHistoryPanel() {
        this.theme = ThemeManager.getInstance();

        setLayout(new BorderLayout());
        setBackground(theme.getBackgroundColor());
        setBorder(theme.createBorder("Move History"));

        // Create table model
        tableModel = new DefaultTableModel(new String[]{"#", "White", "Black"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Create table
        moveTable = new JTable(tableModel);
        moveTable.setFont(theme.getMoveHistoryFont());
        moveTable.setBackground(new Color(30, 30, 30));
        moveTable.setForeground(theme.getForegroundColor());
        moveTable.setGridColor(theme.getSecondaryColor());
        moveTable.setSelectionBackground(theme.getAccentColor());
        moveTable.setSelectionForeground(theme.getForegroundColor());
        moveTable.setRowHeight(24);
        moveTable.getTableHeader().setBackground(theme.getBackgroundColor());
        moveTable.getTableHeader().setForeground(theme.getForegroundColor());
        moveTable.getTableHeader().setFont(theme.getMoveHistoryFont().deriveFont(Font.BOLD));

        // Column widths
        moveTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        moveTable.getColumnModel().getColumn(0).setMaxWidth(50);
        moveTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        moveTable.getColumnModel().getColumn(2).setPreferredWidth(80);

        // Center alignment for move number column
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        moveTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // Custom renderer for move cells
        DefaultTableCellRenderer moveRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);
                if (!isSelected) {
                    setBackground(new Color(30, 30, 30));
                    setForeground(theme.getForegroundColor());
                }
                return this;
            }
        };
        moveTable.getColumnModel().getColumn(1).setCellRenderer(moveRenderer);
        moveTable.getColumnModel().getColumn(2).setCellRenderer(moveRenderer);

        // Scroll pane
        scrollPane = new JScrollPane(moveTable);
        scrollPane.setBackground(theme.getBackgroundColor());
        scrollPane.getViewport().setBackground(new Color(30, 30, 30));
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        add(scrollPane, BorderLayout.CENTER);

        setPreferredSize(new Dimension(220, 400));
    }

    /**
     * Adds a move to the history.
     */
    public void addMove(Move move, int moveNumber, boolean isWhite) {
        String algebraic = move.toAlgebraic();

        if (isWhite) {
            tableModel.addRow(new Object[]{moveNumber + ".", algebraic, ""});
        } else {
            int lastRow = tableModel.getRowCount() - 1;
            if (lastRow >= 0) {
                tableModel.setValueAt(algebraic, lastRow, 2);
            }
        }

        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            int lastRow = moveTable.getRowCount() - 1;
            if (lastRow >= 0) {
                moveTable.scrollRectToVisible(moveTable.getCellRect(lastRow, 0, true));
            }
        });
    }

    /**
     * Updates the history from a list of moves.
     */
    public void updateFromMoves(List<Move> moves) {
        tableModel.setRowCount(0);

        for (int i = 0; i < moves.size(); i++) {
            Move move = moves.get(i);
            int moveNumber = (i / 2) + 1;
            boolean isWhite = (i % 2 == 0);

            if (isWhite) {
                tableModel.addRow(new Object[]{moveNumber + ".", move.toAlgebraic(), ""});
            } else {
                int lastRow = tableModel.getRowCount() - 1;
                if (lastRow >= 0) {
                    tableModel.setValueAt(move.toAlgebraic(), lastRow, 2);
                }
            }
        }

        // Scroll to bottom
        SwingUtilities.invokeLater(() -> {
            int lastRow = moveTable.getRowCount() - 1;
            if (lastRow >= 0) {
                moveTable.scrollRectToVisible(moveTable.getCellRect(lastRow, 0, true));
            }
        });
    }

    /**
     * Clears all moves from the history.
     */
    public void clear() {
        tableModel.setRowCount(0);
    }

    /**
     * Removes the last move from the history.
     */
    public void undoLastMove() {
        int rowCount = tableModel.getRowCount();
        if (rowCount == 0) return;

        int lastRow = rowCount - 1;
        String blackMove = (String) tableModel.getValueAt(lastRow, 2);

        if (blackMove != null && !blackMove.isEmpty()) {
            // Remove black's move
            tableModel.setValueAt("", lastRow, 2);
        } else {
            // Remove entire row (white's move)
            tableModel.removeRow(lastRow);
        }
    }

    /**
     * Gets the total number of half-moves (plies).
     */
    public int getPlyCount() {
        int count = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String white = (String) tableModel.getValueAt(i, 1);
            String black = (String) tableModel.getValueAt(i, 2);
            if (white != null && !white.isEmpty()) count++;
            if (black != null && !black.isEmpty()) count++;
        }
        return count;
    }

    /**
     * Gets the PGN movetext representation.
     */
    public String toPgnMovetext() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String moveNum = (String) tableModel.getValueAt(i, 0);
            String white = (String) tableModel.getValueAt(i, 1);
            String black = (String) tableModel.getValueAt(i, 2);

            sb.append(moveNum).append(" ").append(white);
            if (black != null && !black.isEmpty()) {
                sb.append(" ").append(black);
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }
}
