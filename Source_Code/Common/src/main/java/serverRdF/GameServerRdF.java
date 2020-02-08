package serverRdF;

import dbRdF.DbRdF;
import dbRdF.DbRdFGameServer;
import game.Manche;
import game.Match;
import game.PlayerInGame;
import game.RdFObserver;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Random;

/**
 * Server di gioco. Viene utilizzata una connessione RMI con il client.
 * La classe sfrutta il pattern observer per il quale più giocatori o osservatori vengono
 * aggiunti ad una lista di osservatori e vengono aggiornati sulle modifche
 * che avvengono ai parametri della partita lato server ogniqualvolta viene effettuata una mossa dal giocatore
 * con il turno
 */

public class GameServerRdF extends UnicastRemoteObject implements RdFObservable{
    private static final long serialVersionUID = 1L;
    private String serverName;
    private int port;
    private DbRdFGameServer db;
    private List<RdFObserver> players;
    private List<RdFObserver> viewvers;
    //DATI DEI MATCH
    private PlayerInGame[] playersInstance;   //istanza dei giocatori per aggiornare punteggio
    private String[] phrases;
    private String[] themes;
    private Match matchData;
    private Manche[] manches;
    private int currentMancheNumber;
    private final Random randomNumberGenerator;
    private int turnNumber;   //numero inizialmente casuale che traccia i turni
    //Utili per gioco
    private final static String[] modifcatori = {"600","400","500","300","600","1000","PERDE",
            "300","400","700","500","300","400", "PERDE","300","500","400","600",
            "500","400","600","500","400","300","JOLLY","PASSA","300","600"};
    private final static String[] vocali = {"A","E","I","O","U"};
    private int consonantsNumber;
    private int vocalsNumber;

    public GameServerRdF(String serverName, int port, Match matchData) throws RemoteException {
        this.serverName=serverName;
        this.port=port;
        String[] credentials = DbRdF.getCredentials();
        this.db = new DbRdFGameServer();
        this.db.openConnection(credentials[0], credentials[1], credentials[2]);
        players = new ArrayList<>();
        viewvers= new ArrayList<>();
        randomNumberGenerator = new Random();
        //Inizializzazione campi con dati match
        playersInstance = new PlayerInGame[3];
        this.matchData= matchData;
        manches = new Manche[5];
        //inizializzazione server RMI
        setRMIServer();
        currentMancheNumber = -1;
    }

    /**
     * Setta il sevrer sul localhost utilizzando la porta passata come parametro da ServerRdfCreator
     */
    private void setRMIServer()  {
        System.setSecurityManager(System.getSecurityManager());
        try {
            Registry registro = LocateRegistry.createRegistry(port);
            registro.rebind(this.serverName, this);
        }catch(Exception e) {
            System.err.println("Errore nella creazione del GameServer");
            e.printStackTrace();
        }
        System.out.println("Game server online with name: " + this.serverName + " and port: " + this.port);
    }

    private void setPlayerLabels() throws RemoteException {
        String player1, player2;
        int iter = turnNumber;
        iter = (iter + 1) % 3;
        player1 = "Player: " + playersInstance[iter].getNickname() + ". Punti: "
                + playersInstance[iter].getMancheStorage(currentMancheNumber) + " Bonus: "
                + playersInstance[iter].getBonus();
        iter = (iter + 1) % 3;
        player2 = "Player: " + playersInstance[iter].getNickname() + ". Punti: "
                + playersInstance[iter].getMancheStorage(currentMancheNumber) + " Bonus: "
                + playersInstance[iter].getBonus();
        for(RdFObserver player:players)
            player.setPlayerLabels(player1,player2);
        for (RdFObserver viewer : viewvers)
            viewer.setPlayerLabels(player1, player2);
    }

    private void initPhraseVocalConsonantNumbers(){
        ArrayList<String> calledLetters = new ArrayList<>();
        consonantsNumber=0;
        vocalsNumber=0;
        for(int i =0; i<phrases[currentMancheNumber].length(); i++) {
            String letter = phrases[currentMancheNumber].substring(i, i + 1);
            boolean isPresent = false;
            for (String let : calledLetters) {
                if (let.equals(letter)) {
                    isPresent = true;
                    break;
                }
            }
            if (isPresent == false) {
                if (equalsToAVocal(letter)) {
                    vocalsNumber++;
                    calledLetters.add(letter);
                }
                else if (!letter.equals(" ")) {
                    consonantsNumber++;
                    calledLetters.add(phrases[currentMancheNumber].substring(i, i + 1));
                }
            }
        }
            System.out.println("Consonants in this phrase: "+consonantsNumber);
            System.out.println("Vocals in this phrase: "+vocalsNumber);
    }


    private boolean equalsToAVocal(String letter){
        boolean equalsToOne=false;
        String[] vocals={"A","E","I","O","U"};
        for(String vocal: vocals)
            if (vocal.equalsIgnoreCase(letter)) {
                equalsToOne = true;
                break;
            }
        return equalsToOne;
    }

    /**
     * Usato dal server per conoscere nome del gameServer
     */
    protected String getServerName() {
        return serverName;
    }
    /**
     * Usato dal server per conoscere porta del gameServer
     */
    protected int getServerPort(){return port;}

    private void startMatch() throws RemoteException {
        //SERVER SIDE e DATABASE SIDE
        matchData.setStarted(true);
        //Settaggio frasi
        ArrayList<String> dbPhrasesAndThemes = db.queryPhrases(playersInstance[0].getNickname(),
                playersInstance[1].getNickname(),playersInstance[2].getNickname());
        System.out.println("GameServer: enough players, the game will start");
        if(dbPhrasesAndThemes.size()>=10) {
            phrases = new String[5];
            themes = new String[5];
            int phraseThemeGetter = 0;
            for(int i = 0; i< 5; i++) {
                phrases[i] = dbPhrasesAndThemes.get(phraseThemeGetter);
                phraseThemeGetter++;
                themes[i] = dbPhrasesAndThemes.get(phraseThemeGetter);
                phraseThemeGetter++;
            }
            currentMancheNumber = 0;
            //DB SIDE: aggiungo players e viewers al database delle frasi viste
            List<String> playersAndViewers  = new ArrayList<>();
            for (PlayerInGame p: playersInstance)
                playersAndViewers.add(p.getNickname());
            for(RdFObserver viewer: viewvers)
                playersAndViewers.add(viewer.getPlayerInGame().getNickname());
            String[] playersNickname = new String[playersAndViewers.size()];
            for (int i = 0; i<playersAndViewers.size(); i++)
                playersNickname[i] = playersAndViewers.get(i);
            db.createNewManche(playersNickname,currentMancheNumber, matchData.getMatchId(),phrases[currentMancheNumber]);
            ArrayList<String> viewersName = new ArrayList<>();
            for(RdFObserver viewer: viewvers)
                viewersName.add(viewer.getPlayerInGame().getNickname());
            int mancheid = Integer.parseInt(""+matchData.getMatchId()+currentMancheNumber);
            db.addViewersToManche(viewersName,matchData.getMatchId(),mancheid);

            initPhraseVocalConsonantNumbers(); //inizializzo contatore vocali e consonanti

            //CLIENT SIDE
            for (int i = 0; i<3;i++){
                players.get(i).setStartMatch(phrases,themes);
            }
            for (RdFObserver viewer: viewvers){
                viewer.setStartMatch(phrases,themes);
            }
            //SETTAGGIO MANCHE
            for (int i =0; i<5;i++)
                manches[i]=new Manche(matchData.getMatchId()+i,matchData.getMatchId(),phrases[i]);

            //SETTAGGIO DEL TURNO
            setTurn("Sufficienti giocatori",true);
        }
        else{
            System.out.println("Game_Server: Non ci sono abbastanza frasi nel database. Avviso players e" +
                    " mando email ad admin");
            ServerRdFCreator.getSendMailServer().adviseAdminUpdatePhraseDb(db.queryAdminEmail());
            try {
                for (RdFObserver player : players) {
                    player.notEnoughPhrases();
                }
                for (RdFObserver viewer : viewvers) {
                    viewer.notEnoughPhrases();
                }
            }catch (ConcurrentModificationException cex){
                System.err.println("Errore nella modifica delle liste");
            }
        }
    }

    //DA AGGIUSTARE: quando setto il turno devo assicurarmi che parta il timer del client
    private void setTurn(String reason,boolean randomGeneration) throws RemoteException {
        //Stoppo timer del client
        players.get(turnNumber).setTimer(false);
        players.get(turnNumber).setWheelSpinned(false); // Nel caso in cui il vincitore riprenda il turno
        //Aggiornamenti:
        //SERVER SIDE
        if(randomGeneration)
           turnNumber = randomNumberGenerator.nextInt(3);
        else
            turnNumber= ++turnNumber%3;

        //CLIENT SIDE
        players.get(turnNumber).setCanCallPhraseOrVocal(false);
        for (int i = 0; i<players.size();i++)
            players.get(i).setMyTurn(turnNumber==i);
        for (RdFObserver p : players) {
            p.setConsoleText(reason + ". Turno a " + playersInstance[turnNumber].getNickname());
            p.setCurrentPlayerTextField(playersInstance[turnNumber].getNickname() + ". Punti: "
            + playersInstance[turnNumber].getMancheStorage(currentMancheNumber) + " Bonus: "
                    + playersInstance[turnNumber].getBonus());
            setPlayerLabels();
        }
        for (RdFObserver v : viewvers) {
            v.setConsoleText(reason + ". Turno a " + playersInstance[turnNumber].getNickname());
            v.setCurrentPlayerTextField("Player: " + playersInstance[turnNumber].getNickname() + ". Punti: "
                    + playersInstance[turnNumber].getMancheStorage(currentMancheNumber) + " Bonus: "
                    + playersInstance[turnNumber].getBonus());
            setPlayerLabels();
        }
    }


    private void winManche() throws RemoteException {
        //SERVER SIDE:
        //aggiornamento dati giocatore match e manche
        if(currentMancheNumber<4) {
            int winnerPoints = playersInstance[turnNumber].getMancheStorage(currentMancheNumber);//passo valore a metodo setTurn
            System.out.println("Game server: " + playersInstance[turnNumber].getNickname() + " win " + winnerPoints);
            manches[currentMancheNumber].setWinner(playersInstance[turnNumber].getNickname());
            for (int i = 0; i < 3; i++)
                if (i != turnNumber)
                    playersInstance[i].resetMancheStorage(currentMancheNumber);//setto a 0 il campo punti manche di chi ha perso
                currentMancheNumber++;
            initPhraseVocalConsonantNumbers();
            //DATABASE SIDE: dovrò settare vincitore e aggiungere i punti totali e aggiugngere i viewer e creare nuova manche
            db.setMancheWinner(playersInstance[turnNumber].getNickname(),
                    matchData.getMatchId(), currentMancheNumber - 1);
            ArrayList<String> viewersName = new ArrayList<>();
            for(RdFObserver viewer: viewvers)
                viewersName.add(viewer.getPlayerInGame().getNickname());
            int mancheid = Integer.parseInt(""+matchData.getMatchId()+currentMancheNumber);
            db.addViewersToManche(viewersName,matchData.getMatchId(),mancheid);
            //aggiungo manche e viewers e players alle frasi viste
            List<String> playersAndViewers  = new ArrayList<>();
            for (PlayerInGame p: playersInstance)
                playersAndViewers.add(p.getNickname());
            for(RdFObserver viewer: viewvers)
                playersAndViewers.add(viewer.getPlayerInGame().getNickname());
            String[] playersNickname = new String[playersAndViewers.size()];
            for (int i = 0; i<playersAndViewers.size(); i++)
                playersNickname[i] = playersAndViewers.get(i);
            db.createNewManche(playersNickname,currentMancheNumber, matchData.getMatchId(),phrases[currentMancheNumber]);
            //CLIENT SIDE:
            // aggiornamento gui->
            for (RdFObserver p : players) {
                p.setCanCallPhraseOrVocal(false);
                p.changePhrase();
            }
            for (RdFObserver v : viewvers) {
                v.changePhrase();
            }
            setTurn(playersInstance[turnNumber].getNickname() + " indovina frase e guadagna" + winnerPoints + ". Nuova manche", true);
        }
        else
            endMatch();
    }


    private PlayerInGame getWinnerData(){
        PlayerInGame maxPointsPlayer = playersInstance[0].getMatchStorage() > playersInstance[1].getMatchStorage() ?
                playersInstance[0] : playersInstance[1];

        maxPointsPlayer = maxPointsPlayer.getMatchStorage() > playersInstance[2].getMatchStorage() ?
                maxPointsPlayer : playersInstance[2];

        return maxPointsPlayer;
    }

    private void endMatch() throws RemoteException {
        //SERVER SIDE:
        //aggiornamento dati giocatore match e manche
        for (int i = 0; i < 3; i++){
            if (i != turnNumber)
                playersInstance[i].resetMancheStorage(currentMancheNumber);//setto a 0 il campo punti manche di chi ha perso
        }
        PlayerInGame winnerPLayer = getWinnerData();
        //DATABASE SIDE: dovrò settare vincitore manche e match e aggiungere i punti totali
       
//CLIENT SIDE:
        // aggiornamento gui->
        for (RdFObserver p : players) {
            p.setConsoleText("partita terminata. Vincitore : " + winnerPLayer.getNickname());
        }
        for (RdFObserver v : viewvers) {
            v.setConsoleText("partita terminata. Vincitore : " + winnerPLayer.getNickname());
        } db.setMancheWinner(playersInstance[turnNumber].getNickname(),matchData.getMatchId(),currentMancheNumber-1);

        Object[] otherPlayerData = new Object[4];
        int iter = 0;
        for(int i = 0; i < playersInstance.length; i++){
            if(!(playersInstance[i].getNickname().equals(winnerPLayer.getNickname()))) {
                otherPlayerData[iter] = playersInstance[i].getMatchStorage();
                iter++;
                otherPlayerData[iter] = playersInstance[i].getNickname();
                iter++;
            }
	}
        db.setGameWinner(matchData.getMatchId(),winnerPLayer.getNickname(),
                winnerPLayer.getMatchStorage(),otherPlayerData);
    }

    //Se la lettera è una vocale ritorna true
    private boolean compareToAllVocals(String[] vocali, String letter){
        boolean isVocal = false;
        for(String s: vocali)
            if(letter.equalsIgnoreCase(s))
                isVocal=true;
        return isVocal;
    }

    //Ritorna true se il concorrente con il turno ha più di 1000 punti
    private boolean canCallVocal() throws RemoteException {
        if(playersInstance[turnNumber].getMancheStorage(currentMancheNumber)>=1000 && players.get(turnNumber).canCallPhraseOrVocal())
            return true;
        else
            return false;
    }

    /**
     * Aggiunge alla lista di osservatori un giocatore oppure un viewer in base al parametro isPlayer
     * @param isPlayer
     * @param observer
     * @throws RemoteException
     */
    @Override
    synchronized public void addObserver(boolean isPlayer, RdFObserver observer) throws RemoteException {
        System.out.println("GameServer will add an observer: " + observer.getPlayerInGame().getName());
        if(isPlayer) {
            players.add(observer);
            playersInstance[players.size()-1]=observer.getPlayerInGame();
            db.addPlayer(observer.getPlayerInGame().getNickname(),matchData.getMatchId());
            //Se ci sono tre giocatori il gioco verrà iniziato
            try {
                if (players.size() == 3)
                    startMatch();
            }catch (ConcurrentModificationException cexc){
                System.err.println("Errore nella modifica della lista");
            }
        }
        else{
            viewvers.add(observer);
            db.addViewer(observer.getPlayerInGame().getNickname(),matchData.getMatchId());
            if(currentMancheNumber!=-1) {
                observer.setStartMatch(phrases,themes);
                for(int i = 0; i< currentMancheNumber; i++)
                    observer.changePhrase();
                observer.setCurrentPlayerTextField(playersInstance[currentMancheNumber].getNickname());
                setPlayerLabels();
            }
        }
    }

    /**
     * Elimina un osservatore dalla lista. Se l'eliminato è un player, la partita sarà automaticamente terminata
     * @param isPlayer
     * @param observer
     * @param gameStarted
     * @throws RemoteException
     */
    @Override
    synchronized public void deleteObserver(boolean isPlayer, RdFObserver observer, boolean gameStarted) throws RemoteException {
        System.out.println("GameServer will delete an observer: " + observer.getPlayerInGame().getName());
        if(isPlayer)
            players.remove(observer);
        else
            viewvers.remove(observer);
        if(gameStarted) {
            System.out.println("Game server will end the match");
            for (RdFObserver player : players)
                player.endGameLeaveMatch();
            for (RdFObserver viewer : viewvers)
                viewer.endGameLeaveMatch();
        }
    }

    /**
     * Metodo chiamato dai player per girare la ruota
     * @param modifier
     * @throws RemoteException
     */
    @Override
    public void spinWheel(String modifier) throws RemoteException {
        if(consonantsNumber>0){
            switch (modifier) {
                case "PASSA":
                    db.userAction(playersInstance[turnNumber].getNickname(),matchData.getMatchId(),currentMancheNumber,
                            "LOSETURN",0,0);
                    for (RdFObserver player : players)
                        player.spinWheel(modifier);
                    for (RdFObserver viewer : viewvers)
                        viewer.spinWheel(modifier);
                    //SETTO NUOVO TURNO
                    setTurn(playersInstance[turnNumber].getNickname() + " perde il turno", false);
                    break;
                case "PERDE":
                    //DATABASE SIDE
                    db.userAction(playersInstance[turnNumber].getNickname(),matchData.getMatchId(),
                            currentMancheNumber,"LOSEALLPOINTS"
                    ,0,playersInstance[turnNumber].getMancheStorage(currentMancheNumber));
                    //SERVER SIDE
                    playersInstance[turnNumber].resetMancheStorage(currentMancheNumber);
                    //CLIENT SIDE
                    for (RdFObserver player : players)
                        player.spinWheel(modifier);
                    for (RdFObserver viewer : viewvers)
                        viewer.spinWheel(modifier);
                    setTurn(playersInstance[turnNumber].getNickname() + " perde tutti i punti", false);
                    break;
                case "JOLLY":
                    //DB SIDE
                    db.userAction(playersInstance[turnNumber].getNickname(),matchData.getMatchId(),currentMancheNumber,
                            "EARNJOLLY",0,0);
                    //SERVER SIDE
                    playersInstance[turnNumber].addBonus();
                    //CLIENT SIDE
                    players.get(turnNumber).setWheelSpinned(false); //Il giocatore girerà nuovamente
                    for (RdFObserver player : players) {
                        player.spinWheel(modifier);
                        player.setConsoleText(playersInstance[turnNumber].getNickname() + " guadagna un JOLLY");
                        player.setCurrentPlayerTextField(playersInstance[turnNumber].getNickname() + ". Punti: "
                                + playersInstance[turnNumber].getMancheStorage(currentMancheNumber) + " Bonus: "
                                + playersInstance[turnNumber].getBonus());
                    }
                    for (RdFObserver viewer : viewvers) {
                        viewer.spinWheel(modifier);
                        viewer.setConsoleText(playersInstance[turnNumber].getNickname() + " guadagna un JOLLY");
                        viewer.setCurrentPlayerTextField(playersInstance[turnNumber].getNickname() + ". Punti: "
                                + playersInstance[turnNumber].getMancheStorage(currentMancheNumber) + " Bonus: "
                                + playersInstance[turnNumber].getBonus());
                    }
                    break;
                default:
                    for (RdFObserver player : players) {
                        player.spinWheel(modifier);
                        player.setConsoleText(playersInstance[turnNumber].getNickname() + " gira. Ottiene: " + modifier);
                    }
                    for (RdFObserver viewer : viewvers) {
                        viewer.spinWheel(modifier);
                        viewer.setConsoleText(playersInstance[turnNumber].getNickname() + " gira. Ottiene: " + modifier);
                    }
                    players.get(turnNumber).setTimer(true);
                    break;
            }
        }
        else
            setTurn("Consonanti terminate!",false);
    }

    /**
     * Utilizzato dai giocatori per chiamare una lettera
     * @param letter
     * @param bonus
     * @throws RemoteException
     */
    @Override
    synchronized public void callLetter(String letter, int bonus) throws RemoteException {
        //il server aggiornerà la GUI dei client e aggiungerà i punti ai giocatori corretti
        if(letter.length()>1 || letter.length()==0) {
            setTurn(playersInstance[turnNumber].getNickname() + "lettera non valida", false);
            db.userAction(playersInstance[turnNumber].getNickname(),matchData.getMatchId(),currentMancheNumber,
                   "LOSETURN",bonus,0);
        }
        else if (compareToAllVocals(vocali,letter)) { //Se può chiamare vocale avvio procedura vocale
            System.out.println(letter + ": is a vocal");
            if (canCallVocal()) {
                System.out.println("will call vocal");
                callVocal(bonus, letter);
            }
            else {
                setTurn(playersInstance[turnNumber].getNickname() + " Non può chiamare vocale ", false);
                db.userAction(playersInstance[turnNumber].getNickname(),matchData.getMatchId(),currentMancheNumber,
                      "LOSETURN",bonus,0);
                System.out.println("Can't call vocal");
            }
        }
        else if(bonus==0 && !canCallVocal()) {
            setTurn(playersInstance[turnNumber].getNickname() + " nessuna occorenza lettera chiamata ", false);
            db.userAction(playersInstance[turnNumber].getNickname(),matchData.getMatchId(),currentMancheNumber,
                    "LOSETURN",bonus,0);
        }
        else {
            //Devo aggiornare punti giocatori su server e modificare GUI client
            //SERVER
            playersInstance[turnNumber].addPointsToMancheStorage(bonus,currentMancheNumber);
            consonantsNumber--;
            System.out.println("Server, lettera chiamata, consonanti rimaste: "+consonantsNumber);
            //DATABASE SIDE
            db.userAction(playersInstance[turnNumber].getNickname(),matchData.getMatchId(),currentMancheNumber,
            "CALLCONSONANT",bonus,0);
            //CLIENT
            String consonantsEnd = "";
            if (consonantsNumber==0) {
                consonantsEnd = "Consonanti terminate, non girare ruota";
                for (RdFObserver player:players)
                    player.setCanCallPhraseOrVocal(true);
            }
            players.get(turnNumber).setWheelSpinned(false);
            players.get(turnNumber).setCanCallPhraseOrVocal(true);
            players.get(turnNumber).setTimer(false);
            for (RdFObserver p : players) {
                p.showEquals(letter);
                p.setConsoleText(playersInstance[turnNumber].getNickname() + " acquisce " + bonus + " punti " + consonantsEnd);
                p.setCurrentPlayerTextField(playersInstance[turnNumber].getNickname() + ". Punti: "
                        + playersInstance[turnNumber].getMancheStorage(currentMancheNumber) + " Bonus: "
                        + playersInstance[turnNumber].getBonus());
                p.setWheelSpinned(false);
            }
            for (RdFObserver v : viewvers) {
                v.showEquals(letter);
                v.setConsoleText(playersInstance[turnNumber].getNickname() + " acquisce" + bonus + " punti" + consonantsEnd);
                v.setCurrentPlayerTextField(playersInstance[turnNumber].getNickname() + ". Punti: "
                        + playersInstance[turnNumber].getMancheStorage(currentMancheNumber) + " Bonus: "
                        + playersInstance[turnNumber].getBonus());
            }
            players.get(turnNumber).setTimer(true);
        }
    }

    //Operazioni da fare in caso di chiamata vocale
    private void callVocal(int bonus, String vocal) throws RemoteException {
        //DATABASE SIDE
           db.userAction(playersInstance[turnNumber].getNickname()
                   ,matchData.getMatchId(),currentMancheNumber,"CALLVOCAL"
           ,0,1000);

        //SERVER SIDE
        playersInstance[turnNumber].removePointsToMancheStorage(1000,currentMancheNumber);
        vocalsNumber--;
        //Controllo se vocale è presente + CLIENT SIDE
        String vocalEnd="";
        if (consonantsNumber==0)
            vocalEnd = "Consonanti terminate, non girare ruota ";
        if(bonus>0) {
            players.get(turnNumber).setWheelSpinned(false);
            players.get(turnNumber).setCanCallPhraseOrVocal(true);
            players.get(turnNumber).setTimer(false);
            for (RdFObserver p : players) {
                p.showEquals(vocal);
                p.setConsoleText(playersInstance[turnNumber].getNickname() + " chiama vocale " + vocalEnd);
                p.setCurrentPlayerTextField(playersInstance[turnNumber].getNickname() + ". Punti: "
                        + playersInstance[turnNumber].getMancheStorage(currentMancheNumber) + " Bonus: "
                        + playersInstance[turnNumber].getBonus());
                p.setWheelSpinned(false);
            }
            for (RdFObserver v : viewvers) {
                v.showEquals(vocal);
                v.setConsoleText(playersInstance[turnNumber].getNickname() + " acquisce" + bonus + " punti");
                v.setCurrentPlayerTextField(playersInstance[turnNumber].getNickname() + ". Punti: "
                        + playersInstance[turnNumber].getMancheStorage(currentMancheNumber) + " Bonus: "
                        + playersInstance[turnNumber].getBonus());
            }
            players.get(turnNumber).setTimer(true);
        }
        else{
            setTurn(playersInstance[turnNumber].getNickname() + " Chiama una vocale non presente", false);
            db.userAction(playersInstance[turnNumber].getNickname(),matchData.getMatchId(),currentMancheNumber,
                    "LOSETURN",bonus,0);
        }

    }

    /**
     * Utilizzato dai giocatori per chimare una frase
     * @param phrase
     * @throws RemoteException
     */
    @Override
    public void callPhrase(String phrase) throws RemoteException {
        if(consonantsNumber>0) {
            //Se puo chiamare frase
            if (players.get(turnNumber).canCallPhraseOrVocal()) {
                if (phrase.equals(phrases[currentMancheNumber]))
                    winManche();
                else {
                    setTurn(playersInstance[turnNumber].getNickname() + " non indovina la frase", false);
                    db.userAction(playersInstance[turnNumber].getNickname(), matchData.getMatchId(), currentMancheNumber,
                            "LOSETURN", 0, 0);
                }
            }
            //Se non può chiamare frase aggiorno gui client
            else {
                setTurn(playersInstance[turnNumber].getNickname() + " Non può chiamare frase: Perde turno", false);
                db.userAction(playersInstance[turnNumber].getNickname(), matchData.getMatchId(), currentMancheNumber,
                        "LOSETURN", 0, 0);
            }
        }
        else{
            if (phrase.equals(phrases[currentMancheNumber]))
                winManche();
            else {
                setTurn(playersInstance[turnNumber].getNickname() + " non indovina la frase", false);
                db.userAction(playersInstance[turnNumber].getNickname(), matchData.getMatchId(), currentMancheNumber,
                        "LOSETURN", 0, 0);
            }
        }
    }

    /**
     * Utilizzato dall'interfaccia del giocatore in caso di chiamata fuori tempo
     * @throws RemoteException
     */
    @Override
    public void outTimeCall() throws RemoteException {
        setTurn(playersInstance[turnNumber].getNickname() + " tempo scaduto",false);
        db.userAction(playersInstance[turnNumber].getNickname(),matchData.getMatchId(),currentMancheNumber,
                "LOSETURN",0,0);
    }

    /**
     * Utilizzato dal giocatore per sfruttare un jolly
     * @throws RemoteException
     */
    @Override
    public void useJolly() throws RemoteException {
        db.userAction(playersInstance[turnNumber].getNickname(),matchData.getMatchId(),currentMancheNumber,
                "USEJOLLY",0,0);
        playersInstance[turnNumber].useBonus();
        players.get(turnNumber).setJollyPoppedOut(false);
        players.get(turnNumber).setWheelSpinned(false);
        for (RdFObserver player: players)
            player.setConsoleText(playersInstance[turnNumber].getNickname() + " utilizza un jolly e mantiene turno");
        for (RdFObserver viewer: viewvers)
            viewer.setConsoleText(playersInstance[turnNumber].getNickname() + " utilizza un jolly e mantiene turno");
    }

    @Override
    synchronized public int getMyBonus(String nickname) throws RemoteException {
        int bonus = 0;
        System.out.println("da controllare: " + nickname);
        for(int i = 0; i < playersInstance.length;i++) {
            System.out.println("player: " + i + " " +playersInstance[i].getNickname());
            System.out.println("is equal: " + playersInstance[i].getNickname().equals(nickname));
            if (playersInstance[i].getNickname().equals(nickname)) {
                System.out.println("player bonus: " + bonus);
                return playersInstance[i].getBonus();
            }
        }
        System.out.println("Hey houston we have a small problem");
        return 0;
    }

    /**
     * Utilizzato dal giocatore per ottenre un modificatore della ruota
     * @return
     * @throws RemoteException
     */
    @Override
    public String getModifier() throws RemoteException {
        return modifcatori[randomNumberGenerator.nextInt(modifcatori.length)];
    }

    /**
     * Utilizzato da giocatore e osservatore per avere i dati del match
     * @return
     * @throws RemoteException
     */
    @Override
    public Match getMatchData() throws RemoteException {
        return this.matchData;
    }
}
