package app;

import app.stockfish.StockfishUtil;

public class Main {

    public static void main(final String[] args) 
    {
        final var stockfishUtil = new StockfishUtil();
        try {
            stockfishUtil.startEngine();
            stockfishUtil.sendCommand("uci");
            stockfishUtil.waitFor("uciok");
            stockfishUtil.stopEngine();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
