package Checkers;

import java.util.Stack;

/**
 * Represents the logical state and rules of a Checkers game.
 * Handles piece movement, captures, king promotion, undo/redo,
 * and win detection.
 */
public class CheckersBoard {

    public static final int SIZE = 8;

    public Piece[][] board = new Piece[SIZE][SIZE];
    public boolean redTurn = true;
    public boolean moveAgain = false;
    public String winner = null;

    public Stack<Move> undoStack = new Stack<>();
    public Stack<Move> redoStack = new Stack<>();

    public CheckersBoard() {
        initBoard();
    }

    /** Initialize pieces in their starting positions. */
    public void initBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                board[row][col] = null;
                if ((row + col) % 2 == 1 && row < 3) {
                    board[row][col] = new Piece(false);
                } else if ((row + col) % 2 == 1 && row > 4) {
                    board[row][col] = new Piece(true);
                }
            }
        }
    }

    /** Represents a checkers piece. */
    static class Piece {
        boolean isRed;
        boolean isKing;


        /**
        * Represents an individual checkers piece.
        * Each piece has a color (red/black) and can be a king.
        */
        Piece(boolean isRed) { 
            this.isRed = isRed; 
            this.isKing = false; 
        }

        /** Creates a new piece of the given color. */
        Piece copy() { 
            Piece p = new Piece(this.isRed); 
            p.isKing = this.isKing; 
            return p; 
        }
    }

    /** Represents a move for Undo/Redo. */
    static class Move {
        int fromRow;
        int fromCol; 
        int toRow;
        int toCol;
        Piece movedPiece;
        Piece capturedPiece;
        int capturedRow;
        int capturedCol;
        boolean becameKing;

        /**
         * Creates a move record containing all move details.
         */
        Move(int fr, int fc, int tr, 
            int tc, Piece movedPiece, Piece capturedPiece, int cr, int cc, boolean becameKing) {
            this.fromRow = fr; 
            this.fromCol = fc;
            this.toRow = tr; 
            this.toCol = tc;
            this.movedPiece = movedPiece.copy();
            this.capturedPiece = (capturedPiece == null) ? null : capturedPiece.copy();
            this.capturedRow = cr; 
            this.capturedCol = cc;
            this.becameKing = becameKing;
        }
    }

    private boolean isValidDirection(Piece p, int rowDiff) {
        return p.isKing || (p.isRed && rowDiff < 0) || (!p.isRed && rowDiff > 0);
    }

    private boolean isInBounds(int r, int c) {
        return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
    }

    private void crownIfNeeded(Piece p, int toRow) {
        boolean wasKing = p.isKing;
        if ((p.isRed && toRow == 0) || (!p.isRed && toRow == SIZE - 1)) {
            p.isKing = true;
        }
        if (!wasKing && p.isKing) {
            SoundManager.playSound("king");
        }
    }

    private boolean canCaptureAgain(Piece p, int row, int col) {
        int[] dr = {-2, -2, 2, 2};
        int[] dc = {-2, 2, -2, 2};

        for (int i = 0; i < 4; i++) {
            int toRow = row + dr[i];
            int toCol = col + dc[i];
            int midRow = row + dr[i] / 2;
            int midCol = col + dc[i] / 2;

            if (isInBounds(toRow, toCol)) {
                Piece mid = board[midRow][midCol];
                if (mid != null && mid.isRed != p.isRed 
                    && board[toRow][toCol] == null && isValidDirection(p, dr[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Attempts to move a piece; returns true if successful.
     * Handles normal moves, captures, and king promotions.
     */
    public boolean movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        Piece p = board[fromRow][fromCol];
        if (p == null || board[toRow][toCol] != null) {
            return false;
        }
        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;
        boolean becameKing = false;

        if (Math.abs(rowDiff) == 1 && Math.abs(colDiff) == 1) {
            if (isValidDirection(p, rowDiff)) {
                board[toRow][toCol] = p;
                board[fromRow][fromCol] = null;
                crownIfNeeded(p, toRow);
                if (p.isKing) {
                    becameKing = true;
                }
                undoStack.push(new Move(fromRow, fromCol, 
                    toRow, toCol, p, null, -1, -1, becameKing));
                redoStack.clear();
                SoundManager.playSound("move");
                return true;
            }
        } else if (Math.abs(rowDiff) == 2 && Math.abs(colDiff) == 2) {
            int midRow = (fromRow + toRow) / 2;
            int midCol = (fromCol + toCol) / 2;
            Piece midPiece = board[midRow][midCol];
            if (midPiece != null && midPiece.isRed != p.isRed && isValidDirection(p, rowDiff)) {
                board[toRow][toCol] = p;
                board[fromRow][fromCol] = null;
                board[midRow][midCol] = null;
                crownIfNeeded(p, toRow);
                if (p.isKing) {
                    becameKing = true;
                }
                undoStack.push(new Move(fromRow, fromCol, 
                    toRow, toCol, p, midPiece, midRow, midCol, becameKing));
                redoStack.clear();
                SoundManager.playSound("capture");

                if (canCaptureAgain(p, toRow, toCol)) {
                    moveAgain = true;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the game has ended and determines the winner.
     */
    public boolean endGame() {
        boolean redPieces = false;
        boolean blackPieces = false;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Piece p = board[i][j];
                if (p != null) {
                    if (p.isRed) {
                        redPieces = true;
                    } else {
                        blackPieces = true;
                    }
                }
            }
        }
        if (!redPieces && blackPieces) { 
            winner = "Black Wins!"; 
            return true; 
        } else if (!blackPieces && redPieces) { 
            winner = "Red Wins!"; 
            return true;
        }
        winner = null;
        return false;
    }

    /**
     * Undoes the most recent move, if available.
     */
    public void undoMove() {
        if (undoStack.isEmpty()) {
            return;
        }
        Move m = undoStack.pop();
        Piece moved = m.movedPiece.copy();
        board[m.fromRow][m.fromCol] = moved;
        board[m.toRow][m.toCol] = null;
        if (m.capturedPiece != null) {
            board[m.capturedRow][m.capturedCol] = m.capturedPiece.copy();
        }
        moved.isKing = moved.isKing && !m.becameKing;
        redTurn = !redTurn;
        redoStack.push(m);
        SoundManager.playSound("buzz");
    }

    /**
     * Redoes the most recently undone move, if available.
     */
    public void redoMove() {
        if (redoStack.isEmpty()) {
            return;
        }
        Move m = redoStack.pop();
        Piece moved = m.movedPiece.copy();
        board[m.fromRow][m.fromCol] = null;
        board[m.toRow][m.toCol] = moved;
        if (m.capturedPiece != null) {
            board[m.capturedRow][m.capturedCol] = null;
        }
        if (m.becameKing) {
            moved.isKing = true;
        }
        redTurn = !redTurn;
        undoStack.push(m);
        SoundManager.playSound("buzz");
    }

    /** Resets the game to its initial state. */
    public void restart() { 
        initBoard(); 
        redTurn = true; 
    }
}

