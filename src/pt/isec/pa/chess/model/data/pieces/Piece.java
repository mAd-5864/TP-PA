package pt.isec.pa.chess.model.data.pieces;

import pt.isec.pa.chess.model.data.Board;
import pt.isec.pa.chess.model.data.Position;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Piece implements Serializable {
    private static final long serialVersionUID = 1L;

    private final PieceColor color;
    private final PieceType type;
    private boolean hasMoved;
    private Position position;
    private List<Position> possibleMoves;
    private final String id;

    public Piece(PieceColor color, PieceType type, char col, int row) {
        this.color = color;
        this.type = type;
        this.hasMoved = false;
        this.position = new Position(col, row);
        // Definir ID
        char c = type.toString().charAt(0);
        this.id = (color == PieceColor.WHITE ? c : Character.toLowerCase(c)) + "" + col + row;
    }

    public PieceColor getColor() { return color; }
    public String getType() { return type.name(); }
    public boolean hasMoved() { return hasMoved; }
    public void setHasMoved() { this.hasMoved = true; }
    public String getId() { return id; }

    public Position getPosition() { return position; }

    public void setPosition(Position newPosition) {
        this.position = newPosition;
    }

    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();

        int[][] moveOffsets = getMoveOffsets(); // offsets possiveis de cada peça

        for (int[] offset : moveOffsets) {
            char newCol = position.getCol();
            int newRow = position.getRow();

            // Loop in the direction until blocked
            while (true) {
                newCol = (char) (newCol + offset[0]);
                newRow = newRow + offset[1];

                if (newCol < 'a' || newCol > 'h' || newRow < 1 || newRow > 8)
                    break;

                Position newPosition = new Position(newCol, newRow);
                Piece targetPiece = board.getPieceAt(newPosition);

                if (isValidMove(newPosition, board) ==1){
                    moves.add(newPosition);
                    break;
                } else if(isValidMove(newPosition, board)==2) {
                    moves.add(newPosition);
                } else break;
            }
        }
        return moves;
    }

//    @Override
//    public String toString() {
//        return color.name() + " " + type.name()+ " at " + position.toString();
//    }
    @Override
    public String toString() {
        char c = type.toString().charAt(0);
        return  (color == PieceColor.WHITE ? c : Character.toLowerCase(c)) + "" + position.getCol() + position.getRow();
    }

    public static Piece fromString(String data) {
        if (data.length() < 3)
            return null;

        char idChar = data.charAt(0);
        PieceColor color = Character.isUpperCase(idChar) ? PieceColor.WHITE : PieceColor.BLACK;
        PieceType type = PieceType.fromChar(Character.toUpperCase(idChar));

        String posStr = data.substring(1);
        boolean hasMoved = posStr.contains("*");
        posStr = posStr.replace("*", "");

        Position position = Position.fromString(posStr);
        if (position == null || type == null)
            return null;

        return PieceFactory.createPiece(type, color, position.getCol(), position.getRow() );
    }

    //    0 - false
    //    1 - true
    //    2 - long range (allow recursion)
    public abstract int isValidMove(Position newPosition, Board board);
    protected abstract int[][] getMoveOffsets();

}