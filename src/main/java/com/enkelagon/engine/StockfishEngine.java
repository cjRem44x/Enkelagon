package com.enkelagon.engine;

import com.enkelagon.logic.MoveGenerator;
import com.enkelagon.model.Board;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Manages communication with the Stockfish chess engine.
 */
public class StockfishEngine {

    public static class AnalysisInfo {
        public int depth;
        public int selectiveDepth;
        public long nodes;
        public int nps;
        public int score;
        public boolean isMate;
        public int mateIn;
        public String bestMove;
        public String ponder;
        public String[] principalVariation;

        @Override
        public String toString() {
            if (isMate) {
                return "Mate in " + mateIn;
            }
            return String.format("%.2f", score / 100.0);
        }
    }

    private final String stockfishPath;
    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;
    private EngineConfig config;
    private volatile boolean running;
    private volatile boolean analyzing;
    private final ExecutorService executor;
    private final MoveGenerator moveGenerator;

    private Consumer<AnalysisInfo> analysisCallback;
    private Consumer<String> bestMoveCallback;

    public StockfishEngine() {
        // Try to find Stockfish relative to working directory
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        Path sfPath = workingDir.resolve("res").resolve("stockfish17").resolve("stockfish.exe");

        if (!sfPath.toFile().exists()) {
            // Try parent directory
            sfPath = workingDir.getParent().resolve("res").resolve("stockfish17").resolve("stockfish.exe");
        }

        this.stockfishPath = sfPath.toAbsolutePath().toString();
        this.config = new EngineConfig();
        this.executor = Executors.newSingleThreadExecutor();
        this.moveGenerator = new MoveGenerator();
        this.running = false;
        this.analyzing = false;
    }

    public StockfishEngine(String stockfishPath) {
        this.stockfishPath = stockfishPath;
        this.config = new EngineConfig();
        this.executor = Executors.newSingleThreadExecutor();
        this.moveGenerator = new MoveGenerator();
        this.running = false;
        this.analyzing = false;
    }

    public void setConfig(EngineConfig config) {
        this.config = config;
        if (running) {
            applyOptions();
        }
    }

    public EngineConfig getConfig() {
        return config;
    }

    public void setAnalysisCallback(Consumer<AnalysisInfo> callback) {
        this.analysisCallback = callback;
    }

    public void setBestMoveCallback(Consumer<String> callback) {
        this.bestMoveCallback = callback;
    }

    /**
     * Starts the Stockfish engine process.
     */
    public synchronized void start() throws IOException {
        if (running) {
            return;
        }

        File sfFile = new File(stockfishPath);
        if (!sfFile.exists()) {
            throw new IOException("Stockfish not found at: " + stockfishPath);
        }

        ProcessBuilder pb = new ProcessBuilder(stockfishPath);
        pb.redirectErrorStream(true);
        pb.directory(sfFile.getParentFile());
        process = pb.start();

        // Check if process started
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (!process.isAlive()) {
            throw new IOException("Stockfish process terminated immediately");
        }

        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        running = true;

        // Initialize UCI
        sendCommand("uci");
        waitFor("uciok");

        // Apply configuration
        applyOptions();

        // Signal ready
        sendCommand("isready");
        waitFor("readyok");
    }

    /**
     * Stops the engine process.
     */
    public synchronized void stop() {
        if (!running) {
            return;
        }

        analyzing = false;

        try {
            sendCommand("quit");
            process.waitFor(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            // Ignore
        }

        process.destroyForcibly();
        running = false;

        try {
            reader.close();
            writer.close();
        } catch (IOException e) {
            // Ignore
        }
    }

    /**
     * Checks if the engine is running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Sends a command to the engine.
     */
    private void sendCommand(String command) throws IOException {
        writer.write(command + "\n");
        writer.flush();
    }

    /**
     * Waits for a specific response from the engine.
     */
    private String waitFor(String keyword) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(keyword)) {
                return line;
            }
        }
        return null;
    }

    /**
     * Applies engine options from config.
     */
    private void applyOptions() {
        try {
            for (String option : config.getUciOptions()) {
                sendCommand(option);
            }
            sendCommand("isready");
            waitFor("readyok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets the position using FEN and makes the engine ready.
     */
    public void setPosition(String fen) throws IOException {
        sendCommand("position fen " + fen);
        sendCommand("isready");
        waitFor("readyok");
    }

    /**
     * Sets position from starting position with moves.
     */
    public void setPositionWithMoves(String moves) throws IOException {
        if (moves == null || moves.isEmpty()) {
            sendCommand("position startpos");
        } else {
            sendCommand("position startpos moves " + moves);
        }
        sendCommand("isready");
        waitFor("readyok");
    }

    /**
     * Gets the best move for the current position synchronously.
     */
    public synchronized String getBestMove(String fen) throws IOException {
        if (!running || !process.isAlive()) {
            throw new IOException("Engine not running");
        }

        // Stop any running analysis first
        if (analyzing) {
            analyzing = false;
            sendCommand("stop");
            // Wait a moment for stop to process
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            // Clear any pending output
            while (reader.ready()) { reader.readLine(); }
        }

        sendCommand("isready");
        waitFor("readyok");

        setPosition(fen);
        sendCommand(config.getGoCommand());

        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("bestmove")) {
                String[] parts = line.split("\\s+");
                return parts.length > 1 ? parts[1] : null;
            }
        }
        return null;
    }

    /**
     * Gets the best move asynchronously.
     */
    public CompletableFuture<String> getBestMoveAsync(String fen) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getBestMove(fen);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Starts analysis of the current position.
     */
    public synchronized void startAnalysis(String fen) {
        if (!running || !process.isAlive()) {
            return;
        }

        // Stop any previous analysis
        if (analyzing) {
            analyzing = false;
            try {
                sendCommand("stop");
                Thread.sleep(50);
                while (reader.ready()) { reader.readLine(); }
            } catch (Exception e) { /* ignore */ }
        }

        analyzing = true;

        executor.submit(() -> {
            try {
                synchronized (this) {
                    sendCommand("isready");
                    waitFor("readyok");
                    setPosition(fen);
                    sendCommand("go infinite");
                }

                String line;
                while (analyzing && (line = reader.readLine()) != null) {
                    if (line.startsWith("info") && line.contains("score")) {
                        AnalysisInfo info = parseInfoLine(line);
                        if (info != null && analysisCallback != null) {
                            analysisCallback.accept(info);
                        }
                    } else if (line.startsWith("bestmove")) {
                        String[] parts = line.split("\\s+");
                        String bestMove = parts.length > 1 ? parts[1] : null;
                        if (bestMoveCallback != null) {
                            bestMoveCallback.accept(bestMove);
                        }
                        break;
                    }
                }
            } catch (IOException e) {
                if (analyzing) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Stops the current analysis.
     */
    public synchronized void stopAnalysis() {
        if (!analyzing) return;

        analyzing = false;
        try {
            sendCommand("stop");
            Thread.sleep(50);
            // Clear any pending output
            while (reader.ready()) { reader.readLine(); }
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Gets legal moves for the current position using perft.
     */
    public synchronized Set<String> getLegalMoves(String fen) throws IOException {
        if (!running || !process.isAlive()) {
            return new HashSet<>();
        }

        // Stop any running analysis first
        if (analyzing) {
            analyzing = false;
            sendCommand("stop");
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            while (reader.ready()) { reader.readLine(); }
        }

        sendCommand("isready");
        waitFor("readyok");

        setPosition(fen);
        sendCommand("go perft 1");

        Set<String> moves = new HashSet<>();
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.contains(":") && !line.startsWith("Nodes")) {
                String move = line.split(":")[0].trim();
                if (move.length() >= 4 && move.length() <= 5) {
                    moves.add(move);
                }
            }
            if (line.startsWith("Nodes searched")) {
                break;
            }
        }

        return moves;
    }

    /**
     * Gets legal moves asynchronously.
     */
    public CompletableFuture<Set<String>> getLegalMovesAsync(String fen) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return getLegalMoves(fen);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Parses a UCI info line into an AnalysisInfo object.
     */
    private AnalysisInfo parseInfoLine(String line) {
        AnalysisInfo info = new AnalysisInfo();

        String[] parts = line.split("\\s+");
        for (int i = 0; i < parts.length; i++) {
            switch (parts[i]) {
                case "depth":
                    if (i + 1 < parts.length) {
                        info.depth = Integer.parseInt(parts[i + 1]);
                    }
                    break;
                case "seldepth":
                    if (i + 1 < parts.length) {
                        info.selectiveDepth = Integer.parseInt(parts[i + 1]);
                    }
                    break;
                case "nodes":
                    if (i + 1 < parts.length) {
                        info.nodes = Long.parseLong(parts[i + 1]);
                    }
                    break;
                case "nps":
                    if (i + 1 < parts.length) {
                        info.nps = Integer.parseInt(parts[i + 1]);
                    }
                    break;
                case "score":
                    if (i + 1 < parts.length) {
                        if (parts[i + 1].equals("cp") && i + 2 < parts.length) {
                            info.score = Integer.parseInt(parts[i + 2]);
                            info.isMate = false;
                        } else if (parts[i + 1].equals("mate") && i + 2 < parts.length) {
                            info.mateIn = Integer.parseInt(parts[i + 2]);
                            info.isMate = true;
                        }
                    }
                    break;
                case "pv":
                    // Collect all remaining as principal variation
                    int pvStart = i + 1;
                    int pvEnd = parts.length;
                    info.principalVariation = new String[pvEnd - pvStart];
                    System.arraycopy(parts, pvStart, info.principalVariation, 0, pvEnd - pvStart);
                    if (info.principalVariation.length > 0) {
                        info.bestMove = info.principalVariation[0];
                    }
                    i = pvEnd; // Skip to end
                    break;
            }
        }

        return info;
    }

    /**
     * Evaluates the current position and returns the score in centipawns.
     */
    public int evaluate(String fen) throws IOException {
        setPosition(fen);
        sendCommand("go depth 10");

        int score = 0;
        String line;

        while ((line = reader.readLine()) != null) {
            if (line.contains("score cp")) {
                String[] parts = line.split("score cp ");
                if (parts.length > 1) {
                    String scoreStr = parts[1].split("\\s+")[0];
                    score = Integer.parseInt(scoreStr);
                }
            } else if (line.contains("score mate")) {
                String[] parts = line.split("score mate ");
                if (parts.length > 1) {
                    String mateStr = parts[1].split("\\s+")[0];
                    int mateIn = Integer.parseInt(mateStr);
                    score = mateIn > 0 ? 100000 - mateIn : -100000 - mateIn;
                }
            } else if (line.startsWith("bestmove")) {
                break;
            }
        }

        return score;
    }

    /**
     * Gets the engine name and version.
     */
    public String getEngineInfo() {
        return "Stockfish 17";
    }

    /**
     * Shuts down the executor service.
     */
    public void shutdown() {
        stop();
        executor.shutdown();
        try {
            executor.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
