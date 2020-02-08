package game;

import playerRdF.Player;
import playerRdF.PlayerMenu;
import serverRdF.RdFObservable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 *Interfaccia di gestione del gioco
 */

public class GamePlayerGui extends GameGui {

    /**
     *Inizializza l'interfaccia con riferimenti a server e dati del match e del player e riferimento al menu del player
     */

    public GamePlayerGui(String relatedGameServerName, int relatedServerPort, Match matchData, Player player, PlayerMenu playerMenu) {
        this.guiDetentor=player;
        this.myTurn=false;
        this.isWheelSpinned=false;
        this.isGameStarted=false;
        this.canCallPhraseOrVocal=false;
        this.isJollyPoppedout=false;
        endGame=false;
        //setta il cardLayout per mostrare le frasi
        super.frameConfiguration(this);
        super.getDesign(this);
        super.initPhrasePanel();
        boolean remoteOk = true;
        try {
            this.remoteGuiManager = new RemoteManager(this,relatedGameServerName,relatedServerPort,matchData,player,playerMenu);
        } catch (RemoteException e) {
            System.err.println("Errore nella crezione del manager remoto di gioco");
            remoteOk = false;
        }
        if(remoteOk) {
            this.server = remoteGuiManager.getServer();
            try {
                setTitle(player.getNickname().toUpperCase() + " frame owner. "+"Match ID: " +
                        server.getMatchData().getMatchId() + ". Creator: "
                        + server.getMatchData().getManager());
            } catch (RemoteException e) {
                System.err.println("Errore di comunicazione dati con server");
            }
            setVisible(true);
        }
        else
            JOptionPane.showMessageDialog(null,"Non " + (char) 232 + " stato possibile connettersi al server");
    }

    /**
     * Metodo che richiede se usare il jolly. Prende un array dove 0 e il componente che richiede metodo
     *mentre 1 (e 2 per la lettera) l'operazione da svolgere sul server
     * */
    protected void useJollyRequest(String reason, ArrayList<String> commands) throws RemoteException {
        if(!isJollyPoppedout) {
            isJollyPoppedout=true;
            int choice = JOptionPane.showOptionDialog(null, reason + ". Vuoi usare un jolly?",
                    "Fai una scelta", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                    null, useJollyOptions, useJollyOptions[1]);
            String component=commands.get(0);
            switch (component){
                case"NOTINTIME":
                    if(choice==0)
                        server.useJolly();
                    else {
                        super.actionWaiter.stop();
                        timerLabel.setText("5 sec per lettera, 10 per frase");
                        server.outTimeCall();
                    }
                    break;
                case "WHEEL":
                    if(choice==0)
                        server.useJolly();
                    else
                        server.spinWheel(commands.get(1));
                    break;
                case "LETTERWRONG":
                    if(choice==0)
                        server.useJolly();
                    else
                        server.callLetter(commands.get(1),Integer.parseInt(commands.get(2)));
                    break;
                case "LETTEROUTTIME":
                    if(choice==0)
                        server.useJolly();
                    else
                        server.outTimeCall();
                    break;
                case "PHRASEOUTTIME":
                    if (choice==0)
                        server.useJolly();
                    else
                        server.outTimeCall();
                    break;
            }
        }
    }

    /*private void designNotEnoughPlayerPanel() {
        notEnoughPlayerPanel = new JPanel();
        notEnoughPlayerPanel.setLayout(null);
        notEnoughPlayerPanel.setBounds(phrasePanelx,phrasePanely,phrasePanelWidth,phrasePanelHeight);
        JTextField notEnoughPlayerTxt = new JTextField("In attesa di altri giocatori...");
        notEnoughPlayerTxt.setSize(phrasePanelWidth,phrasePanelHeight);
        notEnoughPlayerTxt.setEditable(false);
        notEnoughPlayerTxt.setFont(new Font("Arial",Font.BOLD,40));
        notEnoughPlayerTxt.setBackground(Color.ORANGE);
        notEnoughPlayerTxt.setHorizontalAlignment(SwingConstants.CENTER);
        notEnoughPlayerPanel.add(notEnoughPlayerTxt);
    }*/
}
