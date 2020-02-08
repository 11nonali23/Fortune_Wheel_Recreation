package dbRdF;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *Database per gestire statistiche utenti e mostrare ai menù
 */

public class DbRdFStatistics implements Serializable {
    private static final long serialVersionUID = 1;
    private Connection con;
    private boolean connectionEstabilished;
    private String url;
    private String user;
    private String password;

    public DbRdFStatistics() {
        connectionEstabilished = false;
    }

    synchronized public Connection getCon(){
        return con;
    }

    synchronized protected void setCredentials(String[] credentials){
        url = credentials[0];
        user = credentials[1];
        password = credentials[2];
    }

    /**
     *Apre una connessione con il db
     */
    synchronized public void openConnection() {
        boolean connOk = true;
        Properties props= new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        try {
            con = DriverManager.getConnection(url,props);
            System.out.println("Connesso al database di statistiche");
        } catch (SQLException e) {
            System.err.println("Errore nella connesione al database");
            e.printStackTrace();
            connOk = false;
        }
        connectionEstabilished=connOk;
    }

    synchronized public boolean isConnectionEstabilished(){
        return this.connectionEstabilished;
    }

    /**
     *Ritorna il nome di tutti gli utenti
     */
    synchronized public String[] userNicknameArray(){
        String[] users;
        PreparedStatement pp = null;
        ResultSet rs = null;
        try {
            pp = con.prepareStatement("select nickname from RdFUser");
        } catch (SQLException e) {
            System.err.println("errore nel settaggio della query");
        }
        try {
            rs = pp.executeQuery();
        } catch (SQLException e) {
            System.err.println("Errore durante esecuzione query");
        }
        System.out.println("Ricerca frasi eseguita");
        List<String> nicknames = new ArrayList<>();
        try {
            while (rs.next()) {
                nicknames.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        users = new String[nicknames.size()];
        for(int i = 0; i<nicknames.size(); i++)
            users[i] = nicknames.get(i);
        return users;
    }

    /**
     *Ritorna statistiche generali utenti
     */
    synchronized public String[] generalStatistics(){
        String[] generalStatisticsArray = null;
        //1)PUNTEGGIO MASSIMO PER PARTITA
        PreparedStatement ppMaxMatchPoints;
        ResultSet rsMaxMatchPoints;
        //2)PUNTEGGIO MASSIMO PER MANCHE
        PreparedStatement ppMaxManchePoints;
        ResultSet rsMaxManchePoints;
        //3)CONCORRENTE CHE HA GIOCATO PIU MANCHE
        PreparedStatement ppMaxManchePlayed;
        ResultSet rsMaxManchePlayed;
        //4)CONCORRENTE CON MEDIA PUNTI PIU ALTA PER MANCHE
        PreparedStatement ppMaxAveragePoints;
        ResultSet rsMaxAveragePoints;
        //5)CONCORRENTE CHE HA PERSO PIÙ VOLTE IL TURNO PER ERRORI
        PreparedStatement ppMaxLostPoints;
        ResultSet rsMaxLostPoints;
        //6)CONCORRENTE CHE HA PERSO PIÙ VOLTE I PUNTI CON LA RUOTA
        PreparedStatement ppMaxLostPointsWheel;
        ResultSet rsMaxLostPointsWheel;
        //7)CHIAMATA DI CONSONANTE PIÙ LUCRATIVA
        PreparedStatement ppMaxConsonantsPoints;
        ResultSet rsMaxConsonantsPoints;
        //8)NUMERO MEDIO DI MOSSE CON LE QUALI SI INDOVINA LA FRASE
        PreparedStatement ppAvgChangeManche;
        ResultSet rsAvgChangeManche;
        //Eseguo le query nel database
        try {
            //1
            ppMaxMatchPoints = con.prepareStatement("select nickname, matchpoints " +
                    "from player " +
                    "where matchpoints in (" +
                    "select max(matchpoints) " +
                    "from player)");
            rsMaxMatchPoints = ppMaxMatchPoints.executeQuery();
            //2
            ppMaxManchePoints= con.prepareStatement("select nickname, points " +
                    "from (" +
                    "select nickname, sum(acquiredpoints-lostpoints) as points " +
                    "from manchedata " +
                    "group by nickname,mancheid) as SumTable " +
                    "where points in (" +
                    "select max(points) " +
                    "from ( " +
                    "select nickname, sum(acquiredpoints-lostpoints) as points " +
                    "from manchedata " +
                    "group by nickname,mancheid) as SumTable)");
            rsMaxManchePoints = ppMaxManchePoints.executeQuery();
            //3
            ppMaxManchePlayed = con.prepareStatement("select nick, count(nick) as countnick\n" +
                    "\tfrom(\n" +
                    "\t\tselect nickname as nick,mancheid as manche\n" +
                    "\t\tfrom manchedata\n" +
                    "\t\tgroup by nickname,mancheid\n" +
                    "\t)as UserMancheTable\n" +
                    "\tgroup by nick");
            rsMaxManchePlayed = ppMaxManchePlayed.executeQuery();
            //4
            ppMaxAveragePoints = con.prepareStatement("select nickname, " +
                    "(sum(acquiredpoints)-sum(lostpoints)) as totalpoints\n" +
                    "from manchedata\n" +
                    "group by nickname");
            rsMaxAveragePoints = ppMaxAveragePoints.executeQuery();
            //5
            ppMaxLostPoints = con.prepareStatement("select nickname,count(nickname) from manchedata where useraction = 'LOSETURN' group by nickname");
            rsMaxLostPoints = ppMaxLostPoints.executeQuery();
            //6
            ppMaxLostPointsWheel = con.prepareStatement("select nickname, count(nickname) from manchedata " +
                    "where useraction = 'LOSEALLPOINTS' group by nickname");
            rsMaxLostPointsWheel = ppMaxLostPointsWheel.executeQuery();
            //7
            ppMaxConsonantsPoints = con.prepareStatement("select nickname, MAX(acquiredpoints) " +
                    "from MancheData where useraction = 'CALLCONSONANT' group by nickname");
            rsMaxConsonantsPoints = ppMaxConsonantsPoints.executeQuery();
            //8
            ppAvgChangeManche = con.prepareStatement("select avg(numMosse) from (select count(mancheid) as numMosse from manchedata group by mancheid) as numMedioMosse");
            rsAvgChangeManche = ppAvgChangeManche.executeQuery();
            System.out.println("Ricerca delle statistiche generali avvenute con successo. Controllale schiacciando general statistics");

            //Creo lista di risultati
            List<String> result = new ArrayList<>();

            //Aggiungo alla lista i valori dei risultati delle query
            //1
            if(rsMaxMatchPoints.next()) {
                result.add("Punti massimi vinti in una partita: "
                        + rsMaxMatchPoints.getString(2)
                        + " di " + rsMaxMatchPoints.getString(1));
            }
            else
                result.add("Ancora nessun punto vinto per partita");
            //2

            if(rsMaxManchePoints.next()) {
                result.add("Punti massimi vinti in una manche: "
                        + rsMaxManchePoints.getInt(2)
                        + " di: " + rsMaxManchePoints.getString(1));
            }
            else
                result.add("Ancora nessun punto vinto per manche");
            //3
            if(rsMaxManchePlayed.next()) {
                result.add("Più manche giocate: "
                        + rsMaxManchePlayed.getInt(2)
                        + " da: " + rsMaxManchePlayed.getString(1));
            }
            else
                result.add("Ancora nessuna manche giocata");
            //4
            if(rsMaxAveragePoints.next()) {
                result.add("Media punti più alta per manche: "
                        + rsMaxAveragePoints.getInt(2)
                        + " di: " + rsMaxAveragePoints.getString(1));
            }
            else
                result.add("Ancora nessun punto vinto per partita");
            //5
            if(rsMaxLostPoints.next()) {
                result.add("Maggior numero di errori: "
                        + rsMaxLostPoints.getInt(2)
                        + " di: " + rsMaxLostPoints.getString(1));
            }
            else
                result.add("Ancora nessun punto perso per partita");
            //6
            if(rsMaxLostPointsWheel.next()) {
                result.add("Maggior numero di PERDE: "
                        + rsMaxLostPointsWheel.getInt(2)
                        + " di: " + rsMaxLostPointsWheel.getString(1));
            }
            else
                result.add("Ancora nessun punto perso per partita");
            //7
            if(rsMaxConsonantsPoints.next()) {
                result.add("Chiamata conosnante più lucrativa: "
                        + rsMaxConsonantsPoints.getInt(2)
                        + " di: " + rsMaxConsonantsPoints.getString(1));
            }
            else
                result.add("Ancora nessuna consonante chiamata");
            //8
            if(rsAvgChangeManche.next()) {
                result.add("Media mosse per indovinare frase: "
                        + rsAvgChangeManche.getInt(1));
            }
            else
                result.add("Ancora nessuna manche giocata");

            //Costruisco e ritorno array da lista
            generalStatisticsArray = new String[result.size()];
            for(int i = 0; i<result.size(); i++)
                generalStatisticsArray[i] = result.get(i);
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        return generalStatisticsArray;
    }

    /**
     *Ritorna statische relative ad un utente a richiesta via interfaccia
     */
    synchronized public String[] perUserStatistics(String user){
        String[] generalStatisticsArray = null;
        //1)NUMERO DI MATCH GIOCATI
        PreparedStatement ppMatchPlayed;
        ResultSet rsMatchPlayed;
        //2)NUMERO MANCHE GIOCATE
        PreparedStatement ppManchePlayed;
        ResultSet rsManchePlayed;
        //3)NUMERO MANCHE VINTE
        PreparedStatement ppMancheWon;
        ResultSet rsMancheWon;
        //4)MATCH VINTI
        PreparedStatement ppMatchWon;
        ResultSet rsMatchWon;
        //5)NUMERO DI PARTITE OSSERVATE
        PreparedStatement ppMatchObserved;
        ResultSet rsMatchObserved;
        //6)NUMERO MANCHE OSSERVATE
        PreparedStatement ppMancheObserved;
        ResultSet rsMancheObserved;
        //7)PUNTEGGIO MEDIO PARTITA
        PreparedStatement ppAvgPoints;
        ResultSet rsAvgPoints;
        //8)NUMERO MEDIO CEDIMENTO TURNO PER MANCHE
        PreparedStatement ppAvgLoseTurn;
        ResultSet rsAvgLoseTurn;
        //9)NUMERO MEDIO CEDIMENTO TURNO PER PARTITA
        PreparedStatement ppMediumLoseTurnMatch;
        ResultSet rsMediumLoseTurnMatch;
        //Eseguo query
        try {
            //1
            ppManchePlayed = con.prepareStatement("select count(matches) " +
                    "from(" +
                    "select count(matchid) as matches " +
                    "from player " +
                    "where nickname = ? " +
                    "group by matchid" +
                    ") as MatchesPlayed");
            ppManchePlayed.setString(1,user);
            rsManchePlayed = ppManchePlayed.executeQuery();
            //2
            ppMatchPlayed = con.prepareStatement("select count(mancheplayed) " +
                    "from(select count(nickname) as mancheplayed " +
                    "from manchedata " +
                    "where nickname= ? " +
                    "group by mancheid) " +
                    "as countTable");
            ppMatchPlayed.setString(1,user);
            rsMatchPlayed =ppMatchPlayed.executeQuery();
            //3
            ppMancheWon = con.prepareStatement("select count(winner) from manche where winner = ?");
            ppMancheWon.setString(1,user);
            rsMancheWon = ppMancheWon.executeQuery();
            //4
            ppMatchWon = con.prepareStatement("select count(isWinner) from player where isWinner <> false and nickname = ?");
            ppMatchWon.setString(1,user);
            rsMatchWon = ppMatchWon.executeQuery();
            //5
            ppMatchObserved = con.prepareStatement("select count(nickname) from observer where nickname = ? group by nickname");
            ppMatchObserved.setString(1,user);
            rsMatchObserved = ppMatchObserved.executeQuery();
            //6
            ppMancheObserved = con.prepareStatement("select count(nickname) from observer where nickname = '' group by mancheid");
            rsMancheObserved = ppMancheObserved.executeQuery();
            //7
            ppAvgPoints = con.prepareStatement("select avg(matchpoints) from Player where nickname = ?");
            ppAvgPoints.setString(1,user);
            rsAvgPoints = ppAvgPoints.executeQuery();
            //8
            ppAvgLoseTurn = con.prepareStatement("select avg(mediumLoseTurn) " +
                    "from(" +
                    "select count(useraction) as mediumLoseTurn " +
                    "from manchedata " +
                    "where useraction = 'LOSEALL' and nickname = ? " +
                    "group by mancheid) " +
                    "as mediumLoseTurnTable");
            ppAvgLoseTurn.setString(1,user);
            rsAvgLoseTurn = ppAvgLoseTurn.executeQuery();
            //9
            ppMediumLoseTurnMatch = con.prepareStatement("select avg(mediumLoseTurn) " +
                    "from(" +
                    "select count(useraction) as mediumLoseTurn " +
                    "from manchedata join manche on manche.mancheid = manchedata.mancheid " +
                    "where useraction = 'LOSEALL' and nickname = ? " +
                    "group by matchid) " +
                    "as mediumLoseTurnTable");
            ppMediumLoseTurnMatch.setString(1,user);
            rsMediumLoseTurnMatch = ppMediumLoseTurnMatch.executeQuery();
            System.out.println("Ricerca per utente eseguita con successo");

            //Aggiungo alla lista i valori dei risultati delle query
            List<String> result = new ArrayList<>();
            //1
            if(rsManchePlayed.next()) {
                result.add("Manche giocate: "
                        + rsManchePlayed.getString(1));
            }
            else
                result.add("Ancora nessuna manche");
            //2
            if(rsMatchPlayed.next()) {
                result.add("Partite giocate: "
                        + rsMatchPlayed.getInt(1));
            }
            else
                result.add("Ancora nessuna partita");
            //3
            if(rsMancheWon.next()) {
                result.add("Manche vinte: "
                        + rsMancheWon.getInt(1));
            }
            else
                result.add("Ancora nessuna manche vinta");
            //4
            if(rsMatchWon.next()) {
                result.add("Partite vinte: "
                        + rsMatchWon.getInt(1));
            }
            else
                result.add("Ancora nessuna partita vinta");
            //5
            if(rsMatchObserved.next()) {
                result.add("Partite osservate: "
                        + rsMatchObserved.getInt(1));
            }
            else
                result.add("Ancora nessun match osservato");
            //6
            if(rsMancheObserved.next()){
                result.add("Manche osservate: " +
                        rsMancheObserved.getInt(1));
            }
            //7
            if(rsAvgPoints.next()) {
                result.add("Punteggio medio vinto a partita: "
                        + rsAvgPoints.getInt(1));
            }
            else
                result.add("Ancora nessun punto vinto");
            //8
            if(rsAvgLoseTurn.next()) {
                result.add("Numero medio di cedimento turno per manche: "
                        + rsAvgLoseTurn.getInt(1));
            }
            else
                result.add("Ancora nessun turno perso");
            if(rsMediumLoseTurnMatch.next()) {
                result.add("Numero medio di cedimento turno per match: "
                        + rsMediumLoseTurnMatch.getInt(1));
            }
            else
                result.add("Ancora nessun turno perso");

            //Costruisco e ritorno array da lista
            generalStatisticsArray = new String[result.size()];
            for(int i = 0; i<result.size(); i++)
                generalStatisticsArray[i] = result.get(i);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    return generalStatisticsArray;
    }
}
