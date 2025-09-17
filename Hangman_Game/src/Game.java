import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Game extends JFrame {
    private String wordToGuess;
    private StringBuilder hiddenWord;
    private int wrongGuesses = 0;
    private JLabel wordLabel;
    private JLabel statusLabel;
    private JPanel letterPanel;
    private HangmanFigurePanel figurePanel;

    public Game() {
        setTitle("Hangman Game 🎮");
        setSize(700, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Paneli grafik për figurën
        figurePanel = new HangmanFigurePanel();
        add(figurePanel, BorderLayout.WEST);

        // Paneli për fjalën
        wordLabel = new JLabel("", SwingConstants.CENTER);
        wordLabel.setFont(new Font("Arial", Font.BOLD, 32));
        add(wordLabel, BorderLayout.NORTH);

        // Paneli për statusin
        statusLabel = new JLabel("Shkruaj një fjalë për të filluar lojën", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        add(statusLabel, BorderLayout.SOUTH);

        // Paneli për butonat e shkronjave
        letterPanel = new JPanel(new GridLayout(3, 9, 5, 5));
        add(letterPanel, BorderLayout.CENTER);

        // Fusha për të futur fjalën (loop derisa të futet e saktë)
        while (true) {
            String inputWord = JOptionPane.showInputDialog(this, "Shkruaj fjalën që do të luash:");
            if (inputWord == null) {
                System.exit(0); // Mbyll programin nëse përdoruesi shtyp Cancel
            } else if (!inputWord.isEmpty()) {
                if (!inputWord.matches("[a-zA-Z]+")) {
                    JOptionPane.showMessageDialog(this,
                            "❌ Duhet të përdorësh vetëm shkronja!",
                            "Gabim", JOptionPane.ERROR_MESSAGE);
                    continue; // rifillon pyetja
                }
                wordToGuess = inputWord.toUpperCase();
                hiddenWord = new StringBuilder();
                for (char c : wordToGuess.toCharArray()) {
                    hiddenWord.append(Character.isLetter(c) ? "_" : c);
                }
                wordLabel.setText(spaced(hiddenWord.toString()));
                createLetterButtons();
                break;
            } else {
                statusLabel.setText("Nuk u vendos asnjë fjalë.");
            }
        }
    }

    private void createLetterButtons() {
        for (char letter = 'A'; letter <= 'Z'; letter++) {
            char currentLetter = letter;
            JButton button = new JButton(String.valueOf(currentLetter));
            button.setFont(new Font("Arial", Font.BOLD, 16));
            button.addActionListener(e -> handleGuess(currentLetter, button));
            letterPanel.add(button);
        }
    }

    private void handleGuess(char letter, JButton button) {
        button.setEnabled(false);
        boolean found = false;

        for (int i = 0; i < wordToGuess.length(); i++) {
            if (wordToGuess.charAt(i) == letter) {
                hiddenWord.setCharAt(i, letter);
                found = true;
            }
        }

        if (found) {
            wordLabel.setText(spaced(hiddenWord.toString()));
            statusLabel.setText("✅ Shkronjë e saktë!");
        } else {
            wrongGuesses++;
            statusLabel.setText("❌ Gabim! Gabime: " + wrongGuesses + "/10");
            figurePanel.setWrongGuesses(wrongGuesses);
        }

        checkGameStatus();
    }

    private void checkGameStatus() {
        if (!hiddenWord.toString().contains("_")) {
            statusLabel.setText("🎉 Urime! E gjete fjalën!");
            JOptionPane.showMessageDialog(this, "Urime! E gjete fjalën: " + wordToGuess);
            restartGame();
        } else if (wrongGuesses >= 10) {
            statusLabel.setText("😢 Humbje! Fjalë ishte: " + wordToGuess);
            wordLabel.setText(wordToGuess);
            disableAllButtons();
        }
    }

    private void restartGame() {
        dispose();
        SwingUtilities.invokeLater(() -> new Game().setVisible(true));
    }

    private void disableAllButtons() {
        for (Component comp : letterPanel.getComponents()) {
            if (comp instanceof JButton) {
                comp.setEnabled(false);
            }
        }
    }

    private String spaced(String word) {
        return word.replaceAll("", " ").trim();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Game().setVisible(true));
    }
}

// Paneli grafik për figurën Hangman
class HangmanFigurePanel extends JPanel {
    private int wrongGuesses;

    public void setWrongGuesses(int wrongGuesses) {
        this.wrongGuesses = wrongGuesses;
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(200, 300);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);

        // Baza dhe struktura
        if (wrongGuesses >= 1) g.drawLine(20, 250, 120, 250); // baza
        if (wrongGuesses >= 2) g.drawLine(70, 250, 70, 50);   // shtylla
        if (wrongGuesses >= 3) g.drawLine(70, 50, 140, 50);   // trar
        if (wrongGuesses >= 4) g.drawLine(140, 50, 140, 80);  // litar

        // Trupi
        if (wrongGuesses >= 5) g.drawOval(130, 80, 20, 20);   // koka
        if (wrongGuesses >= 6) g.drawLine(140, 100, 140, 150); // trupi
        if (wrongGuesses >= 7) g.drawLine(140, 110, 120, 130); // krahu majtas
        if (wrongGuesses >= 8) g.drawLine(140, 110, 160, 130); // krahu djathtas
        if (wrongGuesses >= 9) g.drawLine(140, 150, 120, 180); // këmba majtas
        if (wrongGuesses >= 10) g.drawLine(140, 150, 160, 180); // këmba djathtas
    }
}
