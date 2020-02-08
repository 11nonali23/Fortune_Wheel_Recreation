package serverRdF;

import dbRdF.DbRdF;
import dbRdF.DbRdFStatistics;
import game.Match;
import game.Phrase;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Questa classe è creata per ogni client che vuole comunicare con il server per evitare l'effetto di bottom neck sul
 * server principale. Essa esegue tutte le richieste inviate dal proxy
 */
public class ServerRdFthread extends Thread {
    private DbRdF dbase;
    private Socket socket;
    private ObjectOutputStream toClient;
    private ObjectInputStream fromClient;
    private int deafultRequests;
    private ServerRdFCreator server;

    public ServerRdFthread(Socket s, DbRdF dbc,ServerRdFCreator server) throws IOException {
        this.deafultRequests=0;
        this.server = server;
        this.dbase = dbc;
        this.socket = s;
        this.toClient = new ObjectOutputStream(socket.getOutputStream());
        fromClient = new ObjectInputStream(socket.getInputStream());
        start();
    }

    public void run() {
        boolean fine = false;
        while(!fine) {
            String request = "";
            try {
                request = (String)fromClient.readObject();
            } catch (IOException | ClassNotFoundException e) {
                if(e instanceof EOFException){
                    System.out.println("ServerThread: chiusura inaspettata del client, verrà chiusa la connessione: ");
                    closeConnection();
                }
                else {
                    System.err.println("ServerThread: Errore di comunicazione con client");
                    e.printStackTrace();
                }
            }
            System.out.println("Il client chiede l' operazione di: " + request);
            switch (request) {
                case ("REGISTRAZIONE"):
                    sub();
                    break;

                case ("ACCESSO"):
                    login();
                    break;

                case ("ACCESSOEDATI"):
                    log_piu_dati();
                    break;

                case("NEWMATCH"):
                    addNewMatch();
                    break;

                case("ADDMATCHTOLIVELIST"):
                    addMatchToLiveList();
                    break;

                case("GETLIVEGAMES"):
                    returnLiveGames();
                    break;

                case("GETGAME"):
                    returnLiveGames();
                    break;

                case("GETGAMENOTSTARTED"):
                    returnGameNotStarted();
                    break;

                case("GETSERVERDATA"):
                    getServerData();
                    break;

                case("MATCHDATA"):
                    getMatchData();
                    break;

                case("QUERYALLPHRASES"):
                    queryAllPhrases();
                    break;

                case("ADDPHRASES"):
                    addPhrases();
                    break;

                case("DELPHRASES"):
                    delPhrases();
                    break;

                case ("GETSTATISTICSDB"):
                    returnStatisticsDb();
                    break;

                case ("GETEMAILCODE"):
                    getEmailCode();
                    break;

                case("CHECKEMAILANDUSER"):
                    checkEmailUsername();
                    break;

                case("CHANGEPROFILEDATA"):
                    changeProfileData();
                    break;

                case("RESETPASSWORD"):
                    resetPassword();
                    break;
                case("CLOSEGAMECONNECTION"):
                    closeGameConnection();
                    break;
                case ("CLOSE"):
                    fine = true;
                    System.out.println("Richiesta chiusura connessione, il thread terminerà");
                    break;

                default:
                    System.out.println("operazione di default");
                    this.deafultRequests++;
                    if(deafultRequests==10) {
                        System.out.println("Errore: troppe richieste ravvicinate.Il thread terminerà");
                        fine =true;
                    }
                    break;
            }
        }
        closeConnection();
    }

    private void changeProfileData() {
        HashMap<String,String> changes = null;
        try {
            changes = (HashMap<String,String>) fromClient.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        response(dbase.changeProfileData(changes));
    }

    private void closeGameConnection() {
        int matchId = 0;
        try {
            matchId = (int) fromClient.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            server.removeMatchServer(matchId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void login(){
        boolean accOk;
        System.out.println("Richiesta di registrazione");
        try {
            accOk = dbase.accesso((String) fromClient.readObject(),(String)fromClient.readObject(),
                    (String)fromClient.readObject());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore di comunicazione con server");
            accOk = false;
        }
        response(accOk);
    }
    //metodo di accesso più ritorno dati utenti
    private void log_piu_dati(){
        List<String> subOK = null;
        System.out.println("Richiesta di registrazione");
        try {
            subOK = dbase.log_piu_dati((String) fromClient.readObject(),(String)fromClient.readObject(),
                    (String)fromClient.readObject());
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore di comunicazione con server");
        }
        try {
            toClient.writeObject(subOK);
        } catch (IOException e) {
            System.err.println("Errrore invio dati al client");
        }
    }
    private void sub(){
        boolean regOk;
        try {
            regOk = dbase.registrazione((String) fromClient.readObject(),
                    (String)fromClient.readObject(), (String)fromClient.readObject(), (String)fromClient.readObject(), (String)fromClient.readObject(),(String)fromClient.readObject());
        }catch (IOException | ClassNotFoundException e) {
            System.out.println("Errore di lettura");
            regOk = false;
        }
        response(regOk);
    }
    //aggiunge un nuovo match alla lista del database
    private void addNewMatch() {
        Match newMatch = null;
        String name = "";
        String matchName = "";
        try {
            name = (String) fromClient.readObject();
            matchName = (String) fromClient.readObject();
        }catch (IOException | ClassNotFoundException exc){
            System.err.println("Errore di lettura dati client");
        }
        newMatch = dbase.addNewMatch(name,matchName);
        if(newMatch!=null){
            System.out.println("ServerThread will start new GameServer");
            try {
                server.createNewMatch(newMatch);
                System.out.println(newMatch.getMatchId());
                List<GameServerRdF> games = server.getLiveGames();
                for(GameServerRdF game:games)
                    System.out.println(game.getMatchData().getMatchId());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        response(newMatch!=null,newMatch);
    }

    private void returnStatisticsDb() {
        DbRdFStatistics returnStatisticsDb = DbRdF.getStatisticsDatabase();
        try {
            toClient.writeObject(returnStatisticsDb);
        } catch (IOException e) {
            System.out.println("Errore di comunicazione con il client");
        }
    }

    //Ritorna un nome per il client che cerca il suo server
    private void getServerData() {
        int matchId =-1;
        List<Object> serverData = new ArrayList();
        try {
            matchId = (int)fromClient.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if(matchId!=-1) {
            try {
                serverData = server.getServerDataFromId(matchId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        response(serverData.get(0)!=null,serverData);
    }
    //ritorna i dati del match al client
    private void getMatchData() {
        Match matchData = null;
        try {
            matchData= server.getMatchData((String)fromClient.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        response(matchData!=null,matchData);
    }
    //Aggiunge il match alla live list del server
    private void addMatchToLiveList(){
        try {
            server.getLiveGames().add((GameServerRdF) fromClient.readObject());
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Errore durante la lettura del nuovo match dal client");
        }
    }
    //ritorna i giochi live
     private void returnLiveGames(){
        try {
            toClient.writeObject(server.getLiveGamesName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void returnGameNotStarted() {
        try {
            toClient.writeObject(server.getLiveGamesNameNotStarted());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*Ritorna un singolo gioco al client
    synchronized private void returnLiveGame(){
        GameServerRdF returnGame = null;
        try {
            String clientGameRequest = (String) fromClient.readObject();
            List<GameServerRdF> gameList = ServerRdF.getLiveGames();
            for(GameServerRdF game: gameList) {
                //System.out.println("ServerRDFTHREAD:" + game.getMatchData().getMatchName());
                //if (game.getMatchData().getMatchName().equals(clientGameRequest))
                    //returnGame = game;
            }
             toClient.writeObject(returnGame);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    *
     */
    //aggiunge le frasi nel database lette da csv
    private void addPhrases(){
        boolean phraseOk;
        try {
            phraseOk = dbase.addPhrases((List<String>)fromClient.readObject());
        } catch (IOException | ClassNotFoundException  e) {
            phraseOk = false;
            System.err.println("Errore di comunicazione delle frasi al database");
        }
        response(phraseOk);
    }
    //elimina le frasi scelte dall'admin
    private void delPhrases(){
        boolean phraseOk;
        try {
            phraseOk = dbase.delPhrases((List<String>)fromClient.readObject());
        } catch (IOException | ClassNotFoundException  e) {
            phraseOk = false;
            System.err.println("Errore di comunicazione delle frasi al database");
        }
        response(phraseOk);
    }
    //cerca tutte le frasi nel database per il menu dell'admin
    private void queryAllPhrases(){
        List<Phrase> phrases = dbase.queryAllPhrases();
        try {
            if(phrases.size()>0) {
                toClient.writeObject("OK");
                toClient.writeObject(phrases);
            }
            else
                toClient.writeObject("ERR");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getEmailCode() {
        int code = server.getEmailCode();
        try {
            toClient.writeObject(code);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkEmailUsername() {
        String mail = "";
        String user = "";
        try{
            mail = (String) fromClient.readObject();
            user = (String) fromClient.readObject();
        }catch (IOException | ClassNotFoundException e){
            System.err.println("Errore di comunicazione con client");
        }
        System.out.println(dbase.checkEmailOrUsername(mail,user));
        response(dbase.checkEmailOrUsername(mail,user));
    }

    private void resetPassword() {
        String mail = "";
        try{
            mail = (String) fromClient.readObject();
        }catch (IOException | ClassNotFoundException e){
            System.err.println("Errore di comunicazione con client");
        }
        String newPassword = server.getSendMailServer().generateRandomPassword();
        ArrayList<String> resetPwdRequest = dbase.resetPassword(mail,newPassword);
        if(resetPwdRequest.get(0).equals("QUERYOK"))
            if(resetPwdRequest.get(1).equals("NAMENOTFOUND"))
                response(false);
            else
                response(server.getSendMailServer().requestResetPassword(resetPwdRequest.get(1),
                    mail,newPassword));
        else
            response(false);
    }

    //stampa al server la volonta di chiudere la connessione
    synchronized private void closeConnection(){
        try {
            System.out.println("Il thread chiude il socket");
            this.socket.close();
        } catch (IOException e) {
            System.out.println("Errore nella chiusura del socket");
        }
    }
    private void response(boolean condition){
        try {
            if (condition)
                toClient.writeObject("OK");
            else
                toClient.writeObject("ERR");
        }catch (IOException exc){
            System.err.println("Errore nella comunicazione con client");
        }
    }
    private void response(boolean condition, Object object){
        try {
            if(condition) {
                toClient.writeObject("OK");
                toClient.writeObject(object);
            }
            else
                toClient.writeObject("ERR");
        }catch (IOException exc){
            System.err.println("Errore nella comunicazione frasi ok");
        }
    }
}
