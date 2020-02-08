package adminRdF;

import dbRdF.DbRdFStatistics;
import game.GameViewerGui;
import game.Match;
import game.Phrase;
import playerRdF.Player;
import serverRdF.Proxy;
import useful.CSVFileManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *This class is the menu an admin uses to manage all functions of AdminRdF
 */

public class AdminMenu extends useful.Menu {
    /**
     *The constructor takes the proxy to communicate with server
     */
    public AdminMenu(Proxy proxy) {
        super.playerData = new Player();
        super.serverComunicator= proxy;
        super.getStatisticsDb();
        super.phrasesContent = super.getPhrases();
        super.model = new DefaultListModel<>();
        for(String s: phrasesContent)
            model.addElement(s);
        super.deletePhraseList = new ArrayList<>();
        super.signed = false;
        super.frameSetup(this);
        super.designAdminMenu(this);
        super.designPhrase(this);
        super.designViewer(this);
        super.designStatisticsData();
        designProfileChange();
    }

    private AdminMenu getInstance(){return this;}

    /**
     *Metodo utilizzato dalle classi SubAdmin e LogAdmin per segnare se un utente si è iscritto
     */
    public void setSigned(boolean signResult){
        this.signed = signResult;
    }
    /**
     *Salva localmente i dati dell'utente che utilizza questa finestra
     */

    public void setPlayerData(String name, String surname, String nick, String pwd, String email){
        this.playerData.setAll(name,surname,nick,pwd,email);
        setTitle("Menu principale. " + "Owner " + playerData.getNickname());
    }
}
