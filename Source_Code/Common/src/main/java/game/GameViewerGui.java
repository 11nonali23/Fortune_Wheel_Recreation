package game;

import playerRdF.Player;

import java.awt.*;
import java.rmi.RemoteException;

/**
 *Interfaccia di gestione dell'osservatore.
 * I metodi sono simili al viewer
 */

public class GameViewerGui extends GameGui {

    public GameViewerGui(String relatedGameServerName, int relatedServerPort, Match matchData, Player player, useful.Menu menu) {
        super.isGameStarted=false;
        super.canCallPhraseOrVocal=false;
        //setta il cardLayout per mostrare le frasi
        super.frameConfiguration(this);
        super.getDesign(this);
        super.initPhrasePanel();
        try {
            super.remoteGuiManager = new RemoteManager(this,relatedGameServerName,relatedServerPort,matchData,player,menu);
        } catch (RemoteException e) {
            System.err.println("Errore nella crezione del manager remoto di gioco");
            e.printStackTrace();
        }
        this.server= remoteGuiManager.getServer();
        try {
            setTitle("Match ID: " + server.getMatchData().getMatchId() + " Creatore: " + server.getMatchData().getManager());
        } catch (RemoteException e) {
            System.err.println("Errore di comunicazione dati con server");
        }
        setVisible(true);
    }
}
