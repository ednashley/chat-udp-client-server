import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class UDPServer {
    private static final int PORT = 9876;
    private static final Map<String, InetAddress> clients = new HashMap<>();      // Associe l'ID d'un client (adresse IP + port) à son adresse IP
    private static final Map<String, Integer> clientPorts = new HashMap<>();      // Associe l'ID du client à son port UDP
    private static final Map<String, String> pseudoToClient = new HashMap<>();    // Associe un pseudo au client correspondant

    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            System.out.println("Serveur UDP démarré sur le port " + PORT);
            byte[] receiveData = new byte[1024];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();
                String clientId = clientAddress.toString() + ":" + clientPort;

                // Enregistrement du pseudo
                if (receivedMessage.startsWith("REGISTER ")) {
                    String pseudo = receivedMessage.substring(9).trim();

                    // Vérifier si le pseudo est déjà pris
                    if (pseudoToClient.containsKey(pseudo)) {
                        String errorMessage = "Le pseudo \"" + pseudo + "\" est déjà utilisé. Veuillez en choisir un autre.";
                        byte[] sendData = errorMessage.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                        serverSocket.send(sendPacket);
                    } else {
                        // Si le pseudo est disponible, enregistrer l'utilisateur
                        clients.put(clientId, clientAddress);
                        clientPorts.put(clientId, clientPort);
                        pseudoToClient.put(pseudo, clientId);

                        // Envoyer un message de bienvenue
                        String welcomeMessage = "Bienvenue " + pseudo + " ! Vous êtes maintenant connecté.";
                        byte[] sendData = welcomeMessage.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                        serverSocket.send(sendPacket);

                        System.out.println("Nouveau client : " + pseudo + " (" + clientId + ")");
                    }
                    continue;
                }

                // Gestion de la commande /list
                if (receivedMessage.equals("/list")) {
                    StringBuilder userList = new StringBuilder("Utilisateurs connectés : ");
                    for (String user : pseudoToClient.keySet()) {
                        userList.append(user).append(", ");
                    }
                    if (userList.length() > 24) {
                        userList.setLength(userList.length() - 2); // Retirer la dernière virgule
                    } else {
                        userList.append("Aucun utilisateur en ligne.");
                    }

                    byte[] sendData = userList.toString().getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                    serverSocket.send(sendPacket);
                    continue;
                }

                // Affichage de la liste des commandes disponibles
                if (receivedMessage.equals("/help")) {
                    String helpList = new String ("Commandes disponibles : \n" +
                            "   o /list : voir les utilisateurs en ligne\n" +
                            "   o @pseudo message : envoyer un message privé\n" +
                            "   o /quit : se déconnecter du chat \n");
                    byte[] sendData = helpList.getBytes();
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                    serverSocket.send(sendPacket);
                    continue;
                }

                // Gestion de la déconnexion
                if (receivedMessage.equals("/quit")) {
                    clients.remove(clientId);
                    clientPorts.remove(clientId);
                    pseudoToClient.entrySet().removeIf(entry -> entry.getValue().equals(clientId));
                    System.out.println("Client déconnecté : " + clientId);
                    continue;
                }

                // Gestion des messages publics et privés
                String pseudo = null;
                String message = receivedMessage;

                if (receivedMessage.contains(": ")) {
                    String[] parts = receivedMessage.split(": ", 2);
                    pseudo = parts[0];
                    message = parts[1];
                }

                System.out.println("Message reçu de " + pseudo + " : " + message);

                if (message.startsWith("@")) {
                    String[] parts = message.split(" ", 2);
                    if (parts.length > 1) {
                        String targetUser = parts[0].substring(1);
                        String privateMessage = "[Message privé de " + pseudo + "] " + parts[1];

                        if (pseudoToClient.containsKey(targetUser)) {
                            String targetClientId = pseudoToClient.get(targetUser);
                            InetAddress targetAddress = clients.get(targetClientId);
                            int targetPort = clientPorts.get(targetClientId);

                            byte[] sendData = privateMessage.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, targetAddress, targetPort);
                            serverSocket.send(sendPacket);

                            System.out.println("Message privé envoyé à " + targetUser);
                        } else {
                            System.out.println("Utilisateur " + targetUser + " introuvable.");
                        }
                    }
                } else {
                    // Message public -> envoyer à tous sauf l'expéditeur
                    for (Map.Entry<String, InetAddress> entry : clients.entrySet()) {
                        if (!entry.getKey().equals(clientId)) {
                            byte[] sendData = receivedMessage.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, entry.getValue(), clientPorts.get(entry.getKey()));
                            serverSocket.send(sendPacket);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
