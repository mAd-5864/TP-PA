package pt.isec.pa.chess.model.data.pieces;

public enum PieceType {
    ROOK, BISHOP, QUEEN, KNIGHT, PAWN, KING;

    @Override
    public String toString() {
        return switch (this) {
            case KING -> "K";
            case QUEEN -> "Q";
            case ROOK -> "R";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case PAWN -> "P";
        };
    }

    public static PieceType fromChar(char c) {
        return switch (c) {
            case 'K' -> KING;
            case 'Q' -> QUEEN;
            case 'R' -> ROOK;
            case 'B' -> BISHOP;
            case 'N' -> KNIGHT;
            case 'P' -> PAWN;
            default -> null;
        };
    }
}