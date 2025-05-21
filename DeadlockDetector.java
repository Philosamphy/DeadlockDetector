import javax.swing.*; 
import java.awt.*;
import java.awt.event.*; 
import java.io.*;

public class DeadlockDetector extends JFrame {

    // GUI components
    private JTextField filePathField;
    private JTextArea resultArea;

    // set up the window
    public DeadlockDetector() {
        setTitle("Deadlock Detection Analyser");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // top panel for file input and browse button
        JPanel topPanel = new JPanel(new BorderLayout());
        filePathField = new JTextField(); // text field to show file path
        JButton browseButton = new JButton("Browse"); // button to open file chooser
        topPanel.add(filePathField, BorderLayout.CENTER);
        topPanel.add(browseButton, BorderLayout.EAST);

        // where results are shown
        resultArea = new JTextArea(); // text area for output
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea); 

        // button to run analysis
        JButton analyzeButton = new JButton("Analyse");

        // adding everything to window
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(analyzeButton, BorderLayout.SOUTH);

        // click "Browse" and opens a file chooser
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                filePathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        // click "Analyse" and starts checking for deadlock
        analyzeButton.addActionListener(e -> analyzeFile(filePathField.getText()));
    }

    // reads the file and runs the algorithm
    private void analyzeFile(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            // reads number of processes and resources
            int P = Integer.parseInt(br.readLine().split(":")[1].trim());
            int R = Integer.parseInt(br.readLine().split(":")[1].trim());

            // read available resources
            int[] available = parseLineToIntArray(br.readLine(), R);

            // read allocation matrix
            int[][] allocation = new int[P][R];
            for (int i = 0; i < P; i++) {
                allocation[i] = parseLineToIntArray(br.readLine(), R);
            }

            // read request matrix
            int[][] request = new int[P][R];
            for (int i = 0; i < P; i++) {
                request[i] = parseLineToIntArray(br.readLine(), R);
            }

            // run the deadlock detection
            boolean deadlock = detectDeadlock(P, R, available, allocation, request);
            resultArea.setText(deadlock ? "Deadlock detected!" : "No deadlock.");
        } catch (Exception ex) {
            resultArea.setText("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // converts a line of numbers into int array
    private int[] parseLineToIntArray(String line, int expectedLength) throws Exception {
        String[] parts = line.trim().split("\\s+"); // split by spaces
        if (parts.length != expectedLength)
            throw new Exception("Matrix/vector length mismatch.");
        int[] arr = new int[expectedLength];
        for (int i = 0; i < expectedLength; i++) {
            arr[i] = Integer.parseInt(parts[i]); // convert to int
        }
        return arr;
    }

    // deadlock detection logic (Banker's detection algorithm)
    private boolean detectDeadlock(int P, int R, int[] available, int[][] allocation, int[][] request) {
        boolean[] finished = new boolean[P]; // Tracks if each process can finish
        int[] work = available.clone(); // Copy of available to work with

        boolean progress;

        // find processes that can finish
        do {
            progress = false;
            for (int i = 0; i < P; i++) {
                if (!finished[i]) { // only checks unfinished processes
                    boolean canFinish = true;
                    for (int j = 0; j < R; j++) {
                        if (request[i][j] > work[j]) {
                            canFinish = false; // not enough resources
                            break;
                        }
                    }
                    if (canFinish) {
                        // if can finish, release resources
                        for (int j = 0; j < R; j++) {
                            work[j] += allocation[i][j];
                        }
                        finished[i] = true;
                        progress = true; // change progress from false to true (can finish)
                    }
                }
            }
        } while (progress); // repeat as long as something can finish

        // if any process still unfinished, have deadlock
        for (boolean f : finished) {
            if (!f) return true; // Deadlock exists
        }
        return false; // all processes could finish → no deadlock
    }

    // main for GUI
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DeadlockDetector().setVisible(true));
    }
}
