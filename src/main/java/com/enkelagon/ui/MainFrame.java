package com.enkelagon.ui;

import com.enkelagon.config.ConfigManager;
import com.enkelagon.engine.EngineConfig;
import com.enkelagon.engine.StockfishEngine;
import com.enkelagon.logic.MoveValidator;
import com.enkelagon.logic.PgnHandler;
import com.enkelagon.model.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Main application window.
 */
public class MainFrame extends JFrame {

    private final ThemeManager theme;
    private final ConfigManager config;

    private BoardPanel boardPanel;
    private MoveHistoryPanel moveHistoryPanel;
    private AnalysisPanel analysisPanel;

    private Game game;
    private StockfishEngine engine;
    private MoveValidator validator;
    private PgnHandler pgnHandler;

    private Set<String> currentLegalMoves;
    private boolean engineThinking = false;
    private boolean analysisEnabled = false;
    private boolean playerIsWhite = true;  // Player's color choice

    private JLabel statusLabel;
    private JButton newGameBtn;
    private JButton undoBtn;
    private JButton flipBtn;
    private JButton hintBtn;
    private JToggleButton analysisBtn;

    private File lastSavedFile = null;  // Track last save location for quicksave

    public MainFrame() {
        super("Enkelagon Chess");
        this.theme = ThemeManager.getInstance();
        this.config = ConfigManager.getInstance();
        this.game = new Game();
        this.validator = new MoveValidator();
        this.pgnHandler = new PgnHandler();

        initializeUI();
        initializeEngine();
        updateLegalMoves();

        // Show new game dialog on startup
        SwingUtilities.invokeLater(this::showNewGameDialog);
    }

    private AnimatedBackground animatedBackground;

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(config.getWindowWidth(), config.getWindowHeight());
        setLocationRelativeTo(null);
        setBackground(Color.BLACK);

        // Set icon
        try {
            InputStream iconStream = getClass().getClassLoader().getResourceAsStream("icon.png");
            if (iconStream != null) {
                setIconImage(ImageIO.read(iconStream));
                iconStream.close();
            }
        } catch (Exception e) {
            // Ignore - icon is optional
        }

        // Create menu bar
        setJMenuBar(createMenuBar());

        // Create layered pane for animated background
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBackground(Color.BLACK);

        // Animated background layer
        animatedBackground = new AnimatedBackground();
        layeredPane.add(animatedBackground, JLayeredPane.DEFAULT_LAYER);

        // Main content panel (transparent to show background)
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left side - board and controls
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setOpaque(false);

        // Board panel
        boardPanel = new BoardPanel();
        boardPanel.setBoard(game.getBoard());
        boardPanel.setMoveCallback(this::handleMove);

        // Wrap board in a panel with semi-transparent background
        JPanel boardWrapper = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 0, 0, 180));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        boardWrapper.setOpaque(false);
        boardWrapper.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        boardWrapper.add(boardPanel);

        leftPanel.add(boardWrapper, BorderLayout.CENTER);

        // Control buttons
        JPanel controlPanel = createControlPanel();
        leftPanel.add(controlPanel, BorderLayout.SOUTH);

        contentPanel.add(leftPanel, BorderLayout.CENTER);

        // Right side - move history and analysis
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(0, 0, 0, 200));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
                super.paintComponent(g);
            }
        };
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(240, 0));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Move history
        moveHistoryPanel = new MoveHistoryPanel();
        rightPanel.add(moveHistoryPanel, BorderLayout.CENTER);

        // Analysis panel
        analysisPanel = new AnalysisPanel();
        rightPanel.add(analysisPanel, BorderLayout.SOUTH);

        contentPanel.add(rightPanel, BorderLayout.EAST);

        // Status bar
        JPanel statusBar = createStatusBar();
        contentPanel.add(statusBar, BorderLayout.SOUTH);

        layeredPane.add(contentPanel, JLayeredPane.PALETTE_LAYER);

        // Handle resize
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                Dimension size = layeredPane.getSize();
                animatedBackground.setBounds(0, 0, size.width, size.height);
                contentPanel.setBounds(0, 0, size.width, size.height);
            }
        });

        setContentPane(layeredPane);

        // Window listener to save settings and cleanup
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });

        // Key bindings
        setupKeyBindings();
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(theme.getBackgroundColor());

        // Game menu
        JMenu gameMenu = new JMenu("Game");
        gameMenu.setForeground(theme.getForegroundColor());

        JMenuItem newGame = new JMenuItem("New Game");
        newGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newGame.addActionListener(e -> newGame());
        gameMenu.add(newGame);

        gameMenu.addSeparator();

        JMenuItem loadPgn = new JMenuItem("Load PGN...");
        loadPgn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        loadPgn.addActionListener(e -> loadPgn());
        gameMenu.add(loadPgn);

        JMenuItem quickSave = new JMenuItem("Quicksave");
        quickSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        quickSave.addActionListener(e -> quickSave());
        gameMenu.add(quickSave);

        JMenuItem saveAs = new JMenuItem("Save As...");
        saveAs.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
        saveAs.addActionListener(e -> saveAs());
        gameMenu.add(saveAs);

        gameMenu.addSeparator();

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> {
            cleanup();
            System.exit(0);
        });
        gameMenu.add(exit);

        menuBar.add(gameMenu);

        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setForeground(theme.getForegroundColor());

        JMenuItem undo = new JMenuItem("Undo Move");
        undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        undo.addActionListener(e -> undoMove());
        editMenu.add(undo);

        JMenuItem flipBoard = new JMenuItem("Flip Board");
        flipBoard.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        flipBoard.addActionListener(e -> flipBoard());
        editMenu.add(flipBoard);

        menuBar.add(editMenu);

        // Settings menu
        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.setForeground(theme.getForegroundColor());

        JMenuItem preferences = new JMenuItem("Preferences...");
        preferences.addActionListener(e -> showSettings());
        settingsMenu.add(preferences);

        menuBar.add(settingsMenu);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setForeground(theme.getForegroundColor());

        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> showAbout());
        helpMenu.add(about);

        menuBar.add(helpMenu);

        return menuBar;
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        panel.setBackground(theme.getBackgroundColor());

        newGameBtn = theme.createButton("New Game");
        newGameBtn.addActionListener(e -> newGame());
        panel.add(newGameBtn);

        undoBtn = theme.createButton("Undo");
        undoBtn.addActionListener(e -> undoMove());
        panel.add(undoBtn);

        hintBtn = theme.createButton("Hint");
        hintBtn.addActionListener(e -> showHint());
        panel.add(hintBtn);

        flipBtn = theme.createButton("Flip");
        flipBtn.addActionListener(e -> flipBoard());
        panel.add(flipBtn);

        analysisBtn = new JToggleButton("Analysis");
        analysisBtn.setFont(theme.getUIFont());
        analysisBtn.addActionListener(e -> toggleAnalysis());
        panel.add(analysisBtn);

        JButton settingsBtn = theme.createButton("Settings");
        settingsBtn.addActionListener(e -> showSettings());
        panel.add(settingsBtn);

        return panel;
    }

    private JPanel createStatusBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        statusLabel = new JLabel("White to move");
        statusLabel.setFont(theme.getUIFont());
        statusLabel.setForeground(theme.getForegroundColor());
        panel.add(statusLabel, BorderLayout.WEST);

        JLabel engineLabel = new JLabel("Stockfish 17");
        engineLabel.setFont(theme.getUIFont());
        engineLabel.setForeground(theme.getSecondaryColor());
        panel.add(engineLabel, BorderLayout.EAST);

        return panel;
    }

    private void setupKeyBindings() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "deselect");
        actionMap.put("deselect", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardPanel.clearHighlights();
            }
        });
    }

    private void initializeEngine() {
        try {
            engine = new StockfishEngine();
            engine.setConfig(config.getEngineConfig());
            engine.start();

            engine.setAnalysisCallback(info -> {
                if (analysisEnabled) {
                    analysisPanel.updateAnalysis(info);
                }
            });

            updateStatus("Engine ready. White to move.");
        } catch (IOException e) {
            e.printStackTrace();
            updateStatus("Failed to start engine: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Failed to start Stockfish engine.\nPlease check that stockfish.exe exists in res/stockfish17/",
                    "Engine Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleMove(Position from, Position to) {
        if (engineThinking || game.isGameOver()) {
            return;
        }

        // Check if it's the player's turn
        boolean isPlayerTurn = (playerIsWhite && game.isWhiteToMove()) ||
                               (!playerIsWhite && !game.isWhiteToMove());
        if (!isPlayerTurn) {
            return;
        }

        // Build UCI move
        String uciMove = from.toAlgebraic() + to.toAlgebraic();

        // Check for promotion
        if (validator.isPromotionMove(game.getBoard(), from, to)) {
            Piece promotion = boardPanel.showPromotionDialog(playerIsWhite);
            uciMove += Character.toLowerCase(promotion.getFenChar());
        }

        // Validate move
        if (!validator.isLegalMove(uciMove)) {
            boardPanel.clearHighlights();
            return;
        }

        // Make the move
        Move move = Move.fromUci(uciMove, game.getBoard());
        executeMove(move);

        // Check game status
        if (checkGameEnd()) {
            return;
        }

        // Engine's turn
        engineMove();
    }

    private void executeMove(Move move) {
        game.makeMove(move);
        boardPanel.updatePieces();
        boardPanel.clearSuggestion();  // Clear any hint highlighting
        boardPanel.highlightLastMove(move);

        // Update move history
        int moveNum = (game.getMoveCount() + 1) / 2;
        boolean isWhite = game.getMoveCount() % 2 == 1;
        moveHistoryPanel.addMove(move, moveNum, isWhite);

        // Update legal moves for next player
        updateLegalMoves();

        // Update status
        updateTurnStatus();

        // Check for check
        Position kingPos = game.getBoard().findKing(game.isWhiteToMove());
        if (isInCheck()) {
            boardPanel.highlightCheck(kingPos);
        }

        // Update analysis if enabled
        if (analysisEnabled && engine != null) {
            engine.stopAnalysis();
            engine.startAnalysis(game.getCurrentFen());
        }
    }

    private void engineMove() {
        if (engine == null || !engine.isRunning()) {
            updateStatus("Engine not available");
            return;
        }

        engineThinking = true;
        updateStatus("Stockfish is thinking...");

        String fen = game.getCurrentFen();

        CompletableFuture.supplyAsync(() -> {
            try {
                return engine.getBestMove(fen);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(bestMove -> {
            SwingUtilities.invokeLater(() -> {
                engineThinking = false;

                if (bestMove != null && !game.isGameOver()) {
                    Move move = Move.fromUci(bestMove, game.getBoard());
                    executeMove(move);
                    checkGameEnd();
                } else {
                    updateStatus("Engine error or game over");
                }
            });
        });
    }

    private void updateLegalMoves() {
        if (engine == null || !engine.isRunning()) {
            currentLegalMoves = Set.of();
            validator.setLegalMoves(currentLegalMoves);
            boardPanel.setLegalMoves(currentLegalMoves);
            return;
        }

        String fen = game.getCurrentFen();

        CompletableFuture.supplyAsync(() -> {
            try {
                return engine.getLegalMoves(fen);
            } catch (IOException e) {
                e.printStackTrace();
                return Set.<String>of();
            }
        }).thenAccept(moves -> {
            SwingUtilities.invokeLater(() -> {
                currentLegalMoves = moves;
                validator.setLegalMoves(currentLegalMoves);
                boardPanel.setLegalMoves(currentLegalMoves);
            });
        });
    }

    private boolean isInCheck() {
        // Simple heuristic: if king is attacked
        // For proper implementation, would need to check if any opponent piece attacks king
        return false; // Simplified - engine handles this
    }

    private boolean checkGameEnd() {
        if (currentLegalMoves == null || currentLegalMoves.isEmpty()) {
            // No legal moves - could be checkmate or stalemate
            if (isInCheck()) {
                if (game.isWhiteToMove()) {
                    game.setStatus(Game.Status.BLACK_WINS_CHECKMATE);
                    updateStatus("Checkmate! Black wins.");
                } else {
                    game.setStatus(Game.Status.WHITE_WINS_CHECKMATE);
                    updateStatus("Checkmate! White wins.");
                }
            } else {
                game.setStatus(Game.Status.STALEMATE);
                updateStatus("Stalemate! Draw.");
            }
            showGameEndDialog();
            return true;
        }

        if (game.isFiftyMoveRule()) {
            game.setStatus(Game.Status.DRAW_FIFTY_MOVE);
            updateStatus("Draw by fifty-move rule.");
            showGameEndDialog();
            return true;
        }

        if (game.isThreefoldRepetition()) {
            game.setStatus(Game.Status.DRAW_THREEFOLD);
            updateStatus("Draw by threefold repetition.");
            showGameEndDialog();
            return true;
        }

        return false;
    }

    private void showGameEndDialog() {
        String message = switch (game.getStatus()) {
            case WHITE_WINS_CHECKMATE -> "Checkmate! White wins.";
            case BLACK_WINS_CHECKMATE -> "Checkmate! Black wins.";
            case STALEMATE -> "Stalemate! The game is a draw.";
            case DRAW_FIFTY_MOVE -> "Draw by fifty-move rule.";
            case DRAW_THREEFOLD -> "Draw by threefold repetition.";
            default -> "Game over.";
        };

        int choice = JOptionPane.showConfirmDialog(this,
                message + "\n\nWould you like to start a new game?",
                "Game Over", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            newGame();
        }
    }

    private void updateTurnStatus() {
        if (game.isGameOver()) return;

        String turn = game.isWhiteToMove() ? "White" : "Black";
        boolean isPlayerTurn = (playerIsWhite && game.isWhiteToMove()) ||
                               (!playerIsWhite && !game.isWhiteToMove());
        String player = isPlayerTurn ? "Your" : "Stockfish's";
        updateStatus(player + " turn (" + turn + " to move)");
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    // Actions

    private void newGame() {
        showNewGameDialog();
    }

    private void showNewGameDialog() {
        NewGameDialog.Result result = NewGameDialog.showDialog(this);

        if (result.confirmed) {
            startNewGame(result.playAsWhite);
        }
    }

    private void startNewGame(boolean playAsWhite) {
        if (engine != null) {
            engine.stopAnalysis();
        }

        this.playerIsWhite = playAsWhite;

        game.reset();
        game.setWhitePlayer(playAsWhite ? "You" : "Stockfish");
        game.setBlackPlayer(playAsWhite ? "Stockfish" : "You");

        boardPanel.setBoard(game.getBoard());
        boardPanel.clearHighlights();
        boardPanel.updatePieces();
        moveHistoryPanel.clear();
        analysisPanel.clear();

        // Flip board if playing as black
        if (!playAsWhite && !boardPanel.isFlipped()) {
            boardPanel.flipBoard();
        } else if (playAsWhite && boardPanel.isFlipped()) {
            boardPanel.flipBoard();
        }

        engineThinking = false;
        updateLegalMoves();

        String colorStr = playAsWhite ? "White" : "Black";
        updateStatus("New game. You play as " + colorStr + ". White to move.");

        // If player is black, engine moves first
        if (!playAsWhite) {
            engineMove();
        }
    }

    private void undoMove() {
        if (engineThinking) return;

        // Undo two moves (player + engine) to get back to player's turn
        if (game.getMoveCount() >= 2) {
            game.undoMove();
            game.undoMove();
            moveHistoryPanel.undoLastMove();
            moveHistoryPanel.undoLastMove();
        } else if (game.getMoveCount() >= 1) {
            game.undoMove();
            moveHistoryPanel.undoLastMove();
        }

        boardPanel.updatePieces();
        boardPanel.clearHighlights();
        boardPanel.highlightLastMove(game.getLastMove());
        updateLegalMoves();
        updateTurnStatus();

        if (analysisEnabled && engine != null) {
            engine.stopAnalysis();
            engine.startAnalysis(game.getCurrentFen());
        }
    }

    private void flipBoard() {
        boardPanel.flipBoard();
    }

    private void toggleAnalysis() {
        analysisEnabled = analysisBtn.isSelected();

        if (analysisEnabled && engine != null && engine.isRunning()) {
            analysisPanel.setAnalyzing(true);
            engine.startAnalysis(game.getCurrentFen());
        } else if (engine != null) {
            engine.stopAnalysis();
            analysisPanel.clear();
        }
    }

    private void showHint() {
        if (engineThinking || game.isGameOver()) {
            return;
        }

        // Check if it's the player's turn
        boolean isPlayerTurn = (playerIsWhite && game.isWhiteToMove()) ||
                               (!playerIsWhite && !game.isWhiteToMove());
        if (!isPlayerTurn) {
            updateStatus("Wait for your turn to get a hint.");
            return;
        }

        if (engine == null || !engine.isRunning()) {
            updateStatus("Engine not available for hints.");
            return;
        }

        updateStatus("Calculating best move...");
        hintBtn.setEnabled(false);

        String fen = game.getCurrentFen();

        CompletableFuture.supplyAsync(() -> {
            try {
                return engine.getBestMove(fen);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(bestMove -> {
            SwingUtilities.invokeLater(() -> {
                hintBtn.setEnabled(true);
                if (bestMove != null) {
                    boardPanel.highlightSuggestedMove(bestMove);
                    updateStatus("Suggested move: " + bestMove + " (highlighted in green)");
                } else {
                    updateStatus("Could not calculate hint.");
                }
            });
        });
    }

    private void loadPgn() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PGN Files", "pgn"));

        // Start from last save location if available
        if (lastSavedFile != null) {
            chooser.setCurrentDirectory(lastSavedFile.getParentFile());
        }

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                Game loadedGame = pgnHandler.loadFromFile(file.toPath());
                this.game = loadedGame;
                lastSavedFile = file;  // Remember for quicksave
                boardPanel.setBoard(game.getBoard());
                boardPanel.updatePieces();
                moveHistoryPanel.updateFromMoves(game.getMoveHistory());
                updateLegalMoves();
                updateTurnStatus();
                updateStatus("Loaded: " + file.getName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to load PGN: " + e.getMessage(),
                        "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void quickSave() {
        if (lastSavedFile != null) {
            // Save to the last used file
            try {
                pgnHandler.saveToFile(game, lastSavedFile.toPath());
                updateStatus("Quicksaved: " + lastSavedFile.getName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to quicksave: " + e.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // No previous save, prompt for file
            saveAs();
        }
    }

    private void saveAs() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PGN Files", "pgn"));
        chooser.setSelectedFile(new File("game.pgn"));

        // Start from last save location if available
        if (lastSavedFile != null) {
            chooser.setCurrentDirectory(lastSavedFile.getParentFile());
            chooser.setSelectedFile(lastSavedFile);
        }

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().endsWith(".pgn")) {
                file = new File(file.getAbsolutePath() + ".pgn");
            }

            try {
                pgnHandler.saveToFile(game, file.toPath());
                lastSavedFile = file;  // Remember for quicksave
                updateStatus("Saved: " + file.getName());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to save PGN: " + e.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showSettings() {
        SettingsDialog dialog = new SettingsDialog(this);
        dialog.setVisible(true);

        if (dialog.wasApplied() && engine != null) {
            engine.setConfig(dialog.getEngineConfig());
        }
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "Enkelagon Chess\n\n" +
                        "A chess GUI with Stockfish integration.\n\n" +
                        "Theme: Dragon\n" +
                        "Engine: Stockfish 17\n\n" +
                        "Human vs Computer gameplay.",
                "About Enkelagon", JOptionPane.INFORMATION_MESSAGE);
    }

    private void cleanup() {
        // Stop animation
        if (animatedBackground != null) {
            animatedBackground.stop();
        }

        if (engine != null) {
            engine.shutdown();
        }

        // Save window state
        config.setWindowSize(getWidth(), getHeight(),
                (getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0);
        config.saveConfig();
    }
}
