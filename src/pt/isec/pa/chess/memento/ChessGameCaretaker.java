package pt.isec.pa.chess.memento;

import pt.isec.pa.chess.model.ModelLog;
import pt.isec.pa.chess.model.data.ChessGame;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class ChessGameCaretaker {
    private final Deque<ChessGameMemento> undoHistory;
    private final Deque<ChessGameMemento> redoHistory;
    private final int maxHistorySize;

    public ChessGameCaretaker() {
        this.undoHistory = new ArrayDeque<>();
        this.redoHistory = new ArrayDeque<>();
        this.maxHistorySize = 100;
    }

    public void save(ChessGame game) { // chamado antes de cada move
        redoHistory.clear();

        if (undoHistory.size() >= maxHistorySize) { // caso o jogo seja muito comprido
            undoHistory.removeLast();
        }

        try {
            undoHistory.push(new ChessGameMemento(game));
        } catch (IOException e) {
            ModelLog.getInstance().addLog("Error saving game state: " + e.getMessage());
            System.err.println("Error saving game state: " + e.getMessage());
        }
    }

    public ChessGame undo() {
        if (!canUndo()) {
            return null;
        }

        try {
            ChessGameMemento current = undoHistory.pop();
            ChessGameMemento previous = undoHistory.peek();
            redoHistory.push(current);
            return previous != null ? previous.getState() : null;
        } catch (IOException | ClassNotFoundException e) {
            ModelLog.getInstance().addLog("Error restoring game state: " + e.getMessage());
            System.err.println("Error restoring game state: " + e.getMessage());
            return null;
        }
    }

    public ChessGame redo() {
        if (!canRedo()) {
            return null;
        }

        try {
            ChessGameMemento next = redoHistory.pop();
            undoHistory.push(next);
            return next.getState();
        } catch (IOException | ClassNotFoundException e) {
            ModelLog.getInstance().addLog("Error restoring game state: " + e.getMessage());
            System.err.println("Error restoring game state: " + e.getMessage());
            return null;
        }
    }

    public boolean canUndo() {
        return undoHistory.size() > 1;
    }
    public boolean canRedo() {
        return !redoHistory.isEmpty();
    }
    public void clear() {
        undoHistory.clear();
        redoHistory.clear();
    }

    public void initialize(ChessGame game) {
        clear();
        save(game);
    }
}