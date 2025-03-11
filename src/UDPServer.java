import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class UDPServer {
    private static final int PORT = 9876;
    private static final Map<String, InetAddress> clients = new HashMap<>();
    private static final Map<String, Integer> clientPorts = new HashMap<>();
    private static final Map<String, String> pseudoToClient = new HashMap<>();

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
                    clients.put(clientId, clientAddress);
                    clientPorts.put(clientId, clientPort);
                    pseudoToClient.put(pseudo, clientId);

                    System.out.println("Nouveau client : " + pseudo + " (" + clientId + ")");
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

                        // Vérifier si le destinataire existe
                        if (pseudoToClient.containsKey(targetUser)) {
                            String targetClientId = pseudoToClient.get(targetUser);
                            InetAddress targetAddress = clients.get(targetClientId);
                            int targetPort = clientPorts.get(targetClientId);

                            byte[] sendData = privateMessage.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(
                                    sendData, sendData.length, targetAddress, targetPort);
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
                            DatagramPacket sendPacket = new DatagramPacket(
                                    sendData, sendData.length, entry.getValue(), clientPorts.get(entry.getKey()));
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
