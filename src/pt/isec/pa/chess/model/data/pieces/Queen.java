package pt.isec.pa.chess.model.data.pieces;

import pt.isec.pa.chess.model.data.Board;
import pt.isec.pa.chess.model.data.Position;

public class Queen extends Piece {
    public Queen(PieceColor color, char col, int row) {
        super(color, PieceType.QUEEN, col, row);
    }

    @Override
    public int isValidMove(Position newPosition, Board board) {
        int rowDiff = newPosition.getRow() - getPosition().getRow();
        int colDiff = newPosition.getCol() - getPosition().getCol();

        // A rainha pode mover-se em linha reta ou na diagonal
        if (!(rowDiff == 0 || colDiff == 0 || Math.abs(rowDiff) == Math.abs(colDiff))) {
            return 0;
        }
        if (board.getPieceAt(newPosition) == null) return 2; // casa livre pode verificar a seguinte na direçao
        if (board.getPieceAt(newPosition).getColor() != getColor()) return 1; // pode capturar peça
        return 0; // movimento invalido
    }

    @Override
    protected int[][] getMoveOffsets() {
        return new int[][]{
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}, // Torre
                {1, 1}, {-1, -1}, {1, -1}, {-1, 1} // Bispo
        };
    }
}
