package serverRdF;

import dbRdF.DbRdFStatistics;
import game.GamePlayerGui;
import game.Match;
import game.Phrase;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Questa classe è conforme al pattern proxy e solleva i client dal compito di comunicare con i serverThread
 * in tutte le fasi di connessione al server tranne quella di gioco (gestita da GameServerRdF)
 */
public class Proxy {
    private Socket serverConnection;
    private ObjectOutputStream toServer;
    private ObjectInputStream fromServer;

    public Proxy(Socket socket){
        this.serverConnection = socket;
        try {
            this.toServer = new ObjectOutputStream(this.serverConnection.getOutputStream());
            fromServer = new ObjectInputStream(this.serverConnection.getInputStream());
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Permette di iscriversi al server
     * @param nickame
     * @param email
     * @param name
     * @param surname
     * @param pwd
     * @param adminOrUser
     * @return
     */
    synchronized public boolean sub(String nickame,String email,String name,String surname,String pwd,String adminOrUser) {
        try {
            toServer.writeObject("REGISTRAZIONE");
            toServer.writeObject(nickame);
            toServer.writeObject(email);
            toServer.writeObject(name);
            toServer.writeObject(surname);
            toServer.writeObject(pwd);
            toServer.writeObject(adminOrUser);
        }catch (IOException exc){
            System.err.println("Errore di scrittura su server");
        }
        return serverResponse();
    }

    /**
     * Permette di loggarsi nel gioco e ritorna i dati all'interfaccia di chi effettua il login
     * @param email
     * @param password
     * @param adminOrUser
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    synchronized public List<String> log_piu_dati(String email, String password,String adminOrUser) throws IOException, ClassNotFoundException {
        try {
            toServer.writeObject("ACCESSOEDATI");
            toServer.writeObject(email);
            toServer.writeObject(password);
            toServer.writeObject(adminOrUser);
        }catch (IOException exc){
            System.err.println("Errore di scrittura su server");
        }
        return (List<String>) fromServer.readObject();
    }

    /**
     * Richiede di cercare tutte le frasi nel database
     * @return
     */
    synchronized public Phrase[] queryAllPhrases()  {
        Phrase[] phrases = null;
        List<Phrase> phraseList;
        try {
            toServer.writeObject("QUERYALLPHRASES");
        }catch (IOException exc){
            System.err.println("Errore di scrittura su server");
        }
        if(serverResponse()){
            try {
                phraseList = (List<Phrase>) fromServer.readObject();
                phrases = new Phrase[phraseList.size()];
                for(int i = 0; i<phraseList.size(); i++)
                    phrases[i] = phraseList.get(i);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        else
            phrases = new Phrase[0];
        return phrases;
    }

    /**
     * Richiede di aggiungere una frase nel database
     * @param frasi
     * @return
     */
    synchronized public boolean addPhrases(List<String> frasi) {
        try {
            toServer.writeObject("ADDPHRASES");
            toServer.writeObject(frasi);
        }catch (IOException exc){
            System.err.println("Errore nella comunicazione delle frasi");
        }
        return serverResponse();
    }

    /**
     * Richiede di eliminare una frase dal database
     * @param frasi
     * @return
     */
    synchronized public boolean delPhrase(List<String> frasi) {
        if (frasi.size() == 0)
            System.err.println("Nessuna frase selezionata");
        else {
            try {
                toServer.writeObject("DELPHRASES");
                toServer.writeObject(frasi);
            } catch (IOException exc) {
                System.err.println("Errore nella comunicazione delle frasi");
            }
        }
        return serverResponse();
    }

    /**
     * Richiede di ottenre istanza del database delle statistiche
     */
    synchronized public DbRdFStatistics getStatisticsDatabase() {
        DbRdFStatistics statisticDatabase = null;
        try {
            toServer.writeObject("GETSTATISTICSDB");
            System.out.println("Proxy: Waiting for statistics db");
        }
        catch (IOException e){
            System.err.println("Errore durante comunicazione con server");
        }
        try {
            statisticDatabase = (DbRdFStatistics)fromServer.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if(statisticDatabase==null)
            System.out.println("null");
        return statisticDatabase;
    }

    /**
     * Richiede di aggiungere una nuova partita
     * @param name
     * @param matchName
     * @return
     */
    synchronized public Match addNewMatch(String name, String matchName){
        Match newMatch = null;
        try {
            toServer.writeObject("NEWMATCH");
            toServer.writeObject(name);
            toServer.writeObject(matchName);
        } catch (IOException e) {
            System.err.println("Errore durante comunicazione al server della nuova partita");
        }
        if(serverResponse()) {
            try {
                newMatch = (Match) fromServer.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return newMatch;
    }

    /**
     * Richiede i dati del server
     * @param newMatch
     * @return
     */
    synchronized public List<Object> getMyServerData(Match newMatch) {
        List<Object> serverData = new ArrayList<>();
        int matchId = newMatch.getMatchId();
        try {
            toServer.writeObject("GETSERVERDATA");
            toServer.writeObject(matchId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(serverResponse()) {
            try {
                serverData = (List<Object>) fromServer.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return serverData;
    }

    /**
     * Richiede i dati della partita di un macth server
     * @param serverName
     * @return
     */
    synchronized public Match getMatchData(String serverName) {
        Match matchData = null;
        try {
            toServer.writeObject("MATCHDATA");
            toServer.writeObject(serverName);
            if(serverResponse())
                matchData=(Match)fromServer.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return matchData;
    }

    /**
     * Richiede i giochi in attesa di 3 giocatori oopure di osservatori
     * @return
     */
    synchronized public List<String> getLiveGames(){
        List<String> returnGame = new ArrayList();
        try {
            toServer.writeObject("GETGAME");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            returnGame = (List<String>) fromServer.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    return returnGame;
    }

    /**
     * Richiede i giochi non ancora iniziati
     * @return
     */
    public List<String> getLiveGamesNotStarted() {
        List<String> returnGame = new ArrayList();
        try {
            toServer.writeObject("GETGAMENOTSTARTED");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            returnGame = (List<String>) fromServer.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return returnGame;
    }

    /**
     * Richiede un codice email al server
     * @return
     */
    synchronized public int getEmailCode(){
        int code = 0;
        try {
            toServer.writeObject("GETEMAILCODE");
        } catch (IOException e) {
            System.err.println("Errore durante comunicazione con server");
        }
        try {
            code = (int) fromServer.readObject();
        } catch (IOException | ClassNotFoundException e){
            System.err.println("Proxy: errore nel ricavare codice email");
        }
        return code;
    }

    /**
     * Richiede di controllare se esiste già un utente con email e nickname uguali nel database
     * @param mail
     * @param nickname
     * @return
     */
    synchronized public boolean checkEmailUsername(String mail,String nickname){
        try {
            toServer.writeObject("CHECKEMAILANDUSER");
            toServer.writeObject(mail);
            toServer.writeObject(nickname);
        } catch (IOException e) {
            System.err.println("Errore durante comunicazione con server");
        }
        if (serverResponse())
            return true;
        else
            return false;
    }

    /**
     * Richiede di modificare i dati del proprio profilo
     * @param changesValue
     */
    synchronized public boolean changeProfile(HashMap<String,String> changesValue)  {
        try {
            toServer.writeObject("CHANGEPROFILEDATA");
            toServer.writeObject(changesValue);
        } catch (IOException e) {
            System.err.println("Errore durante comunicazione con server");
        }
        return serverResponse();
    }

    /**
     * Richiede il reset della password
     * @param mail
     * @return
     */
    synchronized public boolean resetPassword(String mail) {
        try {
            toServer.writeObject("RESETPASSWORD");
            toServer.writeObject(mail);
        } catch (IOException e) {
            System.err.println("Errore durante comunicazione con server");
        }
        if (serverResponse())
            return true;
        else
            return false;
    }

    /**
     * Richiede la chiusura del match server
     * @param matchId
     */
    synchronized public void closeGameConnection(int matchId){
        try {
            toServer.writeObject("CLOSEGAMECONNECTION");
            toServer.writeObject(matchId);
        } catch (IOException e) {
            System.err.println("Errore durante comunicazione con server");
        }
    }

    /**
     * Richiede la chiusura della connessione con il serverThread
     */
    synchronized public void closeConnection(){
        try {
            toServer.writeObject("CLOSE");
        } catch (IOException e) {
            System.err.println("Errore di comunicazione su server per chiusura");
        }
    }

    private boolean serverResponse(){
        String serverResponse = null;
        try {
            serverResponse = (String) fromServer.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore nella comunicazione lettura risposta server");
        }
        if (serverResponse.equals("OK"))
            return true;
        else
            return false;
    }
}
