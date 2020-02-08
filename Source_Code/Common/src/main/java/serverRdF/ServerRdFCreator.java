package serverRdF;

import dbRdF.DbRdF;
import dbRdF.SubDbRdF;
import game.Match;
import useful.SendMail;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *La classe creata è conforme al pattern Signleton
 * Per fare ciò il costruttore è privato e il metodo statico
 * ne ritorna una signola istanza
 */

public class ServerRdFCreator {
    private static ServerRdFCreator istanzaServer = null;
    private boolean started;
    private ServerSocket serverConn;
    public static final int PORT = 8888;
    private static DbRdF database;
    //Il server ha istanza email sender per mandare mail
    private static SendMail sendMail = new SendMail();
    //Il server mantiene i codici delle mail da inviare
    private static int emailCode = 3678; //inizializzo il codice in maniera random a 4 cifre
    //Il server terrà una copia di tutti i giochi live da passare ai serverThread
    private static List<GameServerRdF> liveGames;
    private final static String serverSuffix = "GAME_SERVER";
    private static int gameServerCounter=0;
    private static int gameServerPort = 8888;

    private ServerRdFCreator() {
        System.out.println("Creo istanza server");
        this.started = false;
        database = new DbRdF();
        liveGames = new ArrayList<>();
        try {
            serverConn = new ServerSocket(PORT);
        } catch (IOException e) {
            System.err.println("Errore nella creazione del server socket");
        }
    }

    /**
     * Ritorna un istanza del server se non è gia stata creata oppure l'istanza già creata
     */
    synchronized public static ServerRdFCreator getInstance() {
        if(istanzaServer == null)
            istanzaServer = new ServerRdFCreator();
        return istanzaServer;
    }

    synchronized public static SendMail getSendMailServer(){
        return sendMail;
    }

    /**
     * Genera un codice email
     *
     */
    synchronized public static int getEmailCode(){
        //Voglio evitare che il codice email abbia più di 6 cifre
        if(emailCode/1000000>0)
            emailCode=3678;
        emailCode++;
        return emailCode;
    }

    /**
     *Manda ai client la lista dei giochi attualmente in corso di svolgimento
     * o in attesa di giocatori
     */

    synchronized protected static List<GameServerRdF> getLiveGames(){
        return liveGames;
    }

    /**
     Manda ai client la lista con i nomi dei giochi attualmente in corso di svolgimento
     * o in attesa di giocatori
     */
    synchronized protected List<String> getLiveGamesName(){
        List<String> gameNames = new ArrayList<>();
        for(GameServerRdF game: liveGames)
            gameNames.add(game.getServerName());
        return gameNames;
    }

    public Object getLiveGamesNameNotStarted() throws RemoteException {
        List<String> gameNames = new ArrayList<>();
        for(GameServerRdF game: liveGames)
            if(!game.getMatchData().isStarted())
                gameNames.add(game.getServerName());
        return gameNames;
    }


    /**
     * Avvia processo di iscrizione dell'admin del database
     * @return
     */
    synchronized private boolean adminSub() {
        boolean subOk = false;
        SubDbRdF sa = new SubDbRdF(istanzaServer);
        while(!sa.isSubbed()) {
            try {
                System.out.println("Aspetto che l'utente si registri");
                wait();
                if(sa.isClosed()) {
                    System.out.println("Finestra chiusa");
                    System.exit(0);
                }
                if(sa.isSubbed()) {
                    subOk = true;
                    sa.close();
                }
            } catch (InterruptedException e) {
                System.err.println("Attesa di registrazione interrotta");
            }
        }
        return subOk;
    }

    /**
     * Inizializza la procedura di iscrizione per il server
     * @return
     * @throws IOException
     */
    public boolean startServer()  {
        return adminSub();
    }

    /**
     * Crea un nuovo server di gioco su una nuova porta
     * @param match
     * @throws RemoteException
     */
    synchronized protected void createNewMatch(Match match) throws RemoteException {
        gameServerCounter++;
        gameServerPort++;
        GameServerRdF gameServerRdF = new GameServerRdF(serverSuffix+gameServerCounter, gameServerPort,match);
        liveGames.add(gameServerRdF);
    }

    /**
     * rimuove il server di gioco al termine della partita oppure se un giocatore lascia la partita
     * @param matchId
     * @throws RemoteException
     */
    synchronized protected void removeMatchServer(int matchId) throws RemoteException {
        for (GameServerRdF gameServerRdF: liveGames)
            if(gameServerRdF.getMatchData().getMatchId()==matchId) {
                System.out.println("Server will remove gameServer" + gameServerRdF.getServerName());
                liveGames.remove(gameServerRdF);
                gameServerCounter--;
                break;
            }
    }

    /**
     *manda ai client numero di porta e nome del server per aggiungersi al gioco come osservatore o giocatore
     * @param matchId
     * @return
     * @throws RemoteException
     */
    synchronized protected List<Object> getServerDataFromId(int matchId) throws RemoteException {
        List<Object> returnGame = new ArrayList();
        for (GameServerRdF gameServerRdF: liveGames)
            if(gameServerRdF.getMatchData().getMatchId()==matchId) {
                System.out.println(gameServerRdF.getServerName());
                System.out.println(gameServerRdF.getServerPort());
                returnGame.add(gameServerRdF.getServerName());
                returnGame.add(gameServerRdF.getServerPort());
            }
        return returnGame;
    }

    /**
     * Manda al richidente i dati della partita di un particolare game server
     * @param serverName
     * @return
     * @throws RemoteException
     */
    synchronized protected Match getMatchData(String serverName) throws RemoteException {
        Match matchData = null;
        for(GameServerRdF gameServerRdF: liveGames)
            if (gameServerRdF.getServerName().equals(serverName))
                matchData = gameServerRdF.getMatchData();
        return  matchData;
    }

    public ServerSocket getServerSocket() {
        return serverConn;
    }

    /**
     * Manda alla classe serverRdF il database istanziato per aprire connessione
     * @return
     */
    synchronized public DbRdF getDatabase(){
        return  this.database;
    }


    /**
     * Utilizzato dalle interfacce di login e iscrizione del database per notificare
     * il server dell'avvenuta iscrizione
     */
    synchronized public void notifyMe() {
        notifyAll();
    }

    /**
     * chiude il server
     */
    public void close() {
        try {
            this.serverConn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

