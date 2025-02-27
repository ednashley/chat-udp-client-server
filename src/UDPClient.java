import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9876;

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
                    while (true) {
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        socket.receive(receivePacket);
                        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        System.out.println("\n" + message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            receiveThread.start(); // Démarrer l'écoute des messages

            // Envoyer le pseudo au serveur pour l’enregistrement
            byte[] pseudoData = pseudo.getBytes();
            DatagramPacket pseudoPacket = new DatagramPacket(pseudoData, pseudoData.length, serverAddress, SERVER_PORT);
            socket.send(pseudoPacket);

            // Thread principal pour envoyer les messages
            while (true) {
                System.out.print("Vous : ");
                String message = scanner.nextLine();

                // Ajouter le pseudo à chaque message
                String fullMessage = pseudo + ": " + message;

                byte[] sendData = fullMessage.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
                socket.send(sendPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
