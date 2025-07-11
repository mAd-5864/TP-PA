package pt.isec.pa.chess.model;

import pt.isec.pa.chess.model.data.ChessGame;
import pt.isec.pa.chess.model.data.ChessGameSerialization;
import pt.isec.pa.chess.model.data.End_Type;
import pt.isec.pa.chess.model.data.pieces.Piece;
import pt.isec.pa.chess.model.data.pieces.PieceColor;
import pt.isec.pa.chess.memento.ChessGameCaretaker;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Gestor principal do jogo de xadrez.
 * Coordena as operações do jogo, interface de utilizador e funcionalidades avançadas.
 */
public class ChessGameManager {

    private ChessGame game;
    private final PropertyChangeSupport pcs;
    private final ChessGameCaretaker caretaker;
    private boolean gameEndedNotified = false;

    public static final String PROP_BOARD = "boardState";
    public static final String PROP_WHITE_PLAYER = "whitePlayer";
    public static final String PROP_BLACK_PLAYER = "blackPlayer";
    public static final String PROP_SELECTED_PIECE = "selectedPiece";
    public static final String PROP_POSSIBLE_MOVES = "possibleMoves";
    public static final String PROP_MOVE_MADE = "moveMade";

    private boolean learningMode = false;
    private boolean showMovesMode = false;
    private String selectedPiecePosition = null;
    private List<String> possibleMoves = null;

    /**
     * Construtor que inicializa um novo gestor de jogo.
     */
    public ChessGameManager() {
        game = new ChessGame();
        pcs = new PropertyChangeSupport(this);
        caretaker = new ChessGameCaretaker();
        caretaker.initialize(game);
        clearSelectedPiece();
    }

    // === Listener Registration ===
    /**
     * Adiciona um observador para uma propriedade específica.
     *
     * @param property nome da propriedade a observar
     * @param listener observador a adicionar
     */
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(property, listener);
    }

    // ===  Instanciar novo jogo ===
    /**
     * Cria um novo jogo com os jogadores especificados.
     *
     * @param player1 nome do primeiro jogador (brancas)
     * @param player2 nome do segundo jogador (pretas)
     */
    public void newGame(String player1, String player2) {
        ChessGame oldGame = this.game;
        game = new ChessGame(player1, player2);
        notifyGameStateChanged(oldGame);
        caretaker.initialize(game);
        log("Novo jogo criado: " + player1 + " vs " + player2);
        clearSelectedPiece();
    }

    /**
     * Guarda o jogo atual em formato serializado.
     *
     * @param file ficheiro onde guardar o jogo
     */
    public void saveGame(File file) {
        try {
            ChessGameSerialization.serialize(game, file.getPath());
            log("Jogo guardado: " + file.getName());
        } catch (IOException e) {
            log("Erro ao guardar jogo: " + e.getMessage());
        }
    }

    /**
     * Importa um jogo de formato serializado.
     *
     * @param file ficheiro com o jogo guardado
     */
    public void openGame(File file) {
        try {
            ChessGame oldGame = this.game;
            game = ChessGameSerialization.deserialize(file.getPath());
            notifyGameStateChanged(oldGame);
            caretaker.initialize(game);
            log("Jogo carregado: " + file.getName());
            clearSelectedPiece();
        } catch (IOException | ClassNotFoundException e) {
            log("Erro ao abrir jogo: " + e.getMessage());
        }
    }

    /**
     * Importa um jogo de formato CSV/Texto com nomes de jogadores.
     *
     * @param file ficheiro com dados do jogo
     * @param whitePlayerName nome do jogador branco
     * @param blackPlayerName nome do jogador preto
     */
    public void importPartial(File file, String whitePlayerName, String blackPlayerName) {
        try {
            game.loadFromFile(file.getPath(), whitePlayerName, blackPlayerName);
            notifyGameStateChanged(null);
            caretaker.initialize(game);
            log("Jogo importado de: " + file.getName());
        } catch (IllegalArgumentException e) {
            log("Erro ao importar jogo de " + file.getName() + ": " + e.getMessage());
        } finally {
            clearSelectedPiece();
        }
    }

    /**
     * Guarda o jogo atual em formato CSV/Texto.
     *
     * @param file ficheiro onde exportar
     */
    public void exportPartial(File file) {
        game.saveToFile(file.getPath());
        log("Jogo exportado para: " + file.getName());
    }

    // === Game Logic ===
    /**
     * Executa uma jogada legal no jogo.
     *
     * @param from posição de origem
     * @param to posição de destino
     * @return true se a jogada foi executada com sucesso
     */
    public boolean play(String from, String to) {

        PieceColor currentPlayer = getCurrentPlayer();
        PieceColor opponent = currentPlayer.opposite();
        String pieceType = getPieceAt(from);
        boolean isCapture = getPieceAt(to) != null;

        boolean result = game.play(from, to);
        if (result) {
            caretaker.save(game);
            fireBoardUpdate();
            log(currentPlayer + (isCapture ? " captura " : " move ") + from + " -> " + to);

            // Notificar UI que foi feito um move
            boolean isCheck = game.isKingInCheck(opponent);
            boolean isCheckmate = game.isCheckmate(opponent);
            boolean isStalemate = game.isStalemate(opponent);
            MoveInfo moveInfo = new MoveInfo(pieceType, currentPlayer, from, to, isCapture, isCheck, isCheckmate, isStalemate);
            pcs.firePropertyChange(PROP_MOVE_MADE, null, moveInfo);
        } else {
            log("Jogada inválida: " + from + " -> " + to);
        }

        clearSelectedPiece();
        return result;
    }

    /**
     * Promove um peão na posição especificada.
     *
     * @param position posição do peão
     * @param promotionType tipo de peça para promoção
     */
    public void promotePawnAt(String position, String promotionType) {
        game.promotePawnAt(position, promotionType);
        fireBoardUpdate();
        log("Peão promovido a: " + promotionType);
    }

    /**
     * Desfaz a última jogada (apenas em modo de aprendizagem).
     *
     * @return true se a operação foi bem-sucedida
     */
    public boolean undo() {
        if (!canUndo()) return false;
        ChessGame restored = caretaker.undo();
        if (restored != null) {
            game = restored;
            fireBoardUpdate();
            log("Desfazer jogada");
            clearSelectedPiece();
            return true;
        }
        return false;
    }

    /**
     * Refaz uma jogada previamente desfeita (apenas em modo de aprendizagem).
     *
     * @return true se a operação foi bem-sucedida
     */
    public boolean redo() {
        if (!canRedo()) return false;
        ChessGame restored = caretaker.redo();
        if (restored != null) {
            game = restored;
            fireBoardUpdate();
            log("Refazer jogada");
            clearSelectedPiece();
            return true;
        }
        return false;
    }

    // === Accessors ===
    /**
     * Obtém a cor do jogador atual.
     *
     * @return cor do jogador atual
     */

    public PieceColor getCurrentPlayer() {
        return game.getCurrentPlayer();
    }

    /**
     * Obtém o nome do jogador das peças brancas.
     *
     * @return nome do jogador branco
     */
    public String getWhitePlayerName() {
        return game.getWhitePlayerName();
    }

    /**
     * Obtém o nome do jogador das peças pretas.
     *
     * @return nome do jogador preto
     */
    public String getBlackPlayerName() {
        return game.getBlackPlayerName();
    }

    /**
     * Obtém a peça numa determinada posição.
     *
     * @param position posição no tabuleiro
     * @return representação da peça ou null se não existir
     */
    public String getPieceAt(String position) {
        return game.getPieceAt(position);
    }

    /**
     * Obtém os movimentos possíveis para uma peça.
     *
     * @param position posição da peça
     * @return lista de movimentos possíveis
     */
    public List<String> getPossibleMoves(String position) {
        return game.getPossibleMoves(position);
    }

    /**
     * Verifica se o jogo terminou.
     *
     * @return true se o jogo terminou
     */
    public boolean isGameEnded() {
        return game.isGameEnded();
    }

    /**
     * Verifica se o jogo deve continuar e notifica se terminar.
     *
     * @return tipo de fim de jogo
     */
    public End_Type checkGameOver() {
        End_Type result = game.checkGameOver();
        if (!gameEndedNotified && (result == End_Type.CHECKMATE || result == End_Type.STALEMATE)) {
            gameEndedNotified = true;
            if (result == End_Type.CHECKMATE) {
                log("CheckMate winner: " + game.getWinner());
            } else if (result == End_Type.STALEMATE) {
                log("STALEMATE");
            } else {
                result = End_Type.CONTINUE;
            }
        }
        return result;
    }

    // === UI State Management ===
    /**
     * Seleciona uma peça para mostrar os seus movimentos possíveis.
     *
     * @param position posição da peça a selecionar
     */
    public void selectPiece(String position) {
        String piece = getPieceAt(position);
        if (piece != null) {
            selectedPiecePosition = position;
            possibleMoves = getPossibleMoves(position);
            pcs.firePropertyChange(PROP_SELECTED_PIECE, null, selectedPiecePosition);
            pcs.firePropertyChange(PROP_POSSIBLE_MOVES, null, possibleMoves);
        } else {
            clearSelectedPiece();
        }
    }

    /**
     * Retira a selecao da peça.
     */
    public void clearSelectedPiece() {
        String oldSelected = selectedPiecePosition;
        List<String> oldMoves = possibleMoves;
        selectedPiecePosition = null;
        possibleMoves = null;
        pcs.firePropertyChange(PROP_SELECTED_PIECE, oldSelected, null);
        pcs.firePropertyChange(PROP_POSSIBLE_MOVES, oldMoves, null);
    }

    /**
     * Obtém a posição da peça selecionada.
     *
     * @return posicao da peça em formato string
     */
    public String getSelectedPiecePosition() {
        return selectedPiecePosition;
    }

    /**
     * Obtém os movimentos possiveis da peça selecionada.
     *
     * @return lista de movimentos da peça em formato string
     */
    public List<String> getSelectedPiecePossibleMoves() {
        return possibleMoves;
    }

    /**
     * Define o estado do modo de aprendizagem.
     * Quando desativado, limpa a seleção atual da peça.
     *
     * @param active true para ativar o modo de aprendizagem, false para desativar
     */
    public void setLearningMode(boolean active) {
        this.learningMode = active;
        if (!active) clearSelectedPiece();
        log("Learning mode " + (active ? "enabled" : "disabled"));
    }

    /**
     * Verifica se o modo de aprendizagem está ativo.
     *
     * @return true se o modo de aprendizagem estiver ativo, false caso contrário
     */
    public boolean isLearningMode() {
        return learningMode;
    }

    /**
     * Verifica se é possível desfazer uma jogada.
     * Só é possível no modo de aprendizagem.
     *
     * @return true se for possível desfazer, false caso contrário
     */
    public boolean canUndo() {
        return learningMode && caretaker.canUndo();
    }

    /**
     * Verifica se é possível refazer uma jogada.
     * Só é possível no modo de aprendizagem.
     *
     * @return true se for possível refazer, false caso contrário
     */
    public boolean canRedo() {
        return learningMode && caretaker.canRedo();
    }

    /**
     * Verifica se o modo de mostrar movimentos está ativo.
     *
     * @return true se o modo de mostrar movimentos estiver ativo, false caso contrário
     */
    public boolean isShowMovesMode() {
        return showMovesMode;
    }

    /**
     * Define o estado do modo de mostrar movimentos.
     *
     * @param showMovesMode true para ativar o modo de mostrar movimentos, false para desativar
     */
    public void setShowMovesMode(boolean showMovesMode) {
        this.showMovesMode = showMovesMode;
    }

    /**
     * Obtém o tamanho do tabuleiro de jogo.
     *
     * @return tamanho do tabuleiro (número de casas por lado)
     */
    public int getBoardSize() {
        return game.getBoardSize();
    }

    // === Utility Methods ===

    /**
     * Notifica os observadores sobre mudanças no estado do jogo.
     * Dispara eventos de mudança para o tabuleiro e nomes dos jogadores.
     *
     * @param oldGame estado anterior do jogo (pode ser null)
     */
    private void notifyGameStateChanged(ChessGame oldGame) {
        pcs.firePropertyChange(PROP_BOARD, oldGame, game);
        pcs.firePropertyChange(PROP_WHITE_PLAYER, null, getWhitePlayerName());
        pcs.firePropertyChange(PROP_BLACK_PLAYER, null, getBlackPlayerName());
    }

    /**
     * Dispara uma atualização do tabuleiro para os observadores.
     * Métod_o de conveniência que chama notifyGameStateChanged com null.
     */
    private void fireBoardUpdate() {
        notifyGameStateChanged(null);
    }

    /**
     * Regista uma mensagem no log do modelo.
     *
     * @param message mensagem a ser passada para os logs
     */
    private void log(String message) {
        ModelLog.getInstance().addLog(message);
    }

    /**
     * Classe interna que encapsula informações sobre um movimento de xadrez.
     * Contém detalhes como tipo de peça, posições, capturas e estados especiais.
     */
    public static class MoveInfo {
        private final String pieceType;
        private final PieceColor playerColor;
        private final String from;
        private final String to;
        private final boolean isCapture;
        private final boolean isCheck;
        private final boolean isCheckmate;
        private final boolean isStalemate;

        /**
         * Construtor para criar informações sobre um movimento.
         *
         * @param pieceType tipo da peça que se move
         * @param playerColor cor do jogador que faz o movimento
         * @param from posição de origem do movimento
         * @param to posição de destino do movimento
         * @param isCapture true se o movimento captura uma peça
         * @param isCheck true se o movimento resulta em xeque
         * @param isCheckmate true se o movimento resulta em xeque-mate
         * @param isStalemate true se o movimento resulta em Stalemate
         */
        public MoveInfo(String pieceType, PieceColor playerColor, String from, String to, boolean isCapture, boolean isCheck, boolean isCheckmate, boolean isStalemate) {
            this.pieceType = pieceType;
            this.playerColor = playerColor;
            this.from = from;
            this.to = to;
            this.isCapture = isCapture;
            this.isCheck = isCheck;
            this.isCheckmate = isCheckmate;
            this.isStalemate = isStalemate;
        }

        /**
         * Obtém o tipo da peça que fez o movimento.
         *
         * @return tipo da peça
         */
        public String getPieceType() {
            return pieceType;
        }

        /**
         * Obtém a cor do jogador que fez o movimento.
         *
         * @return cor do jogador
         */
        public PieceColor getPlayerColor() {
            return playerColor;
        }

        /**
         * Obtém a posição de origem do movimento.
         *
         * @return posição de origem
         */
        public String getFrom() {
            return from;
        }

        /**
         * Obtém a posição de destino do movimento.
         *
         * @return posição de destino
         */
        public String getTo() {
            return to;
        }

        /**
         * Verifica se o movimento capturou uma peça.
         *
         * @return true se houve captura, false caso contrário
         */
        public boolean isCapture() {
            return isCapture;
        }

        /**
         * Verifica se o movimento resultou em xeque.
         *
         * @return true se resultou em xeque, false caso contrário
         */
        public boolean isCheck() {
            return isCheck;
        }

        /**
         * Verifica se o movimento resultou em xeque-mate.
         *
         * @return true se resultou em xeque-mate, false caso contrário
         */
        public boolean isCheckmate() {
            return isCheckmate;
        }
        public boolean isStalemate() {
            return isStalemate;
        }
    }
}