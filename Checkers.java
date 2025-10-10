import javax.swing.*;
import java.awt.*;

/**
 * Basic Checkers board rendering.
 * Displays an 8x8 alternating grid.
 * @version 0.0.1
 */
public class Checkers extends JPanel {

private static final int SIZE = 8;
    private static final int TILE_SIZE = 80;
    private Piece[][] board = new Piece[SIZE][SIZE];

    /** Represents a piece on the board. */
    static class Piece {
        boolean isRed;
        Piece(boolean isRed) { this.isRed = isRed; }
    }

    public Checkers() {
        setPreferredSize(new Dimension(SIZE * TILE_SIZE, SIZE * TILE_SIZE));
        initBoard();
    }

    /** Places red and black pieces on starting rows. */
    private void initBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if ((row + col) % 2 == 1 && row < 3)
                    board[row][col] = new Piece(false); // black
                else if ((row + col) % 2 == 1 && row > 4)
                    board[row][col] = new Piece(true); // red
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                boolean light = (row + col) % 2 == 0;
                g.setColor(light ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                Piece p = board[row][col];
                if (p != null) {
                    g.setColor(p.isRed ? Color.RED : Color.BLACK);
                    g.fillOval(col * TILE_SIZE + 10, row * TILE_SIZE + 10,
                               TILE_SIZE - 20, TILE_SIZE - 20);
                }
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Checkers Board");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Checkers());
        frame.pack();
        frame.setVisible(true);
    }
}
