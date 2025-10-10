import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Basic Checkers game with rendering, initial setup, movement, and capture mechanics.
 * @version 0.0.4
 */
public class Checkers extends JPanel implements MouseListener {

    private static final int SIZE = 8;          // Board dimensions
    private static final int TILE_SIZE = 80;    // Tile size in pixels

    private Piece[][] board = new Piece[SIZE][SIZE]; // Stores piece positions
    private int selectedRow = -1, selectedCol = -1;  // Track selected piece
    private boolean redTurn = true;                  // Red moves first

    /**
     * Represents a checkers piece.
     */
    static class Piece {
        boolean isRed;
        boolean isKing = false;
        Piece(boolean isRed) { this.isRed = isRed; }
    }

    /**
     * Constructor: sets up board and mouse events.
     */
    public Checkers() {
        setPreferredSize(new Dimension(SIZE * TILE_SIZE, SIZE * TILE_SIZE));
        addMouseListener(this);
        initBoard();
    }

    /**
     * Places initial pieces for red (bottom) and black (top).
     */
    private void initBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if ((row + col) % 2 == 1 && row < 3)
                    board[row][col] = new Piece(false); // black
                else if ((row + col) % 2 == 1 && row > 4)
                    board[row][col] = new Piece(true);  // red
            }
        }
    }

    /**
     * Draws the checkerboard and pieces.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                // Draw board tiles
                boolean light = (row + col) % 2 == 0;
                g.setColor(light ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                // Highlight selected square
                if (row == selectedRow && col == selectedCol) {
                    g.setColor(Color.YELLOW);
                    g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }

                // Draw pieces
                Piece p = board[row][col];
                if (p != null) {
                    g.setColor(p.isRed ? Color.RED : Color.BLACK);
                    g.fillOval(col * TILE_SIZE + 10, row * TILE_SIZE + 10,
                               TILE_SIZE - 20, TILE_SIZE - 20);
                }
            }
        }
    }

    /**
     * Handles player clicks: selects and moves pieces.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        int col = e.getX() / TILE_SIZE;
        int row = e.getY() / TILE_SIZE;

        if (selectedRow == -1) {
            // Select a piece if it belongs to the current player
            Piece p = board[row][col];
            if (p != null && p.isRed == redTurn) {
                selectedRow = row;
                selectedCol = col;
            }
        } else {
            // Try to move to clicked tile
            movePiece(selectedRow, selectedCol, row, col);
            selectedRow = -1;
            selectedCol = -1;
        }
        repaint();
    }

    /**
     * Moves or captures pieces if valid.
     */
    private void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        Piece p = board[fromRow][fromCol];
        if (p == null || board[toRow][toCol] != null) return;

        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;

        // --- BASIC DIAGONAL MOVE (1 square) ---
        if (Math.abs(rowDiff) == 1 && Math.abs(colDiff) == 1) {
            if ((p.isRed && rowDiff == -1) || (!p.isRed && rowDiff == 1)) {
                board[toRow][toCol] = p;
                board[fromRow][fromCol] = null;
                redTurn = !redTurn;
            }
        }

        // --- CAPTURE MOVE (2 squares) ---
        else if (Math.abs(rowDiff) == 2 && Math.abs(colDiff) == 2) {
            int midRow = (fromRow + toRow) / 2;
            int midCol = (fromCol + toCol) / 2;
            Piece midPiece = board[midRow][midCol];

            // Must be an enemy piece in the middle
            if (midPiece != null && midPiece.isRed != p.isRed) {
                board[toRow][toCol] = p;
                board[fromRow][fromCol] = null;
                board[midRow][midCol] = null; // remove captured piece
                redTurn = !redTurn;
            }
        }
    }

    // Unused MouseListener methods
    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    /**
     * Entry point: runs the Checkers window.
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Basic Checkers");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Checkers());
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
