package pt.isec.pa.chess.model.data.pieces;

public class PieceFactory {

    //Criar uma peça baseado no seu tipo
    public static Piece createPiece(PieceType type, PieceColor color, char col, int row) {
        return switch (type) {
            case KING -> new King(color, col, row);
            case QUEEN -> new Queen(color, col, row);
            case ROOK -> new Rook(color, col, row);
            case BISHOP -> new Bishop(color, col, row);
            case KNIGHT -> new Knight(color, col, row);
            case PAWN -> new Pawn(color, col, row);
        };
    }

    //Criar uma peça baseada na String do tipo "Bc1"
    public static Piece createPiece(String pieceStr) {

        if (pieceStr == null || pieceStr.length() < 3)
            throw new IllegalArgumentException("Invalid piece format");

        //Partir a string
        char type = pieceStr.charAt(0);
        char col = pieceStr.charAt(1);
        int row = Character.getNumericValue(pieceStr.charAt(2));

        PieceColor color = Character.isUpperCase(type) ? PieceColor.WHITE : PieceColor.BLACK;
        return switch (Character.toUpperCase(type)) {
            case 'K' -> new King(color, col, row);
            case 'Q' -> new Queen(color, col, row);
            case 'R' -> new Rook(color, col, row);
            case 'B' -> new Bishop(color, col, row);
            case 'N' -> new Knight(color, col, row);
            case 'P' -> new Pawn(color, col, row);
            default -> throw new IllegalArgumentException("Unknown piece type");
        };
    }
}
