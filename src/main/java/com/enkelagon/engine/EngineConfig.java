package com.enkelagon.engine;

/**
 * Configuration for the Stockfish engine including presets and advanced options.
 */
public class EngineConfig {

    public enum Preset {
        EASY("Easy", 5, 5, 500),
        MEDIUM("Medium", 10, 10, 1000),
        HARD("Hard", 20, 20, 2000),
        CUSTOM("Custom", 20, 20, 1000);

        private final String displayName;
        private final int skillLevel;
        private final int depth;
        private final int moveTimeMs;

        Preset(String displayName, int skillLevel, int depth, int moveTimeMs) {
            this.displayName = displayName;
            this.skillLevel = skillLevel;
            this.depth = depth;
            this.moveTimeMs = moveTimeMs;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getSkillLevel() {
            return skillLevel;
        }

        public int getDepth() {
            return depth;
        }

        public int getMoveTimeMs() {
            return moveTimeMs;
        }
    }

    private Preset preset;
    private int threads;
    private int hashMB;
    private int skillLevel;
    private int depthLimit;
    private int moveTimeMs;
    private int multiPV;
    private boolean ponder;

    public EngineConfig() {
        // Default to medium difficulty
        this.preset = Preset.MEDIUM;
        this.threads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
        this.hashMB = 256;
        this.skillLevel = preset.skillLevel;
        this.depthLimit = preset.depth;
        this.moveTimeMs = preset.moveTimeMs;
        this.multiPV = 1;
        this.ponder = false;
    }

    public void applyPreset(Preset preset) {
        this.preset = preset;
        if (preset != Preset.CUSTOM) {
            this.skillLevel = preset.skillLevel;
            this.depthLimit = preset.depth;
            this.moveTimeMs = preset.moveTimeMs;
        }
    }

    public Preset getPreset() {
        return preset;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = Math.max(1, Math.min(threads, 128));
        this.preset = Preset.CUSTOM;
    }

    public int getHashMB() {
        return hashMB;
    }

    public void setHashMB(int hashMB) {
        this.hashMB = Math.max(1, Math.min(hashMB, 16384));
        this.preset = Preset.CUSTOM;
    }

    public int getSkillLevel() {
        return skillLevel;
    }

    public void setSkillLevel(int skillLevel) {
        this.skillLevel = Math.max(0, Math.min(skillLevel, 20));
        this.preset = Preset.CUSTOM;
    }

    public int getDepthLimit() {
        return depthLimit;
    }

    public void setDepthLimit(int depthLimit) {
        this.depthLimit = Math.max(1, Math.min(depthLimit, 100));
        this.preset = Preset.CUSTOM;
    }

    public int getMoveTimeMs() {
        return moveTimeMs;
    }

    public void setMoveTimeMs(int moveTimeMs) {
        this.moveTimeMs = Math.max(100, Math.min(moveTimeMs, 60000));
        this.preset = Preset.CUSTOM;
    }

    public int getMultiPV() {
        return multiPV;
    }

    public void setMultiPV(int multiPV) {
        this.multiPV = Math.max(1, Math.min(multiPV, 10));
    }

    public boolean isPonder() {
        return ponder;
    }

    public void setPonder(boolean ponder) {
        this.ponder = ponder;
    }

    /**
     * Returns the UCI commands to configure the engine.
     */
    public String[] getUciOptions() {
        return new String[]{
                "setoption name Threads value " + threads,
                "setoption name Hash value " + hashMB,
                "setoption name Skill Level value " + skillLevel,
                "setoption name MultiPV value " + multiPV,
                "setoption name Ponder value " + ponder
        };
    }

    /**
     * Returns the go command parameters.
     */
    public String getGoCommand() {
        return "go depth " + depthLimit + " movetime " + moveTimeMs;
    }

    /**
     * Returns a display-friendly summary of current settings.
     */
    public String getSummary() {
        if (preset != Preset.CUSTOM) {
            return preset.displayName + " (Skill " + skillLevel + ", Depth " + depthLimit + ")";
        }
        return "Custom (Skill " + skillLevel + ", Depth " + depthLimit + ", " + moveTimeMs + "ms)";
    }

    @Override
    public String toString() {
        return "EngineConfig{" +
                "preset=" + preset +
                ", threads=" + threads +
                ", hashMB=" + hashMB +
                ", skillLevel=" + skillLevel +
                ", depthLimit=" + depthLimit +
                ", moveTimeMs=" + moveTimeMs +
                ", multiPV=" + multiPV +
                '}';
    }
}
