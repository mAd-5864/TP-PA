package pt.isec.pa.chess.model.data.pieces;

import pt.isec.pa.chess.model.data.Board;
import pt.isec.pa.chess.model.data.MoveResult;
import pt.isec.pa.chess.model.data.Position;
import java.util.List;

public class King extends Piece {
    public King(PieceColor color, char col, int row) {
        super(color, PieceType.KING, col, row);
    }

    @Override
    public int isValidMove(Position newPosition, Board board) {
        int rowDiff = newPosition.getRow() - getPosition().getRow();
        int colDiff = newPosition.getCol() - getPosition().getCol();

        if (!(rowDiff == 0 || colDiff == 0 || Math.abs(rowDiff) == Math.abs(colDiff))) {
            return 0;
        }
        if (board.getPieceAt(newPosition) == null || board.getPieceAt(newPosition).getColor() != getColor()) return 1;
        return 0;
    }

    @Override
    protected int[][] getMoveOffsets() {
        return new int[][]{
                {1, 0}, {-1, 0}, {0, 1}, {0, -1}, // Torre
                {1, 1}, {-1, -1}, {1, -1}, {-1, 1} // Bispo
        };
    }

    @Override
    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = super.getPossibleMoves(board);

        if (!hasMoved()) {
            // Kingside castle
            Position kingsideRook = new Position('h', getPosition().getRow());
            Position kingsideDest = new Position('g', getPosition().getRow());
            if (canCastle(kingsideRook, board)) {
                moves.add(kingsideDest);
            }

            // Queenside castle
            Position queensideRook = new Position('a', getPosition().getRow());
            Position queensideDest = new Position('c', getPosition().getRow());
            if (canCastle(queensideRook, board)) {
                moves.add(queensideDest);
            }
        }

        return moves;
    }

    public List<Position> getBasicMoves(Board board) {
        return super.getPossibleMoves(board);
    }

    public MoveResult trySpecialMove(Position to, Board board) {
        Piece target = board.getPieceAt(to);

        if (target instanceof Rook && getColor() == target.getColor()) {
            return tryCastling(to, board);
        }

        // Rei mexe 2 casas
        if (Math.abs(to.getCol() - getPosition().getCol()) > 1) {
            char rookCol = (to.getCol() == 'g') ? 'h' : 'a';
            Position rookPos = new Position(rookCol, getPosition().getRow());
            Piece rook = board.getPieceAt(rookPos);

            if (rook instanceof Rook && rook.getColor() == getColor()) {
                return tryCastling(rookPos, board);
            }
        }

        List<Position> possibleMoves = getPossibleMoves(board);
        if (!possibleMoves.contains(to))
            return MoveResult.INVALID_MOVE;

        return MoveResult.SUCCESS;
    }

    private boolean canCastle(Position rookPos, Board board) {
        Piece rook = board.getPieceAt(rookPos);
        if (!(rook instanceof Rook) || rook.getColor() != getColor() || rook.hasMoved()) return false;

        if (!board.isClearPath(getPosition(), rookPos)) return false;
        if (board.isPositionUnderAttack(getPosition(), getColor().opposite())) return false; // is king in check

        boolean isKingside = rookPos.getCol() > getPosition().getCol();
        if (isKingside) {
            Position f = new Position('f', getPosition().getRow());
            Position g = new Position('g', getPosition().getRow());

            // Check if squares are empty or in check
            if (board.getPieceAt(f) != null || board.getPieceAt(g) != null) return false;

            if (board.isPositionUnderAttack(f, this.getColor().opposite()) ||
                    board.isPositionUnderAttack(g, this.getColor().opposite())) return false;
        } else {
            Position b = new Position('b', getPosition().getRow());
            Position c = new Position('c', getPosition().getRow());
            Position d = new Position('d', getPosition().getRow());

            // Check if squares are empty and in check
            if (board.getPieceAt(b) != null ||
                    board.getPieceAt(c) != null ||
                    board.getPieceAt(d) != null) return false;

            if (board.isPositionUnderAttack(c, getColor().opposite()) ||
                    board.isPositionUnderAttack(d, getColor().opposite())) return false;
        }

        return true;
    }

    public MoveResult tryCastling(Position rookPos, Board board) {
        Piece rook = board.getPieceAt(rookPos);
        if (rook == null || !(rook instanceof Rook)) return MoveResult.INVALID_MOVE;
        if (getColor() != rook.getColor()) return MoveResult.INVALID_MOVE;
        if (hasMoved() || rook.hasMoved()) return MoveResult.ILLEGAL_MOVE;
        if (!canCastle(rookPos, board)) return MoveResult.ILLEGAL_MOVE;

        return MoveResult.CASTLE;
    }

    public void executeCastle(Position rookPos, Board board) {
        boolean isKingside = rookPos.getCol() > getPosition().getCol();

        Position kingTargetPos;
        Position rookTargetPos;
        Position actualRookPos;

        if (isKingside) { // King-side castle
            actualRookPos = new Position('h', getPosition().getRow());
            kingTargetPos = new Position('g', getPosition().getRow());
            rookTargetPos = new Position('f', getPosition().getRow());
        } else { // Queen-side castle
            actualRookPos = new Position('a', getPosition().getRow());
            kingTargetPos = new Position('c', getPosition().getRow());
            rookTargetPos = new Position('d', getPosition().getRow());
        }

        Piece rook = board.getPieceAt(actualRookPos);

        board.movePiece(this, kingTargetPos);
        board.movePiece(rook, rookTargetPos);
    }
}