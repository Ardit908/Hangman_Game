import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Random;

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

        // Paneli për figurën Hangman
        figurePanel = new HangmanFigurePanel();
        add(figurePanel, BorderLayout.WEST);

        // Paneli për fjalën
        wordLabel = new JLabel("", SwingConstants.CENTER);
        wordLabel.setFont(new Font("Arial", Font.BOLD, 32));
        add(wordLabel, BorderLayout.NORTH);

        // Paneli për statusin
        statusLabel = new JLabel("Zgjidh mënyrën e lojës për të filluar", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        add(statusLabel, BorderLayout.SOUTH);

        // Paneli për butonat e shkronjave
        letterPanel = new JPanel(new GridLayout(3, 9, 5, 5));
        add(letterPanel, BorderLayout.CENTER);

        // Shfaq modalitetin e lojës
        showModeSelectionDialog();
    }

    private void showModeSelectionDialog() {
        String[] options = {"Luaj tani", "Luaj me kompjuterin", "Dil"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Zgjidh opsionin e lojës:",
                "Zgjedh mënyrën e lojës",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            promptForWordFromPlayer();
        } else if (choice == 1) {
            setupComputerWord();
        } else {
            System.exit(0);
        }
    }

    private void promptForWordFromPlayer() {
        while (true) {
            JPasswordField pf = new JPasswordField();
            int okCxl = JOptionPane.showConfirmDialog(
                    this, pf, "Shkruaj fjalën që do të luash (vetëm shkronja):",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
            );
            if (okCxl != JOptionPane.OK_OPTION) {
                System.exit(0);
            }
            String inputWord = new String(pf.getPassword());
            if (!inputWord.isEmpty() && inputWord.matches("[a-zA-Z]+")) {
                wordToGuess = inputWord.toUpperCase();
                initializeHiddenWordAndUI();
                break;
            } else {
                JOptionPane.showMessageDialog(this, "Duhet të përdorësh vetëm shkronja!", "Gabim", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setupComputerWord() {
        try {
            String randomWord = getRandomWord();
            String description = getDefinition(randomWord);
            wordToGuess = randomWord.toUpperCase();

            JOptionPane.showMessageDialog(this,
                    "Description (EN):\n" + description + "\n\nPress OK to start.",
                    "Word Hint", JOptionPane.INFORMATION_MESSAGE);

            initializeHiddenWordAndUI();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Gabim gjatë marrjes së fjalës nga interneti.\nDo përdoret fjala 'APPLE'.",
                    "Gabim", JOptionPane.ERROR_MESSAGE);
            wordToGuess = "APPLE";
            initializeHiddenWordAndUI();
        }
    }

    private void initializeHiddenWordAndUI() {
        wrongGuesses = 0;
        figurePanel.setWrongGuesses(wrongGuesses);
        hiddenWord = new StringBuilder();
        for (char c : wordToGuess.toCharArray()) {
            hiddenWord.append(Character.isLetter(c) ? "_" : c);
        }
        wordLabel.setText(spaced(hiddenWord.toString()));
        statusLabel.setText("Zgjidh një shkronjë.");
        createLetterButtons();
    }

    private void createLetterButtons() {
        letterPanel.removeAll();
        for (char letter = 'A'; letter <= 'Z'; letter++) {
            char currentLetter = letter;
            JButton button = new JButton(String.valueOf(currentLetter));
            button.setFont(new Font("Arial", Font.BOLD, 16));
            button.addActionListener(e -> handleGuess(currentLetter, button));
            letterPanel.add(button);
        }
        letterPanel.revalidate();
        letterPanel.repaint();
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
            wordLabel.setText(spaced(wordToGuess));
            disableAllButtons();
            int res = JOptionPane.showConfirmDialog(this,
                    "Humbët! Fjala ishte: " + wordToGuess + "\nDëshiron të luash përsëri?",
                    "Humbje",
                    JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                restartGame();
            } else {
                System.exit(0);
            }
        }
    }

    private void restartGame() {
        dispose();
        SwingUtilities.invokeLater(() -> new Game().setVisible(true));
    }

    private void disableAllButtons() {
        for (Component comp : letterPanel.getComponents()) {
            if (comp instanceof JButton) comp.setEnabled(false);
        }
    }

    private String spaced(String word) {
        return word.replaceAll("", " ").trim();
    }

    // ================== API WITHOUT org.json ==================
    private String getRandomWord() {
        int attempts = 5; // provon 5 herë në rast dështimi
        while (attempts > 0) {
            try {
                URL url = new URL("https://random-word-api.herokuapp.com/word");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(2000); // 2 sekonda
                conn.setReadTimeout(2000);
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String response = br.readLine(); // ["apple"]
                br.close();
                if (response != null && !response.isEmpty()) {
                    return response.replaceAll("[\\[\\]\"]", "");
                }
            } catch (Exception e) {
                // provon përsëri
            }
            attempts--;
        }
        return "APPLE"; // fallback
    }

    private String getDefinition(String word) {
        try {
            URL url = new URL("https://api.dictionaryapi.dev/api/v2/entries/en/" + word);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            String json = sb.toString();
            int index = json.indexOf("\"definition\":\"");
            if (index != -1) {
                int start = index + 14;
                int end = json.indexOf("\"", start);
                if (end != -1) {
                    return json.substring(start, end);
                }
            }
        } catch (Exception e) {
            // fallback në rast gabimi
        }
        return "No definition found";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Game().setVisible(true));
    }
}

// ================== HANGMAN FIGURE PANEL ==================
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

        if (wrongGuesses >= 1) g.drawLine(20, 250, 120, 250); // baza
        if (wrongGuesses >= 2) g.drawLine(70, 250, 70, 50);   // shtylla
        if (wrongGuesses >= 3) g.drawLine(70, 50, 140, 50);   // trar
        if (wrongGuesses >= 4) g.drawLine(140, 50, 140, 80);  // litar
        if (wrongGuesses >= 5) g.drawOval(130, 80, 20, 20);   // koka
        if (wrongGuesses >= 6) g.drawLine(140, 100, 140, 150); // trupi
        if (wrongGuesses >= 7) g.drawLine(140, 110, 120, 130); // krahu majtas
        if (wrongGuesses >= 8) g.drawLine(140, 110, 160, 130); // krahu djathtas
        if (wrongGuesses >= 9) g.drawLine(140, 150, 120, 180); // këmba majtas
        if (wrongGuesses >= 10) g.drawLine(140, 150, 160, 180); // këmba djathtas
    }
}
