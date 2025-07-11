package pt.isec.pa.chess.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import pt.isec.pa.chess.model.ChessGameManager;
import pt.isec.pa.chess.model.data.pieces.PieceColor;

import java.io.File;
import java.util.stream.Stream;

public class RootPane extends BorderPane {

    private final ChessGameManager gameManager;
    private ChessBoardView boardView;
    private ChessMenu menu;
    private Label labelPreto, labelBranco;
    private Button undoButton, redoButton;
    private ToggleButton soundButton;

    public RootPane(ChessGameManager gameManager) {
        this.gameManager = gameManager;
        createViews();
        registerHandlers();
    }

    private void createViews() {
        //Cria o menu e coloca-o no topo da interface
        menu = new ChessMenu();
        this.setTop(menu);

        boardView = new ChessBoardView(gameManager);
        setupLayout();
        createToolbar();

        this.setFocusTraversable(true);
        this.requestFocus();
    }

    private void setupLayout() {
        VBox layout = setupPlayerLabels();
        this.setCenter(layout);
    }

    private VBox setupPlayerLabels() {
        labelPreto = new Label(gameManager.getBlackPlayerName());
        labelBranco = new Label(gameManager.getWhitePlayerName());

        Stream.of(labelPreto, labelBranco).forEach(label -> {
            label.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
            label.setPadding(new Insets(8));
            label.setMaxWidth(Double.MAX_VALUE);
            label.setAlignment(Pos.CENTER);
        });

        labelBranco.prefWidthProperty().bind(boardView.widthProperty());
        labelPreto.prefWidthProperty().bind(boardView.widthProperty());

        VBox topBox = new VBox(labelPreto);
        VBox bottomBox = new VBox(labelBranco);
        topBox.setAlignment(Pos.CENTER);
        bottomBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(topBox, boardView, bottomBox);
        layout.setAlignment(Pos.CENTER);
        layout.setFillWidth(false);
        layout.setSpacing(5);

        return layout;
    }

    private void createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(5, 10, 5, 10));
        toolbar.setAlignment(Pos.CENTER);

        undoButton = new Button("Undo");
        redoButton = new Button("Redo");
        soundButton = new ToggleButton();

        undoButton.setDisable(true);
        redoButton.setDisable(true);

        soundButton.setSelected(boardView.isSoundEnabled());
        soundButton.setText(soundButton.isSelected() ? "🔊" : "🔇");
        soundButton.setOnAction(e -> {
            boolean enabled = soundButton.isSelected();
            boardView.toggleSound();
            soundButton.setText(enabled ? "🔊" : "🔇");
        });

        toolbar.getChildren().addAll(undoButton, redoButton, soundButton);
        this.setBottom(toolbar);
    }

    private void registerHandlers() {
        gameManager.addPropertyChangeListener(ChessGameManager.PROP_BOARD, evt -> update());
        gameManager.addPropertyChangeListener(ChessGameManager.PROP_WHITE_PLAYER, evt -> {
            if (labelBranco != null)
                labelBranco.setText(String.valueOf(evt.getNewValue()));
        });
        gameManager.addPropertyChangeListener(ChessGameManager.PROP_BLACK_PLAYER, evt -> {
            if (labelPreto != null)
                labelPreto.setText(String.valueOf(evt.getNewValue()));
        });

        this.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case LEFT -> handleUndo();
                case RIGHT -> handleRedo();
            }
        });

        // Menu actions
        menu.getNewGame().setOnAction(e -> handleNewGame());
        menu.getOpenGame().setOnAction(e -> handleOpenAction(gameManager::openGame, "Abrir jogo"));
        menu.getSaveGame().setOnAction(e -> handleSaveAction(gameManager::saveGame, "Guardar jogo"));
        menu.getImportGame().setOnAction(e -> handleImportGame());
        menu.getExportGame().setOnAction(e -> handleExportGame(gameManager::exportPartial, "Exportar jogo parcial"));
        menu.getQuit().setOnAction(e -> System.exit(0));

        menu.getUndo().setOnAction(e -> handleUndo());
        undoButton.setOnAction(e -> handleUndo());

        menu.getRedo().setOnAction(e -> handleRedo());
        redoButton.setOnAction(e -> handleRedo());

        menu.getNormalMode().setOnAction(e -> {
            gameManager.setLearningMode(false);
            updateUndoRedoButtons();
        });

        menu.getLearningMode().setOnAction(e -> {
            gameManager.setLearningMode(true);
            updateUndoRedoButtons();
        });

        menu.getShowPossibleMoves().setOnAction(e -> {
            gameManager.setShowMovesMode(menu.getShowPossibleMoves().isSelected());
            if (boardView != null) {
                boardView.setShowPossibleMoves(gameManager.isShowMovesMode());
            }
        });
    }

    private void handleNewGame() {
        TextInputDialog dialog1 = new TextInputDialog("Jogador 1");
        dialog1.setTitle("Novo Jogo");
        dialog1.setHeaderText("Introduz o nome do jogador 1:");
        String player1 = dialog1.showAndWait().orElse(null);
        if (player1 != null) {
            TextInputDialog dialog2 = new TextInputDialog("Jogador 2");
            dialog2.setTitle("Novo Jogo");
            dialog2.setHeaderText("Introduz o nome do jogador 2:");
            String player2 = dialog2.showAndWait().orElse(null);

            if (player2 != null) {
                gameManager.newGame(player1, player2);
                boardView = new ChessBoardView(gameManager);
                setupLayout();
                update();
            }
        }
    }

    private void handleImportGame() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importar jogo parcial");
        File file = fileChooser.showOpenDialog(getScene().getWindow());

        if (file != null) {
            TextInputDialog dialog1 = new TextInputDialog("Jogador 1");
            dialog1.setTitle("Importar Jogo");
            dialog1.setHeaderText("Introduz o nome do jogador 1 (brancas):");
            String player1 = dialog1.showAndWait().orElse(null);

            if (player1 != null) {
                TextInputDialog dialog2 = new TextInputDialog("Jogador 2");
                dialog2.setTitle("Importar Jogo");
                dialog2.setHeaderText("Introduz o nome do jogador 2 (pretas):");
                String player2 = dialog2.showAndWait().orElse(null);

                if (player2 != null) {
                    gameManager.importPartial(file, player1, player2);
                    update();
                }
            }
        }
    }

    private void handleExportGame(java.util.function.Consumer<File> action, String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            action.accept(file);
        }
    }

    private void handleOpenAction(java.util.function.Consumer<File> action, String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            action.accept(file);
        }
    }

    private void handleSaveAction(java.util.function.Consumer<File> action, String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            action.accept(file);
        }
    }


    private void handleUndo() {
        if (gameManager.canUndo()) {
            gameManager.undo();
            updateUndoRedoButtons();
        }
    }

    private void handleRedo() {
        if (gameManager.canRedo()) {
            gameManager.redo();
            updateUndoRedoButtons();
        }
    }

    private void updateUndoRedoButtons() {
        boolean learningModeActive = gameManager.isLearningMode();

        menu.getLearningMode().setSelected(learningModeActive);
        menu.getNormalMode().setSelected(!learningModeActive);
        menu.getShowPossibleMoves().setDisable(!learningModeActive);

        boolean canUndo = gameManager.canUndo();
        boolean canRedo = gameManager.canRedo();

        undoButton.setDisable(!canUndo);
        redoButton.setDisable(!canRedo);
        menu.getUndo().setDisable(!canUndo);
        menu.getRedo().setDisable(!canRedo);
    }

    private void updateTurnColors() {
        if (labelBranco == null || labelPreto == null) return;

        String activeStyle = "-fx-background-color: #467099; -fx-border-color: #CCD5D9; -fx-border-width: 2;";
        String inactiveStyle = "-fx-background-color: #f0f0f0; -fx-border-color: #ccc; -fx-border-width: 2;";

        if (gameManager.getCurrentPlayer() == PieceColor.WHITE) {
            labelBranco.setStyle(activeStyle);
            labelPreto.setStyle(inactiveStyle);
        } else {
            labelPreto.setStyle(activeStyle);
            labelBranco.setStyle(inactiveStyle);
        }
    }

    public void update() {
        if (gameManager == null || boardView == null) return;

        boardView.draw();
        updateTurnColors();
        updateUndoRedoButtons();

        if (labelBranco != null)
            labelBranco.setText(gameManager.getWhitePlayerName());
        if (labelPreto != null)
            labelPreto.setText(gameManager.getBlackPlayerName());
    }
}
