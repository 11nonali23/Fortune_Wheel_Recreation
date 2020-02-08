package game;

import java.io.Serializable;

public class Phrase implements Serializable {
    private static final long serialVersionUID = 1L;
    private String phrase;
    private String theme;
    private String creator;

    public Phrase(String phrase, String theme, String creator) {
        this.phrase = phrase;
        this.creator = creator;
        this.theme = theme;
    }

    public String getPhrase() {
        return phrase;
    }

    private void setPhrase(String phrase) {
        this.phrase = phrase;
    }

    public String getTheme() {
        return theme;
    }

    private void setTheme(String theme) {
        this.theme = theme;
    }

    public String getCreator() {
        return creator;
    }

    private void setCreator(String creator) {
        this.creator = creator;
    }

    public void setAll(String creator,String phrase, String theme){
        setCreator(creator);
        setPhrase(phrase);
        setTheme(theme);
    }
}
