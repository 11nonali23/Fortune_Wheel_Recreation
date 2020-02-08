package game;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

public class Match implements Serializable {
    private static final long serialVersionUID = 1L;
    private  int matchId;
    private String matchName;
    private String manager;
    private Timestamp dayTime;
    private boolean isStarted;

    public Match(int matchId,String matchName, String manager, Timestamp dayTime){
        this.matchId=matchId;
        this.matchName=matchName;
        this.manager=manager;
        this.dayTime=dayTime;
        isStarted = false;
    }

    public int getMatchId() {
        return matchId;
    }

    private void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public String getMatchName() {
        return matchName;
    }

    private void setMatchName(String matchName) {
        this.matchName = matchName;
    }

    public String getManager() {
        return manager;
    }

    private void setManager(String manager) {
        this.manager = manager;
    }

    private void setDayTime(Timestamp dayTime) {
        this.dayTime = dayTime;
    }

    public void setAll(int matchId, String matchName,String manager, Timestamp dayTime){
        setMatchId(matchId);
        setMatchName(matchName);
        setManager(manager);
        setDayTime(dayTime);
    }

    public void setStarted(boolean started){
        this.isStarted = started;
    }

    public boolean isStarted(){
        return isStarted;
    }
}
