package useful;

import java.util.TimerTask;

/*
 *Classe usata per chiudere interfaccia subscription dopo 10 minuti
 */

public class OutTimeExecSub extends TimerTask {
    private Subscription subAdmin;

    public OutTimeExecSub(Subscription sub) {
        this.subAdmin = sub;
    }

    /*
     *Chiude interfaccia
     */
    @Override
    public void run() {
        System.out.println("Timer is over: i will close the login window");
        this.subAdmin.outTimeClose();
        this.subAdmin.notifyServer();
    }
}