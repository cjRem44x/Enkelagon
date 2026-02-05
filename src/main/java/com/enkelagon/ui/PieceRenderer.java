package com.enkelagon.ui;

import com.enkelagon.config.ConfigManager;
import com.enkelagon.model.Piece;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;

/**
 * Loads and caches piece images.
 */
public class PieceRenderer {

    private static PieceRenderer instance;

    private final Map<Piece, BufferedImage> pieceImages;
    private final Map<Piece, Map<Integer, Image>> scaledCache;
    private String currentPieceSet;

    private PieceRenderer() {
        this.pieceImages = new EnumMap<>(Piece.class);
        this.scaledCache = new EnumMap<>(Piece.class);
        this.currentPieceSet = ConfigManager.getInstance().getPieceSet();
        loadPieceSet(currentPieceSet);
    }

    public static synchronized PieceRenderer getInstance() {
        if (instance == null) {
            instance = new PieceRenderer();
        }
        return instance;
    }

    /**
     * Loads a piece set from resources.
     */
    public void loadPieceSet(String pieceSet) {
        pieceImages.clear();
        scaledCache.clear();
        this.currentPieceSet = pieceSet;

        for (Piece piece : Piece.values()) {
            BufferedImage img = loadPieceImage(piece, pieceSet);
            if (img != null) {
                pieceImages.put(piece, img);
                scaledCache.put(piece, new java.util.HashMap<>());
            }
        }
    }

    private BufferedImage loadPieceImage(Piece piece, String pieceSet) {
        String fileName = piece.getImageFileName();

        // Try multiple paths
        String[] paths = {
                // Resource path (Maven)
                "pieces/" + pieceSet + "/" + fileName,
                // External res directory
                "res/img/pieces-basic-png/" + fileName,
                // Absolute path from working directory
                System.getProperty("user.dir") + "/res/img/pieces-basic-png/" + fileName
        };

        // Try classpath resources first
        for (String path : paths) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
                if (is != null) {
                    return ImageIO.read(is);
                }
            } catch (IOException e) {
                // Try next path
            }
        }

        // Try file system
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        Path[] filePaths = {
                workingDir.resolve("res/img/pieces-basic-png/" + fileName),
                workingDir.resolve("src/main/resources/pieces/" + pieceSet + "/" + fileName),
                workingDir.resolve("src/main/resources/pieces/classic/" + fileName)
        };

        for (Path filePath : filePaths) {
            if (Files.exists(filePath)) {
                try {
                    return ImageIO.read(filePath.toFile());
                } catch (IOException e) {
                    // Try next path
                }
            }
        }

        System.err.println("Could not load piece image: " + fileName);
        return null;
    }

    /**
     * Gets the original piece image.
     */
    public BufferedImage getImage(Piece piece) {
        return pieceImages.get(piece);
    }

    /**
     * Gets a scaled piece image for the given size.
     */
    public Image getScaledImage(Piece piece, int size) {
        if (piece == null || !pieceImages.containsKey(piece)) {
            return null;
        }

        Map<Integer, Image> cache = scaledCache.get(piece);
        if (cache.containsKey(size)) {
            return cache.get(size);
        }

        BufferedImage original = pieceImages.get(piece);
        Image scaled = original.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        cache.put(size, scaled);
        return scaled;
    }

    /**
     * Gets a piece icon for the given size.
     */
    public ImageIcon getIcon(Piece piece, int size) {
        Image img = getScaledImage(piece, size);
        return img != null ? new ImageIcon(img) : null;
    }

    /**
     * Draws a piece on a graphics context.
     */
    public void drawPiece(Graphics2D g2d, Piece piece, int x, int y, int size) {
        Image img = getScaledImage(piece, size);
        if (img != null) {
            g2d.drawImage(img, x, y, null);
        }
    }

    /**
     * Draws a piece centered in a square.
     */
    public void drawPieceCentered(Graphics2D g2d, Piece piece, int squareX, int squareY, int squareSize) {
        int pieceSize = (int) (squareSize * 0.85);
        int offset = (squareSize - pieceSize) / 2;
        drawPiece(g2d, piece, squareX + offset, squareY + offset, pieceSize);
    }

    /**
     * Gets the current piece set name.
     */
    public String getCurrentPieceSet() {
        return currentPieceSet;
    }

    /**
     * Clears the scaled image cache.
     */
    public void clearCache() {
        for (Map<Integer, Image> cache : scaledCache.values()) {
            cache.clear();
        }
    }

    /**
     * Gets available piece sets.
     */
    public String[] getAvailablePieceSets() {
        return new String[]{"classic"};
    }
}
