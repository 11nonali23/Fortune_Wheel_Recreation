package game;

import javax.swing.*;
import java.awt.*;

/**
 *Classe utilizzata per gestire il layout della frase nei pannelli GamePlayer e GameViewer.
 *
 */

public class PhraseLayoutManager extends JPanel{
    private static final long serialVersionUID = 1L;
    private JTextField[] caseContainer;
    private String phrase;
    private int consonantsNumber;
    private int vocalsNumber;

    public PhraseLayoutManager(String phrase) {
        panelLayout();
        this.phrase=phrase;
        //initPhraseVocalConsonantNumbers();
        System.out.println(vocalsNumber);
        System.out.println(consonantsNumber);
        this.caseContainer = new JTextField[this.phrase.length()];
        initButtons();
    }

    public String getPhrase(){
        return phrase;
    }

    public int getVocalsNumber(){
        return vocalsNumber;
    }

    public int getConsonantsNumber(){
        return consonantsNumber;
    }

    private void panelLayout() {
        setLayout(null);
        setBounds(GamePlayerGui.phrasePanelx,GamePlayerGui.phrasePanely,GamePlayerGui.phrasePanelWidth,GamePlayerGui.phrasePanelHeight);
        setBackground(Color.ORANGE);
    }

    /**
     *Mostra occorrenza lettera
     */
    public void showEquals(String letter){
        for(int i = 0; i<phrase.length(); i++)
            if (letter.equalsIgnoreCase(phrase.substring(i, i+1))) {
                caseContainer[i].setText(letter);
            }
    }

    public int getEqualsNumber(String letter){
        int occurences = 0;
        for(int i = 0; i<phrase.length(); i++)
            if (letter.equalsIgnoreCase(phrase.substring(i, i+1)))
                occurences++;
        return occurences;
    }

    //crea i bottoni e li aggiunge al frame
    /**
     *Crea un bottone per ogni lettera distanziato di x = 5 e y = 10 ogni volta che si raggiunge il bordo del frame
     */
    private void initButtons(){
        int btnSize = 50;
        int xButtonScalar = 0;
        int yButton = 5;
        for(int iter = 0; iter<phrase.length(); iter++) {
            if(btnSize*xButtonScalar>1040) {
                yButton += btnSize + 10;
                xButtonScalar = 0;
            }
            this.caseContainer[iter] = new JTextField();//this.phrase.substring(iter, iter+1)
            if(this.phrase.substring(iter, iter+1).equals(" "))
                caseContainer[iter].setText("-");
            caseContainer[iter].setBounds(btnSize*xButtonScalar+5,yButton, btnSize,btnSize);
            caseContainer[iter].setFont(new Font("Arial",Font.BOLD,30));
            caseContainer[iter].setHorizontalAlignment(SwingConstants.CENTER);
            caseContainer[iter].setEditable(false);
            add(caseContainer[iter]);
            xButtonScalar++;
        }
    }
}
