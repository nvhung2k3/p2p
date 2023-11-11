package peertopeer;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class PeerServer {
    private JFrame frame;
    private JTextArea textArea;
    private JTextField textField, peerPortField;
    private JButton connectButton, sendButton;
    private int port;
    private List<PrintWriter> connectedPeers;

    public PeerServer(int port) {
        this.port = port;
        this.connectedPeers = new ArrayList<>();

        frame = new JFrame("Peer " + port);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        textField = new JTextField(20);
        sendButton = new JButton("Gửi");
        peerPortField = new JTextField(5);
        connectButton = new JButton("kết nối tới Peer");
        panel.add(peerPortField);
        panel.add(connectButton);
        panel.add(textField);
        panel.add(sendButton);
        frame.add(panel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> {
            for (PrintWriter out : connectedPeers) {
                out.println(textField.getText());
            }
            textArea.append("Me: " + textField.getText() + "\n");
            textField.setText("");
        });

        connectButton.addActionListener(e -> {
            int peerPort = Integer.parseInt(peerPortField.getText());
            try {
                Socket peerSocket = new Socket("127.0.0.1", peerPort);
                PrintWriter out = new PrintWriter(peerSocket.getOutputStream(), true);
                connectedPeers.add(out);
                BufferedReader in = new BufferedReader(new InputStreamReader(peerSocket.getInputStream()));
                new Thread(() -> handleInput(in)).start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        frame.setVisible(true);
        startListening();
    }

    private void startListening() {
        new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    connectedPeers.add(out);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    new Thread(() -> handleInput(in)).start();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void handleInput(BufferedReader in) {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                String finalMessage = message;
                SwingUtilities.invokeLater(() -> {
                    textArea.append("Peer: " + finalMessage + "\n");
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = Integer.parseInt(JOptionPane.showInputDialog("nhập cổng cho peer này:"));
        SwingUtilities.invokeLater(() -> new PeerServer(port));
    }
}
