package pt.isec.pa.chess.ui;

import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;

public class ChessMenu extends MenuBar {

    // Game Menu
    Menu gameMenu;

    MenuItem newGame;
    MenuItem openGame;
    MenuItem saveGame;
    MenuItem importGame;
    MenuItem exportGame;
    MenuItem quit;

    // Mode menu
    Menu modeMenu;

    RadioMenuItem normalMode;
    RadioMenuItem learningMode;
    CheckMenuItem showPossibleMoves;
    MenuItem undo;
    MenuItem redo;

    public ChessMenu() {
        createViews();
        registerHandlers();
    }

    private void createViews() {
        // Game menu
        gameMenu = new Menu("Game");
        //Game options
        newGame = new MenuItem("New");
        openGame = new MenuItem("Open");
        saveGame = new MenuItem("Save");
        importGame = new MenuItem("Import");
        exportGame = new MenuItem("Export");
        quit = new MenuItem("Quit");

        //Atalhos
        newGame.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        openGame.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        saveGame.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        importGame.setAccelerator(KeyCombination.keyCombination("Ctrl+I"));
        exportGame.setAccelerator(KeyCombination.keyCombination("Ctrl+E"));
        quit.setAccelerator(KeyCombination.keyCombination("Esc"));

        gameMenu.getItems().addAll(
                newGame, openGame, saveGame,
                new SeparatorMenuItem(),
                importGame, exportGame,
                new SeparatorMenuItem(),
                quit
        );


        // Mode menu
        modeMenu = new Menu("Mode");
        //Mode options
        normalMode = new RadioMenuItem("Normal");
        learningMode = new RadioMenuItem("Learning");
        showPossibleMoves = new CheckMenuItem("Show possible moves");
        undo = new MenuItem("Undo");
        redo = new MenuItem("Redo");

        // Mode Menu
        ToggleGroup modeGroup = new ToggleGroup();
        normalMode.setToggleGroup(modeGroup);
        learningMode.setToggleGroup(modeGroup);
        normalMode.setSelected(true);

        undo.setAccelerator(KeyCombination.keyCombination("Ctrl+Z"));
        redo.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+Z"));
        undo.setDisable(true);
        redo.setDisable(true);
        showPossibleMoves.setDisable(true);

        modeMenu.getItems().addAll(
                normalMode, learningMode,
                new SeparatorMenuItem(),
                showPossibleMoves,
                undo,
                redo
        );

        this.getMenus().addAll(gameMenu, modeMenu);
    }

    private void registerHandlers() {
        normalMode.setOnAction(e -> {
            showPossibleMoves.setDisable(true);
            undo.setDisable(true);
            redo.setDisable(true);
        });

        learningMode.setOnAction(e -> {
            showPossibleMoves.setDisable(false);
        });
    }


    // Getters para os menus
    public MenuItem getNewGame() { return newGame; }
    public MenuItem getOpenGame() { return openGame; }
    public MenuItem getSaveGame() { return saveGame; }
    public MenuItem getImportGame() { return importGame; }
    public MenuItem getExportGame() { return exportGame; }
    public MenuItem getQuit() { return quit; }

    public RadioMenuItem getNormalMode() { return normalMode; }
    public RadioMenuItem getLearningMode() { return learningMode; }
    public CheckMenuItem getShowPossibleMoves() { return showPossibleMoves; }
    public MenuItem getUndo() { return undo; }
    public MenuItem getRedo() { return redo; }
}