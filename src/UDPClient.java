import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9876;
    private static boolean running = true; // Variable pour contrôler l'écoute

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);
            Scanner scanner = new Scanner(System.in);

            System.out.print("Entrez votre pseudo : ");
            String pseudo = scanner.nextLine();

            // Thread pour recevoir les messages
            Thread receiveThread = new Thread(() -> {
                try {
                    byte[] receiveData = new byte[1024];
                    while (running) { // Écoute uniquement si `running` est vrai
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        socket.receive(receivePacket);
                        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                        // Efface la ligne en cours pour éviter la superposition
                        System.out.print("\r" + " ".repeat(50) + "\r");

                        System.out.println(message);
                        System.out.print("Vous : ");
                    }
                } catch (Exception e) {
                    if (running) { // Si le socket est fermé normalement, on évite l'affichage de l'erreur
                        e.printStackTrace();
                    }
                }
            });

            receiveThread.start(); // Démarrer l'écoute des messages

            // Envoyer le pseudo au serveur pour l’enregistrement
            byte[] pseudoData = ("REGISTER " + pseudo).getBytes();
            DatagramPacket pseudoPacket = new DatagramPacket(pseudoData, pseudoData.length, serverAddress, SERVER_PORT);
            socket.send(pseudoPacket);

            // Thread principal pour envoyer les messages
            while (running) {
                System.out.print("Vous : ");
                String message = scanner.nextLine();

                if (message.equalsIgnoreCase("/quit")) {
                    System.out.println("Déconnexion...");
                    running = false;
                    break;
                }

                // Ajouter le pseudo à chaque message
                String fullMessage = pseudo + ": " + message;

                byte[] sendData = fullMessage.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
                socket.send(sendPacket);
            }

            // Fermer le socket après l'arrêt du thread
            socket.close();
            receiveThread.interrupt(); // Arrêter proprement le thread d'écoute

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
