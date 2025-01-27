package Socket;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import java.awt.Color;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;

public class Server {
    
    private static Set<ClientHandler> clientHandlers = new HashSet<>();

    private JFrame frmServer;
    static JTextArea textArea;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Server window = new Server();
                window.frmServer.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try (ServerSocket serverSocket = new ServerSocket(12345)) {
            log("Server is listening...");
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            log("Error: " + e.getMessage());
        }
    }

    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clientHandlers) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public static void log(String message) {
        SwingUtilities.invokeLater(() -> textArea.append(message + "\n"));
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out = new PrintWriter(socket.getOutputStream(), true);

                // Ask for username
                out.println("Enter your username:");
                username = in.readLine();
                log(username + " joined the chat.");
                broadcast(username + " joined the chat.", this);

                String message;
                while ((message = in.readLine()) != null) {
                    String formattedMessage = username + ": " + message;
                    log(formattedMessage);
                    broadcast(formattedMessage, this);
                }
            } catch (IOException e) {
                log(username + " disconnected.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    log("Error closing connection for " + username);
                }
                clientHandlers.remove(this);
                broadcast(username + " left the chat.", this);
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }

    /**
     * Create the application.
     */
    public Server() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmServer = new JFrame();
        frmServer.setTitle("Server");
        frmServer.getContentPane().setBackground(Color.BLACK);
        frmServer.getContentPane().setLayout(null);

        textArea = new JTextArea();
        textArea.setForeground(Color.WHITE);
        textArea.setToolTipText("");
        textArea.setBackground(Color.BLACK);
        textArea.setEditable(false);
        textArea.setBounds(31, 89, 326, 491);
        frmServer.getContentPane().add(textArea);
        frmServer.setBounds(100, 100, 404, 682);
        frmServer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
