package Checkers;

import java.awt.*;
import javax.swing.*;

/**
 * Basic Checkers game implementing:
 * 1. Board rendering
 * 2. Initial piece placement
 * 3. Basic movement
 * 4. Capturing
 * 5. King promotion
 * 6. Multicapture
 * 7. Undo/Redo/Restart functionality (buttons + Ctrl+Z/Y)
 * 8. Sound effects
 * 
 * @version 2.2.0
 */
public class Checkers {
    public static void main(String[] args) {
        final JFrame frame = new JFrame("Checkers with Undo/Redo (Ctrl+Z/Y)");
        CheckersBoard logic = new CheckersBoard();
        final CheckersPanel panel = new CheckersPanel(logic);
        JButton undoButton = new JButton("Undo");
        JButton redoButton = new JButton("Redo");
        JButton restartButton = new JButton("Restart");

        //Style buttons
        Color buttonColor = new Color(60, 63, 65);
        Color hoverColor = new Color(77, 80, 82);
        Color textColor = Color.WHITE;
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 14);

        JButton[] buttons = {undoButton, redoButton, restartButton};
        for (JButton btn : buttons) {
            btn.setBackground(buttonColor);
            btn.setForeground(textColor);
            btn.setFont(buttonFont);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            
            // Hover effect
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    btn.setBackground(hoverColor);
                }
                
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    btn.setBackground(buttonColor);
                }
            });
        }

        //Style control panel
        JPanel controls = new JPanel();
        controls.setBackground(new Color(40, 42, 54));  // dark background
        controls.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        controls.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 5));

        controls.add(undoButton);
        controls.add(redoButton);
        controls.add(restartButton);

        //Add actions
        undoButton.addActionListener(e -> logic.undoMove());
        undoButton.addActionListener(e -> panel.repaint());
        redoButton.addActionListener(e -> logic.redoMove());
        redoButton.addActionListener(e -> panel.repaint());
        restartButton.addActionListener(e -> logic.restart());
        restartButton.addActionListener(e -> panel.repaint());

        //Frame setup
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.add(controls, BorderLayout.SOUTH);
        frame.pack();
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
