import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TicTacToe {

    private static final int PORT = 12345;
    private static JFrame frame;
    private static JButton[] buttons;
    private static PrintWriter out;
    private static BufferedReader in;
    private static boolean isServer;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
            if (args.length > 0 && args[0].equals("server")) {
                startServer();
            } else {
                startClient();
            }
        });
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Tic Tac Toe");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(3, 3));

        buttons = new JButton[9];
        for (int i = 0; i < 9; i++) {
            buttons[i] = new JButton("");
            buttons[i].setFont(new Font("Arial", Font.PLAIN, 40));
            buttons[i].addActionListener(new ButtonClickListener(i));
            frame.add(buttons[i]);
        }

        frame.setSize(300, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Waiting for client connection...");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected");

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            isServer = true;

            waitForOpponent();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startClient() {
        try {
            String serverAddress = JOptionPane.showInputDialog("Enter server IP address:");
            Socket socket = new Socket(serverAddress, PORT);

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            isServer = false;

            waitForOpponent();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void waitForOpponent() {
        try {
            JOptionPane.showMessageDialog(frame, "Waiting for opponent to connect...");
            String response = in.readLine();
            JOptionPane.showMessageDialog(frame, "Opponent connected! You are " + (response.equals("X") ? "O" : "X"));
            playGame(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void playGame(String marker) {
        while (true) {
            try {
                String move = in.readLine();
                int index = Integer.parseInt(move);
                buttons[index].setText(marker);

                if (checkWin(marker)) {
                    JOptionPane.showMessageDialog(frame, "Player " + marker + " wins!");
                    break;
                }

                if (isBoardFull()) {
                    JOptionPane.showMessageDialog(frame, "It's a draw!");
                    break;
                }

                // Enable the buttons for the current player
                for (JButton button : buttons) {
                    button.setEnabled(!button.getText().equals("X") && !button.getText().equals("O"));
                }

                // Wait for the other player's move
                move = in.readLine();
                index = Integer.parseInt(move);
                buttons[index].setText(marker.equals("X") ? "O" : "X");

                if (checkWin(marker.equals("X") ? "O" : "X")) {
                    JOptionPane.showMessageDialog(frame, "Player " + (marker.equals("X") ? "O" : "X") + " wins!");
                    break;
                }

                if (isBoardFull()) {
                    JOptionPane.showMessageDialog(frame, "It's a draw!");
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
    }

    private static boolean checkWin(String marker) {
        // Check rows
        for (int i = 0; i < 3; i++) {
            if (buttons[i].getText().equals(marker) &&
                    buttons[i + 3].getText().equals(marker) &&
                    buttons[i + 6].getText().equals(marker)) {
                return true;
            }
        }

        // Check columns
        for (int i = 0; i < 9; i += 3) {
            if (buttons[i].getText().equals(marker) &&
                    buttons[i + 1].getText().equals(marker) &&
                    buttons[i + 2].getText().equals(marker)) {
                return true;
            }
        }

        // Check diagonals
        if (buttons[0].getText().equals(marker) &&
                buttons[4].getText().equals(marker) &&
                buttons[8].getText().equals(marker)) {
            return true;
        }
        if (buttons[2].getText().equals(marker) &&
                buttons[4].getText().equals(marker) &&
                buttons[6].getText().equals(marker)) {
            return true;
        }

        return false;
    }

    private static boolean isBoardFull() {
        for (JButton button : buttons) {
            if (button.getText().equals("")) {
                return false;
            }
        }
        return true;
    }

    private static class ButtonClickListener implements ActionListener {
        private final int index;

        public ButtonClickListener(int index) {
            this.index = index;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            buttons[index].setText(isServer ? "X" : "O");
            out.println(index);

            if (checkWin(isServer ? "X" : "O")) {
                JOptionPane.showMessageDialog(frame, "Player " + (isServer ? "X" : "O") + " wins!");
                System.exit(0);
            }

            if (isBoardFull()) {
                JOptionPane.showMessageDialog(frame, "It's a draw!");
                System.exit(0);
            }

            for (JButton button : buttons) {
                button.setEnabled(false);
            }
        }
    }
}
