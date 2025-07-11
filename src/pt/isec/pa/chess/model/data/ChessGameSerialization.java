package pt.isec.pa.chess.model.data;

import java.io.*;

public final class ChessGameSerialization {

    private ChessGameSerialization() {} // Impede instância

    public static void serialize(ChessGame game, String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(game);
        }
    }

    public static ChessGame deserialize(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return (ChessGame) in.readObject();
        }
    }
}
