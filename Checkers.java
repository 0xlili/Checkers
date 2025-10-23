import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.*;
import java.io.File;
import java.util.Stack;

/**
 * Basic Checkers game implementing:
 * 1. Board rendering
 * 2. Initial piece placement
 * 3. Basic movement
 * 4. Capturing
 * 5. King promotion
 * 6. Undo/Redo functionality (buttons + Ctrl+Z/Y)
 * 7. Sound effects
 *
 * @version 2.2.0
 */
public class Checkers extends JPanel implements MouseListener {

    private static final int SIZE = 8;
    private static final int TILE_SIZE = 80;

    private Piece[][] board = new Piece[SIZE][SIZE];
    private int selectedRow = -1;
    private int selectedCol = -1;
    private boolean redTurn = true;

    private boolean moveAgain = false;
    private String winner = null;

    private Stack<Move> undoStack = new Stack<>();
    private Stack<Move> redoStack = new Stack<>();

    /** Represents a checkers piece. */
    static class Piece {
        boolean isRed;
        boolean isKing;
        Piece(boolean isRed) { this.isRed = isRed; this.isKing = false; }
        Piece copy() { Piece p = new Piece(this.isRed); p.isKing = this.isKing; return p; }
    }

    /** Represents a move for Undo/Redo. */
    static class Move {
        int fromRow, fromCol, toRow, toCol;
        Piece movedPiece, capturedPiece;
        int capturedRow, capturedCol;
        boolean becameKing;

        Move(int fr, int fc, int tr, int tc, Piece movedPiece, Piece capturedPiece, int cr, int cc, boolean becameKing) {
            this.fromRow = fr; this.fromCol = fc;
            this.toRow = tr; this.toCol = tc;
            this.movedPiece = movedPiece.copy();
            this.capturedPiece = (capturedPiece == null) ? null : capturedPiece.copy();
            this.capturedRow = cr; this.capturedCol = cc;
            this.becameKing = becameKing;
        }
    }

    public Checkers() {
        setPreferredSize(new Dimension(SIZE * TILE_SIZE, SIZE * TILE_SIZE));
        addMouseListener(this);
        initBoard();
        setupKeyBindings();
    }

    /** Initialize pieces in their starting positions. */
    private void initBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                board[row][col] = null;
                if ((row + col) % 2 == 1 && row < 3)
                    board[row][col] = new Piece(false);
                else if ((row + col) % 2 == 1 && row > 4)
                    board[row][col] = new Piece(true);
            }
        }
    }

    /** Sets up Ctrl+Z (Undo) and Ctrl+Y (Redo) shortcuts. */
    private void setupKeyBindings() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), "redo");

        am.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { undoMove(); }
        });
        am.put("redo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { redoMove(); }
        });
    }

    /** Play sound effect from /sounds folder. */
    private void playSound(String soundName) {
        new Thread(() -> {
            try {
                // Use getResource to access file inside 'sounds' folder
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(
                    getClass().getResource("/sounds/" + soundName + ".wav")
                );
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
                clip.addLineListener(e -> {
                    if (e.getType() == LineEvent.Type.STOP) clip.close();
                });
            } catch (Exception e) {
                System.err.println("Failed to play sound: " + soundName + " (" + e.getMessage() + ")");
            }
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                boolean light = (row + col) % 2 == 0;
                g.setColor(light ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                if (row == selectedRow && col == selectedCol) {
                    g.setColor(new Color(255, 255, 0, 128));
                    g.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }

                Piece p = board[row][col];
                if (p != null) {
                    g.setColor(p.isRed ? Color.RED : Color.BLACK);
                    g.fillOval(col * TILE_SIZE + 10, row * TILE_SIZE + 10, TILE_SIZE - 20, TILE_SIZE - 20);
                    if (p.isKing) {
                        g.setColor(Color.WHITE);
                        g.setFont(new Font("Arial", Font.BOLD, 18));
                        g.drawString("K", col * TILE_SIZE + 34, row * TILE_SIZE + 47);
                    }
                }
            }
        }

        if (endGame()) {
            playSound("gameover");
            g.setColor(new Color(0, 0, 0, 150));
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

    @Override
    public void mousePressed(MouseEvent e) {
        int col = e.getX() / TILE_SIZE;
        int row = e.getY() / TILE_SIZE;

        if (selectedRow == -1) {
            Piece p = board[row][col];
            if (p != null && p.isRed == redTurn) {
                selectedRow = row;
                selectedCol = col;
            } else {
                playSound("no"); // invalid selection
            }
        } else {
            if (movePiece(selectedRow, selectedCol, row, col)) {
                if (!moveAgain) {
                    redTurn = !redTurn;
                    selectedRow = -1;
                    selectedCol = -1;
                }
                moveAgain = false;
            } else {
                playSound("no"); // invalid move
                selectedRow = -1;
                selectedCol = -1;
            }
        }
        repaint();
    }

    private boolean isValidDirection(Piece p, int rowDiff) {
        return p.isKing || (p.isRed && rowDiff < 0) || (!p.isRed && rowDiff > 0);
    }

    private void crownIfNeeded(Piece p, int toRow) {
        boolean wasKing = p.isKing;
        if ((p.isRed && toRow == 0) || (!p.isRed && toRow == SIZE - 1)) {
            p.isKing = true;
        }
        if (!wasKing && p.isKing) {
            playSound("king");
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

    private boolean movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        Piece p = board[fromRow][fromCol];
        if (p == null || board[toRow][toCol] != null) return false;

        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;
        boolean becameKing = false;

        if (Math.abs(rowDiff) == 1 && Math.abs(colDiff) == 1) {
            if (isValidDirection(p, rowDiff)) {
                board[toRow][toCol] = p;
                board[fromRow][fromCol] = null;
                crownIfNeeded(p, toRow);
                if (p.isKing) becameKing = true;
                undoStack.push(new Move(fromRow, fromCol, toRow, toCol, p, null, -1, -1, becameKing));
                redoStack.clear();
                playSound("move");
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
                if (p.isKing) becameKing = true;
                undoStack.push(new Move(fromRow, fromCol, toRow, toCol, p, midPiece, midRow, midCol, becameKing));
                redoStack.clear();
                playSound("capture");

                if (canCaptureAgain(p, toRow, toCol)) {
                    moveAgain = true;
                    selectedRow = toRow;
                    selectedCol = toCol;
                }
                return true;
            }
        }
        return false;
    }

    private boolean endGame() {
        boolean redPieces = false;
        boolean blackPieces = false;
        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++) {
                Piece p = board[i][j];
                if (p != null) {
                    if (p.isRed) redPieces = true;
                    else blackPieces = true;
                }
            }
        if (!redPieces && blackPieces) { winner = "Black Wins!"; return true; }
        else if (!blackPieces && redPieces) { winner = "Red Wins!"; return true; }
        winner = null;
        return false;
    }

    public void undoMove() {
        if (undoStack.isEmpty()) return;
        Move m = undoStack.pop();
        Piece moved = m.movedPiece.copy();
        board[m.fromRow][m.fromCol] = moved;
        board[m.toRow][m.toCol] = null;
        if (m.capturedPiece != null)
            board[m.capturedRow][m.capturedCol] = m.capturedPiece.copy();
        moved.isKing = moved.isKing && !m.becameKing;
        redTurn = !redTurn;
        redoStack.push(m);
        playSound("buzz");
        repaint();
    }

    public void redoMove() {
        if (redoStack.isEmpty()) return;
        Move m = redoStack.pop();
        Piece moved = m.movedPiece.copy();
        board[m.fromRow][m.fromCol] = null;
        board[m.toRow][m.toCol] = moved;
        if (m.capturedPiece != null)
            board[m.capturedRow][m.capturedCol] = null;
        if (m.becameKing) moved.isKing = true;
        redTurn = !redTurn;
        undoStack.push(m);
        playSound("buzz");
        repaint();
    }

    public void mouseReleased(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    /** Launch the window. */
    public static void main(String[] args) {
        JFrame frame = new JFrame("Checkers with Undo/Redo (Ctrl+Z/Y)");
        Checkers game = new Checkers();

        JButton undoButton = new JButton("Undo");
        JButton redoButton = new JButton("Redo");

        undoButton.addActionListener(e -> game.undoMove());
        redoButton.addActionListener(e -> game.redoMove());

        JPanel controls = new JPanel();
        controls.add(undoButton);
        controls.add(redoButton);

        frame.setLayout(new BorderLayout());
        frame.add(game, BorderLayout.CENTER);
        frame.add(controls, BorderLayout.SOUTH);
        frame.pack();
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
