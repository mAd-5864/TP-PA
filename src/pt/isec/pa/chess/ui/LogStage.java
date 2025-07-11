package pt.isec.pa.chess.ui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import pt.isec.pa.chess.model.ModelLog;

public class LogStage extends Stage {

    ListView<String> logView;
    Button btnClear;

    public LogStage() {
        setTitle("Log de Eventos");

        logView = new ListView<>();
        btnClear = new Button("Limpar Logs");

        BorderPane root = new BorderPane();
        root.setCenter(logView);

        HBox hbox = new HBox(btnClear);
        hbox.setAlignment(Pos.CENTER);
        hbox.setPadding(new Insets(10));
        root.setBottom(hbox);

        setScene(new Scene(root, 400, 500));

        // Update logs when changes happen
        ModelLog.getInstance().addPropertyChangeListener(evt -> {
            logView.getItems().setAll(ModelLog.getInstance().getLogs());

            // Faz scroll para o último item para mostrar o log mais recente
            if (!logView.getItems().isEmpty()) {
                Platform.runLater(() ->
                        logView.scrollTo(logView.getItems().size() - 1)
                );
            }
        });

        // Clear logs
        btnClear.setOnAction(e -> ModelLog.getInstance().clearLogs());

        // Initial load
        logView.getItems().addAll(ModelLog.getInstance().getLogs());

        // Scroll inicial (caso haja logs já guardados)
        if (!logView.getItems().isEmpty()) {
            Platform.runLater(() ->
                    logView.scrollTo(logView.getItems().size() - 1)
            );
        }
    }
}
