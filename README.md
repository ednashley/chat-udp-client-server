# [EN] UDP Chat Application

## Description
This project implements a simple UDP-based chat application with a client-server architecture. Users can register with a unique pseudonym, send public and private messages, and view the list of connected users.

## Features
- User registration with unique pseudonym validation.
- Public and private messaging.
- List connected users with `/list` command.
- Disconnect with `/quit` command.
- Help command `/help` for available commands.

## Requirements
- Java 8 or later

## Usage

### Server
1. Compile the server:

    ```bash
    javac UDPServer.java
    ```

2. Run the server:

    ```bash
    java UDPServer
    ```

### Client
1. Compile the client:

    ```bash
    javac UDPClient.java
    ```

2. Run the client:

    ```bash
    java UDPClient
    ```

### Commands
- `/list` : Display connected users.
- `/help` : Show available commands.
- `/quit` : Disconnect from the chat.
- `@pseudo message` : Send a private message.

## How It Works
1. The client registers with a unique pseudonym.
2. Messages are sent via UDP packets.
3. The server manages connected clients and relays messages.

## Limitation
- No message history.


---

# [FR] Application de Chat UDP

## Description
Ce projet implémente une application de chat simple basée sur UDP avec une architecture client-serveur. Les utilisateurs peuvent s'inscrire avec un pseudonyme unique, envoyer des messages publics et privés, et afficher la liste des utilisateurs connectés.

## Fonctionnalités
- Inscription avec validation d'un pseudonyme unique.
- Messagerie publique et privée.
- Afficher les utilisateurs connectés avec la commande `/list`.
- Se déconnecter avec la commande `/quit`.
- Aide avec la commande `/help`.

## Prérequis
- Java 8 ou version ultérieure

## Utilisation

### Serveur
1. Compiler le serveur :

    ```bash
    javac UDPServer.java
    ```

2. Exécuter le serveur :

    ```bash
    java UDPServer
    ```

### Client
1. Compiler le client :

    ```bash
    javac UDPClient.java
    ```

2. Exécuter le client :

    ```bash
    java UDPClient
    ```

### Commandes
- `/list` : Afficher les utilisateurs connectés.
- `/help` : Afficher les commandes disponibles.
- `/quit` : Se déconnecter du chat.
- `@pseudo message` : Envoyer un message privé.

## Fonctionnement
1. Le client s'inscrit avec un pseudonyme unique.
2. Les messages sont envoyés via des paquets UDP.
3. Le serveur gère les clients connectés et relaye les messages.

## Limitation
- Pas d'historique des messages.

