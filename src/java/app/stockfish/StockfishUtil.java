package app.stockfish;

import java.io.*;

public class StockfishUtil {
    private String StockfishExePath = null;
    private Process process;
    private BufferedReader reader;
    private BufferedWriter writer;
    
    public StockfishUtil()
    {
        StockfishExePath = System.getProperty("user.dir")+"/../../res/stockfish17/stockfish.exe";
    }

    public String get_StockfishExePath()
    {
        return this.StockfishExePath;
    }

    /** Start Stockfish process */
    public void startEngine() throws IOException 
    {
        ProcessBuilder pb = new ProcessBuilder(get_StockfishExePath());
        process = pb.start();
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
    }

    /** Send command to Stockfish */
    public void sendCommand(String command) throws IOException 
    {
        writer.write(command + "\n");
        writer.flush();
    }

    /** Read a single line response (blocking) */
    public String readLine() throws IOException 
    {
        return reader.readLine();
    }

    /** Read until a keyword appears (like "uciok", "readyok", "bestmove") */
    public String waitFor(String keyword) throws IOException 
    {
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("Engine: " + line);
            if (line.contains(keyword)) {
                return line;
            }
        }
        return null;
    }

    /** Stop Stockfish */
    public void stopEngine() throws IOException 
    {
        sendCommand("quit");
        process.destroy();
    }
}
