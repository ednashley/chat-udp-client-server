import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class UDPServer {
    private static final int PORT = 9876;
    private static Map<String, InetAddress> clients = new HashMap<>();
    private static Map<String, Integer> clientPorts = new HashMap<>();

    public static void main(String[] args) {
        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            System.out.println("Serveur UDP démarré sur le port " + PORT);
            byte[] receiveData = new byte[1024];

            while (true) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);

                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
                InetAddress clientAddress = receivePacket.getAddress();
                int clientPort = receivePacket.getPort();

                // Identifier l'expéditeur
                String clientId = clientAddress.toString() + ":" + clientPort;
                if (!clients.containsKey(clientId)) {
                    clients.put(clientId, clientAddress);
                    clientPorts.put(clientId, clientPort);
                }

                System.out.println("Message reçu de " + clientId + " : " + message);

                // Vérifier si c'est un message privé (@username message)
                if (message.startsWith("@")) {
                    String[] parts = message.split(" ", 2);
                    if (parts.length > 1) {
                        String targetUser = parts[0].substring(1); // Supprime le "@"
                        String privateMessage = "Message privé de " + clientId + ": " + parts[1];

                        // Rechercher l'utilisateur cible
                        for (Map.Entry<String, InetAddress> entry : clients.entrySet()) {
                            if (entry.getKey().equals(targetUser)) {
                                byte[] sendData = privateMessage.getBytes();
                                DatagramPacket sendPacket = new DatagramPacket(
                                        sendData, sendData.length, entry.getValue(), clientPorts.get(entry.getKey())
                                );
                                serverSocket.send(sendPacket);
                                System.out.println("Message privé envoyé à " + targetUser);
                                break;
                            }
                        }
                    }
                } else {
                    // Sinon, message public -> envoyer à tous sauf l'expéditeur
                    for (Map.Entry<String, InetAddress> entry : clients.entrySet()) {
                        if (!entry.getKey().equals(clientId)) {
                            byte[] sendData = message.getBytes();
                            DatagramPacket sendPacket = new DatagramPacket(
                                    sendData, sendData.length, entry.getValue(), clientPorts.get(entry.getKey())
                            );
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
