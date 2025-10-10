import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Basic Checkers with board rendering, initial setup, and basic diagonal movement.
 * @version 0.0.3
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
     * Constructor: initializes the game board and mouse listener.
     */
    public Checkers() {
        setPreferredSize(new Dimension(SIZE * TILE_SIZE, SIZE * TILE_SIZE));
        addMouseListener(this);
        initBoard();
    }

    /**
     * Places initial pieces for red (bottom) and black (top) players.
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
     * Renders the board and pieces.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                // Draw checkerboard tiles
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
     * Handles selecting and moving pieces.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        int col = e.getX() / TILE_SIZE;
        int row = e.getY() / TILE_SIZE;

        if (selectedRow == -1) {
            // Select a piece
            Piece p = board[row][col];
            if (p != null && p.isRed == redTurn) {
                selectedRow = row;
                selectedCol = col;
            }
        } else {
            // Attempt to move to clicked location
            movePiece(selectedRow, selectedCol, row, col);
            selectedRow = -1;
            selectedCol = -1;
        }
        repaint();
    }

    /**
     * Performs a simple diagonal move if valid (1 square forward).
     */
    private void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        Piece p = board[fromRow][fromCol];
        if (p == null) return;
        if (board[toRow][toCol] != null) return;

        int rowDiff = toRow - fromRow;
        int colDiff = Math.abs(toCol - fromCol);

        // Valid move: 1 diagonal step in allowed direction
        if (Math.abs(rowDiff) == 1 && colDiff == 1) {
            if ((p.isRed && rowDiff == -1) || (!p.isRed && rowDiff == 1)) {
                board[toRow][toCol] = p;
                board[fromRow][fromCol] = null;
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
     * Launches the application window.
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
