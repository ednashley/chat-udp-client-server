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

            System.out.println("\n*** BIENVENUE DANS LE CHAT " + pseudo.toUpperCase() + " ! ***\n");
            System.out.println("Voici les différentes commandes disponibles :");
            System.out.println("   o /list : voir les utilisateurs en ligne");
            System.out.println("   o @pseudo : envoyer un message privé");
            System.out.println("   o /quit : se déconnecter du chat \n");
            System.out.println("----- Commencer à discuter ----- \n");

            // Thread pour recevoir les messages
            Thread receiveThread = new Thread(() -> {
                try {
                    byte[] receiveData = new byte[1024];
                    while (running) {
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        socket.receive(receivePacket);
                        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                        // Effacer la ligne en cours pour éviter la superposition
                        System.out.print("\r" + " ".repeat(50) + "\r");

                        System.out.println(message);
                        System.out.print("Vous : ");
                    }
                } catch (Exception e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            });

            receiveThread.start();

            // Envoyer le pseudo au serveur pour l’enregistrement
            byte[] pseudoData = ("REGISTER " + pseudo).getBytes();
            DatagramPacket pseudoPacket = new DatagramPacket(pseudoData, pseudoData.length, serverAddress, SERVER_PORT);
            socket.send(pseudoPacket);

            while (running) {
                System.out.print("Vous : ");
                String message = scanner.nextLine();

                if (message.equalsIgnoreCase("/quit")) {
                    System.out.println("Déconnexion...");
                    running = false;
                    byte[] quitData = "/quit".getBytes();
                    DatagramPacket quitPacket = new DatagramPacket(quitData, quitData.length, serverAddress, SERVER_PORT);
                    socket.send(quitPacket);
                    break;
                }

                if (message.equalsIgnoreCase("/list")) {
                    byte[] listData = "/list".getBytes();
                    DatagramPacket listPacket = new DatagramPacket(listData, listData.length, serverAddress, SERVER_PORT);
                    socket.send(listPacket);
                    continue;
                }

                String fullMessage = pseudo + ": " + message;
                byte[] sendData = fullMessage.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
                socket.send(sendPacket);
            }

            socket.close();
            receiveThread.interrupt();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
