package game;

import playerRdF.Player;

public class PlayerInGame extends Player{
    private static final long serialVersionUID=1L;
    private int bonus;
    private int[] mancheStorage;


    public PlayerInGame(String name, String surname, String nick, String pwd, String email) {
        setAll(name, surname, nick, pwd, email);
        this.bonus = 0;
        this.mancheStorage = new int[5];
        for(int value: mancheStorage)
            value=0;
    }

    public int getBonus() {
        return bonus;
    }

    public void addBonus() {
        this.bonus++;
    }

    public void useBonus(){
        this.bonus--;
    }

    public int getMancheStorage(int currentManche) {
        return mancheStorage[currentManche];
    }

    public void addPointsToMancheStorage(int points, int currentManche){
        this.mancheStorage[currentManche]+=points;
    }

    public void removePointsToMancheStorage(int points, int currentManche){
        this.mancheStorage[currentManche]-=points;
    }

    public void resetMancheStorage(int currentManche){
        this.mancheStorage[currentManche]=0;
    }

    public int getMatchStorage() {
        int totalPoints = 0;
        for(int manchePoints: mancheStorage)
            totalPoints+=manchePoints;
        return totalPoints;
    }
}
