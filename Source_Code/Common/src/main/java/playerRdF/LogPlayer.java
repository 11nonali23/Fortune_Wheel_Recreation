package playerRdF;

import serverRdF.Proxy;
import useful.Login;
import useful.OutTimeExecLog;

import java.util.Timer;

public class LogPlayer extends Login {
    private static final long serialVersionUID = 1;
    private Timer timer;
    private PlayerMenu playerMenu;


    public LogPlayer(Proxy proxy, PlayerMenu pm) {
        super.myIstance = this;
        super.accessed = false;
        super.closed = false;
        super.serverConnection = proxy;
        this.playerMenu = pm;
        super.designLogin();
        timer = new Timer();
        timer.schedule(new OutTimeExecLog(this),600000);
    }

    @Override
    protected void outTimeClose() {
        dispose();
    }

    public PlayerMenu getPlayerMenu() {
        return playerMenu;
    }
}
