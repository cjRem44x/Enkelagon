package com.enkelagon;

import com.enkelagon.ui.MainFrame;
import com.enkelagon.ui.ThemeManager;

import javax.swing.*;

/**
 * Main entry point for Enkelagon Chess.
 */
public class App {

    public static void main(String[] args) {
        // Set system properties for better rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Initialize theme before creating any UI
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialize look and feel
                ThemeManager.getInstance().initializeLookAndFeel();

                // Create and show main frame
                MainFrame frame = new MainFrame();
                frame.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Failed to start application: " + e.getMessage(),
                        "Startup Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
