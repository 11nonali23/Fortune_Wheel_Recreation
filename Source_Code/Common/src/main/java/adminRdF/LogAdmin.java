package adminRdF;

import serverRdF.Proxy;
import useful.Login;
import useful.OutTimeExecLog;

import java.util.Timer;

public class LogAdmin extends Login {
    private static final long serialVersionUID = 1;
    private Timer timer;
    private AdminMenu adminMenu;

    public LogAdmin(Proxy proxy, AdminMenu am) {
        super.myIstance = this;
        super.accessed = false;
        super.closed = false;
        super.serverConnection = proxy;
        this.adminMenu = am;
        super.designLogin();
        timer = new Timer();
        timer.schedule(new OutTimeExecLog(this),600000);

    }

    public AdminMenu getAdminMenu(){
        return adminMenu;
    }
    @Override
    protected void outTimeClose() {
        dispose();
    }
}
