package dbRdF;

import serverRdF.ServerRdFCreator;
import useful.OutTimeExecSub;
import useful.Subscription;

import java.util.Timer;


public class SubDbRdF extends Subscription {
    private static final long serialVersionUID = 1L;
    private ServerRdFCreator server; //Riferimento al server
    private Timer timer;

    public SubDbRdF(ServerRdFCreator server) {
        super.myIstance = this;
        super.subbed = false;
        super.closed = false;
        super.setFrame();
        super.designSub();
        super.designEnterCode();
        super.designDbRegPanel();
        cards.show(cardsContainer,"DBREG");
        super.regAdv.setText("REGISTRAZIONE ADMIN DB");
        this.server = server;
        this.setVisible(true);
        timer = new Timer();
        timer.schedule(new OutTimeExecSub(this), 600000);
    }

    public ServerRdFCreator getServer(){
        return this.server;
    }

    public void close() {
        closed = true;
        dispose();
        timer.cancel();
    }

    public boolean isSubbed() {
        return super.subbed;
    }

    public boolean isClosed() {
        return super.closed;
    }

    public void outTimeClose() {
        closed = true;
        this.dispose();
    }
}