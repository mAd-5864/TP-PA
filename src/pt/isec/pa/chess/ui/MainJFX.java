package pt.isec.pa.chess.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pt.isec.pa.chess.model.ChessGameManager;
import pt.isec.pa.chess.model.ModelLog;


public class MainJFX extends Application {

    ChessGameManager gameManager = new ChessGameManager();

    @Override
    public void start(Stage stage1) {
        createStage(stage1);

        Stage stage2 = new Stage();
        createStage(stage2);


        LogStage logStage = new LogStage();
        logStage.show(); // Mostra o LogStage

        stage1.setOnCloseRequest(windowEvent -> {
            stage2.close();
            logStage.close();
        });
        stage2.setOnCloseRequest(windowEvent -> {
            stage1.close();
            logStage.close();
        });
    }

    public void createStage(Stage stage) {
        RootPane root = new RootPane(gameManager);
        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Chess Game");
        stage.setScene(scene);
        stage.setMaximized(true); // Janela abre no ecrã completo
        stage.show();
    }


}
