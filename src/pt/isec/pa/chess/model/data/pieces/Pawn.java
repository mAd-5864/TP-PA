package pt.isec.pa.chess.model.data.pieces;

import pt.isec.pa.chess.model.data.Board;
import pt.isec.pa.chess.model.data.MoveResult;
import pt.isec.pa.chess.model.data.Position;
import java.util.List;

public class Pawn extends Piece {
    public Pawn(PieceColor color, char col, int row) {
        super(color, PieceType.PAWN, col, row);
    }

    @Override
    public int isValidMove(Position newPosition, Board board) {
        int direction = (getColor() == PieceColor.WHITE) ? 1 : -1; // branco sobe, preto desce
        int rowDiff = newPosition.getRow() - getPosition().getRow();
        int colDiff = Math.abs(newPosition.getCol() - getPosition().getCol());
        Piece targetPiece = board.getPieceAt(newPosition);

        // Movimento normal de uma casa para frente
        if (colDiff == 0 && rowDiff == direction) {
            if (targetPiece == null) return 1;
        }

        // Movimento inicial de duas casas para frente
        if (!hasMoved() && colDiff == 0 && rowDiff == 2 * direction) {
            Position primeiraCasa = new Position(getPosition().getCol(), getPosition().getRow() + direction);
            if (board.getPieceAt(primeiraCasa) == null && targetPiece == null) return 1;
        }

        // Captura diagonal
        if (colDiff == 1 && rowDiff == direction) {
            if (targetPiece != null && targetPiece.getColor() != getColor()) return 1;
        }

        return 0;
    }

    @Override
    protected int[][] getMoveOffsets() {
        if (getColor() == PieceColor.WHITE) {
            return new int[][]{{0, 1}, {0, 2}, {-1, 1}, {1, 1}};
        } else {
            return new int[][]{{0, -1}, {0, -2}, {-1, -1}, {1, -1}};
        }
    }

    @Override
    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = super.getPossibleMoves(board);

        int direction = (getColor() == PieceColor.WHITE) ? 1 : -1;
        if (getPosition().getRow() + direction > 8 || getPosition().getRow() + direction < 1) return moves;

        // en passant esquerda
        if (getPosition().getCol() > 'a') {
            Position leftEnPassant = new Position(
                    (char)(getPosition().getCol() - 1),
                    getPosition().getRow() + direction
            );
            if (leftEnPassant.isValid()) {
                MoveResult leftResult = tryEnPassant(leftEnPassant, board);
                if (leftResult == MoveResult.EN_PASSANT) {
                    moves.add(leftEnPassant);
                }
            }
        }

        // en passant direita
        if (getPosition().getCol() < 'h') {
            Position rightEnPassant = new Position(
                    (char)(getPosition().getCol() + 1),
                    getPosition().getRow() + direction
            );
            if (rightEnPassant.isValid()) {
                MoveResult rightResult = tryEnPassant(rightEnPassant, board);
                if (rightResult == MoveResult.EN_PASSANT) {
                    moves.add(rightEnPassant);
                }
            }
        }

        return moves;
    }

    public MoveResult trySpecialMove(Position to, Board board) {
        MoveResult enPassantResult = tryEnPassant(to, board);
        if (enPassantResult == MoveResult.EN_PASSANT) {
            return enPassantResult;
        }

        List<Position> possibleMoves = getPossibleMoves(board);
        if (!possibleMoves.contains(to))
            return MoveResult.INVALID_MOVE;

        return MoveResult.SUCCESS;
    }

    public MoveResult tryEnPassant(Position to, Board board) {
        // Verificar ultimo movimento (en passant só pode ser jogado logo após o peao adversario mover 2 casas)
        Piece lastMovedPiece = board.getLastMovedPiece();
        Position lastMoveFrom = board.getLastMoveFrom();
        Position lastMoveTo = board.getLastMoveTo();

        if (lastMovedPiece == null || !(lastMovedPiece instanceof Pawn)) return MoveResult.INVALID_MOVE;
        if (Math.abs(lastMoveTo.getRow() - lastMoveFrom.getRow()) != 2) return MoveResult.INVALID_MOVE;

        // Peao deve estar na linha 5 - brancas, 4 - pretas
        int expectedRank = (getColor() == PieceColor.WHITE) ? 5 : 4;
        if (getPosition().getRow() != expectedRank) return MoveResult.INVALID_MOVE;

        // Verificar se peoes estao lado a lado
        if (Math.abs(getPosition().getCol() - lastMovedPiece.getPosition().getCol()) != 1) return MoveResult.INVALID_MOVE;
        if (getPosition().getRow() != lastMovedPiece.getPosition().getRow()) return MoveResult.INVALID_MOVE;

        // Verificar direção
        int direction = (getColor() == PieceColor.WHITE) ? 1 : -1;
        Position expectedTo = new Position(
                (char) lastMovedPiece.getPosition().getCol(),
                getPosition().getRow() + direction
        );

        if (!to.equals(expectedTo)) return MoveResult.INVALID_MOVE;
        if (board.getPieceAt(to) != null) return MoveResult.INVALID_MOVE;

        return MoveResult.EN_PASSANT;
    }

    public void executeEnPassant(Position to, Board board) {
        Piece capturedPawn = board.getLastMovedPiece();
        board.removePiece(capturedPawn);
        board.movePiece(this, to);
    }

    public void promote(String promotionType, Board board) {
        Position position = getPosition();

        Piece promoted = switch (promotionType.toUpperCase()) {
            case "ROOK" -> new Rook(getColor(), position.getCol(), position.getRow());
            case "BISHOP" -> new Bishop(getColor(), position.getCol(), position.getRow());
            case "KNIGHT" -> new Knight(getColor(), position.getCol(), position.getRow());
            default -> new Queen(getColor(), position.getCol(), position.getRow());
        };

        board.removePiece(this);
        board.placePiece(promoted, position);
    }

    public boolean canPromote() {
        int promotionRank = (getColor() == PieceColor.WHITE) ? 8 : 1;
        return getPosition().getRow() == promotionRank;
    }
}