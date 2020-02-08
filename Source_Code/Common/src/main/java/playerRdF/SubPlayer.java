package playerRdF;

import serverRdF.Proxy;
import useful.OutTimeExecSub;
import useful.Subscription;

import java.util.Timer;

public class SubPlayer extends Subscription {
    private static final long serialVersionUID = 1L;
    private PlayerMenu playerMenu;
    private Timer timer;

    public SubPlayer(Proxy proxy, PlayerMenu pm) {
        super.myIstance = this;
        super.subbed = false;
        super.closed = false;
        super.serverComunicator = proxy;
        this.playerMenu = pm;
        super.setFrame();
        super.designSub();
        super.designEnterCode();
        cards.show(cardsContainer,"LOGIN");
        super.regAdv.setText("REGISTRAZIONE PLAYER");
        this.setVisible(true);
        timer = new Timer();
        timer.schedule(new OutTimeExecSub(this), 600000);
    }

    public PlayerMenu getPlayerMenu() {
        return this.playerMenu;
    }


    protected boolean isSubbed() {
        return subbed;
    }

    protected boolean isClosed() {
        return closed;
    }

    //Metodo che chiude la finestra se è scaduto il tempo
    @Override
    protected void outTimeClose() {
        closed = true;
        dispose();
    }

}
