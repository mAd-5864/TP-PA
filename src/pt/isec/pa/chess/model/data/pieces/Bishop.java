package pt.isec.pa.chess.model.data.pieces;

import pt.isec.pa.chess.model.data.Board;
import pt.isec.pa.chess.model.data.Position;

public class Bishop extends Piece {
    public Bishop(PieceColor color, char col, int row) {
        super(color, PieceType.BISHOP, col, row);
    }

    @Override
    public int isValidMove(Position newPosition, Board board) {
        int rowDiff = Math.abs(newPosition.getRow() - this.getPosition().getRow());
        int colDiff = Math.abs(newPosition.getCol() - this.getPosition().getCol());

        if (rowDiff == colDiff) { // Mexe na diagonal
            if (board.getPieceAt(newPosition) == null) return 2;
            if (board.getPieceAt(newPosition).getColor() != getColor()) return 1;
        }
        return 0;
    }

    @Override
    protected int[][] getMoveOffsets() {
        return new int[][]{
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1} // 4 diagonal directions
        };
    }
}
