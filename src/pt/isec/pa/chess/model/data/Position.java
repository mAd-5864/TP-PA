package pt.isec.pa.chess.model.data;

import java.io.Serializable;
import java.util.Objects;

public class Position implements Serializable {
    private static final long serialVersionUID = 1L;

    private char col; // A-H
    private int row; // 1-8

    public Position(char col, int row) {
        if (col < 'a' || col > 'h' || row < 1 || row > 8) {
            throw new IllegalArgumentException("Posição inválida: " + col + row);
        }
        this.col = col;
        this.row = row;
    }

    public char getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Position position = (Position) obj;
        return row == position.row && col == position.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "" + col + row;
    }

    public static Position fromString(String pos) {
        if (pos == null || pos.length() != 2) {
            return null;
        }

        char col = pos.charAt(0);
        char rowChar = pos.charAt(1);

        if (col < 'a' || col > 'h' || rowChar < '1' || rowChar > '8') {
            return null; // Posição inválida
        }

        int row = Character.getNumericValue(rowChar); // Converte '1'-'8' para inteiro

        return new Position(col, row);
    }

    public boolean isValid() {
        return !(col < 'a' || col > 'h' || row < 1 || row > 8);
    }
}
