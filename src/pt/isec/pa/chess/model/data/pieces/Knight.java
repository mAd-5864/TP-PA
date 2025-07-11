package pt.isec.pa.chess.model.data.pieces;

import pt.isec.pa.chess.model.data.Board;
import pt.isec.pa.chess.model.data.Position;

public class Knight extends Piece {
    public Knight(PieceColor color, char col, int row) {
        super(color, PieceType.KNIGHT, col, row);
    }

    public int isValidMove(Position newPosition, Board board) {
        Position current = getPosition();
        Piece targetPiece = board.getPieceAt(newPosition);

        int colDiff = Math.abs(newPosition.getCol() - current.getCol());
        int rowDiff = Math.abs(newPosition.getRow() - current.getRow());

        // O movimento do cavalo é sempre 2x1 ou 1x2
        boolean isValidLMove = (colDiff == 2 && rowDiff == 1) || (colDiff == 1 && rowDiff == 2);

        // Se a casa estiver vazia, pode mover; se estiver ocupada, só pode se for peça inimiga
        if (isValidLMove && (targetPiece == null || targetPiece.getColor() != getColor())) return 1;
        return 0;
    }

    @Override
    protected int[][] getMoveOffsets() {
        return new int[][]{
                {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
                {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };
    }
}
