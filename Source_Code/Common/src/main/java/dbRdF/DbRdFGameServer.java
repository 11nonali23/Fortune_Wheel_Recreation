package dbRdF;

import serverRdF.ServerRdFCreator;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 *Database per gestire il gioco
 */

public class DbRdFGameServer {
    private static Connection con;

    /**
     *Apre la connessione con il database. Necessaria prima di eseguire le query
     */
    synchronized public void openConnection(String url,String usr, String pwd) {
        Properties props= new Properties();
        props.setProperty("user", usr);
        props.setProperty("password", pwd);
        try {
            con = DriverManager.getConnection(url,props);
            System.out.println("Game server: connesso al db "+con.getCatalog());
        } catch (SQLException e) {
            System.err.println("Errore nella connesione al database");
            e.printStackTrace();
        }
    }

    /**
     *Aggiunge un giocatore nel db al campo player
     */
    synchronized public void addPlayer(String nickname, int matchId) {
        PreparedStatement pp = null;
        try {
            pp = con.prepareStatement("insert into player values(?,?,0,false)");
        } catch (SQLException e) {
            System.err.println("errore nel settaggio della query");
        }
        try {
            pp.setString(1, nickname);
            pp.setInt(2, matchId);
        } catch (SQLException e) {
            System.err.println("Errore nel settaggio dei dati");
        }
        try {
            pp.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore durante esecuzione update");
            e.printStackTrace();
        }
    }

    /**
     *Aggiungere un giocatore nel db al campo viewer
     */
    synchronized public void addViewer(String nickname, int matchId) {
        PreparedStatement pp = null;
        try {
            pp = con.prepareStatement("insert into observer values(?,?)");
        } catch (SQLException e) {
            System.err.println("errore nel settaggio della query");
        }
        try {
            pp.setString(1, nickname);
            pp.setInt(2, matchId);
        } catch (SQLException e) {
            System.err.println("Errore nel settaggio dei dati");
        }
        try {
            pp.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore durante esecuzione update");
            e.printStackTrace();
        }
    }

    /**
     *Aggiunge un giocatore nel db al campo manche.viewer
     */
    synchronized public void addViewersToManche(ArrayList<String> nickname, int matchId,int mancheid) {
        PreparedStatement pp;
        for(String nick: nickname) {
            try {
                pp = con.prepareStatement("insert into observer values(?,?,?)");
                pp.setString(1, nick);
                pp.setInt(2, matchId);
                pp.setInt(3, mancheid);
                pp.executeUpdate();
            }catch (SQLException exc){
                System.err.println("Database: errore nell'update dei viewer");
            }
        }
    }

    /**
     *Ricerca frasi nel db seguendo i criteri stabiliti
     */
    synchronized public ArrayList<String> queryPhrases(String player1Nick, String player2Nick, String player3Nick){
        PreparedStatement pp = null;
        ResultSet rs = null;
        ArrayList<String> phrasesAndThemes = new ArrayList<>();
        try {
            pp = con.prepareStatement("select phrase,tema " +
                    "from phrase " +
                    "where phrase not in(" +
                    "select phrase " +
                    "from phraseviewed " +
                    "where nickname = ? or nickname = ? or nickname = ?)");
        } catch (SQLException e) {
            System.err.println("errore nel settaggio della query");
        }
        try {
            pp.setString(1,player1Nick);
            pp.setString(2,player2Nick);
            pp.setString(3,player3Nick);
        } catch (SQLException e) {
            System.err.println("Errore nel settaggio dei dati");
        }
        try {
            rs = pp.executeQuery();
        } catch (SQLException e) {
            System.err.println("Errore durante ricerca frasi nel database");
            e.printStackTrace();
        }
        //Ritorno le frasi
       try {
           int iter = 0;
           while (rs.next() && iter<5) {
               phrasesAndThemes.add(rs.getString(1));
               phrasesAndThemes.add(rs.getString(2));
               iter++;
           }
       } catch (SQLException e){
           e.printStackTrace();
       }
        return phrasesAndThemes;
    }

    /**
     *Crea una nuova manche
     */
    synchronized public void createNewManche(String[] playersNickname,int mancheID, int matchID,String phrase){
        PreparedStatement pp = null;
        String mancheIDValue = "" + matchID + mancheID;
        try {
            pp = con.prepareStatement("insert into Manche values (?,?,?)");
        } catch (SQLException e) {
            System.err.println("errore nel settaggio della query");
        }
        try {
            pp.setInt(1, Integer.parseInt(mancheIDValue));
            pp.setInt(2, matchID);
            pp.setString(3, phrase);
        } catch (SQLException e) {
            System.err.println("Errore nel settaggio dei dati");
        }
        try {
            pp.executeUpdate();
            System.out.println("Manche created: " + mancheIDValue);
        } catch (SQLException e) {
            System.err.println("Errore durante esecuzione update");
            e.printStackTrace();
        }
        try {
            for (int i = 0; i<playersNickname.length;i++) {
                pp = con.prepareStatement("insert into PhraseViewed values(?,?)");
                pp.setString(1, phrase);
                pp.setString(2, playersNickname[i]);
                pp.executeUpdate();
            }
        }catch (SQLException exc){
            exc.printStackTrace();
        }
    }

    /**
     *Setta il vincitore della manche
     */
    synchronized public void setMancheWinner(String winnerNickname, int matchID, int mancheID){
        PreparedStatement pp = null;
        String matchIdValue = "" + matchID + mancheID;
        try {
            pp = con.prepareStatement("update Manche set winner = ? where mancheid = ?");
        } catch (SQLException e) {
            System.err.println("errore nel settaggio della query");
        }
        try {
            pp.setString(1, winnerNickname);
            pp.setInt(2,Integer.parseInt(matchIdValue));
        } catch (SQLException e) {
            System.err.println("Errore nel settaggio dei dati");
        }
        try {
            pp.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore durante esecuzione update");
            e.printStackTrace();
        }
    }

    /**
     *Setta il vincitore della manche
     */
    synchronized public void setGameWinner(int matchID,String winnerNickname, int winnerPoints, Object[] otherPlayerData){
        PreparedStatement pp = null;
        try {
            pp = con.prepareStatement("update player set iswinner = true, matchpoints = ? where matchid = ? and nickname = ?");
        } catch (SQLException e) {
            System.err.println("errore nel settaggio della query");
        }
        try {
            pp.setInt(1, winnerPoints);
            pp.setInt(2,matchID);
            pp.setString(3,winnerNickname);
        } catch (SQLException e) {
            System.err.println("Errore nel settaggio dei dati");
        }
        try {
            pp.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore durante esecuzione update");
            e.printStackTrace();
        }
        int iter = 0;
        while (iter<4){
            try {
                pp = con.prepareStatement("update player set iswinner = false, matchpoints = ? where matchid = ? and nickname = ?");
                pp.setInt(1,(int)otherPlayerData[iter]);
		        iter++;
                pp.setInt(2,matchID);
                pp.setString(3,(String)otherPlayerData[iter]);
                iter++;
            } catch (SQLException e) {
                System.err.println("errore nel settaggio della query");
                e.printStackTrace();
            }
        }
    }

    /**
     *Registra azione utente nel db
     */
    synchronized public void userAction(String nickname, int matchId, int mancheID, String userAction,int acquiredPoints,int lostPoints){
        PreparedStatement pp = null;
        String mancheIDValue = "" + matchId + mancheID;
        try {
            pp = con.prepareStatement("insert into mancheData values (?,?,?,?,?)");
        } catch (SQLException e) {
            System.err.println("errore nel settaggio della query");
        }
        try {
            pp.setString(1, nickname);
            pp.setInt(2, Integer.parseInt(mancheIDValue));
            pp.setString(3, userAction);
            pp.setInt(4, acquiredPoints);
            pp.setInt(5, lostPoints);
        } catch (SQLException e) {
            System.err.println("Errore nel settaggio dei dati");
        }
        try {
            pp.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore durante esecuzione update");
            e.printStackTrace();
        }
    }

    synchronized public ArrayList<String> queryAdminEmail(){
        PreparedStatement pp = null;
        ResultSet rs;
        ArrayList<String> returnValue = new ArrayList<>();
        try {
            pp = con.prepareStatement("SELECT nome, email FROM rdfuser NATURAL JOIN admin");
        } catch (SQLException e) {
            System.err.println("errore nel settaggio della query");
        }
        try {
            rs = pp.executeQuery();
            while (rs.next()) {
                returnValue.add(rs.getString(1));
                returnValue.add(rs.getString(2));
            }
        } catch (SQLException e) {
            System.err.println("Errore durante esecuzione query");
            e.printStackTrace();
        }
        return returnValue;
    }
}
