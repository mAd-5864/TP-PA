package pt.isec.pa.chess.model.data;

public enum MoveResult {
    SUCCESS,
    INVALID_POSITION,
    NO_PIECE,
    NOT_YOUR_TURN,
    INVALID_MOVE,
    ILLEGAL_MOVE,
    CASTLE,
    PROMOTION,
    EN_PASSANT,
    CHECK
}
