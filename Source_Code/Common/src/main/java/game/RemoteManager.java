package game;

import adminRdF.AdminMenu;
import playerRdF.Player;
import playerRdF.PlayerMenu;
import serverRdF.RdFObservable;
import useful.Menu;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 *Classe che gestisce la connessione remota tra player e game server.
 *Viene utilizzato RMI
 */

public class RemoteManager extends UnicastRemoteObject implements RdFObserver {
    private static final long serialVersionUID = 1L;
    private String gameServerName;
    private int relatedServerPort;
    private RdFObservable server;
    private GameGui gui;
    private PlayerInGame player;
    private Menu parentMenu;
    private Match matchData;


    public RemoteManager(GameGui gui, String relatedGameServerName, int relatedServerPort, Match matchData, Player player, Menu menu) throws RemoteException {
        super();
        this.gameServerName=relatedGameServerName;
        this.relatedServerPort=relatedServerPort;
        setRemoteClient();
        this.matchData = matchData;
        this.player = new PlayerInGame(player.getName(),player.getSurname(),player.getNickname(),
                player.getPassword(),player.getEmail());
        this.parentMenu = menu;
        this.gui = gui;
        this.server.addObserver(true,this);
    }

    /**
     *Metodo per cercare server su registry
     */
    private void setRemoteClient() {
        System.setSecurityManager(System.getSecurityManager());
        try {
            Registry registro = LocateRegistry.getRegistry("localhost", this.relatedServerPort);
            System.out.println("Remote handler: searching server with name: " + this.gameServerName + " and port: " + this.relatedServerPort);
            this.server = (RdFObservable) registro.lookup(this.gameServerName);
        } catch (Exception e) {
            System.err.println("Client: errore");
            e.printStackTrace();
        }
    }

    protected RdFObservable getServer(){
        return this.server;
    }

    /**
     *metodo usato per chiudere connessione. Chiuderà anche il game server
     */
    protected void closeGameConnection() {
        parentMenu.closeGameConnection(matchData.getMatchId());
    }


    /**
     *Metodo utilizzato dalla gui per eliminarsi dal server
     */
    protected void leaveMatch(){
        try {
            server.deleteObserver(true,this,gui.isGameStarted());
        } catch (RemoteException e) {
            System.err.println("Errore di comunicazione con server remoto");
            e.printStackTrace();
        }
    }

    //METODI CHIAMABILI DAL SERVER
    @Override
    public void spinWheel(String modifier) throws RemoteException {
        gui.setWheelText(modifier);
    }

    @Override
    public void setWheelSpinned(boolean isWheelToBeSpinned) throws RemoteException {
        gui.setWheelSpinned(isWheelToBeSpinned);
    }

    @Override
    public void setStartMatch(String[] phrases,String[] themes) throws RemoteException {
        gui.setStartMatch(phrases, themes);
    }

    @Override
    public void setTimer(boolean set) throws RemoteException {
        gui.setTimer(set);
    }

    @Override
    public void setJollyPoppedOut(boolean set) throws RemoteException {
        gui.setJollyPoppedOut(set);
    }

    @Override
    public void changePhrase() throws RemoteException {
        gui.changePhrase();
    }

    @Override
    public void setMyTurn(boolean isMyTurn) throws RemoteException {
        gui.setMyTurn(isMyTurn);
    }

    @Override
    public void showEquals(String letter) throws RemoteException {
        gui.showEquals(letter);
    }

    @Override
    public void setConsoleText(String text) throws RemoteException {
        gui.setConsoleText(text);
    }

    @Override
    public void setCurrentPlayerTextField(String text) throws RemoteException {
        gui.setCurrentPlayertextField(text);
    }



    @Override
    public PlayerInGame getPlayerInGame() throws RemoteException{
        return this.player;
    }

    @Override
    public void endGameLeaveMatch() throws RemoteException {
        gui.endGameLeaveMatch();
    }

    @Override
    public boolean canCallPhraseOrVocal() throws RemoteException {
        return gui.hasGuessedOneLetter();
    }

    @Override
    public void setCanCallPhraseOrVocal(boolean canCallPhraseOrVocal) throws RemoteException {
        gui.setCanCallPhraseOrVocal(canCallPhraseOrVocal);
    }

    @Override
    public void notEnoughPhrases() throws RemoteException {
        gui.notEnoughPhrases();
    }

    @Override
    public void setPlayerLabels(String player1, String player2) throws RemoteException {
        gui.setPlayerLabel(player1,player2);
    }
}
