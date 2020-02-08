package playerRdF;

import serverRdF.Proxy;



/**
 * Interfaccia di gestione del modulo playerRdF
 */

public class PlayerMenu extends useful.Menu  {

    public PlayerMenu(Proxy proxy) {
        signed = false;
        this.serverComunicator = proxy;
        this.playerData = new Player();
        super.getStatisticsDb();
        frameSetup(this);
        super.designPlayerMenu(this);
        super.designPlayer(this);
        super.designViewer(this);
        super.designStatisticsData();
        super.designProfileChange();
    }

    //Ritorna istanza classe: usato nei listener anonimi
    private PlayerMenu getInstance(){
        return this;
    }

    /**
     * setta se l'utente e loggato o meno
     * @param isSigned
     */
    public void setSigned(boolean isSigned){
        signed = isSigned;
    }

    public void setPlayerData(String name, String surname, String nick, String pwd, String email){
        this.playerData.setAll(name,surname,nick,pwd,email);
        setTitle("Menu principale. " + "Owner " + playerData.getNickname());
    }
}
