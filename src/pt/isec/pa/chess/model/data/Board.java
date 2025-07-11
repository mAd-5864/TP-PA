package pt.isec.pa.chess.model.data;

import pt.isec.pa.chess.model.data.pieces.*;

import java.io.Serializable;
import java.util.*;


public class Board implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<Position, Piece> board;  // Mapa de peças  com respetivo ID
    private int boardSize;
    private Position lastMoveFrom;
    private Position lastMoveTo;
    private Piece lastMovedPiece;

    public Board() {
        this(8); //tamanho default - 8x8
    }

    public Board(int size) {
        if (size < 1)
            throw new IllegalArgumentException("[ERROR] Board size");
        this.boardSize = size;
        this.board = new HashMap<>();
        initializeBoard();
    }

    public Piece getPieceAt(Position pos) {
        if (pos == null || !pos.isValid())
            return null;
        return board.get(pos);
    }

    public Piece getPieceById(String id) {
        for (Piece piece : board.values()) {
            if (piece.getId().equals(id))
                return piece;
        }
        return null;
    }

    public Collection<Piece> getAllPieces() {
        return new ArrayList<>(board.values()); // Return a copy of the values in board
    }

    public Collection<Piece> getPiecesOfColor(PieceColor color) {
        return board.values().stream()
                .filter(piece -> piece.getColor() == color)
                .toList();
    }

    public void clear() {
        board.clear();
    }

    private void initializeBoard() {
        //Linha 8 - Peças Pretas
        placePiece(PieceFactory.createPiece("ra" + boardSize));
        placePiece(PieceFactory.createPiece("nb" + boardSize));
        placePiece(PieceFactory.createPiece("bc" + boardSize));
        placePiece(PieceFactory.createPiece("qd" + boardSize));
        placePiece(PieceFactory.createPiece("ke" + boardSize));
        placePiece(PieceFactory.createPiece("bf" + boardSize));
        placePiece(PieceFactory.createPiece("ng" + boardSize));
        placePiece(PieceFactory.createPiece("rh" + boardSize));

        //Linha 7 e 2 - Peões
        for (int i = 0; i < 8; i++) {
            char col = (char) ('a' + i);
            placePiece(PieceFactory.createPiece("p" + col + (boardSize - 1)));
            placePiece(PieceFactory.createPiece("P" + col + "2"));
        }

        //Linha 1 - Peças Brancas
        placePiece(PieceFactory.createPiece("Ra1"));
        placePiece(PieceFactory.createPiece("Nb1"));
        placePiece(PieceFactory.createPiece("Bc1"));
        placePiece(PieceFactory.createPiece("Qd1"));
        placePiece(PieceFactory.createPiece("Ke1"));
        placePiece(PieceFactory.createPiece("Bf1"));
        placePiece(PieceFactory.createPiece("Ng1"));
        placePiece(PieceFactory.createPiece("Rh1"));
    }

    public void printDebug() {
        System.out.println("DEBUG - Estado do Tabuleiro:");
        for (int row = boardSize; row >= 1; row--) {
            System.out.print(row + "  ");
            for (char col = 'a'; col <= 'h'; col++) {
                Position pos = new Position(col, row);
                Piece piece = board.get(pos);
                System.out.print((piece == null ? " . " : piece.getId()) + " ");
            }
            System.out.println();
        }
        System.out.println("    a   b   c   d   e   f   g   h\n");
    }

    public void placePiece(Piece piece) {
        if (piece == null || piece.getPosition() == null || !piece.getPosition().isValid())
            throw new IllegalArgumentException("Invalid piece or position");

        board.put(piece.getPosition(), piece);
    }

    public void placePiece(Piece piece, Position pos) {
        if (piece == null || pos == null || !pos.isValid())
            throw new IllegalArgumentException("Invalid piece or position");

        piece.setPosition(pos);
        board.put(pos, piece);
    }

    public void removePiece(Piece piece) {
        if (piece == null || piece.getPosition() == null || !piece.getPosition().isValid())
            throw new IllegalArgumentException("Invalid piece or position");

        board.remove(piece.getPosition());
    }

    public Piece removePiece(Position pos) {
        if (pos == null || !pos.isValid())
            return null;
        return board.remove(pos);
    }

    public void movePiece(Piece piece, Position newPos) {
        if (piece == null || newPos == null || !newPos.isValid())
            throw new IllegalArgumentException("Invalid piece or position");

        board.remove(piece.getPosition());
        piece.setPosition(newPos);
        board.put(newPos, piece);
        piece.setHasMoved();
    }

    public boolean isEmpty(Position pos) {
        return getPieceAt(pos) == null;
    }

    public boolean isOccupiedBy(Position pos, PieceColor color) {
        Piece piece = getPieceAt(pos);
        return piece != null && piece.getColor() == color;
    }

    public Position findKingPosition(PieceColor color) {
        for (Piece piece : getAllPieces()) {
            if (piece instanceof pt.isec.pa.chess.model.data.pieces.King &&
                    piece.getColor() == color) {
                return piece.getPosition();
            }
        }
        return null;
    }

    public boolean isPositionUnderAttack(Position pos, PieceColor attackingColor) {
        for (Piece piece : getAllPieces()) {
            if (piece.getColor() == attackingColor) {
                if (piece instanceof King) {
                    List<Position> basicMoves = ((King) piece).getBasicMoves(this);
                    if (basicMoves.contains(pos)) {
                        return true;
                    }
                } else {
                    List<Position> moves = piece.getPossibleMoves(this);
                    if (moves.contains(pos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isClearPath(Position from, Position to) {
        if (from.getRow() != to.getRow()) return false;

        int startCol = Math.min(from.getCol(), to.getCol()) + 1;
        int endCol = Math.max(from.getCol(), to.getCol());

        for (int col = startCol; col < endCol; col++) {
            Position pos = new Position((char) col, from.getRow());
            if (getPieceAt(pos) != null)
                return false;
        }
        return true;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public void setLastMove(Position from, Position to, Piece piece) {
        this.lastMoveFrom = from;
        this.lastMoveTo = to;
        this.lastMovedPiece = piece;
    }

    public Position getLastMoveFrom() {
        return lastMoveFrom;
    }

    public Position getLastMoveTo() {
        return lastMoveTo;
    }

    public Piece getLastMovedPiece() {
        return lastMovedPiece;
    }
}