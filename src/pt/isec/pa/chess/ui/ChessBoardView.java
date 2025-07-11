package pt.isec.pa.chess.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import pt.isec.pa.chess.model.ChessGameManager;
import pt.isec.pa.chess.model.data.End_Type;
import pt.isec.pa.chess.model.data.Position;
import pt.isec.pa.chess.model.data.pieces.*;
import pt.isec.pa.chess.ui.res.ImageManager;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import pt.isec.pa.chess.ui.res.SoundManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChessBoardView extends Canvas {
    private ChessGameManager gameManager;
    private final Map<String, Image> pieceImages;
    private String selectedPosition = null;
    private String invalidClickPosition = null;
    private final long INVALID_CLICK_DURATION_MS = 300;
    private Timeline invalidClickTimeline;
    private boolean soundEnabled = true;

    private Image lightCellBackground;
    private Image darkCellBackground;
    private Image backgroundImage;

    public ChessBoardView(ChessGameManager gameManager) {
        this.gameManager = gameManager;
        this.pieceImages = new HashMap<>();

        setWidth(500);
        setHeight(500);

        widthProperty().addListener(evt -> update());
        heightProperty().addListener(evt -> update());

        createViews();
        registerHandlers();
        update();
    }
    private void createViews() {
        // Carregar as imagens das peças
        // Carregar as imagens das peças
        loadPieceImages();

        // Carregar as imagens de fundo das casas
        lightCellBackground = ImageManager.getImage("light_square.jpg");
        darkCellBackground = ImageManager.getImage("dark_square.jpeg");
        backgroundImage = ImageManager.getImage("background.jpg");


    }
    private void registerHandlers() {
        setOnMousePressed(event -> {
            Position clickedPosition = getPositionFromMouse(event.getX(), event.getY());
            if (clickedPosition == null) return;

            String posStr = clickedPosition.toString();
            String pieceData = gameManager.getPieceAt(posStr);
            Piece piece = pieceData != null ? Piece.fromString(pieceData) : null;

            if (piece != null && piece.getColor() == gameManager.getCurrentPlayer()) {
                selectedPosition = posStr;
                gameManager.selectPiece(posStr);
            } else if (selectedPosition != null) {
                handlePieceMovement(clickedPosition);
            } else {
                triggerInvalidClickEffect(posStr);
            }

            update();
        });

        gameManager.addPropertyChangeListener(ChessGameManager.PROP_SELECTED_PIECE, evt -> update());
        gameManager.addPropertyChangeListener(ChessGameManager.PROP_POSSIBLE_MOVES, evt -> update());
        gameManager.addPropertyChangeListener(ChessGameManager.PROP_BOARD, evt -> update());

        gameManager.addPropertyChangeListener(ChessGameManager.PROP_MOVE_MADE, evt -> {
            if (soundEnabled && evt.getNewValue() instanceof ChessGameManager.MoveInfo) {
                ChessGameManager.MoveInfo moveInfo = (ChessGameManager.MoveInfo) evt.getNewValue();
                SoundManager.playMoveAnnouncement(
                        moveInfo.getPieceType(),
                        moveInfo.getPlayerColor(),
                        moveInfo.getFrom(),
                        moveInfo.getTo(),
                        moveInfo.isCapture(),
                        moveInfo.isCheck(),
                        moveInfo.isCheckmate(),
                        moveInfo.isStalemate()
                );
            }
        });
    }

    private void handlePieceMovement(Position destPosition) {
        boolean moved = gameManager.play(selectedPosition, destPosition.toString());

        if (moved) {
            String pieceStr = gameManager.getPieceAt(destPosition.toString());
            if (pieceStr != null) {
                Piece piece = Piece.fromString(pieceStr);
                if (piece instanceof Pawn) {
                    char row = destPosition.toString().charAt(1);
                    if ((piece.getColor() == PieceColor.WHITE && row == '8') ||
                            (piece.getColor() == PieceColor.BLACK && row == '1')) {
                        promptPawnPromotion(destPosition.toString());
                    }
                }
            }
            selectedPosition = null;
            checkGameStatus();
            update();
        } else {
            triggerInvalidClickEffect(destPosition.toString());
        }
    }

    private void triggerInvalidClickEffect(String posStr) {
        invalidClickPosition = posStr;
        if (invalidClickTimeline != null) {
            invalidClickTimeline.stop();
        }

        invalidClickTimeline = new Timeline(
                new KeyFrame(Duration.millis(INVALID_CLICK_DURATION_MS), e -> {
                    invalidClickPosition = null;
                    update();
                })
        );
        invalidClickTimeline.setCycleCount(1);
        invalidClickTimeline.play();
    }

    private void promptPawnPromotion(String position) {
        String pieceStr = gameManager.getPieceAt(position);
        Piece piece = Piece.fromString(pieceStr);
        String colorStr = piece.getColor() == PieceColor.WHITE ? "W" : "B";

        Dialog<String> dialog = new Dialog<>();
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20));

        String[] pieces = {"queen", "rook", "bishop", "knight"};
        String[] names = {"Queen", "Rook", "Bishop", "Knight"};

        for (int i = 0; i < pieces.length; i++) {
            String pieceType = pieces[i];
            String pieceName = names[i];

            Button button = new Button();

            // Imagem da peça
            String imageKey = pieceType + "_" + colorStr;
            Image pieceImage = pieceImages.get(imageKey);
            if (pieceImage != null) {
                ImageView imageView = new ImageView(pieceImage);
                imageView.setFitWidth(60);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
                button.setGraphic(imageView);
            }
            button.setOnAction(e -> {
                dialog.setResult(pieceName);
                dialog.close();
            });
            buttonBox.getChildren().add(button);
        }

        dialog.getDialogPane().setContent(buttonBox);
        dialog.getDialogPane().getButtonTypes().clear();

        dialog.showAndWait().ifPresent(choice -> {
            gameManager.promotePawnAt(position, choice);
            update();
        });
    }

    public void update() {
        draw();
    }

    protected void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        int boardSize = gameManager.getBoardSize();

        double cellSize = getCellSize();
        double margin = getMargin();

        drawBackground(gc);
        drawBoard(gc, cellSize, margin, boardSize);
        drawLabels(gc, cellSize, margin, boardSize);

        if (selectedPosition != null) {
            highlightSelectedPosition(gc, cellSize, margin, boardSize, selectedPosition);
        }

        if (invalidClickPosition != null) {
            highlightInvalidClick(gc, getCellSize(), getMargin(), gameManager.getBoardSize(), invalidClickPosition);
        }

        drawPieces(gc, cellSize, margin, boardSize);
        drawPossibleMoves(gc, cellSize, margin, boardSize);
    }

    private void checkGameStatus() {
        End_Type gameStatus = gameManager.checkGameOver();
        if (gameStatus == End_Type.CHECKMATE) {
            String winner = gameManager.getCurrentPlayer() == PieceColor.WHITE ?
                    gameManager.getBlackPlayerName() :
                    gameManager.getWhitePlayerName();
            showGameOverAlert("Checkmate", "Fim do jogo", "Checkmate! " + winner + " venceu.");
        } else if (gameStatus == End_Type.STALEMATE) {
            showGameOverAlert("Stalemate", "Fim do jogo", "Empate por stalemate!");
        }
    }

    private void showGameOverAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void loadPieceImages() {
        String[] pieceTypes = {"king", "queen", "rook", "bishop", "knight", "pawn"};
        String[] colors = {"W", "B"};

        for (String pieceType : pieceTypes) {
            for (String color : colors) {
                String filename = pieceType + color + ".png";
                String key = pieceType + "_" + color;
                pieceImages.put(key, ImageManager.getImage(filename));
            }
        }
    }



    private void drawBackground(GraphicsContext gc) {
        gc.clearRect(0, 0, getWidth(), getHeight());
        if (backgroundImage != null) {
            gc.drawImage(backgroundImage, 0, 0, getWidth(), getHeight());
        } else {
            gc.setFill(Color.rgb(111, 150, 166));
            gc.fillRect(0, 0, getWidth(), getHeight());
        }
    }

   private void drawBoard(GraphicsContext gc, double cellSize, double margin, int boardSize) {
       for (int row = 0; row < boardSize; row++) {
           for (int col = 0; col < boardSize; col++) {
               double x = margin + col * cellSize;
               double y = margin + row * cellSize;

               // Escolhe a imagem de fundo certa
               Image bgImage = ((row + col) % 2 == 0) ? lightCellBackground :darkCellBackground;
               if (bgImage != null) {
                   gc.drawImage(bgImage, x, y, cellSize, cellSize);
               }

               // (opcional) sobreposição de cor semi-transparente
               if ((row + col) % 2 == 0)
                   gc.setFill(Color.rgb(138, 176, 191, 0.3));  // ajusta opacidade
               else
                   gc.setFill(Color.rgb(228, 240, 242, 0.3));
               gc.fillRect(x, y, cellSize, cellSize);
           }
       }
   }


    private void drawLabels(GraphicsContext gc, double cellSize, double margin, int boardSize) {
        gc.setFill(Color.WHITESMOKE);
        gc.setFont(Font.font("Verdana", cellSize * 0.25));

        // Labels das colunas (a, b, c, ...)
        for (int col = 0; col < boardSize; col++) {
            char columnLabel = (char) ('a' + col);
            double x = margin + (col + 0.4) * cellSize;
            double yTop = margin / 1.5;
            double yBottom = margin + boardSize * cellSize + margin / 1.5;

            gc.fillText(String.valueOf(columnLabel), x, yTop);
            gc.fillText(String.valueOf(columnLabel), x, yBottom);
        }

        // Labels das linhas (8, 7, ..., 1)
        for (int row = 0; row < boardSize; row++) {
            int rowLabel = boardSize - row;
            double y = margin + (row + 0.6) * cellSize;
            double xLeft = margin / 3.0;
            double xRight = margin + boardSize * cellSize + margin / 3;

            gc.fillText(String.valueOf(rowLabel), xLeft, y);
            gc.fillText(String.valueOf(rowLabel), xRight, y);
        }
    }

    private void highlightSelectedPosition(GraphicsContext gc, double cellSize, double margin, int boardSize, String position) {
        Position pos = Position.fromString(position);
        if (pos != null) {
            int row = boardSize - pos.getRow();
            int col = pos.getCol() - 'a';
            double x = margin + col * cellSize;
            double y = margin + row * cellSize;
            gc.setFill(Color.rgb(168, 230, 255));
            gc.fillRect(x, y, cellSize, cellSize);
        }
    }

    private void highlightInvalidClick(GraphicsContext gc, double cellSize, double margin, int boardSize, String position) {
        Position pos = Position.fromString(position);
        if (pos != null) {
            int row = boardSize - pos.getRow();
            int col = pos.getCol() - 'a';
            double x = margin + col * cellSize;
            double y = margin + row * cellSize;
            gc.setFill(Color.rgb(255, 0, 0, 0.4));
            gc.fillRect(x, y, cellSize, cellSize);
        }
    }

    private void drawPossibleMoves(GraphicsContext gc, double cellSize, double margin, int boardSize) {
        if (!gameManager.isLearningMode() || selectedPosition == null || !gameManager.isShowMovesMode()) return;

        List<String> possibleMoves = gameManager.getSelectedPiecePossibleMoves();
        if (possibleMoves == null || possibleMoves.isEmpty()) return;

        for (String move : possibleMoves) {
            Position pos = Position.fromString(move);
            if (pos == null) continue;

            int row = boardSize - pos.getRow();
            int col = pos.getCol() - 'a';
            double x = margin + col * cellSize + cellSize / 2;
            double y = margin + row * cellSize + cellSize / 2;

            String pieceAt = gameManager.getPieceAt(move);
            double circleSize = pieceAt == null ? cellSize * 0.4 : cellSize * 0.8;
            if (pieceAt == null) {
                gc.setFill(Color.rgb(21, 21, 21, 0.35));
                gc.fillOval(x - circleSize / 2, y - circleSize / 2, circleSize, circleSize);
            } else {
                gc.setStroke(Color.rgb(21, 21, 21, 0.35));
                gc.setLineWidth(5);
                gc.strokeOval(x - circleSize / 2, y - circleSize / 2, circleSize, circleSize);
            }
        }
    }

    private void drawPieces(GraphicsContext gc, double cellSize, double margin, int boardSize) {
        for (int row = 1; row <= boardSize; row++) {
            for (char col = 'a'; col < 'a' + boardSize; col++) {
                String posStr = "" + col + row;
                String pieceStr = gameManager.getPieceAt(posStr);

                if (pieceStr == null) continue;
                Piece piece = Piece.fromString(pieceStr);
                if (piece == null) continue;

                String key = getPieceKey(piece);
                if (key == null) continue;

                double x = margin + (col - 'a') * cellSize;
                double y = margin + (boardSize - row) * cellSize;
                Image img = pieceImages.get(key);
                if (img != null) {
                    double imgMargin = cellSize * 0.02;
                    gc.drawImage(img, x + imgMargin, y + imgMargin,
                            cellSize - 2 * imgMargin, cellSize - 2 * imgMargin);
                }
            }
        }
    }

    private String getPieceKey(Piece piece) {
        String color = piece.getColor() == PieceColor.WHITE ? "W" : "B";
        if (piece instanceof King) return "king_" + color;
        if (piece instanceof Queen) return "queen_" + color;
        if (piece instanceof Rook) return "rook_" + color;
        if (piece instanceof Bishop) return "bishop_" + color;
        if (piece instanceof Knight) return "knight_" + color;
        if (piece instanceof Pawn) return "pawn_" + color;
        return null;
    }

    private Position getPositionFromMouse(double x, double y) {
        double cellSize = getCellSize();
        double margin = getMargin();

        int col = (int) ((x - margin) / cellSize);
        int row = (int) ((y - margin) / cellSize);

        if (col < 0 || col >= gameManager.getBoardSize() || row < 0 || row >= gameManager.getBoardSize())
            return null;

        char colChar = (char) ('a' + col);
        int rowNum = gameManager.getBoardSize() - row;
        return new Position(colChar, rowNum);
    }

    private double getCellSize() {
        return Math.min(getWidth(), getHeight()) / (gameManager.getBoardSize() + 1);
    }

    private double getMargin() {
        return getCellSize() * 0.5;
    }

    public void setShowPossibleMoves(boolean active) {
        if (gameManager != null) gameManager.setShowMovesMode(active);
    }
    public boolean isShowPossibleMoves() {
        return gameManager != null ? gameManager.isShowMovesMode() : false;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    public boolean toggleSound() {
        soundEnabled = !soundEnabled;
        SoundManager.setSoundEnabled(soundEnabled);
        return soundEnabled;
    }
}
