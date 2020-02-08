package dbRdF;

import game.Match;
import game.Phrase;
import useful.Encryption;

import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 *Database per gestire dati iscrizioni etc.
 */

public class DbRdF {
    private static Connection con;
    private TableCreator tableCreator;
    private static DbRdFStatistics statisticDb;
    private static String url;
    private static String username;
    private static String password;

    /**
     *Costruisce db e db statistiche
     */
    public DbRdF() {
        tableCreator = new TableCreator();
        statisticDb = new DbRdFStatistics();
    }

    synchronized public static String[] getCredentials(){
        return new String[]{url,username,password};
    }


    /**
     *ritorna db statistiche
     */
    synchronized public static DbRdFStatistics getStatisticsDatabase(){
        if (!statisticDb.isConnectionEstabilished()){
            System.out.println("DbRdf will open statistics database connection");
            String[] credentials = {url, username, password};
            statisticDb.setCredentials(credentials);
        }
        return statisticDb;
    }

    /**
     *Apre la connessione con il database. Necessaria prima di eseguire le query
     */
    synchronized public boolean openConnection(String url,String usr, String pwd) {
        boolean connOk = true;
        Properties props= new Properties();
        props.setProperty("user", usr);
        props.setProperty("password", pwd);
        try {
            con = DriverManager.getConnection(url,props);
            System.out.println("Connesso al db "+con.getCatalog());
        } catch (SQLException e) {
            System.err.println("Errore nella connesione al database");
            connOk = false;
            e.printStackTrace();
        }
        if(connOk) {
            this.url = url;
            this.username = usr;
            this.password = pwd;
            tableCreator.createTables(con);
        }
        return connOk;
    }

    synchronized public boolean oneAdmin(){
        PreparedStatement pp;
        ResultSet rs;
        try {
            pp = con.prepareStatement("select nickname from rdfuser natural join admin");
            rs = pp.executeQuery();
            if(rs.next())
                return true;
            else
                return false;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     *Permette ad un utente di cercare le proprie credenziali nel database
     */
    synchronized public boolean accesso(String email, String password,String adminOrUser) {
        boolean risultatoQuery;
        ResultSet rs = null;
        PreparedStatement pp = null;
        try {
            if(adminOrUser.equals("ADMIN"))
                pp = con.prepareStatement("select * from Admin where email = ? and password = ?");
            else
                pp = con.prepareStatement("select * from RdFUser where email = ? and password = ?");
        } catch (SQLException e) {
            System.err.println("errore nel settaggio della query");
        }
        try {
            pp.setString(1, email);
            pp.setString(2, password);
        } catch (SQLException e) {
            System.err.println("Errore nel settaggio dei dati");
        }
        try {
            rs = pp.executeQuery();
        } catch (SQLException e) {
            System.err.println("Errore nell' esecuzione della ricerca");
        }
        System.out.println("Query di accesso eseguita");
        try {
            risultatoQuery = rs.next() ? true: false;
        } catch (SQLException e) {
            System.err.println("Errore nel next");
            risultatoQuery = false;
        }
        return risultatoQuery;
    }

    /**
     *Permette ad un utente di registrarsi aggiornando il database
     */
    synchronized public boolean registrazione(String nickname, String email, String nome, String cognome,String password,String adminOrUser) {
        boolean subbed = true;
        PreparedStatement pp = null;
        try {
            pp = con.prepareStatement("insert into RDFuser values (?,?,?,?,?)");
        } catch (SQLException e) {
            subbed = false;
            System.err.println("errore nel settaggio della query");
        }
        try {
            pp.setString(1, nickname);
            pp.setString(2, email);
            pp.setString(3, nome);
            pp.setString(4, cognome);
            pp.setString(5, password);
        } catch (SQLException e) {
            subbed = false;
            System.err.println("Errore nel settaggio dei dati");
        }
        try {
            pp.executeUpdate();
        } catch (SQLException e) {
            subbed = false;
            System.err.println("Errore durante esecuzione update");
        }
        //Se admin lo inserirò anche nella tabella admin
        if (adminOrUser.equals("ADMIN")) {
            try {
                pp = con.prepareStatement("insert into Admin values (?)");
            } catch (SQLException e) {
                subbed = false;
                System.err.println("errore nel settaggio della query");
            }
            try {
                pp.setString(1, nickname);
            } catch (SQLException e) {
                subbed = false;
                System.err.println("Errore nel settaggio dei dati");
            }
            try {
                pp.executeUpdate();
            } catch (SQLException e) {
                subbed = false;
                System.err.println("Errore durante esecuzione update");
                e.printStackTrace();
            }
        }
            System.out.println("Update di regsitrazione eseguita");
        return subbed;
    }


    /**
     *un utente si registra e vengono ritornati i dati
     */
    synchronized public List<String> log_piu_dati(String email, String password,String adminOrUser){
        List<String> risultatoQuery = new ArrayList<>();
        ResultSet rs;
        PreparedStatement pp = null;
        try {
            if(adminOrUser.equals("ADMIN"))
                pp  =con.prepareStatement("select * from admin natural join RDFuser where email = ? and password = ?");
            else
                pp = con.prepareStatement("select * from RdFUser where email = ? and password = ?" +
                        " and nickname not in(select nickname from admin)");
        } catch (SQLException e) {
            System.err.println("errore nel settaggio della query");
            risultatoQuery.add(0,"ERR");
        }
        try {
            pp.setString(1, email);
            pp.setString(2, password);
        } catch (SQLException e) {
            System.err.println("Errore nel settaggio dei dati");
            risultatoQuery.add(0,"ERR");
        }
        try {
            rs = pp.executeQuery();
            System.out.println("Query eseguita");
            if(rs.next()) {
                risultatoQuery.add(rs.getString(1));
                risultatoQuery.add(rs.getString(2));
                risultatoQuery.add(rs.getString(3));
                risultatoQuery.add(rs.getString(4));
                risultatoQuery.add(rs.getString(5));
            }
            else {
                risultatoQuery.add(0,"ERRZEROUTENTI");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Errore nell' esecuzione della ricerca");
            risultatoQuery.add(0,"ERR");
        }
        return risultatoQuery;
    }

    /**
     * Ricerca tutte le frasi presenti nel database
     */
    synchronized public List<Phrase> queryAllPhrases(){
        List<Phrase> phrases = new ArrayList<>();
        PreparedStatement pp = null;
        ResultSet rs = null;
        try {
            pp = con.prepareStatement("select * from Phrase",ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_UPDATABLE);
        } catch (SQLException e) {
            System.err.println("errore nel settaggio della query");
        }
        try {
            rs = pp.executeQuery();
        } catch (SQLException e) {
            System.err.println("Errore durante esecuzione query");
        }
        System.out.println("Ricerca frasi eseguita");
        //inizializzo e creo array con frasi
        try{
            int i = 0;
            while(rs.next()){
                phrases.add(i,new Phrase(rs.getString("phrase"),rs.getString("tema"),
                        rs.getString("creator")));
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Errore nel settaggio dell'array di frasi");
        }
        return phrases;
    }

    /**
     *Aggiunge le frasi al database
     */
    synchronized public boolean addPhrases(List<String> frasi){
        boolean phraseOk = true;
        PreparedStatement pp = null;
        for(int i = 0; i<frasi.size(); i+=3) {
            try {
                pp = con.prepareStatement("insert into Phrase values (?,?,?) ");
            } catch (SQLException e) {
                phraseOk = false;
                System.err.println("errore nel settaggio della query");
            }
            try {
                pp.setString(1, frasi.get(i));
                pp.setString(2, frasi.get(i+1));
                pp.setString(3, frasi.get(i+2));
            }catch (SQLException exc){
                System.err.println("Errore nel settaggio dei dati dell'update");
                phraseOk = false;
            }
            try {
                pp.executeUpdate();
            } catch (SQLException e) {
                phraseOk = false;
                System.err.println("Errore durante esecuzione update");
                e.printStackTrace();
            }
        }
        System.out.println("Update di regsitrazione eseguita");
        return phraseOk;
    }

    /**
     * Elimina le frasi dal database
     */
    synchronized public boolean delPhrases(List<String> frasi) {
        boolean phraseOk = true;
        PreparedStatement pp = null;
        for (int i = 0; i < frasi.size(); i++) {
            try {
                pp = con.prepareStatement("delete from Phrase where phrase  = ?");
            } catch (SQLException e) {
                phraseOk = false;
                System.err.println("errore nel settaggio della query");
            }
            try {
                pp.setString(1, frasi.get(i));
            } catch (SQLException exc) {
                System.err.println("Errore nel settaggio dei dati dell'update");
                phraseOk = false;
            }
            try {
                pp.executeUpdate();
            } catch (SQLException e) {
                phraseOk = false;
                System.err.println("Errore durante esecuzione update");
            }
        }
        return phraseOk;
    }

    /**
     *Ricerca le frasi nel database seguendo i criterri richiesti
     */


    /**
     *Aggiunge un nuovo match
     */
    synchronized public Match addNewMatch(String name, String matchName){
        boolean queryOk= true;
        Match newMatch = null;
        PreparedStatement pp = null;
        int maxMatchId= getMaxMatchID()+1;
        Timestamp dateAndTime = getCurrentTimestamp();
        try {
            pp = con.prepareStatement("insert into game values (?,?,?,?)");
        } catch (SQLException e) {
            System.err.println("errore nel settaggio della query");
            queryOk=false;
        }
        try {
            pp.setInt(1, (maxMatchId));
            pp.setString(2, matchName);
            pp.setString(3,name);
            pp.setTimestamp(4, dateAndTime);
        } catch (SQLException e) {
            System.err.println("Errore nel settaggio dei dati");
            queryOk=false;
        }
        try {
            pp.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            queryOk=false;
        }
        if(queryOk)
            newMatch = new Match(maxMatchId, matchName, name, dateAndTime);
        return newMatch;
    }

    /**
     *metodo utilizzato solo quando cerco le frasi e inizializzo array
     */
     private String[] phraseArray(ResultSet resultSet) throws SQLException {
        int resultSetLenght = 0;
        String[] phrases = new String[5];
        Random randomNumber = new Random();
        //Conto la lunghezza del result set e la salvo in una variabile
        while(resultSet.next() && resultSetLenght<5){
            resultSetLenght++;
        }
        //Se l'array avrà lunghezza 0 allora il server thread manderà l' errore al proxy
        if(resultSetLenght<5){
            return new String[0];
        }
        else {
            //inizializzo array
            for (int i = 0; i < 5; i++) {
                phrases[i] = resultSet.getString(randomNumber.nextInt(resultSetLenght));
            }
        }
        return phrases;
    }

    /**
     *Cerca se nel db è presente un email o un nickname
     * uguali a quelli passati
     * come parametro
     */
    synchronized public boolean checkEmailOrUsername(String email, String nick ) {
        boolean isThereAnyOne = true;
        PreparedStatement pp;
        ResultSet rs;
        try{
            pp = con.prepareStatement("select nome from RDFuser where email = ? OR nickname = ?");
            pp.setString(1,email);
            pp.setString(2,nick);
            rs = pp.executeQuery();
            if(rs.next())
                isThereAnyOne = true;
            else
                isThereAnyOne=false;
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return isThereAnyOne;
    }

    /**
     * Utilizzato per modificare i dati del richiedente
     * @param changes
     * @return
     */
    synchronized public boolean changeProfileData(HashMap<String,String> changes) {
        PreparedStatement pp;
        boolean updateOk = true;
        try {
            if(!changes.get("NAME").equals("NONE")){
                pp = con.prepareStatement("update rdfuser set nome = ? where nickname = ?");
                pp.setString(1,changes.get("NAME"));
                pp.setString(2,changes.get("MYNICK"));
                pp.executeUpdate();
            }
            if(!changes.get("SURNAME").equals("NONE")){
                pp = con.prepareStatement("update rdfuser set surname = ? where nickname = ?");
                pp.setString(1,changes.get("SURNAME"));
                pp.setString(2,changes.get("MYNICK"));
                pp.executeUpdate();
            }
            if(!changes.get("NICK").equals("NONE")){
                pp = con.prepareStatement("update rdfuser set nickname = ? where nickname = ?");
                pp.setString(1,changes.get("NICK"));
                pp.setString(2,changes.get("MYNICK"));
                pp.executeUpdate();
            }
            if(!changes.get("PWD").equals("NONE")){
                pp = con.prepareStatement("update rdfuser set password = ? where nickname = ?");
                pp.setString(1,changes.get("PWD"));
                pp.setString(2,changes.get("MYNICK"));
                pp.executeUpdate();
            }
        }catch (SQLException e){
            e.printStackTrace();
            updateOk = false;
        }
        return updateOk;
    }

    /**
     *metodo utilizzato per aggiornare password nel db
     */
    synchronized public ArrayList<String> resetPassword(String mail, String pwd){
        ArrayList<String> queryResultAndName = new ArrayList<>();
        boolean queryOk= true;
        PreparedStatement pp = null;
        try {
            pp = con.prepareStatement("update RDFuser set password = ? where email = ? ");
        } catch (SQLException e) {
            System.err.println("errore nel settaggio della query");
            queryOk=false;
        }
        try {
            pp.setString(1, pwd);
            pp.setString(2,mail);
        } catch (SQLException e) {
            System.err.println("Errore nel settaggio dei dati");
            queryOk=false;
        }
        try {
            pp.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore nell' esecuzione del reset di password");
            queryOk=false;
        }
        if (queryOk){
            queryResultAndName.add("QUERYOK");
            try {
                ResultSet rs;
                pp = con.prepareStatement("select nome from RDFuser where email = ? ");
                pp.setString(1,mail);
                rs = pp.executeQuery();
                if(rs.next())
                    queryResultAndName.add(rs.getString(1));
                else
                    queryResultAndName.add("NAMENOTFOUND");
            }
            catch (SQLException exc){
                exc.printStackTrace();
            }
        }
        else
            queryResultAndName.add("QUERYERR");
        return queryResultAndName;
    }

    /**
     *Ritorna la data corrente
     */
    private Timestamp getCurrentTimestamp() {
        java.util.Date today = new java.util.Date();
        Date currentDate = new Date(today.getTime());
        return new Timestamp(currentDate.getTime());
    }

    /**
     *ritorna l'id più alto
     */
    private int getMaxMatchID(){
        PreparedStatement pp = null;
        ResultSet rs = null;
        int result = -1;
        try {
            pp = con.prepareStatement("select max(matchId) from game ");
        } catch (SQLException e) {
            System.err.println("errore nel settaggio della query");
        }
        try {
            rs = pp.executeQuery();
        } catch (SQLException e) {
            System.err.println("Errore nell' esecuzione della ricerca");
        }
        try {
            rs.next();
            result = rs.getInt("max");
        } catch (SQLException | NullPointerException e) {
            System.out.println("Errore nel ricavare il massimo matchId");
        }
        return result;
    }
}
