package com.enkelagon.ui;

import com.enkelagon.config.ConfigManager;
import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Manages the application theme and styling.
 */
public class ThemeManager {

    private static ThemeManager instance;
    private final ConfigManager config;

    private ThemeManager() {
        this.config = ConfigManager.getInstance();
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    /**
     * Initializes the look and feel with Dragon theme (black, red, white, grey).
     */
    public void initializeLookAndFeel() {
        try {
            // Use FlatLaf dark theme as base
            FlatDarkLaf.setup();

            // Customize UI defaults for dragon theme
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Component.innerFocusWidth", 0);
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 5);

            // Dragon theme colors - true black with red/white/grey accents
            Color bg = config.getBackgroundColor();           // #000000 - true black
            Color fg = config.getForegroundColor();           // #FFFFFF - white
            Color accent = config.getAccentColor();           // #DD0000 - dragon red
            Color secondary = config.getSecondaryColor();     // #707070 - grey
            Color panelBg = config.getPanelBackgroundColor(); // #0A0A0A - near black
            Color buttonBg = config.getButtonBackgroundColor(); // #2A2A2A - dark grey
            Color buttonHover = config.getButtonHoverColor(); // #8B0000 - dark red

            // Core backgrounds - TRUE BLACK
            UIManager.put("Panel.background", bg);
            UIManager.put("RootPane.background", bg);
            UIManager.put("ScrollPane.background", bg);
            UIManager.put("Viewport.background", bg);

            // Labels - white text
            UIManager.put("Label.foreground", fg);

            // Buttons - dark grey with red hover
            UIManager.put("Button.background", buttonBg);
            UIManager.put("Button.foreground", fg);
            UIManager.put("Button.hoverBackground", buttonHover);
            UIManager.put("Button.pressedBackground", accent);
            UIManager.put("Button.focusedBackground", buttonBg);
            UIManager.put("ToggleButton.background", buttonBg);
            UIManager.put("ToggleButton.foreground", fg);
            UIManager.put("ToggleButton.selectedBackground", accent);

            // Text fields - dark panels
            UIManager.put("TextField.background", panelBg);
            UIManager.put("TextField.foreground", fg);
            UIManager.put("TextField.caretForeground", accent);
            UIManager.put("TextArea.background", panelBg);
            UIManager.put("TextArea.foreground", fg);
            UIManager.put("TextPane.background", panelBg);
            UIManager.put("TextPane.foreground", fg);

            // Lists and tables
            UIManager.put("List.background", bg);
            UIManager.put("List.foreground", fg);
            UIManager.put("List.selectionBackground", accent);
            UIManager.put("List.selectionForeground", fg);
            UIManager.put("Table.background", bg);
            UIManager.put("Table.foreground", fg);
            UIManager.put("Table.selectionBackground", accent);
            UIManager.put("Table.gridColor", new Color(30, 30, 30));
            UIManager.put("TableHeader.background", panelBg);
            UIManager.put("TableHeader.foreground", fg);

            // Menus - black with red selection
            UIManager.put("MenuBar.background", bg);
            UIManager.put("MenuBar.foreground", fg);
            UIManager.put("Menu.background", bg);
            UIManager.put("Menu.foreground", fg);
            UIManager.put("Menu.selectionBackground", accent);
            UIManager.put("MenuItem.background", bg);
            UIManager.put("MenuItem.foreground", fg);
            UIManager.put("MenuItem.selectionBackground", accent);
            UIManager.put("MenuItem.selectionForeground", fg);
            UIManager.put("PopupMenu.background", panelBg);
            UIManager.put("PopupMenu.foreground", fg);

            // Combo boxes and spinners
            UIManager.put("ComboBox.background", panelBg);
            UIManager.put("ComboBox.foreground", fg);
            UIManager.put("ComboBox.selectionBackground", accent);
            UIManager.put("ComboBox.buttonBackground", buttonBg);
            UIManager.put("Spinner.background", panelBg);
            UIManager.put("Spinner.foreground", fg);

            // Sliders - grey track, red thumb
            UIManager.put("Slider.background", bg);
            UIManager.put("Slider.foreground", fg);
            UIManager.put("Slider.trackColor", secondary);
            UIManager.put("Slider.thumbColor", accent);

            // Tabs
            UIManager.put("TabbedPane.background", bg);
            UIManager.put("TabbedPane.foreground", fg);
            UIManager.put("TabbedPane.selectedBackground", panelBg);
            UIManager.put("TabbedPane.hoverColor", buttonHover);
            UIManager.put("TabbedPane.focusColor", accent);

            // Scroll bars - subtle grey
            UIManager.put("ScrollBar.background", bg);
            UIManager.put("ScrollBar.thumbColor", secondary);
            UIManager.put("ScrollBar.thumbHighlightColor", buttonHover);

            // Progress bar
            UIManager.put("ProgressBar.background", panelBg);
            UIManager.put("ProgressBar.foreground", accent);

            // Tooltips
            UIManager.put("ToolTip.background", panelBg);
            UIManager.put("ToolTip.foreground", fg);

            // Borders
            UIManager.put("TitledBorder.titleColor", fg);
            UIManager.put("Component.borderColor", secondary);
            UIManager.put("Component.focusColor", accent);

            // Checkboxes and radio buttons
            UIManager.put("CheckBox.background", bg);
            UIManager.put("CheckBox.foreground", fg);
            UIManager.put("RadioButton.background", bg);
            UIManager.put("RadioButton.foreground", fg);

            // Separator
            UIManager.put("Separator.foreground", secondary);

        } catch (Exception e) {
            e.printStackTrace();
            // Fall back to system look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                // Ignore
            }
        }
    }

    public Color getLightSquareColor() {
        return config.getLightSquareColor();
    }

    public Color getDarkSquareColor() {
        return config.getDarkSquareColor();
    }

    public Color getHighlightColor() {
        return config.getHighlightColor();
    }

    public Color getLastMoveColor() {
        return config.getLastMoveColor();
    }

    public Color getLegalMoveColor() {
        return config.getLegalMoveColor();
    }

    public Color getCheckColor() {
        return config.getCheckColor();
    }

    public Color getBackgroundColor() {
        return config.getBackgroundColor();
    }

    public Color getForegroundColor() {
        return config.getForegroundColor();
    }

    public Color getAccentColor() {
        return config.getAccentColor();
    }

    public Color getSecondaryColor() {
        return config.getSecondaryColor();
    }

    public Color getPanelBackgroundColor() {
        return config.getPanelBackgroundColor();
    }

    public Color getButtonBackgroundColor() {
        return config.getButtonBackgroundColor();
    }

    public Color getButtonHoverColor() {
        return config.getButtonHoverColor();
    }

    public Font getMoveHistoryFont() {
        return config.getMoveHistoryFont();
    }

    public Font getAnalysisFont() {
        return config.getAnalysisFont();
    }

    public Font getCoordinatesFont() {
        return config.getCoordinatesFont();
    }

    public Font getUIFont() {
        return config.getUIFont();
    }

    /**
     * Creates a styled button with dragon theme.
     */
    public JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(getUIFont());
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBackground(getButtonBackgroundColor());
        button.setForeground(getForegroundColor());
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getSecondaryColor(), 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        // Add hover effect
        Color normalBg = getButtonBackgroundColor();
        Color hoverBg = getButtonHoverColor();
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverBg);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(normalBg);
            }
        });

        return button;
    }

    /**
     * Creates a styled label.
     */
    public JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(getUIFont());
        label.setForeground(getForegroundColor());
        return label;
    }

    /**
     * Creates a header label.
     */
    public JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(getUIFont().deriveFont(Font.BOLD, 16f));
        label.setForeground(getAccentColor());
        return label;
    }

    /**
     * Creates a styled panel with background.
     */
    public JPanel createPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(getBackgroundColor());
        return panel;
    }

    /**
     * Creates a styled border with dragon theme.
     */
    public Border createBorder(String title) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(getAccentColor(), 1),
                        title,
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        getUIFont().deriveFont(Font.BOLD),
                        getAccentColor()
                ),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        );
    }

    /**
     * Applies theme to a component and its children.
     */
    public void applyTheme(Component component) {
        if (component instanceof JPanel) {
            component.setBackground(getBackgroundColor());
        }
        if (component instanceof JLabel) {
            component.setForeground(getForegroundColor());
        }

        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyTheme(child);
            }
        }
    }
}
