import playerRdF.PlayerMenu;
import serverRdF.Proxy;
import serverRdF.ServerRdFCreator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

//PlayerRdF apre un menu per il player
public class PlayerRdF {
    public static void main(String args[])  {
        Socket serverConnection;
        Proxy serverComunicator = null;
        try {
            serverConnection = new Socket(InetAddress.getLocalHost(), ServerRdFCreator.PORT);
            serverComunicator = new Proxy(serverConnection);
        } catch (IOException e) {
            System.err.println("Connessione al server non riuscita, oppure indirizzo IP non trovato");
        }
        PlayerMenu defaultMenu = new PlayerMenu(serverComunicator);
        defaultMenu.setVisible(true);
    }
}
