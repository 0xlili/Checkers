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

    public Checkers() {
        setPreferredSize(new Dimension(SIZE * TILE_SIZE, SIZE * TILE_SIZE));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw alternating light/dark tiles
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                boolean light = (row + col) % 2 == 0;
                g.setColor(light ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
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
