package Checkers;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * Handles rendering and user interaction for the Checkers game.
 */
public class CheckersPanel extends JPanel implements MouseListener {

    public static final int TILE_SIZE = 80;

    private final CheckersBoard boardLogic;
    private int selectedRow = -1;
    private int selectedCol = -1;

    /**
    * Handles Checkers Board.
    */
    public CheckersPanel(CheckersBoard boardLogic) {
        this.boardLogic = boardLogic;
        setPreferredSize(new Dimension(CheckersBoard.SIZE * TILE_SIZE, 
            CheckersBoard.SIZE * TILE_SIZE));
        addMouseListener(this);
        setupKeyBindings();
    }

    /** Sets up Ctrl+Z and Ctrl+Y for undo/redo. */
    private void setupKeyBindings() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redo");

        am.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardLogic.undoMove();
                repaint();
            }
        });
        am.put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boardLogic.redoMove();
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        CheckersBoard.Piece[][] board = boardLogic.board;

        for (int row = 0; row < CheckersBoard.SIZE; row++) {
            for (int col = 0; col < CheckersBoard.SIZE; col++) {
                boolean light = (row + col) % 2 == 0;
                g.setColor(light ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                if (row == selectedRow && col == selectedCol) {
                    g.setColor(new Color(255, 255, 0, 128));
                    g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }

                CheckersBoard.Piece p = board[row][col];
                if (p != null) {
                    g.setColor(p.isRed ? Color.RED : Color.BLACK);
                    g.fillOval(col * TILE_SIZE + 10, row * TILE_SIZE + 10,
                        TILE_SIZE - 20, TILE_SIZE - 20);
                    if (p.isKing) {
                        g.setColor(Color.WHITE);
                        g.setFont(new Font("Arial", Font.BOLD, 18));
                        g.drawString("K", col * TILE_SIZE + 34, row * TILE_SIZE + 47);
                    }
                }
            }
        }

        if (boardLogic.endGame()) {
            SoundManager.playSound("gameover");
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            String text = "GAME OVER";
            FontMetrics fm = g.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent() - 30;
            g.drawString(text, x, y);

            if (boardLogic.winner != null) {
                g.setFont(new Font("Arial", Font.BOLD, 40));
                FontMetrics fm2 = g.getFontMetrics();
                int wx = (getWidth() - fm2.stringWidth(boardLogic.winner)) / 2;
                int wy = y + 60;
                g.drawString(boardLogic.winner, wx, wy);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int col = e.getX() / TILE_SIZE;
        int row = e.getY() / TILE_SIZE;

        CheckersBoard.Piece[][] board = boardLogic.board;

        if (selectedRow == -1) {
            CheckersBoard.Piece p = board[row][col];
            if (p != null && p.isRed == boardLogic.redTurn) {
                selectedRow = row;
                selectedCol = col;
            } else {
                SoundManager.playSound("no");
            }
        } else {
            if (boardLogic.movePiece(selectedRow, selectedCol, row, col)) {
                if (!boardLogic.moveAgain) {
                    boardLogic.redTurn = !boardLogic.redTurn;
                    selectedRow = -1;
                    selectedCol = -1;
                } else {
                    selectedRow = row;
                    selectedCol = col;
                }
                boardLogic.moveAgain = false;
            } else {
                SoundManager.playSound("no");
                selectedRow = -1;
                selectedCol = -1;
            }
        }
        repaint();
    }

    public void mouseReleased(MouseEvent e) {}

    public void mouseClicked(MouseEvent e) {}

    public void mouseEntered(MouseEvent e) {}

    public void mouseExited(MouseEvent e) {}
}

