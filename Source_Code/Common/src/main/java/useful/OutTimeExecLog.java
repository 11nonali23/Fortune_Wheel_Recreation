package useful;

import java.util.TimerTask;

/*
 *Classe usata per la chiusura dell'interfaccia di login dopo 10 minuti
 */
public class OutTimeExecLog extends TimerTask {
    private Login logAdmin;

    public OutTimeExecLog(Login log) {
        this.logAdmin = log;
    }

    /*
     *Chiude interfaccia
     */
    @Override
    public void run() {
        System.out.println("Timer is over: i will close the login window");
        this.logAdmin.outTimeClose();
    }

}