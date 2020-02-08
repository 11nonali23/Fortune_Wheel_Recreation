package dbRdF;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TableCreator {

    protected boolean createTables(Connection dbCon){
        System.out.println("Database will create the tables");
        boolean queryOk = true;
        PreparedStatement pp;
        try{
            //tabella utente
            pp = dbCon.prepareStatement("create table if not exists RDFuser (" +
                    "nickname varchar(30) primary key, " +
                    "email varchar(40) unique, " +
                    "nome varchar(30) not null, " +
                    "surname varchar(30) not null, " +
                    "password varchar(80) not null" +
                    ");");
            pp.executeUpdate();
            //tabella gioco
            pp = dbCon.prepareStatement("create table if not exists Game( " +
                    "matchID int primary key, " +
                    "matchName varchar(20) not null, " +
                    "manager varchar(30) references RDFuser(nickname) on update cascade on delete cascade, " +
                    "dateAndTime timestamp without time zone not null"+
                    ");");
            pp.executeUpdate();
            //tabella player
            pp = dbCon.prepareStatement("create table if not exists Player( " +
                    "nickname varchar(30) references RDFuser on update cascade on delete cascade, " +
                    "matchID int references Game on update cascade on delete cascade, " +
                    "matchpoints int, " +
                    "iswinner boolean " +
                    ");");
            pp.executeUpdate();
            //tabella frasi
            pp = dbCon.prepareStatement("create table if not exists Phrase(" +
                    "phrase varchar(60) primary key, " +
                    "tema varchar(20) not null, " +
                    "creator varchar(30) references RdFuser(nickname) on update cascade on delete set null" +
                    ");");
            pp.executeUpdate();
            //tabella manche
            pp = dbCon.prepareStatement("create Table if not exists Manche(" +
                    " mancheID int primary key, " +
                    "matchID int references Game on update cascade on delete cascade, " +
                    "phrase varchar(60) default 'deleted phrase' references Phrase on update cascade on delete set default, " +
                    "winner varchar(30) default 'no one or deleted user' references RDFuser on update cascade on delete set default" +
                    "); ");
            pp.executeUpdate();
            //tabella Viewer
            pp = dbCon.prepareStatement("create table if not exists Observer( " +
                    "nickname varchar(30) references RDFuser on update cascade on delete cascade, " +
                    "matchID int references Game on update cascade on delete cascade, " +
                    "mancheid int references Manche on update cascade on delete no action" +
                    ");");
            pp.executeUpdate();
            //tabella admin
            pp = dbCon.prepareStatement("create table if not exists Admin(" +
                    " nickname varchar(30) references RDFuser on update cascade on delete cascade" +
                    ");");
            pp.executeUpdate();
            //tabella frasi viste da utente
            pp = dbCon.prepareStatement("create table if not exists PhraseViewed(" +
                    "phrase varchar(60) references Phrase on update cascade on delete cascade, " +
                    "nickname varchar(30) references RDFuser on update cascade on delete cascade " +
                    ");");
            pp.executeUpdate();
            //tabella dati manche
            pp = dbCon.prepareStatement("create table if not exists mancheData(" +
                    "nickname varchar(30) references RDFuser on update cascade on delete cascade, " +
                    "mancheID int references Manche on update cascade on delete cascade, " +
                    "userAction varchar(20) not null, " +
                    "acquiredPoints int, " +
                    "lostPoints int" +
                    ");");
            pp.executeUpdate();
            //inserisco utente di default
            pp = dbCon.prepareStatement("select nickname from rdfuser where nickname = 'no one or deleted user'");
            ResultSet rs = pp.executeQuery();
            if(!rs.next()){
                pp = dbCon.prepareStatement("insert into rdfuser values('no one or deleted user','no','no','no','no')");
                pp.executeUpdate();
            }
        }catch (SQLException sqlExc){
            sqlExc.printStackTrace();
            queryOk =false;
        }
        return queryOk;
    }
}
