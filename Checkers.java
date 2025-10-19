import java.awt.*;
import java.awt.event.*;
import javax.swing.*;



/**
 * Basic Checkers game implementing:
 * 1. Board rendering
 * 2. Initial piece placement
 * 3. Basic movement
 * 4. Capturing
 * 5. King promotion
 *
 * @version 1.0.0
 */
public class Checkers extends JPanel implements MouseListener {

    private static final int SIZE = 8;          // 8x8 board
    private static final int TILE_SIZE = 80;    // Tile size in pixels

    private Piece[][] board = new Piece[SIZE][SIZE]; // Game board state
    private int selectedRow = -1;
    private int selectedCol = -1;  // Currently selected piece
    private boolean redTurn = true;                  // Red always starts

    /**
     * Represents a checkers piece.
     */
    static class Piece {
        boolean isRed;   // True for red, false for black
        boolean isKing;  // True if piece has been promoted
        Piece(boolean isRed) { this.isRed = isRed; this.isKing = false; }
    }

    /**
     * Initializes board and listeners.
     */
    public Checkers() {
        setPreferredSize(new Dimension(SIZE * TILE_SIZE, SIZE * TILE_SIZE));
        addMouseListener(this);
        initBoard();
    }

    /**
     * Places red and black pieces on starting rows.
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
     * Draws the checkerboard and all pieces.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
            

                // Draw alternating tiles
                boolean light = (row + col) % 2 == 0;
                g.setColor(light ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                // Highlight selected piece
                if (row == selectedRow && col == selectedCol) {
                    g.setColor(new Color(255, 255, 0, 128));
                    g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }

                // Draw pieces
                Piece p = board[row][col];
                if (p != null) {
                    g.setColor(p.isRed ? Color.RED : Color.BLACK);
                    g.fillOval(col * TILE_SIZE + 10, row * TILE_SIZE + 10,
                               TILE_SIZE - 20, TILE_SIZE - 20);

                    // Draw crown symbol for kings
                    if (p.isKing) {
                        g.setColor(Color.WHITE);
                        g.setFont(new Font("Arial", Font.BOLD, 18));
                        g.drawString("K", col * TILE_SIZE + 34, row * TILE_SIZE + 47);
                    }
                }
            }
        }
        if (endGame()) {
            g.setColor(new Color(0, 0, 0, 150)); // semi-transparent overlay
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            FontMetrics fm = g.getFontMetrics();
            String text = "GAME OVER";
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() - 30;
            g.drawString(text, x, y);
            if (winner != null) {
                g.setFont(new Font("Arial", Font.BOLD, 40));
                FontMetrics fm2 = g.getFontMetrics();
                int wx = (getWidth() - fm2.stringWidth(winner)) / 2;
                int wy = y + 60;
                g.drawString(winner, wx, wy);
            }
        }
    }

    /**
     * Handles player clicks for selecting and moving pieces.
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
            if (!moveAgain) {
                redTurn = !redTurn;
                selectedRow = -1;
                selectedCol = -1; 
            }
            moveAgain = false;
        }
        repaint();
    }

    private boolean isValidDirection(Piece p, int rowDiff) {
        return p.isKing || (p.isRed && rowDiff < 0) || (!p.isRed && rowDiff > 0);
    }

    private void crownIfNeeded(Piece p, int toRow) {
        if (p.isRed && toRow == 0) {
            p.isKing = true;
        }
        if (!p.isRed && toRow == SIZE - 1) {
            p.isKing = true;
        }
    }

    private boolean isInBounds(int r, int c) {
        return r >= 0 && r < SIZE && c >= 0 && c < SIZE;
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
                if (mid != null && mid.isRed != p.isRed && board[toRow][toCol] == null && isValidDirection(p, dr[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean moveAgain = false;


    

    /**
     * Handles both normal moves and captures, including king promotion.
     */
    private boolean movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        Piece p = board[fromRow][fromCol];
        if (p == null || board[toRow][toCol] != null) {
            return false;
        }

        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;
        // --- BASIC DIAGONAL MOVE (1 square) ---
        if (Math.abs(rowDiff) == 1 && Math.abs(colDiff) == 1) {
            if (isValidDirection(p, rowDiff)) {
                board[toRow][toCol] = p;
                board[fromRow][fromCol] = null;
                crownIfNeeded(p, toRow);
                return true;
            }
        }

        // --- CAPTURE MOVE (2 squares) ---
        else if (Math.abs(rowDiff) == 2 && Math.abs(colDiff) == 2) {
            int midRow = (fromRow + toRow) / 2;
            int midCol = (fromCol + toCol) / 2;
            Piece midPiece = board[midRow][midCol];

            // Must be an enemy piece in the middle
            if (midPiece != null && midPiece.isRed != p.isRed && isValidDirection(p, rowDiff)) {
                board[toRow][toCol] = p;                    
                board[fromRow][fromCol] = null;
                board[midRow][midCol] = null; // remove captured piece
                
                if (canCaptureAgain(p, toRow, toCol)) {
                    moveAgain = true;
                    selectedRow = toRow;
                    selectedCol = toCol;
                
                }
                
                crownIfNeeded(p, toRow);
                return true;
            }         
        }
    
        return false;
    }

    private String winner = null;

    /**
    * Checks if the game has ended and determines the winner.
    * @return true if the game is over
    */
    private boolean endGame() {
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

        winner = null; // still playing
        return false;
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
