import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class UDPClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9876;
    private static boolean running = true;

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress serverAddress = InetAddress.getByName(SERVER_ADDRESS);
            Scanner scanner = new Scanner(System.in);
            String pseudo = "";

            // Boucle pour demander un pseudo valide
            boolean pseudoValide = false;
            while (!pseudoValide) {
                System.out.print("Entrez votre pseudo : ");
                pseudo = scanner.nextLine();

                // Envoi du pseudo au serveur
                byte[] pseudoData = ("REGISTER " + pseudo).getBytes();
                DatagramPacket pseudoPacket = new DatagramPacket(pseudoData, pseudoData.length, serverAddress, SERVER_PORT);
                socket.send(pseudoPacket);

                // Attendre la réponse du serveur
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                socket.receive(receivePacket);
                String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

                // Si le serveur renvoie un message d'erreur, redemander un nouveau pseudo
                if (response.contains("est déjà utilisé")) {
                    System.out.println(response);  // Afficher le message d'erreur du serveur
                } else {
                    pseudoValide = true;  // Le pseudo est valide, on sort de la boucle
                }
            }

            // Une fois le pseudo validé, on affiche le message de bienvenue
            System.out.println("\n*** BIENVENUE DANS LE CHAT " + pseudo.toUpperCase() + " ! ***\n");
            System.out.println("Voici les différentes commandes disponibles :");
            System.out.println("   o /list : voir les utilisateurs en ligne");
            System.out.println("   o @pseudo message : envoyer un message privé");
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
                        System.out.print("\r");
                        for (int i = 0; i < 50; i++) {
                            System.out.print(" ");
                        }
                        System.out.print("\r");


                        System.out.println(message);
                        System.out.print("Vous: ");
                    }
                } catch (Exception e) {
                    if (running) {
                        e.printStackTrace();
                    }
                }
            });

            receiveThread.start();

            while (running) {
                System.out.print("Vous: ");
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

                if (message.equalsIgnoreCase("/help")) {
                    byte[] helpData = "/help".getBytes();
                    DatagramPacket helpPacket = new DatagramPacket(helpData, helpData.length, serverAddress, SERVER_PORT);
                    socket.send(helpPacket);
                    continue;
                }

                String fullMessage = pseudo + ": " + message;
                byte[] sendData = fullMessage.getBytes();
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, SERVER_PORT);
                socket.send(sendPacket);
            }

            scanner.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

