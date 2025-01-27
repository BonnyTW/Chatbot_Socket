package Socket;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Client2 {

    private JFrame frmClient;
    private JTextField textField;
    private JPanel messagePanel;
    private JScrollPane scrollPane;
    private PrintWriter out;
    private BufferedReader in;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Client2 window = new Client2();
                window.frmClient.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Create the application.
     */
    public Client2() {
        connectToServer();
        initialize();
    }

    /**
     * Connect to the server.
     */
    private void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 12345);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Prompt for username
            String username = JOptionPane.showInputDialog("Enter your username:");
            if (username == null || username.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Username cannot be empty. Exiting.");
                System.exit(0);
            }
            out.println(username); // Send username to server

            // Create a thread to listen for messages from the server
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        appendMessage(serverMessage, SwingConstants.LEFT);
                    }
                } catch (IOException e) {
                    appendMessage("Connection to server lost.", SwingConstants.LEFT);
                }
            }).start();

        } catch (IOException e) {
            appendMessage("Unable to connect to the server.", SwingConstants.LEFT);
        }
    }

    /**
     * Append a message to the message panel with alignment.
     */
    private void appendMessage(String message, int alignment) {
        SwingUtilities.invokeLater(() -> {
            JTextArea messageArea = new JTextArea(message);
            messageArea.setOpaque(true);
            messageArea.setBackground(alignment == SwingConstants.RIGHT ? new Color(238,152,253) : new Color(31,164,253));
            messageArea.setForeground(Color.WHITE);
            messageArea.setLineWrap(true); // Enable line wrapping
            messageArea.setWrapStyleWord(true); // Ensure word wrapping
            
            // Set some margin or border for spacing
            messageArea.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            JPanel alignmentPanel = new JPanel(new BorderLayout());
            alignmentPanel.setBackground(Color.BLACK);
            alignmentPanel.add(messageArea, alignment == SwingConstants.RIGHT ? BorderLayout.EAST : BorderLayout.WEST);
            
            // Adding some vertical spacing between messages
            if (alignment == SwingConstants.RIGHT) {
                alignmentPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Top and bottom padding
            } else {
                alignmentPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0)); // Top and bottom padding
            }

            messagePanel.add(alignmentPanel);
            messagePanel.revalidate();
            scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
        });
    }



    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmClient = new JFrame();
        frmClient.setTitle("Client1");
        frmClient.getContentPane().setBackground(Color.BLACK);
        frmClient.getContentPane().setLayout(new BorderLayout());

        // Message panel
        messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(Color.BLACK);

        scrollPane = new JScrollPane(messagePanel);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        frmClient.getContentPane().add(scrollPane, BorderLayout.CENTER);

        // Text field for input
        textField = new JTextField();
        textField.setForeground(Color.WHITE);
        textField.setBackground(Color.DARK_GRAY);
        textField.setCaretColor(Color.WHITE);
        textField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String message = textField.getText().trim();
                    if (!message.isEmpty()) {
                        out.println(message); // Send message to the server
                        appendMessage("You: " + message, SwingConstants.RIGHT); // Append own message to chat area
                        textField.setText(""); // Clear input field
                    }
                }
            }
        });

        frmClient.getContentPane().add(textField, BorderLayout.SOUTH);

        frmClient.setBounds(100, 100, 400, 600);
        frmClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}

