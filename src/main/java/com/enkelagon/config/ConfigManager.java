package com.enkelagon.config;

import com.enkelagon.engine.EngineConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.*;
import java.io.*;
import java.nio.file.*;

/**
 * Manages application configuration using JSON.
 */
public class ConfigManager {

    private static final String CONFIG_FILE = "config/settings.json";
    private static ConfigManager instance;

    private JsonObject config;
    private final Gson gson;
    private Path configPath;

    private ConfigManager() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        loadConfig();
    }

    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadConfig() {
        // Try to load from resources first
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                config = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();
                return;
            }
        } catch (Exception e) {
            // Fall through to default
        }

        // Try to load from file system
        configPath = Paths.get(System.getProperty("user.dir"), "src/main/resources", CONFIG_FILE);
        if (Files.exists(configPath)) {
            try {
                String content = Files.readString(configPath);
                config = JsonParser.parseString(content).getAsJsonObject();
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Use defaults
        config = createDefaultConfig();
    }

    private JsonObject createDefaultConfig() {
        String defaultJson = """
                {
                  "theme": {
                    "name": "Dragon",
                    "lightSquare": "#3D3D3D",
                    "darkSquare": "#1A1A1A",
                    "highlight": "#8B0000",
                    "lastMove": "#4A1010",
                    "legalMove": "#6B2D2D",
                    "check": "#FF2222",
                    "background": "#000000",
                    "foreground": "#FFFFFF",
                    "accent": "#DD0000",
                    "secondary": "#707070",
                    "panelBg": "#0A0A0A",
                    "buttonBg": "#2A2A2A",
                    "buttonHover": "#8B0000"
                  },
                  "fonts": {
                    "moveHistory": { "family": "Consolas", "size": 14 },
                    "analysis": { "family": "Segoe UI", "size": 12 },
                    "coordinates": { "family": "Arial", "size": 11, "bold": true },
                    "ui": { "family": "Segoe UI", "size": 13 }
                  },
                  "pieceSet": "classic",
                  "board": {
                    "showCoordinates": true,
                    "animateMoves": true,
                    "highlightLegalMoves": true,
                    "highlightLastMove": true,
                    "soundEnabled": true
                  },
                  "engine": {
                    "preset": "medium",
                    "threads": 4,
                    "hashMB": 256,
                    "skillLevel": 10,
                    "depthLimit": 10,
                    "moveTimeMs": 1000
                  },
                  "window": {
                    "width": 1200,
                    "height": 800,
                    "maximized": false
                  }
                }
                """;
        return JsonParser.parseString(defaultJson).getAsJsonObject();
    }

    public void saveConfig() {
        if (configPath == null) {
            configPath = Paths.get(System.getProperty("user.dir"), "src/main/resources", CONFIG_FILE);
        }

        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, gson.toJson(config));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Theme colors
    public Color getLightSquareColor() {
        return parseColor(getThemeValue("lightSquare", "#4A4A4A"));
    }

    public Color getDarkSquareColor() {
        return parseColor(getThemeValue("darkSquare", "#2D2D2D"));
    }

    public Color getHighlightColor() {
        return parseColor(getThemeValue("highlight", "#8B0000"));
    }

    public Color getLastMoveColor() {
        return parseColor(getThemeValue("lastMove", "#5C1A1A"));
    }

    public Color getLegalMoveColor() {
        return parseColor(getThemeValue("legalMove", "#6B2D2D"));
    }

    public Color getCheckColor() {
        return parseColor(getThemeValue("check", "#FF4444"));
    }

    public Color getBackgroundColor() {
        return parseColor(getThemeValue("background", "#1A1A1A"));
    }

    public Color getForegroundColor() {
        return parseColor(getThemeValue("foreground", "#FFFFFF"));
    }

    public Color getAccentColor() {
        return parseColor(getThemeValue("accent", "#CC0000"));
    }

    public Color getSecondaryColor() {
        return parseColor(getThemeValue("secondary", "#707070"));
    }

    public Color getPanelBackgroundColor() {
        return parseColor(getThemeValue("panelBg", "#0A0A0A"));
    }

    public Color getButtonBackgroundColor() {
        return parseColor(getThemeValue("buttonBg", "#2A2A2A"));
    }

    public Color getButtonHoverColor() {
        return parseColor(getThemeValue("buttonHover", "#8B0000"));
    }

    private String getThemeValue(String key, String defaultValue) {
        try {
            JsonObject theme = config.getAsJsonObject("theme");
            if (theme != null && theme.has(key)) {
                return theme.get(key).getAsString();
            }
        } catch (Exception e) {
            // Use default
        }
        return defaultValue;
    }

    public void setThemeColor(String key, Color color) {
        String hex = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue());
        JsonObject theme = config.getAsJsonObject("theme");
        if (theme == null) {
            theme = new JsonObject();
            config.add("theme", theme);
        }
        theme.addProperty(key, hex);
    }

    // Fonts
    public Font getMoveHistoryFont() {
        return getFont("moveHistory", new Font("Consolas", Font.PLAIN, 14));
    }

    public Font getAnalysisFont() {
        return getFont("analysis", new Font("Segoe UI", Font.PLAIN, 12));
    }

    public Font getCoordinatesFont() {
        return getFont("coordinates", new Font("Arial", Font.BOLD, 11));
    }

    public Font getUIFont() {
        return getFont("ui", new Font("Segoe UI", Font.PLAIN, 13));
    }

    private Font getFont(String key, Font defaultFont) {
        try {
            JsonObject fonts = config.getAsJsonObject("fonts");
            if (fonts != null && fonts.has(key)) {
                JsonObject fontObj = fonts.getAsJsonObject(key);
                String family = fontObj.has("family") ? fontObj.get("family").getAsString() : defaultFont.getFamily();
                int size = fontObj.has("size") ? fontObj.get("size").getAsInt() : defaultFont.getSize();
                boolean bold = fontObj.has("bold") && fontObj.get("bold").getAsBoolean();
                return new Font(family, bold ? Font.BOLD : Font.PLAIN, size);
            }
        } catch (Exception e) {
            // Use default
        }
        return defaultFont;
    }

    // Piece set
    public String getPieceSet() {
        try {
            return config.get("pieceSet").getAsString();
        } catch (Exception e) {
            return "classic";
        }
    }

    public void setPieceSet(String pieceSet) {
        config.addProperty("pieceSet", pieceSet);
    }

    // Board options
    public boolean isShowCoordinates() {
        return getBoardOption("showCoordinates", true);
    }

    public boolean isAnimateMoves() {
        return getBoardOption("animateMoves", true);
    }

    public boolean isHighlightLegalMoves() {
        return getBoardOption("highlightLegalMoves", true);
    }

    public boolean isHighlightLastMove() {
        return getBoardOption("highlightLastMove", true);
    }

    public boolean isSoundEnabled() {
        return getBoardOption("soundEnabled", true);
    }

    private boolean getBoardOption(String key, boolean defaultValue) {
        try {
            JsonObject board = config.getAsJsonObject("board");
            if (board != null && board.has(key)) {
                return board.get(key).getAsBoolean();
            }
        } catch (Exception e) {
            // Use default
        }
        return defaultValue;
    }

    public void setBoardOption(String key, boolean value) {
        JsonObject board = config.getAsJsonObject("board");
        if (board == null) {
            board = new JsonObject();
            config.add("board", board);
        }
        board.addProperty(key, value);
    }

    // Engine config
    public EngineConfig getEngineConfig() {
        EngineConfig engineConfig = new EngineConfig();

        try {
            JsonObject engine = config.getAsJsonObject("engine");
            if (engine != null) {
                if (engine.has("threads")) {
                    engineConfig.setThreads(engine.get("threads").getAsInt());
                }
                if (engine.has("hashMB")) {
                    engineConfig.setHashMB(engine.get("hashMB").getAsInt());
                }
                if (engine.has("skillLevel")) {
                    engineConfig.setSkillLevel(engine.get("skillLevel").getAsInt());
                }
                if (engine.has("depthLimit")) {
                    engineConfig.setDepthLimit(engine.get("depthLimit").getAsInt());
                }
                if (engine.has("moveTimeMs")) {
                    engineConfig.setMoveTimeMs(engine.get("moveTimeMs").getAsInt());
                }
                if (engine.has("preset")) {
                    String preset = engine.get("preset").getAsString();
                    try {
                        engineConfig.applyPreset(EngineConfig.Preset.valueOf(preset.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        // Invalid preset, use custom
                    }
                }
            }
        } catch (Exception e) {
            // Use defaults
        }

        return engineConfig;
    }

    public void setEngineConfig(EngineConfig engineConfig) {
        JsonObject engine = new JsonObject();
        engine.addProperty("preset", engineConfig.getPreset().name().toLowerCase());
        engine.addProperty("threads", engineConfig.getThreads());
        engine.addProperty("hashMB", engineConfig.getHashMB());
        engine.addProperty("skillLevel", engineConfig.getSkillLevel());
        engine.addProperty("depthLimit", engineConfig.getDepthLimit());
        engine.addProperty("moveTimeMs", engineConfig.getMoveTimeMs());
        config.add("engine", engine);
    }

    // Window settings
    public int getWindowWidth() {
        return getWindowValue("width", 1200);
    }

    public int getWindowHeight() {
        return getWindowValue("height", 800);
    }

    public boolean isWindowMaximized() {
        try {
            JsonObject window = config.getAsJsonObject("window");
            if (window != null && window.has("maximized")) {
                return window.get("maximized").getAsBoolean();
            }
        } catch (Exception e) {
            // Use default
        }
        return false;
    }

    private int getWindowValue(String key, int defaultValue) {
        try {
            JsonObject window = config.getAsJsonObject("window");
            if (window != null && window.has(key)) {
                return window.get(key).getAsInt();
            }
        } catch (Exception e) {
            // Use default
        }
        return defaultValue;
    }

    public void setWindowSize(int width, int height, boolean maximized) {
        JsonObject window = config.getAsJsonObject("window");
        if (window == null) {
            window = new JsonObject();
            config.add("window", window);
        }
        window.addProperty("width", width);
        window.addProperty("height", height);
        window.addProperty("maximized", maximized);
    }

    private Color parseColor(String hex) {
        try {
            return Color.decode(hex);
        } catch (NumberFormatException e) {
            return Color.GRAY;
        }
    }
}
