package pt.isec.pa.chess.memento;

import pt.isec.pa.chess.model.data.ChessGame;

import java.io.*;

// Memento with serialization
public class ChessGameMemento implements Serializable {
    private final byte[] gameState;

    public ChessGameMemento(ChessGame game) throws IOException { // Exception if serialization fails
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(game);
        oos.close();
        this.gameState = baos.toByteArray();
    }

    //    Override
    //    public Object getSnapshot() throws IOException, ClassNotFoundException {
    public ChessGame getState() throws IOException, ClassNotFoundException { // Exception if serialization fails or chessGame not found
        if (gameState == null) return null;
        ByteArrayInputStream bais = new ByteArrayInputStream(gameState);
        ObjectInputStream ois = new ObjectInputStream(bais);
        ChessGame game = (ChessGame) ois.readObject();
        ois.close();
        return game;
    }
}