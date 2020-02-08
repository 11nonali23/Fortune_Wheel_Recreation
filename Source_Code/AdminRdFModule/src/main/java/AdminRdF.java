import adminRdF.AdminMenu;
import serverRdF.Proxy;
import serverRdF.ServerRdFCreator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class AdminRdF {
    public static void main(String args[])  {
        Socket serverConnection ;
        Proxy serverComunicator = null;
        try {
            serverConnection = new Socket(InetAddress.getLocalHost(), ServerRdFCreator.PORT);
            serverComunicator = new Proxy(serverConnection);
        } catch (IOException e) {
            System.err.println("Connessione al server non riuscita, oppure indirizzo IP server non trovato");
        }
        AdminMenu defaultMenu = new AdminMenu(serverComunicator);
        defaultMenu.setVisible(true);
    }
}
