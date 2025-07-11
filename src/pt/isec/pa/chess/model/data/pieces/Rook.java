package pt.isec.pa.chess.model.data.pieces;

import pt.isec.pa.chess.model.data.Board;
import pt.isec.pa.chess.model.data.Position;

public class Rook extends Piece {
    public Rook(PieceColor color, char col, int row) {
        super(color, PieceType.ROOK, col, row);
    }

    @Override
    public int isValidMove(Position newPosition, Board board) {
        int rowDiff = Math.abs(newPosition.getRow() - this.getPosition().getRow());
        int colDiff = Math.abs(newPosition.getCol() - this.getPosition().getCol());

        if ((rowDiff > 0 && colDiff == 0) || (rowDiff == 0 && colDiff > 0)) { // Mexe nas linhas e colunas
            if (board.getPieceAt(newPosition) == null) return 2;
            if (board.getPieceAt(newPosition).getColor() != getColor()) return 1;
        }
        return 0;
    }

    @Override
    protected int[][] getMoveOffsets() {
        return new int[][]{
                {1, 0}, {-1, 0}, {0, 1}, {0, -1} // 4 horizontal e vertical
        };
    }
}
