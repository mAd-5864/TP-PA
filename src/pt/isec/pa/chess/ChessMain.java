package pt.isec.pa.chess;
import javafx.application.Application;
import pt.isec.pa.chess.ui.MainJFX;

public class ChessMain {

    public static void main(String[] args) {

        System.out.println("PA Chess Game");

//        ChessGame game = new ChessGame();
//        Commands commands = new Commands(game);
//        commands.start();

        Application.launch(MainJFX.class,args);
    }
}

