package adminRdF;

import serverRdF.Proxy;
import useful.OutTimeExecSub;
import useful.Subscription;

import java.util.Timer;

public class SubAdmin extends Subscription {
    private static final long serialVersionUID = 1L;
    private AdminMenu adminMenu;
    private Timer timer;

    public SubAdmin(Proxy proxy, AdminMenu am) {
        super.myIstance = this;
        super.subbed = false;
        super.closed = false;
        super.serverComunicator = proxy;
        this.adminMenu = am;
        super.setFrame();
        super.designSub();
        super.designEnterCode();
        cards.show(cardsContainer,"LOGIN");
        super.regAdv.setText("REGISTRAZIONE ADMIN");
        this.setVisible(true);
        timer = new Timer();
        timer.schedule(new OutTimeExecSub(this), 600000);
    }

    public AdminMenu getAdminMenu() {
        return this.adminMenu;
    }

    protected boolean isSubbed() {
        return subbed;
    }

    protected boolean isClosed() {
        return closed;
    }

    //Metodo esuguito quando il timer di 10 minuti scade
    @Override
    protected void outTimeClose() {
        closed = true;
        dispose();
    }
}
