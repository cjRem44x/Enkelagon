package com.enkelagon.ui;

import com.enkelagon.config.ConfigManager;
import com.enkelagon.engine.EngineConfig;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * Dialog for configuring AI settings and application preferences.
 */
public class SettingsDialog extends JDialog {

    private final ConfigManager config;
    private final ThemeManager theme;

    private JComboBox<String> presetCombo;
    private JSlider skillSlider;
    private JSlider depthSlider;
    private JSpinner threadsSpinner;
    private JSpinner hashSpinner;
    private JSpinner moveTimeSpinner;

    private JCheckBox showCoordsCheck;
    private JCheckBox animateMovesCheck;
    private JCheckBox highlightLegalCheck;
    private JCheckBox highlightLastMoveCheck;
    private JCheckBox soundEnabledCheck;

    private boolean applied = false;

    public SettingsDialog(Frame parent) {
        super(parent, "Settings", true);
        this.config = ConfigManager.getInstance();
        this.theme = ThemeManager.getInstance();

        setSize(450, 500);
        setLocationRelativeTo(parent);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(theme.getBackgroundColor());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Tabbed pane for settings categories
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(theme.getBackgroundColor());
        tabbedPane.setForeground(theme.getForegroundColor());

        tabbedPane.addTab("Engine", createEnginePanel());
        tabbedPane.addTab("Board", createBoardPanel());
        tabbedPane.addTab("Theme", createThemePanel());

        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(theme.getBackgroundColor());

        JButton applyBtn = theme.createButton("Apply");
        JButton cancelBtn = theme.createButton("Cancel");
        JButton okBtn = theme.createButton("OK");

        applyBtn.addActionListener(e -> applySettings());
        cancelBtn.addActionListener(e -> dispose());
        okBtn.addActionListener(e -> {
            applySettings();
            dispose();
        });

        buttonPanel.add(applyBtn);
        buttonPanel.add(cancelBtn);
        buttonPanel.add(okBtn);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        loadCurrentSettings();
    }

    private JPanel createEnginePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(theme.getBackgroundColor());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Preset selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(theme.createLabel("Difficulty Preset:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        presetCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard", "Custom"});
        presetCombo.addActionListener(e -> onPresetChanged());
        panel.add(presetCombo, gbc);

        // Skill level
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(theme.createLabel("Skill Level (0-20):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        skillSlider = new JSlider(0, 20, 10);
        skillSlider.setMajorTickSpacing(5);
        skillSlider.setMinorTickSpacing(1);
        skillSlider.setPaintTicks(true);
        skillSlider.setPaintLabels(true);
        skillSlider.setBackground(theme.getBackgroundColor());
        skillSlider.setForeground(theme.getForegroundColor());
        skillSlider.addChangeListener(e -> presetCombo.setSelectedItem("Custom"));
        panel.add(skillSlider, gbc);

        // Depth
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        panel.add(theme.createLabel("Search Depth:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        depthSlider = new JSlider(1, 30, 10);
        depthSlider.setMajorTickSpacing(5);
        depthSlider.setPaintTicks(true);
        depthSlider.setPaintLabels(true);
        depthSlider.setBackground(theme.getBackgroundColor());
        depthSlider.setForeground(theme.getForegroundColor());
        depthSlider.addChangeListener(e -> presetCombo.setSelectedItem("Custom"));
        panel.add(depthSlider, gbc);

        // Threads
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        panel.add(theme.createLabel("CPU Threads:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        int maxThreads = Runtime.getRuntime().availableProcessors();
        threadsSpinner = new JSpinner(new SpinnerNumberModel(4, 1, maxThreads, 1));
        panel.add(threadsSpinner, gbc);

        // Hash
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        panel.add(theme.createLabel("Hash (MB):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        hashSpinner = new JSpinner(new SpinnerNumberModel(256, 16, 4096, 64));
        panel.add(hashSpinner, gbc);

        // Move time
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0;
        panel.add(theme.createLabel("Move Time (ms):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        moveTimeSpinner = new JSpinner(new SpinnerNumberModel(1000, 100, 30000, 100));
        panel.add(moveTimeSpinner, gbc);

        // Spacer
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weighty = 1;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    private JPanel createBoardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(theme.getBackgroundColor());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        showCoordsCheck = new JCheckBox("Show coordinates");
        showCoordsCheck.setBackground(theme.getBackgroundColor());
        showCoordsCheck.setForeground(theme.getForegroundColor());
        panel.add(showCoordsCheck, gbc);

        gbc.gridy = 1;
        animateMovesCheck = new JCheckBox("Animate moves");
        animateMovesCheck.setBackground(theme.getBackgroundColor());
        animateMovesCheck.setForeground(theme.getForegroundColor());
        panel.add(animateMovesCheck, gbc);

        gbc.gridy = 2;
        highlightLegalCheck = new JCheckBox("Highlight legal moves");
        highlightLegalCheck.setBackground(theme.getBackgroundColor());
        highlightLegalCheck.setForeground(theme.getForegroundColor());
        panel.add(highlightLegalCheck, gbc);

        gbc.gridy = 3;
        highlightLastMoveCheck = new JCheckBox("Highlight last move");
        highlightLastMoveCheck.setBackground(theme.getBackgroundColor());
        highlightLastMoveCheck.setForeground(theme.getForegroundColor());
        panel.add(highlightLastMoveCheck, gbc);

        gbc.gridy = 4;
        soundEnabledCheck = new JCheckBox("Enable sounds");
        soundEnabledCheck.setBackground(theme.getBackgroundColor());
        soundEnabledCheck.setForeground(theme.getForegroundColor());
        panel.add(soundEnabledCheck, gbc);

        // Spacer
        gbc.gridy = 5;
        gbc.weighty = 1;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    private JPanel createThemePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(theme.getBackgroundColor());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(theme.createLabel("Theme: Dragon (Black/Red/White/Grey)"), gbc);

        gbc.gridy = 1;
        JLabel previewLabel = theme.createLabel("Color customization coming soon...");
        previewLabel.setForeground(theme.getSecondaryColor());
        panel.add(previewLabel, gbc);

        // Board preview could go here
        gbc.gridy = 2;
        gbc.weighty = 1;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    private void onPresetChanged() {
        String selected = (String) presetCombo.getSelectedItem();
        if (selected == null || selected.equals("Custom")) return;

        EngineConfig.Preset preset = switch (selected) {
            case "Easy" -> EngineConfig.Preset.EASY;
            case "Medium" -> EngineConfig.Preset.MEDIUM;
            case "Hard" -> EngineConfig.Preset.HARD;
            default -> null;
        };

        if (preset != null) {
            // Temporarily remove listeners to avoid triggering Custom
            ChangeListener[] skillListeners = skillSlider.getChangeListeners();
            ChangeListener[] depthListeners = depthSlider.getChangeListeners();

            for (ChangeListener l : skillListeners) skillSlider.removeChangeListener(l);
            for (ChangeListener l : depthListeners) depthSlider.removeChangeListener(l);

            skillSlider.setValue(preset.getSkillLevel());
            depthSlider.setValue(preset.getDepth());
            moveTimeSpinner.setValue(preset.getMoveTimeMs());

            for (ChangeListener l : skillListeners) skillSlider.addChangeListener(l);
            for (ChangeListener l : depthListeners) depthSlider.addChangeListener(l);
        }
    }

    private void loadCurrentSettings() {
        EngineConfig engineConfig = config.getEngineConfig();

        // Engine settings
        presetCombo.setSelectedItem(engineConfig.getPreset().getDisplayName());
        skillSlider.setValue(engineConfig.getSkillLevel());
        depthSlider.setValue(engineConfig.getDepthLimit());
        threadsSpinner.setValue(engineConfig.getThreads());
        hashSpinner.setValue(engineConfig.getHashMB());
        moveTimeSpinner.setValue(engineConfig.getMoveTimeMs());

        // Board settings
        showCoordsCheck.setSelected(config.isShowCoordinates());
        animateMovesCheck.setSelected(config.isAnimateMoves());
        highlightLegalCheck.setSelected(config.isHighlightLegalMoves());
        highlightLastMoveCheck.setSelected(config.isHighlightLastMove());
        soundEnabledCheck.setSelected(config.isSoundEnabled());
    }

    private void applySettings() {
        // Engine settings
        EngineConfig engineConfig = new EngineConfig();
        engineConfig.setSkillLevel(skillSlider.getValue());
        engineConfig.setDepthLimit(depthSlider.getValue());
        engineConfig.setThreads((Integer) threadsSpinner.getValue());
        engineConfig.setHashMB((Integer) hashSpinner.getValue());
        engineConfig.setMoveTimeMs((Integer) moveTimeSpinner.getValue());

        String preset = (String) presetCombo.getSelectedItem();
        if (preset != null && !preset.equals("Custom")) {
            try {
                engineConfig.applyPreset(EngineConfig.Preset.valueOf(preset.toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Keep as custom
            }
        }

        config.setEngineConfig(engineConfig);

        // Board settings
        config.setBoardOption("showCoordinates", showCoordsCheck.isSelected());
        config.setBoardOption("animateMoves", animateMovesCheck.isSelected());
        config.setBoardOption("highlightLegalMoves", highlightLegalCheck.isSelected());
        config.setBoardOption("highlightLastMove", highlightLastMoveCheck.isSelected());
        config.setBoardOption("soundEnabled", soundEnabledCheck.isSelected());

        config.saveConfig();
        applied = true;
    }

    public boolean wasApplied() {
        return applied;
    }

    public EngineConfig getEngineConfig() {
        EngineConfig engineConfig = new EngineConfig();
        engineConfig.setSkillLevel(skillSlider.getValue());
        engineConfig.setDepthLimit(depthSlider.getValue());
        engineConfig.setThreads((Integer) threadsSpinner.getValue());
        engineConfig.setHashMB((Integer) hashSpinner.getValue());
        engineConfig.setMoveTimeMs((Integer) moveTimeSpinner.getValue());
        return engineConfig;
    }
}
