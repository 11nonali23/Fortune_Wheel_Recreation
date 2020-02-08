package game;

import playerRdF.Player;
import serverRdF.RdFObservable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class GameGui extends JFrame {
    protected static final long serialVersionUID = 1L;
    protected Player guiDetentor;
    //Variabili per settare grandezza della gui
    protected static final int Gamex = 1100;
    protected static final int Gamey = 650;
    protected static final int phrasePanelx = 5;
    protected static final int phrasePanely = 50;
    protected static final int phrasePanelHeight = 210;
    protected static final int phrasePanelWidth = 1085;
    protected static final String[] useJollyOptions ={"Usa jolly","conserva jolly"};
    //Timer avviabili dal server
    protected static final int delay = 1000;
    protected static final int letterWaitTime= 5;
    protected static final int phraseWaitTime= 10;
    protected String[] themes;
    protected int secodnsGone;
    protected Timer actionWaiter;
    //Variabili utili per logica gioco del client
    protected RemoteManager remoteGuiManager;
    protected boolean isWheelSpinned;
    protected boolean isGameStarted;
    protected boolean myTurn;
    protected boolean endGame;
    protected boolean canCallPhraseOrVocal;
    protected boolean isJollyPoppedout;
    protected RdFObservable server;
    //Pannelli da switchare a cambio frase ed inzio e fine gioco
    protected CardLayout currentPhrase;
    protected JPanel cardsContainer;
    protected JPanel notEnoughPlayerPanel; //Pannello che indica che non ci sono ancora abbastanza giocatori
    protected JPanel leaveMatchPlayerPanel; //Pannello che indica fine del gioco per abbandono di un utente
    protected int currentShowingPhrase;
    protected PhraseLayoutManager[] phrasePanel;
    protected String[] phraseThemes;
    //Componenti avulsi da settaggio server
    protected JTextField currentPlayerTextField;
    protected JLabel timerLabel;
    //COMPONENTI DEL FRAME SETTABILI DAL SERVER
    protected JTextField gameConsole;
    protected JTextField phraseTheme;
    protected JTextField insert_letter;
    protected JTextField insert_phrase;
    protected JTextField ruota;
    protected JLabel player1Lbl;
    protected JLabel player2Lbl;

    boolean testJolly = true;


    /**
     * configura l'interfaccia: cambia tra viewer e player
     * @param instance
     */
    protected void frameConfiguration(GameGui instance) {
        if (instance instanceof GameViewerGui) {
            viewerFrameConfiguration();
        }
        else
            playerGuiConfiguration((GamePlayerGui) instance);
    }

    private void viewerFrameConfiguration() {
        //setta le direttive base del frame
        setSize(Gamex, Gamey);
        setLayout(null);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                remoteGuiManager.leaveMatch();
                dispose();
            }


        });
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }
    }

    private void playerGuiConfiguration(GamePlayerGui instance) {
        //setta le direttive base del frame
        setSize(Gamex, Gamey);
        setLayout(null);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Closing the frame dialog");
                remoteGuiManager.closeGameConnection();
                remoteGuiManager.leaveMatch();
                dispose();
            }


        });
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }
        //Set del timer
        secodnsGone=0;
        ActionListener taskPerformer = e -> {
            //Mi occupo della chiamata in ritardo della lettera direttamente sul bottone
            secodnsGone++;
            timerLabel.setText(Integer.toString(secodnsGone));
            if (secodnsGone==phraseWaitTime) {
                instance.setTimer(false);
                if(instance.getMyBonus()>0){
                    ArrayList<String> commands = new ArrayList<>();
                    commands.add("NOTINTIME");
                    try {
                        instance.useJollyRequest("Il tempo è scaduto",commands);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
                else {
                    try {
                        server.outTimeCall();
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        actionWaiter = new Timer(delay,taskPerformer);
    }

    /**
     * setta il design: cambia se lo chiama un viewer o un player
     * @param instance
     */
    protected void getDesign (GameGui instance) {
        if (instance instanceof GamePlayerGui)
            designPlayer((GamePlayerGui) instance);
        else
            designViewer();
    }

    private void designPlayer (GamePlayerGui instance){
        //setta le componenti essenziali per il gioco
        gameConsole = new JTextField("Console di gioco");
        gameConsole.setBounds(5,5,730,40);
        gameConsole.setEditable(false);
        gameConsole.setFont(new Font("Arial",Font.BOLD,15));
        gameConsole.setBackground(Color.white);
        gameConsole.setSelectedTextColor(Color.red);
        add(gameConsole);

        phraseTheme = new JTextField("Tema frase: ");
        phraseTheme.setBounds(735,5,360,40);
        phraseTheme.setEditable(false);
        phraseTheme.setFont(new Font("Arial",Font.BOLD,15));
        gameConsole.setSelectedTextColor(Color.GREEN);
        phraseTheme.setBackground(Color.white);
        add(phraseTheme);

        ruota = new JTextField();
        ruota.setBounds(5, 420, 417, 135);
        ruota.setEditable(false);
        ruota.setFont(new Font("Arial",Font.BOLD,30));
        ruota.setBackground(Color.ORANGE);
        ruota.setHorizontalAlignment(SwingConstants.CENTER);
        add(ruota);
        ruota.setColumns(10);

        JButton btnGira = new JButton("GIRA LA RUOTA");
        btnGira.setBounds(5, 565, 417, 55);
        btnGira.setBackground(Color.GREEN);
        btnGira.addActionListener(e -> {
            if(!endGame) {
                if (isGameStarted) {
                    if (myTurn) {
                        if (!isWheelSpinned) {
                            isWheelSpinned = true;
                            //se e solo se ho il bonus e lo utilizzo posso evitare i casi perde e passa
                            String modifier = "JOLLY";
                            try {
                                if(testJolly) {
                                    modifier = "JOLLY";
                                    testJolly = false;
                                }
                                else
                                    modifier = server.getModifier();

                                    if((modifier.equals("PASSA")) &&
                                        instance.getMyBonus()>0) {
                                    ArrayList<String> commands=new ArrayList();
                                    commands.add("WHEEL");
                                    commands.add(modifier);
                                    instance.useJollyRequest("Hai ottenuto passa",commands);
                                }
                                else
                                    server.spinWheel(modifier);
                            } catch (RemoteException ex) {
                                System.err.println("Client: errore di comunicazione con server remoto");
                                ex.printStackTrace();
                            }
                        } else
                            JOptionPane.showMessageDialog(null, "Hai gi" + (char) 224 +"  girato la ruota");
                    } else
                        JOptionPane.showMessageDialog(null, "Attendi il tuo turno");
                } else
                    JOptionPane.showMessageDialog(null, "Attendi che il gioco inizi");
            }
            else
                JOptionPane.showMessageDialog(null, "Il gioco " + (char) 232 +" terminato");
        });
        add(btnGira);

        player1Lbl = new JLabel("Player 1");
        player1Lbl.setBounds(5, 300, 417, 40);
        player1Lbl.setFont(new Font("Arial",Font.BOLD,13));
        add(player1Lbl);

        player2Lbl = new JLabel("Player 2");
        player2Lbl.setBounds(5,340,417,40);
        player2Lbl.setFont(new Font("Arial",Font.BOLD,13));
        add(player2Lbl);//380

        JLabel playerLbl = new JLabel("CURRENT PLAYER:");
        playerLbl.setBounds(452, 278, 150, 48);
        playerLbl.setBackground(Color.GREEN);
        add(playerLbl);

        currentPlayerTextField = new JTextField("Turno non assegnato");
        currentPlayerTextField.setBounds(452, 319, 532, 61);
        currentPlayerTextField.setFont(new Font("Arial",Font.BOLD,30));
        currentPlayerTextField.setBackground(Color.ORANGE);
        currentPlayerTextField.setHorizontalAlignment(SwingConstants.CENTER);
        currentPlayerTextField.setEditable(false);
        add(currentPlayerTextField);
        currentPlayerTextField.setColumns(10);

        timerLabel = new JLabel("5 sec per lettera, 10 per frase");
        timerLabel.setBounds(452,417,256,20);
        timerLabel.setFont(new Font("Arial",Font.BOLD,15));
        timerLabel.setForeground(Color.RED);
        add(timerLabel);

        JLabel lblInsertLetter = new JLabel("INSERT LETTER:");
        lblInsertLetter.setBounds(452, 437, 265, 48);
        add(lblInsertLetter);

        insert_letter = new JTextField();
        insert_letter.setBounds(452, 470, 322, 55);
        add(insert_letter);
        insert_letter.setColumns(10);

        JLabel lblInsertPhrase = new JLabel("INSERT PHRASE:");
        lblInsertPhrase.setBounds(452, 530, 265, 48);
        add(lblInsertPhrase);

        insert_phrase = new JTextField();
        insert_phrase.setColumns(10);
        insert_phrase.setBounds(452, 565, 322, 55);
        add(insert_phrase);

        JButton conferma_lettera = new JButton("CONFERMA LETTERA");
        conferma_lettera.setBounds(784, 470, 200, 55);
        conferma_lettera.setBackground(Color.RED);
        conferma_lettera.addActionListener(e -> {
            if(!endGame) {
                if (isGameStarted) {
                    if (myTurn) {
                        if(!instance.equalsToAVocal(insert_letter.getText())) {
                            if (isWheelSpinned) {
                                if (secodnsGone < 6) {
                                    instance.setTimer(false);
                                    String letter = insert_letter.getText();
                                    int letterOccurences = phrasePanel[currentShowingPhrase].getEqualsNumber(letter);
                                    int wheelBonus = Integer.parseInt(ruota.getText());
                                    if (letterOccurences == 0 && instance.getMyBonus() > 0) {
                                        ArrayList<String> commands = new ArrayList<>();
                                        commands.add("LETTERWRONG");
                                        commands.add(letter);
                                        commands.add(Integer.toString(letterOccurences * wheelBonus));
                                        try {
                                            instance.useJollyRequest("Lettera non trovata", commands);
                                        } catch (RemoteException ex) {
                                            ex.printStackTrace();
                                        }
                                    } else {
                                        try {
                                            server.callLetter(letter, letterOccurences * wheelBonus);
                                        } catch (RemoteException ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                } else {
                                    instance.setTimer(false);
                                    if (instance.getMyBonus()> 0) {
                                        ArrayList<String> commands = new ArrayList<>();
                                        commands.add("LETTEROUTTIME");
                                        try {
                                            instance.useJollyRequest("Tempo scaduto per chiamare lettera", commands);
                                        } catch (RemoteException ex) {
                                            ex.printStackTrace();
                                        }
                                    } else {
                                        try {
                                            server.outTimeCall();
                                        } catch (RemoteException ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                            } else
                                JOptionPane.showMessageDialog(null, "Prima devi girare la ruota");
                        }
                        else{
                            if (secodnsGone < 6) {
                                instance.setTimer(false);
                                String letter = insert_letter.getText();
                                int letterOccurences = phrasePanel[currentShowingPhrase].getEqualsNumber(letter);
                                int wheelBonus = Integer.parseInt(ruota.getText());
                                if (letterOccurences == 0 && instance.getMyBonus() > 0) {
                                    ArrayList<String> commands = new ArrayList<>();
                                    commands.add("LETTERWRONG");
                                    commands.add(letter);
                                    commands.add(Integer.toString(letterOccurences * wheelBonus));
                                    try {
                                        instance.useJollyRequest("Lettera non trovata", commands);
                                    } catch (RemoteException ex) {
                                        ex.printStackTrace();
                                    }
                                } else {
                                    try {
                                        server.callLetter(letter, letterOccurences * wheelBonus);
                                    } catch (RemoteException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            } else {
                                instance.setTimer(false);
                                if (instance.getMyBonus() > 0) {
                                    ArrayList<String> commands = new ArrayList<>();
                                    commands.add("LETTEROUTTIME");
                                    try {
                                        instance.useJollyRequest("Tempo scaduto per chiamare lettera", commands);
                                    } catch (RemoteException ex) {
                                        ex.printStackTrace();
                                    }
                                } else {
                                    try {
                                        server.outTimeCall();
                                    } catch (RemoteException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }
                    } else
                        JOptionPane.showMessageDialog(null, "Attendi il tuo turno");
                } else
                    JOptionPane.showMessageDialog(null, "Attendi che il gioco inizi");
            }
            else
                JOptionPane.showMessageDialog(null, "Il gioco " + (char) 232 + " terminato");
        });
        add(conferma_lettera);

        JButton conferma_frase = new JButton("CONFERMA FRASE");
        conferma_frase.setBounds(784, 565, 200, 55);
        conferma_frase.setBackground(Color.RED);
        conferma_frase.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!endGame) {
                    if (isGameStarted) {
                        if (myTurn) {
                            instance.setTimer(false);
                            try {
                                server.callPhrase(insert_phrase.getText());
                            } catch (RemoteException ex) {
                                ex.printStackTrace();
                            }
                        } else
                            JOptionPane.showMessageDialog(null, "Attendi il tuo turno");
                    } else
                        JOptionPane.showMessageDialog(null, "Attendi che il gioco inizi");
                }
                else
                    JOptionPane.showMessageDialog(null, "Il gioco " + (char) 232 +" terminato");
            }
        });
        add(conferma_frase);
    }

    private void designViewer () {
        //setta le componenti essenziali per il gioco
        gameConsole = new JTextField("Console di gioco");
        gameConsole.setBounds(452,278,643,135);
        gameConsole.setEditable(false);
        gameConsole.setFont(new Font("Arial",Font.BOLD,15));
        gameConsole.setBackground(Color.white);
        gameConsole.setSelectedTextColor(Color.red);
        add(gameConsole);

        phraseTheme = new JTextField("Tema frase: ");
        phraseTheme.setBounds(5,5,1090,40);
        phraseTheme.setEditable(false);
        phraseTheme.setFont(new Font("Arial",Font.BOLD,20));
        gameConsole.setSelectedTextColor(Color.GREEN);
        phraseTheme.setBackground(Color.white);
        add(phraseTheme);

        //Label per i giocatori
        player1Lbl = new JLabel("Player 1");
        player2Lbl = new JLabel("Player 2");

        player1Lbl.setBounds(5,278,417,40);
        player2Lbl.setBounds(5,333,417,40);

        player1Lbl.setFont(new Font("Arial",Font.BOLD,12));
        player2Lbl.setFont(new Font("Arial",Font.BOLD,12));

        add(player1Lbl);
        add(player2Lbl);


        ruota = new JTextField("RUOTA");
        ruota.setBounds(5, 471, 417, 119);
        ruota.setEditable(false);
        ruota.setFont(new Font("Arial",Font.BOLD,30));
        ruota.setBackground(Color.ORANGE);
        ruota.setHorizontalAlignment(SwingConstants.CENTER);
        add(ruota);

        JLabel currentPlayerLbl = new JLabel("CURRENT PLAYER:");
        currentPlayerLbl.setBounds(452, 418, 417, 48);
        currentPlayerLbl.setBackground(Color.GREEN);
        add(currentPlayerLbl);

        currentPlayerTextField = new JTextField("Turno non assegnato");
        currentPlayerTextField.setBounds(452, 471, 643, 119);
        currentPlayerTextField.setFont(new Font("Arial",Font.BOLD,20));
        currentPlayerTextField.setBackground(Color.ORANGE);
        currentPlayerTextField.setHorizontalAlignment(SwingConstants.CENTER);
        add(currentPlayerTextField);
    }

    //----------------------------------------------------------------------------------------------------------------------

    //METODI CHIAMABILI DAL SERVER
    //chiamato quando ci saranno sufficienti giocatori
    protected void setStartMatch(String[] phrases, String[] themes) {
        isGameStarted=true;
        currentShowingPhrase=0;
        this.phrasePanel = new PhraseLayoutManager[5];
        for (int i = 0; i<5;i++){
            phrasePanel[i] = new PhraseLayoutManager(phrases[i]);
            cardsContainer.add("PHRASE"+i, phrasePanel[i]);
        }
        this.phraseThemes = themes;
        currentPhrase.show(cardsContainer,"PHRASE"+currentShowingPhrase);
        phraseTheme.setText("Tema frase: "+ phraseThemes[0]);
    }

    protected void setTimer(boolean set){
        if(set)
            actionWaiter.start();
        else {
            secodnsGone=0;
            actionWaiter.stop();
            timerLabel.setText("5 sec per letter, 10 per frase");
        }
    }

    protected void setJollyPoppedOut(boolean set) {
        this.isJollyPoppedout=set;
    }

    protected void changePhrase(){
        currentShowingPhrase++;
        currentPhrase.show(cardsContainer,"PHRASE"+currentShowingPhrase);
        phraseTheme.setText("Tema frase: "+phraseThemes[currentShowingPhrase]);
    }
    /**
     * chiamato quando si vorrà mostrare una lettera indovinata della frase
     */
    protected void showEquals(String letter) {
        phrasePanel[currentShowingPhrase].showEquals(letter);
    }

    protected void endGameLeaveMatch(){
        currentPhrase.show(cardsContainer,"LEAVEDMATCHPLAYER");
        endGame = true;
    }

    protected boolean hasGuessedOneLetter(){
        return this.canCallPhraseOrVocal;
    }

    protected void setCanCallPhraseOrVocal(boolean can){
        this.canCallPhraseOrVocal=can;
    }

    protected int getMyBonus(){
        int bonus = 0;
        try {
            System.out.println(guiDetentor.getNickname());
            bonus=server.getMyBonus(guiDetentor.getNickname());
            System.out.println("bonus: " + bonus);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        return bonus;
    }

//----------------------------------------------------------------------------------------------------------------------

    //SETTABILI DA SERVER
    protected void setMyTurn(boolean myTurn) {
        this.myTurn = myTurn;
    }
    protected void setConsoleText(String text){
        gameConsole.setText("Console di gioco: "+ text);
    }
    public void setWheelText(String ruota) {
        this.ruota.setText(ruota);
    }
    protected void setWheelSpinned(boolean isWheelToBeSpinned){
        this.isWheelSpinned = isWheelToBeSpinned;
    }

    public void setCurrentPlayertextField(String text) {
        this.currentPlayerTextField.setText(text);
    }
    public void setPlayerLabel(String player1,String player2){
        player1Lbl.setText(player1);
        player2Lbl.setText(player2);

    }
    public void notEnoughPhrases() {
        JOptionPane.showMessageDialog(null,"Non ci sono abbastanza frasi nel database." +
                "Verranno avvisati gli admin");
        remoteGuiManager.closeGameConnection();
        remoteGuiManager.leaveMatch();
        dispose();
    }

    //----------------------------------------------------------------------------------------------------------------------


    //METODI UTILI PER LA CLASSE GAMEPLAYER E PER QUESTA CLASSE
    protected boolean isGameStarted(){
        return isGameStarted;
    }

    protected boolean equalsToAVocal(String letter){
        boolean equalsToOne=false;
        String[] vocals={"A","E","I","O","U"};
        for(String vocal: vocals)
            if (vocal.equalsIgnoreCase(letter))
                equalsToOne=true;
        return equalsToOne;
    }

    protected void initPhrasePanel() {
        currentPhrase = new CardLayout();
        cardsContainer = new JPanel();
        cardsContainer.setLayout(currentPhrase);
        cardsContainer.setBounds(phrasePanelx,phrasePanely,phrasePanelWidth,phrasePanelHeight);
        //design pannelli di attesa giocatori e utente che lascia match
        designNotEnoughPlayerPanel();
        designLeaveMatchPlayerPanel();
        cardsContainer.add("NOTENOUGHPLAYERS",notEnoughPlayerPanel);
        cardsContainer.add("LEAVEDMATCHPLAYER",leaveMatchPlayerPanel);
        this.getContentPane().add(cardsContainer);
        currentPhrase.show(cardsContainer, "NOTENOUGHPLAYERS");
    }

    private void designNotEnoughPlayerPanel() {
        notEnoughPlayerPanel = new JPanel();
        notEnoughPlayerPanel.setLayout(null);
        notEnoughPlayerPanel.setBounds(phrasePanelx,phrasePanely,phrasePanelWidth,phrasePanelHeight);
        JTextField notEnoughPlayerTxt = new JTextField("In attesa di altri giocatori...");
        notEnoughPlayerTxt.setSize(phrasePanelWidth,phrasePanelHeight);
        notEnoughPlayerTxt.setEditable(false);
        notEnoughPlayerTxt.setFont(new Font("Arial",Font.BOLD,40));
        notEnoughPlayerTxt.setBackground(Color.ORANGE);
        notEnoughPlayerTxt.setHorizontalAlignment(SwingConstants.CENTER);
        notEnoughPlayerPanel.add(notEnoughPlayerTxt);
    }

    private void designLeaveMatchPlayerPanel() {
        leaveMatchPlayerPanel = new JPanel();
        leaveMatchPlayerPanel.setLayout(null);
        leaveMatchPlayerPanel.setBounds(phrasePanelx,phrasePanely,phrasePanelWidth,phrasePanelHeight);
        JTextField notEnoughPlayerTxt = new JTextField("Un giocatore ha lasciato. La Partita è terminata");
        notEnoughPlayerTxt.setSize(phrasePanelWidth,phrasePanelHeight);
        notEnoughPlayerTxt.setEditable(false);
        notEnoughPlayerTxt.setFont(new Font("Arial",Font.BOLD,40));
        notEnoughPlayerTxt.setBackground(Color.ORANGE);
        notEnoughPlayerTxt.setHorizontalAlignment(SwingConstants.CENTER);
        leaveMatchPlayerPanel.add(notEnoughPlayerTxt);
    }
}
