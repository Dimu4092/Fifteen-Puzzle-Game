import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collections;
import java.util.ArrayList;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class FifteenPuzzleGUI extends JFrame {
    private JButton[][] buttons;
    private int[][] board;
    private final int SIZE;
    private final int MAX_SIZE = 4; // Максимальний розмір
    private JButton resetButton;
    private final int ANIMATION_DURATION = 300; // Тривалість анімації (в мс)

    public FifteenPuzzleGUI(int size) {
        this.SIZE = size;
        setTitle("Fifteen Puzzle");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(SIZE, SIZE));

        buttons = new JButton[SIZE][SIZE];
        board = new int[SIZE][SIZE];

        initBoard(); // Ініціалізуємо і перемішуємо дошку
        drawBoard(boardPanel);

        resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> resetBoard());

        add(boardPanel, BorderLayout.CENTER);
        add(resetButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void initBoard() {
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 1; i < SIZE * SIZE; i++) {
            numbers.add(i);
        }
        numbers.add(0); // Порожнє місце

        // Перемішуємо плитки
        do {
            Collections.shuffle(numbers);
        } while (!isSolvable(numbers)); // Перевірка на розв'язуваність

        // Заповнюємо дошку значеннями
        int index = 0;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                board[i][j] = numbers.get(index++);
            }
        }
    }

    // Метод перевірки, чи є конфігурація розв'язуваною
    private boolean isSolvable(ArrayList<Integer> numbers) {
        int inversions = 0;
        for (int i = 0; i < numbers.size() - 1; i++) {
            for (int j = i + 1; j < numbers.size(); j++) {
                if (numbers.get(i) > numbers.get(j) && numbers.get(j) != 0) {
                    inversions++;
                }
            }
        }
        // Якщо розмір дошки непарний, кількість інверсій має бути парною
        if (SIZE % 2 != 0) {
            return inversions % 2 == 0;
        } else {
            // Якщо розмір парний, дивимося на позицію порожньої плитки
            int emptyRow = SIZE - (numbers.indexOf(0) / SIZE);
            return (inversions + emptyRow) % 2 == 0;
        }
    }

    private void drawBoard(JPanel panel) {
        panel.removeAll();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setText(board[i][j] == 0 ? "_" : String.valueOf(board[i][j]));
                buttons[i][j].setFont(new Font("Arial", Font.PLAIN, 40));
                buttons[i][j].setFocusable(false);
                buttons[i][j].addActionListener(new ButtonClickListener(i, j));
                panel.add(buttons[i][j]);
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    private class ButtonClickListener implements ActionListener {
        private int row;
        private int col;

        public ButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (makeMove(row, col)) {
                playSound("move.wav");
                animateMove(row, col); // Анімація
                if (won()) {
                    JOptionPane.showMessageDialog(null, "Вітаємо, ви виграли!");
                }
            }
        }
    }

    private boolean makeMove(int row, int col) {
        int emptyRow = -1, emptyCol = -1;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (board[i][j] == 0) {
                    emptyRow = i;
                    emptyCol = j;
                }
            }
        }

        if ((Math.abs(emptyRow - row) == 1 && emptyCol == col) ||
                (Math.abs(emptyCol - col) == 1 && emptyRow == row)) {
            // Перемістити плитку
            board[emptyRow][emptyCol] = board[row][col];
            board[row][col] = 0;
            return true;
        }
        return false;
    }

    private boolean won() {
        int expectedValue = 1;
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (i == SIZE - 1 && j == SIZE - 1) {
                    if (board[i][j] != 0) {
                        return false;
                    }
                } else {
                    if (board[i][j] != expectedValue) {
                        return false;
                    }
                    expectedValue++;
                }
            }
        }
        return true;
    }

    private void resetBoard() {
        initBoard();
        drawBoard((JPanel) getContentPane().getComponent(0));
    }

    private void playSound(String soundFile) {
        try {
            Clip clip = AudioSystem.getClip();
            var audioStream = getClass().getClassLoader().getResourceAsStream(soundFile);
            if (audioStream == null) {
                throw new IllegalArgumentException("Файл не знайдено: " + soundFile);
            }
            clip.open(AudioSystem.getAudioInputStream(audioStream));
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void animateMove(int row, int col) {
        JButton button = buttons[row][col];
        JButton emptyButton = null;
        int emptyRow = -1, emptyCol = -1;

        // Знаходимо порожню кнопку
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (buttons[i][j].getText().equals("_")) {
                    emptyButton = buttons[i][j];
                    emptyRow = i;
                    emptyCol = j;
                }
            }
        }

        // Координати початкової та кінцевої позицій
        Point startLocation = button.getLocation();
        Point endLocation = emptyButton.getLocation();

        // Анімація переміщення
        Timer timer = new Timer(10, new ActionListener() {
            long startTime = System.currentTimeMillis();

            @Override
            public void actionPerformed(ActionEvent e) {
                float progress = (float) (System.currentTimeMillis() - startTime) / ANIMATION_DURATION;
                if (progress >= 1.0f) {
                    ((Timer) e.getSource()).stop();
                    button.setLocation(endLocation);
                    drawBoard((JPanel) getContentPane().getComponent(0)); // Оновлюємо після анімації
                } else {
                    int newX = (int) (startLocation.x + (endLocation.x - startLocation.x) * progress);
                    int newY = (int) (startLocation.y + (endLocation.y - startLocation.y) * progress);
                    button.setLocation(newX, newY);
                }
            }
        });
        timer.start();
    }

    public static void main(String[] args) {
        String[] options = {"3x3", "4x4"};
        int choice = JOptionPane.showOptionDialog(null, "Оберіть розмір поля:", "Вибір поля",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

        if (choice == 0) {
            new FifteenPuzzleGUI(3); // 3x3
        } else if (choice == 1) {
            new FifteenPuzzleGUI(4); // 4x4
        }
    }
}