package game;

public class Manche {
    private int mancheID;
    private int matchID;
    private String phrase;
    private String winner;

    public Manche(int mancheID, int matchID,String phrase){
        this.mancheID=mancheID;
        this.matchID=matchID;
        this.phrase=phrase;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }
}
