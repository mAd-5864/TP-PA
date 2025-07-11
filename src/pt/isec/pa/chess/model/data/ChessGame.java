package pt.isec.pa.chess.model.data;

import pt.isec.pa.chess.model.data.pieces.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe principal que representa um jogo de xadrez.
 * Gere o estado do jogo, jogadores, movimentos e regras.
 */
public class ChessGame implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Board board;
    private PieceColor currentPlayer;
    private boolean gameOver;
    private PieceColor winner;
    private String whitePlayerName;
    private String blackPlayerName;

    /**
     * Construtor padrão que inicializa um novo jogo com jogadores padrão.
     */
    public ChessGame() {
        this.board = new Board();
        this.currentPlayer = PieceColor.WHITE;
        this.gameOver = false;
        this.winner = null;
        this.whitePlayerName = "Player 1";
        this.blackPlayerName = "Player 2";
    }

    /**
     * Construtor que inicializa um novo jogo com nomes específicos para os jogadores.
     *
     * @param whitePlayer nome do jogador das peças brancas
     * @param blackPlayer nome do jogador das peças pretas
     */
    public ChessGame(String whitePlayer, String blackPlayer) {
        this.board = new Board();
        this.currentPlayer = PieceColor.WHITE;
        this.gameOver = false;
        this.winner = null;
        this.whitePlayerName = whitePlayer;
        this.blackPlayerName = blackPlayer;
    }

    /**
     * Construtor que inicializa um jogo a partir de dados exportados.
     *
     * @param data dados do jogo em formato string
     */
    public ChessGame(String data) {
        this.board = new Board();
        this.currentPlayer = PieceColor.WHITE;
        this.gameOver = false;
        this.winner = null;
        this.whitePlayerName = "Player 1";
        this.blackPlayerName = "Player 2";

        if (data != null && !data.isBlank()) {
            importGameState(data);
        }
    }

    /**
     * Construtor que inicializa um jogo com jogadores específicos e dados exportados.
     *
     * @param whitePlayer nome do jogador das peças brancas
     * @param blackPlayer nome do jogador das peças pretas
     * @param data dados do jogo em formato string
     */
    public ChessGame(String whitePlayer, String blackPlayer, String data) {
        this.board = new Board();
        this.currentPlayer = PieceColor.WHITE;
        this.gameOver = false;
        this.winner = null;
        this.whitePlayerName = whitePlayer;
        this.blackPlayerName = blackPlayer;

        if (data != null && !data.isBlank()) {
            importGameState(data);
        }
    }

    /**
     * Obtém o nome do jogador das peças brancas.
     *
     * @return nome do jogador branco
     */
    public String getWhitePlayerName() {
        return whitePlayerName;
    }

    /**
     * Obtém o nome do jogador das peças pretas.
     *
     * @return nome do jogador preto
     */
    public String getBlackPlayerName() {
        return blackPlayerName;
    }

    /**
     * Define o nome do jogador das peças brancas.
     *
     * @param whitePlayerName novo nome para o jogador branco
     */
    public void setWhitePlayerName(String whitePlayerName) {
        this.whitePlayerName = whitePlayerName;
    }

    /**
     * Define o nome do jogador das peças pretas.
     *
     * @param blackPlayerName novo nome para o jogador preto
     */
    public void setBlackPlayerName(String blackPlayerName) {
        this.blackPlayerName = blackPlayerName;
    }

    /**
     * Obtém o nome do jogador atual.
     *
     * @return nome do jogador cuja vez é de jogar
     */
    public String getCurrentPlayerName() {
        return currentPlayer == PieceColor.WHITE ? whitePlayerName : blackPlayerName;
    }

    /**
     * Obtém a cor do jogador atual.
     *
     * @return cor do jogador atual
     */
    public PieceColor getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Obtém a representação em string da peça numa determinada posição.
     *
     * @param posStr posição no formato string (ex: "a1")
     * @return representação da peça ou null se não existir peça
     */
    public String getPieceAt(String posStr) {
        Position pos = Position.fromString(posStr);
        Piece piece = board.getPieceAt(pos);
        return piece == null ? null : piece.toString();
    }

    /**
     * Imprime o tabuleiro no console para depuração.
     */
    public void printBoard() {
        board.printDebug();
    }

    /**
     * Obtém a representação em string de uma peça pelo seu identificador.
     *
     * @param id identificador da peça
     * @return representação da peça ou null se não encontrada
     */
    public String getPieceById(String id) {
        Piece piece = board.getPieceById(id);
        return piece == null ? null : piece.toString();
    }

    /**
     * Obtém lista de movimentos possíveis para uma peça numa posição.
     *
     * @param posStr posição da peça no formato string
     * @return lista de posições válidas para movimento
     */
    public List<String> getPossibleMoves(String posStr) {
        Position pos = Position.fromString(posStr);
        if (pos == null)
            return null;

        Piece piece = board.getPieceAt(pos);
        if (piece == null)
            return null;

        List<Position> moves = piece.getPossibleMoves(board);
        List<String> moveStrings = new ArrayList<>();

        for (Position move : moves) {
            if (isLegalMove(piece, piece.getPosition(), move))
                moveStrings.add(move.toString());
        }

        return List.copyOf(moveStrings);
    }

    /**
     * Executa um movimento no jogo.
     *
     * @param fromStr posição de origem no formato string
     * @param toStr posição de destino no formato string
     * @return true se o movimento foi executado com sucesso
     */
    public boolean play(String fromStr, String toStr) {
        Position from = Position.fromString(fromStr);
        Position to = Position.fromString(toStr);
        if (from == null || to == null) return false;

        Piece piece = board.getPieceAt(from);
        if (piece == null) return false;
        if (piece.getColor() != currentPlayer) return false;

        MoveResult result;

        // Moves especiais
        if (piece instanceof King && isLegalMove(piece, from, to)) {
            King king = (King) piece;
            result = king.trySpecialMove(to, board);
        } else if (piece instanceof Pawn && isLegalMove(piece, from, to)) {
            Pawn pawn = (Pawn) piece;
            result = pawn.trySpecialMove(to, board);
        } else {
            result = tryRegularMove(piece, from, to);
        }

        return handleMoveResult(result, piece, from, to);
    }

    /**
     * Processa o resultado de um movimento e atualiza o estado do jogo.
     *
     * @param result resultado do movimento tentado
     * @param piece peça que foi movida
     * @param from posição de origem
     * @param to posição de destino
     * @return true se o movimento foi processado com sucesso
     */
    private boolean handleMoveResult(MoveResult result, Piece piece, Position from, Position to) {
        switch (result) {
            case SUCCESS -> {
                Piece capturedPiece = board.getPieceAt(to);
                if (capturedPiece != null)
                    board.removePiece(capturedPiece);

                board.movePiece(piece, to);
                board.setLastMove(from, to, piece);
                currentPlayer = currentPlayer.opposite();

                if (checkGameOver() != End_Type.CONTINUE) {
                    this.gameOver = true;
                }
                return true;
            }
            case CASTLE -> {
                King king = (King) piece;
                king.executeCastle(to, board);
                board.setLastMove(from, to, piece);
                currentPlayer = currentPlayer.opposite();
                if (checkGameOver() != End_Type.CONTINUE) {
                    this.gameOver = true;
                }
                return true;
            }
            case EN_PASSANT -> {
                Pawn pawn = (Pawn) piece;
                pawn.executeEnPassant(to, board);
                board.setLastMove(from, to, piece);
                currentPlayer = currentPlayer.opposite();
                if (checkGameOver() != End_Type.CONTINUE) {
                    this.gameOver = true;
                }
                return true;
            }
            case NOT_YOUR_TURN -> System.out.println("Não é a sua vez. Jogador atual: " + getCurrentPlayerName());
            case NO_PIECE -> System.out.println("Não existe peça na posição de origem.");
            case INVALID_MOVE -> System.out.println("Movimento inválido para esta peça.");
            case INVALID_POSITION -> System.out.println("Posição inválida.");
            case ILLEGAL_MOVE -> System.out.println("Movimento ilegal para esta peça.");
        }

        return false;
    }

    /**
     * Tenta executar um movimento regular (não especial).
     *
     * @param piece peça a mover
     * @param from posição de origem
     * @param to posição de destino
     * @return resultado da tentativa de movimento
     */
    private MoveResult tryRegularMove(Piece piece, Position from, Position to) {
        List<Position> possibleMoves = piece.getPossibleMoves(board);
        if (!possibleMoves.contains(to))
            return MoveResult.INVALID_MOVE;

        if (!isLegalMove(piece, from, to))
            return MoveResult.ILLEGAL_MOVE;

        return MoveResult.SUCCESS;
    }

    /**
     * Verifica se um jogador está em xeque-mate.
     *
     * @param color cor do jogador a verificar
     * @return true se o jogador está em xeque-mate
     */
    public boolean isCheckmate(PieceColor color) {
        if (!isKingInCheck(color)) {
            return false; // Não está em check, logo não é checkmate
        }
        return !hasLegalMoves(color);
    }

    /**
     * Verifica se um jogador está em empate por afogamento.
     *
     * @param color cor do jogador a verificar
     * @return true se o jogador está em empate por afogamento
     */
    public boolean isStalemate(PieceColor color) {
        if (isKingInCheck(color)) {
            return false; // Está em check, logo não pode ser empate por afogamento
        }
        return !hasLegalMoves(color);
    }

    /**
     * Verifica se o rei de uma cor está em xeque.
     *
     * @param color cor do rei a verificar
     * @return true se o rei está em xeque
     */
    public boolean isKingInCheck(PieceColor color) {
        Position kingPos = board.findKingPosition(color);
        if (kingPos == null)
            return false;

        return board.isPositionUnderAttack(kingPos, color.opposite());
    }

    /**
     * Verifica se um jogador tem movimentos legais disponíveis.
     *
     * @param color cor do jogador a verificar
     * @return true se o jogador tem pelo menos um movimento legal
     */
    private boolean hasLegalMoves(PieceColor color) {
        for (Piece piece : board.getPiecesOfColor(color)) {
            List<Position> possibleMoves = piece.getPossibleMoves(board);

            for (Position to : possibleMoves) {
                if (isLegalMove(piece, piece.getPosition(), to)) {
                    return true; // existe pelo menos 1 movimento legal
                }
            }
        }
        return false;
    }

    /**
     * Verifica se um movimento é legal (não deixa o próprio rei em xeque).
     *
     * @param piece peça a mover
     * @param from posição de origem
     * @param to posição de destino
     * @return true se o movimento é legal
     */
    public boolean isLegalMove(Piece piece, Position from, Position to) {
        Piece captured = board.getPieceAt(to);
        // Simula jogada
        board.removePiece(piece);
        Piece tempCaptured = null;
        if (captured != null) {
            tempCaptured = captured;
            board.removePiece(captured);
        }
        board.placePiece(piece, to);

        boolean isCheck = isKingInCheck(piece.getColor());

        // Desfaz jogada
        board.removePiece(piece);
        if (tempCaptured != null)
            board.placePiece(tempCaptured, to);
        board.placePiece(piece, from);

        return !isCheck;
    }

    /**
     * Promove um peão numa determinada posição.
     *
     * @param posStr posição do peão no formato string
     * @param promotionType tipo de peça para promoção
     */
    public void promotePawnAt(String posStr, String promotionType) {
        Position position = Position.fromString(posStr);
        Piece piece = board.getPieceAt(position);

        if (piece instanceof Pawn) {
            Pawn pawn = (Pawn) piece;
            pawn.promote(promotionType, board);
        }
    }

    /**
     * Verifica se o jogo terminou.
     *
     * @return true se o jogo terminou
     */
    public boolean isGameEnded() {
        return gameOver;
    }

    /**
     * Obtém a cor do vencedor.
     *
     * @return cor do vencedor ou null se empate/jogo não terminou
     */
    public PieceColor getWinner() {
        return winner;
    }

    /**
     * Verifica o estado de fim de jogo.
     *
     * @return tipo de fim de jogo (xeque-mate, empate ou continuar)
     */
    public End_Type checkGameOver() {
        if (isCheckmate(currentPlayer)) {
            this.winner = currentPlayer.opposite();
            String winnerName = winner == PieceColor.WHITE ? whitePlayerName : blackPlayerName;
            return End_Type.CHECKMATE;
        } else if (isStalemate(currentPlayer)) {
            this.winner = null;
            return End_Type.STALEMATE;
        }
        return End_Type.CONTINUE;
    }

    /**
     * Exporta o estado atual do jogo para formato string.
     *
     * @return estado do jogo em formato CSV
     */
    public String exportGameState() {
        StringBuilder sb = new StringBuilder();

        // cor do proximo jogador
        sb.append(currentPlayer.name());
        sb.append(",");

        // id's das peças
        for (Piece piece : board.getAllPieces()) {
            sb.append(piece.toString()).append(",");
        }

        if (sb.charAt(sb.length() - 1) == ',')
            sb.setLength(sb.length() - 1); // Remover ultima virgula
        return sb.toString();
    }

    /**
     * Importa o estado do jogo a partir de dados em formato string.
     *
     * @param data dados do jogo em formato CSV
     */
    public void importGameState(String data) throws IllegalArgumentException {
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("Dados do ficheiro estão vazios ou inválidos.");
        }

        String[] parts = data.strip().split(",");
        if (!parts[0].equalsIgnoreCase("WHITE") && !parts[0].equalsIgnoreCase("BLACK")) {
            throw new IllegalArgumentException("Cor de jogador inválida: " + parts[0]);
        }

        try {
            this.currentPlayer = PieceColor.valueOf(parts[0].trim().toUpperCase());
            board.clear();

            for (int i = 1; i < parts.length; i++) {
                String pieceStr = parts[i].trim();
                if (pieceStr.isEmpty()) continue;

                try {
                    Piece piece = PieceFactory.createPiece(pieceStr);
                    board.placePiece(piece, piece.getPosition());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Peça inválida: " + pieceStr, e);
                }
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Erro ao interpretar dados do jogo.", e);
        }
    }


    /**
     * Guarda o jogo num ficheiro.
     *
     * @param filename nome do ficheiro
     */
    public void saveToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            String export = exportGameState();
            writer.write(export);
        } catch (Exception e) {
            System.err.println("Erro ao salvar o jogo: " + e.getMessage());
        }
    }

    /**
     * Carrega o jogo a partir de um ficheiro.
     *
     * @param filename nome do ficheiro
     */
    public void loadFromFile(String filename) throws IllegalArgumentException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }

            if (!content.isEmpty()) {
                importGameState(content.toString()); // pode lançar exceção
            } else {
                throw new IllegalArgumentException("Ficheiro está vazio.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Erro ao ler o ficheiro: " + filename, e);
        }
    }

    /**
     * Carrega o jogo a partir de um ficheiro definindo nomes dos jogadores.
     *
     * @param filename nome do ficheiro
     * @param whitePlayer nome do jogador branco
     * @param blackPlayer nome do jogador preto
     */
    public void loadFromFile(String filename, String whitePlayer, String blackPlayer) throws IllegalArgumentException {
        loadFromFile(filename);
        this.whitePlayerName = whitePlayer;
        this.blackPlayerName = blackPlayer;
    }

    /**
     * Obtém o tamanho do tabuleiro.
     *
     * @return tamanho do tabuleiro
     */
    public int getBoardSize(){return board.getBoardSize();}
}