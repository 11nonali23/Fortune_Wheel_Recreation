import serverRdF.ServerRdFCreator;
import serverRdF.ServerRdFthread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerRdF {
    public static void main(String args[]) throws IOException {
        ServerRdFCreator serverRdFCreator = ServerRdFCreator.getInstance();
        if(serverRdFCreator.startServer()) {
            System.out.println("La registrazione è avvenuta con successo:\n"
                    + "verranno attivati il server ed il database");
            ServerSocket serverConnection = serverRdFCreator.getServerSocket();
            System.out.println("Server e database online");
            try {
                while(true) {
                    Socket clientConnection = serverConnection.accept();
                    System.out.println("Server: richiesta ricevuta");
                    try {
                        System.out.println("Creo un thread per gestire la connessione");
                        new ServerRdFthread(clientConnection,serverRdFCreator.getDatabase(),serverRdFCreator);
                    } catch (IOException e) {
                        // Se fallisce chiude il socket, altrimenti lo farà il thread
                        clientConnection.close();
                    }
                }
            }finally {
                System.out.println("Chiudo il server...");
                serverRdFCreator.close();
            }
        }
        else {
            System.err.println("L'inizializzazione del server non è andata a buon fine");
            System.exit(0);
        }
    }
}
