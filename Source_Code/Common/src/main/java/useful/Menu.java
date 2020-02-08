package useful;

import adminRdF.AdminMenu;
import adminRdF.LogAdmin;
import adminRdF.SubAdmin;
import dbRdF.DbRdFStatistics;
import game.GamePlayerGui;
import game.Match;
import game.Phrase;
import playerRdF.LogPlayer;
import playerRdF.Player;
import playerRdF.PlayerMenu;
import playerRdF.SubPlayer;
import serverRdF.Proxy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public abstract class Menu extends JFrame {
    protected static final long serialVersionUID = 1L;
    protected boolean signed;
    protected User playerData;
    protected Proxy serverComunicator;
    protected CardLayout currentPanel;
    protected Container cardsContainer;
    protected JPanel menuDesign;
    protected JPanel playerDesign;
    protected JPanel viewerDesign;
    protected JPanel staticsDataDesign;
    protected JPanel newMatch;
    protected JPanel createdMatch;
    protected JPanel profileChangeDesign;
    protected JTextField nameTxt;
    protected DbRdFStatistics statisticDb;
    protected List<String> liveGameList;
    protected DefaultListModel<String> model;
    protected JList<String> matchList;
    protected JComboBox<String> matchListViewer;
    protected List<String> deletePhraseList;
    protected String[] phrasesContent;
    protected JPanel allPhrase;
    protected JPanel addPhrase;
    protected JPanel delPhrase;
    protected JButton btnSub;
    protected JButton btnLog;
    protected JPanel phraseDesign;
    protected JButton changeBtn;
    protected JTabbedPane phraseLayout;
    protected JList<String> phraseListAdd;
    protected JList<String> phraseListDel;


    /**
     * ritorna database statistiche
     */
    public void getStatisticsDb(){
        this.statisticDb=serverComunicator.getStatisticsDatabase();
        statisticDb.openConnection();
    }

    public void refresh(Menu instance){
        if (instance instanceof PlayerMenu) {
            liveGameList = serverComunicator.getLiveGames();
            List<String> gameNotStarted = serverComunicator.getLiveGamesNotStarted();
            model.removeAllElements();
            matchListViewer.removeAllItems();
            for (String gameNs : gameNotStarted)
                model.addElement(gameNs);
            for (String game : liveGameList)
                matchListViewer.addItem(game);
        }
        else{
            liveGameList = serverComunicator.getLiveGames();
            phrasesContent = getPhrases();
            matchListViewer.removeAllItems();
            model.removeAllElements();
            for(String game: liveGameList){
                matchListViewer.addItem(game);
            }
            for(String s: phrasesContent){
                model.addElement(s);
            }
        }
    }

    public void designPlayerMenu (PlayerMenu instance) {
        //creo bottoni e design
        //Bottoni e label per registrazione

        JLabel regLbl = new JLabel("Before playing make sure to sign up!");
        regLbl.setBounds(10, 10, 330, 15);
        menuDesign.add(regLbl);

        JButton btnSub = new JButton("SUBSCRITPION");
        btnSub.setBounds(10,30,330,25);
        btnSub.addActionListener(e -> {
            SubPlayer sp = new SubPlayer(serverComunicator,instance);
        });
        menuDesign.add(btnSub);

        JButton btnLog = new JButton("LOGIN");
        btnLog.setBounds(10,60,330,25);
        btnLog.addActionListener(e -> new Thread(() -> {
            LogPlayer lp = new LogPlayer(serverComunicator,instance);
            lp.setVisible(true);
        }).start());
        menuDesign.add(btnLog);

        JButton changeBtn = new JButton("PROFILE SETTINGS");
        changeBtn.setFont(new Font("Arial Black", Font.PLAIN, 24));
        changeBtn.setBounds(350, 30, 400, 55);
        changeBtn.addActionListener(e -> {
            if(signed)
                currentPanel.show(cardsContainer,"PROFILE");
            else
                JOptionPane.showMessageDialog(null,"Sign up before manage your data");
        });
        menuDesign.add(changeBtn);

        //Bottoni per indicare il tipo di giocatore
        JButton playerBtn = new JButton("PLAYER");
        System.out.println();
        playerBtn.setFont(new Font("Arial Black", Font.PLAIN, 24));
        playerBtn.setBounds(181, 419, 161, 60);
        playerBtn.addActionListener(e -> {
            if(signed) {
                refresh(instance);
                currentPanel.show(cardsContainer, "PLAYER");
            }
            else
                JOptionPane.showMessageDialog(null,"Sign up before playing");
        });
        menuDesign.add(playerBtn);
        JButton viewerBtn = new JButton("VIEWER");
        viewerBtn.setFont(new Font("Arial Black", Font.PLAIN, 24));
        viewerBtn.setBounds(10, 419, 161, 60);
        viewerBtn.addActionListener(e -> {
            if(signed){
                refresh(instance);
                currentPanel.show(cardsContainer, "VIEWER");
            }
            else
                JOptionPane.showMessageDialog(null,"Sign up before vieweing");
        });
        menuDesign.add(viewerBtn);
        JButton statisticsDataBtn = new JButton("STATISTICS DATA");
        statisticsDataBtn.setFont(new Font("Arial Black", Font.PLAIN, 24));
        statisticsDataBtn.setBounds(347, 419, 280, 60);
        statisticsDataBtn.addActionListener(e -> {
            if(signed)
                currentPanel.show(cardsContainer,"STATISTICS");
            else
                JOptionPane.showMessageDialog(null,"Sign up before see statistics data");
        });
        menuDesign.add(statisticsDataBtn);
    }

    public void designAdminMenu (AdminMenu instance) {
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                serverComunicator.closeConnection();
                dispose();
                System.exit(0);
            }
        });
        //creo bottoni e design
        //Bottoni e label per registrazione
        JLabel regLbl = new JLabel("Before playing make sure to sign up!");
        regLbl.setBounds(10, 10, 330, 15);
        menuDesign.add(regLbl);
        btnSub = new JButton("SUBSCRITPION");
        btnSub.setBounds(10,30,330,25);
        btnSub.addActionListener(e -> {
            SubAdmin sa = new SubAdmin(serverComunicator,instance);
        });
        menuDesign.add(btnSub);
        btnLog = new JButton("LOGIN");
        btnLog.setBounds(10,60,330,25);
        btnLog.addActionListener(e -> {
            LogAdmin la = new LogAdmin(serverComunicator,instance);
            la.setVisible(true);
        });
        menuDesign.add(btnLog);
        //Bottone per modifcare dati profilo
        changeBtn = new JButton("PROFILE SETTINGS");
        changeBtn.setFont(new Font("Arial Black", Font.PLAIN, 24));
        changeBtn.setBounds(350, 30, 400, 55);
        changeBtn.addActionListener(e -> {
            if(signed)
                currentPanel.show(cardsContainer, "PROFILE");
            else
                JOptionPane.showMessageDialog(null,"before changing the profile, please sign up");
        });
        menuDesign.add(changeBtn);
        //Bottoni per indicare il tipo di giocatore
        JButton phrasesBtn = new JButton("PHRASES");
        phrasesBtn.setFont(new Font("Arial Black", Font.PLAIN, 24));
        phrasesBtn.setBounds(181, 419, 161, 60);
        phrasesBtn.addActionListener(e -> {
            if(signed) {
                refresh(instance);
                currentPanel.show(cardsContainer, "PHRASES");
            }
            else
                JOptionPane.showMessageDialog(null,"before managing phrases, please sign up");
        });
        menuDesign.add(phrasesBtn);

        JButton statisticsDataBtn = new JButton("STATISTICS DATA");
        statisticsDataBtn.setFont(new Font("Arial Black", Font.PLAIN, 24));
        statisticsDataBtn.setBounds(347, 419, 280, 60);
        statisticsDataBtn.addActionListener(e -> {
            if(signed)
                currentPanel.show(cardsContainer,"STATS");
            else
                JOptionPane.showMessageDialog(null,"Sign up before playing");
        });
        menuDesign.add(statisticsDataBtn);
        menuDesign.add(phrasesBtn);
        JButton viewerBtn = new JButton("VIEWER");
        viewerBtn.setFont(new Font("Arial Black", Font.PLAIN, 24));
        viewerBtn.setBounds(10, 419, 161, 60);
        viewerBtn.addActionListener(e -> {
            if(signed) {
                refresh(instance);
                currentPanel.show(cardsContainer, "VIEWER");
            }
            else
                JOptionPane.showMessageDialog(null,"before viewing matches, please sign up");
        });
        menuDesign.add(viewerBtn);
        //immagine di sfondo
        JLabel imgLbl = new JLabel();
        imgLbl.setIcon(new ImageIcon(""));
        imgLbl.setHorizontalAlignment(SwingConstants.CENTER);
        imgLbl.setBounds(0, 0, 942, 539);
        menuDesign.add(imgLbl);
    }

    /**
     * setup: cambia tra player e admin
     * @param instance
     */
    public void frameSetup(Menu instance){
        if (instance instanceof PlayerMenu)
            playerSetUp();
        else
            adminSetup();
    }

    private void playerSetUp() {
        setTitle("Menu principale. Owner: no one");
        //inizializzo il frame
        this.setBounds(10,10,900,600);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                serverComunicator.closeConnection();
                System.exit(0);
            }
        });
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }
        //inizializzazione di tutti i frame
        currentPanel = new CardLayout();
        cardsContainer = getContentPane();
        cardsContainer.setSize(600,600);
        cardsContainer.setLayout(currentPanel);
        menuDesign = new JPanel();
        playerDesign = new JPanel();
        viewerDesign = new JPanel();
        staticsDataDesign = new JPanel();
        profileChangeDesign = new JPanel();
        newMatch = new JPanel();
        createdMatch = new JPanel();
        //settaggio del layout
        menuDesign.setLayout(null);
        playerDesign.setLayout(null);
        viewerDesign.setLayout(null);
        staticsDataDesign.setLayout(null);
        profileChangeDesign.setLayout(null);
        newMatch.setLayout(null);
        createdMatch.setLayout(null);
        //aggiungo le cards al container
        cardsContainer.add("MENU", menuDesign);
        cardsContainer.add("PLAYER", playerDesign);
        cardsContainer.add("VIEWER",viewerDesign);
        cardsContainer.add("STATISTICS", staticsDataDesign);
        cardsContainer.add("PROFILE",profileChangeDesign);

        ImageIcon imageIcon = new ImageIcon("src/main/resources/lb");
        JLabel imgLabel = new JLabel(imageIcon);
        imgLabel.setBounds(140,145,480,220);
        menuDesign.add(imgLabel);
    }

    private void adminSetup () {
        setTitle("Menu principale. Owner: no one");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        //inizializzo il frame
        this.setBounds(10,10,900,600);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                serverComunicator.closeConnection();
                dispose();
            }
        });
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }
        //inizializzazione di tutti i frame
        currentPanel = new CardLayout();
        cardsContainer = getContentPane();
        cardsContainer.setSize(900,600);
        cardsContainer.setLayout(currentPanel);
        menuDesign = new JPanel();
        phraseDesign = new JPanel();
        viewerDesign = new JPanel();
        staticsDataDesign = new JPanel();
        profileChangeDesign = new JPanel();
        allPhrase = new JPanel();
        addPhrase = new JPanel();
        delPhrase = new JPanel();
        //settaggio del layout
        menuDesign.setLayout(null);
        phraseDesign.setLayout(null);
        viewerDesign.setLayout(null);
        staticsDataDesign.setLayout(null);
        profileChangeDesign.setLayout(null);
        allPhrase.setLayout(null);
        addPhrase.setLayout(null);
        delPhrase.setLayout(null);
        //aggiungo le cards al container
        cardsContainer.add("MENU", menuDesign);
        cardsContainer.add("PHRASES", phraseDesign);
        cardsContainer.add("VIEWER",viewerDesign);
        cardsContainer.add("STATS",staticsDataDesign);
        cardsContainer.add("PROFILE",profileChangeDesign);

        ImageIcon imageIcon = new ImageIcon("src/main/resources/lb");
        JLabel imgLabel = new JLabel(imageIcon);
        imgLabel.setBounds(140,145,480,220);
        menuDesign.add(imgLabel);
    }

    /**
     * design del viewer: comune tra admin e player
     * @param instance
     */
    public void designViewer(Menu instance) {
        matchListViewer = new JComboBox<>();
        matchListViewer.setBounds(30, 30, 500, 60);
        liveGameList = serverComunicator.getLiveGames();
        if(liveGameList.size()!=0) {
            for (String game : liveGameList) {
                matchListViewer.addItem(game);
            }
        }
        else
            matchListViewer.addItem("Ancora nessun match creato");

        viewerDesign.add(matchListViewer);
        viewerDesign.add(backButton());

        JButton confirmBtn = new JButton("CONFERMA");
        confirmBtn.setBounds(540, 30, 170, 35);
        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Match match = serverComunicator.getMatchData((String) matchListViewer.getSelectedItem());
                final int serverPort = (int) serverComunicator.getMyServerData(match).get(1);
                if (match != null) {
                    new Thread(() -> {
                        //GameViewerGui gameViewerGui = new GameViewerGui((String) matchListViewer.getSelectedItem(),
                                //serverPort, match, playerData,instance);
                    }).start();
                } else
                    JOptionPane.showMessageDialog(null, "Errore di recupero dei dati del gioco da server");
            }
        });
        viewerDesign.add(confirmBtn);
    }


    /**
     * design dell'interfaccia di giocatore
     * @param instance
     */
    public void designPlayer(PlayerMenu instance) {
        //aggiungo componenti per creazione partita e lista delle partite
        designPlayerTabbedPane(instance);
        playerDesign.add(backButton());
        //tabbed pane per creare match o partecipare
        JTabbedPane phraseLayout = new JTabbedPane();
        phraseLayout.setBounds(5, 0, 890, 520);
        phraseLayout.addTab("nuova partita", newMatch);
        phraseLayout.addTab("partite create", createdMatch);
        playerDesign.add(phraseLayout);
    }

    private void designPlayerTabbedPane(PlayerMenu instance) {
        //design partite da creare
        JLabel matchLbl = new JLabel("Aggiungi un nome alla partita di massimo 20 caratteri!");
        matchLbl.setBounds(5, 30, 890, 20);
        newMatch.add(matchLbl);
        this.nameTxt = new JTextField();
        nameTxt.setBounds(5, 55, 500, 30);
        newMatch.add(nameTxt);
        JButton confirmNewMatchBtn = new JButton("CONFERMA");
        confirmNewMatchBtn.setBounds(5, 95, 500, 35);
        newMatch.add(confirmNewMatchBtn);
        confirmNewMatchBtn.addActionListener(e -> {
            Match newMatch;
            List<Object> relatedServerData = null;
            newMatch = serverComunicator.addNewMatch(playerData.getNickname(), nameTxt.getText());
            if (newMatch != null) {
                System.out.println("Client match id: " + newMatch.getMatchId());
                relatedServerData = serverComunicator.getMyServerData(newMatch);
                System.out.println(relatedServerData.get(0));
                System.out.println(relatedServerData.get(1));
            }
            if (newMatch != null && relatedServerData != null && relatedServerData.get(0) != null) {
                GamePlayerGui gamePlayerGui = new GamePlayerGui((String) relatedServerData.get(0),
                        (int) relatedServerData.get(1), newMatch, (Player) playerData, instance);
            } else
                JOptionPane.showMessageDialog(null, "C'" + (char) 232 + " stato un errore" +
                        " nella creazione del match controlla che il nome sia massimo 20 caratteri, altrimenti chiud" +
                        "i e riapri l'interfaccia");

        });
        //design partite create
        //lista partite
        liveGameList = serverComunicator.getLiveGames();
        model = new DefaultListModel<>();
        matchList = new JList<>(model);
        matchList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        matchList.setLayoutOrientation(JList.VERTICAL);
        matchList.setVisibleRowCount(-1);
        for(String m: liveGameList)
            model.addElement(m);
        JScrollPane listScroller = new JScrollPane(matchList);
        listScroller.setBounds(5,5,800,400);
        listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        createdMatch.add(listScroller);
        //bottone di conferma partita scelta tra quelle live
        JButton confirmBtnCreatedMatch = new JButton("CONFERMA");
        confirmBtnCreatedMatch.setBounds(5, 405, 800, 35);
        confirmBtnCreatedMatch.addActionListener(e -> {
            final Match match = serverComunicator.getMatchData(matchList.getSelectedValue());
            final int serverPort = (int) serverComunicator.getMyServerData(match).get(1);
            if (match != null) {
                new Thread(() -> {
                    GamePlayerGui gamePlayerGui = new GamePlayerGui(matchList.getSelectedValue(),
                            serverPort, match, (Player) playerData,instance);
                }).start();
            } else
                JOptionPane.showMessageDialog(null, "Errore di recupero dei dati del gioco da server");
        });
        createdMatch.add(confirmBtnCreatedMatch);
        //confirmBtnCreatedMatch
    }

    /**
     * design statistiche: comune tra player e admin
     */
    public void designStatisticsData(){
        //Statistiche generali
        //Label
        JLabel generalStatisticsLabel = new JLabel("Statistiche generali");
        generalStatisticsLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
        generalStatisticsLabel.setBounds(12, 0, 546, 33);
        staticsDataDesign.add(generalStatisticsLabel);
        //Testo con statistiche
        String[] generalStatisticsArray = statisticDb.generalStatistics();
        JList<String> generalStatistics = new JList<>(generalStatisticsArray);
        generalStatistics.setVisible(true);
        generalStatistics.setLayoutOrientation(JList.VERTICAL);
        generalStatistics.setVisibleRowCount(-1);
        generalStatistics.setBounds(597, 45, 256, 386);
        generalStatistics.setBounds(12, 46, 546, 200);
        JScrollPane generalStatsScroller = new JScrollPane(generalStatistics);
        generalStatsScroller.setBounds(12, 46, 546, 200);
        generalStatsScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        staticsDataDesign.add(generalStatsScroller);

        //Statistiche per utente
        //Label stat utenti
        JLabel lblStatisticheUtente = new JLabel("Statistiche Utente");
        lblStatisticheUtente.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblStatisticheUtente.setBounds(12, 259, 546, 33);
        staticsDataDesign.add(lblStatisticheUtente);
        final JTextArea userStatistics = new JTextArea();
        userStatistics.setBounds(12, 305, 546, 200);
        userStatistics.setEditable(false);
        JScrollPane UserStatsScroller = new JScrollPane(userStatistics);
        UserStatsScroller.setBounds(12, 305, 546, 200);
        UserStatsScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        staticsDataDesign.add(UserStatsScroller);
        //Label lista utenti
        JLabel userListAdvisor = new JLabel("Lista degli utenti");
        userListAdvisor.setFont(new Font("Tahoma", Font.BOLD, 15));
        userListAdvisor.setBounds(597, 0, 256, 33);
        staticsDataDesign.add(userListAdvisor);
        //Lista utenti
        String[] userNickList = statisticDb.userNicknameArray();
        final JList<String> userList = new JList(userNickList);
        userList.setVisible(true);
        userList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        userList.setLayoutOrientation(JList.VERTICAL);
        userList.setVisibleRowCount(-1);
        userList.setBounds(597, 45, 256, 386);
        JScrollPane listScroller = new JScrollPane(userList);
        listScroller.setBounds(597, 45, 256, 386);
        listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        staticsDataDesign.add(listScroller);
        //Bottone di conferma
        JButton confirmUserBtn = new JButton("Conferma utente");
        confirmUserBtn.setFont(new Font("Tahoma", Font.BOLD, 16));
        confirmUserBtn.setBounds(607, 444, 233, 61);
        confirmUserBtn.addActionListener(e -> {
            String[] perUserStatsArray = statisticDb.perUserStatistics(userList.getSelectedValue());
            userStatistics.setText("");
            for(String s : perUserStatsArray)
                userStatistics.setText(userStatistics.getText()+"\n " +s);
        });
        staticsDataDesign.add(confirmUserBtn);

        //Bottone back
        JButton backButton = backButton();
        staticsDataDesign.add(backButton);
    }

    /**
     *Cerca tutte le frasi nel db tramite proxy
     */
    public String[] getPhrases(){
        Phrase[] phrases =serverComunicator.queryAllPhrases();
        String[] phrasesContent = new String[phrases.length];
        for(int i = 0; i<phrases.length;i++)
            phrasesContent[i]=phrases[i].getPhrase();
        return phrasesContent;
    }

    /**
     *Design pannello di management delle frasi
     */
    public void designPhrase(AdminMenu instance) {
        //design dei pannelli da inserire nella tabbed pane
        //design del pannello di aggiunta frasi
        addPhraseDesign(instance);
        //design lista scrollabile e aggiunta ai panneli a cui occore
        allPhrase.add(phraseListADD());
        delPhrase.add(phraseListDEL());
        //design bottoni utili ai pannelli
        allPhrase.add(backButton());
        addPhrase.add(backButton());
        delPhrase.add(backButton());
        delPhrase.add(confirmButton());

        //design tabbed pane con pannelli sopra creati
        phraseLayout = new JTabbedPane();
        phraseLayout.setBounds(5, 0, 900, 560);
        phraseLayout.addTab("tutte", allPhrase);
        phraseLayout.addTab("aggiungi", addPhrase);
        phraseLayout.addTab("elimina", delPhrase);
        phraseDesign.add(phraseLayout);
    }

    private JScrollPane phraseListADD() {
        phraseListAdd = new JList<>(model);
        phraseListAdd.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        phraseListAdd.setLayoutOrientation(JList.VERTICAL);
        phraseListAdd.setVisibleRowCount(-1);
        phraseListAdd.setBounds(5, 5, 880, 400);
        JScrollPane listScroller = new JScrollPane(phraseListAdd);
        listScroller.setBounds(5,5,880,400);
        listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        return listScroller;
    }
    private JScrollPane phraseListDEL() {
        phraseListDel = new JList<>(model);
        phraseListDel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        phraseListDel.setLayoutOrientation(JList.VERTICAL);
        phraseListDel.setVisibleRowCount(-1);
        phraseListDel.setBounds(5, 5, 880, 400);
        JScrollPane listScroller = new JScrollPane(phraseListDel);
        listScroller.setBounds(5,5,880,400);
        listScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        return listScroller;
    }

    /**
     *Design aggiunta frasi
     */
    private void addPhraseDesign(AdminMenu instance) {
        JLabel advLbl = new JLabel("Aggiungi le frasi tramite file csv");
        advLbl.setBounds(5, 5, 800, 20);
        addPhrase.add(advLbl);

        JLabel genreLbl = new JLabel("Inserisci il percorso del file da importare");
        genreLbl.setBounds(5, 35, 800, 20);
        addPhrase.add(genreLbl);

        final JTextField pathTxt = new JTextField();
        pathTxt.setBounds(5, 60, 800, 30);
        addPhrase.add(pathTxt);

        JButton directoryButton = new JButton("Clicca qui per sfogliare directory");
        directoryButton.setBounds(5,100,800,40);
        directoryButton.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser("Seleziona il file con le frasi");
            jfc.setDialogTitle("Seleziona la directory con le frasi");
            jfc.setMultiSelectionEnabled(false);
            int response = jfc.showSaveDialog(instance);
            if(response == JFileChooser.APPROVE_OPTION) {
                pathTxt.setText(jfc.getSelectedFile().toString());
            }
            else
                System.out.println("operation deleted");
        });
        addPhrase.add(directoryButton);
        JButton confirmBtn = new JButton("CONFERMA");
        confirmBtn.setBounds(5,165,800,30);
        confirmBtn.addActionListener(e -> {
            boolean fileFound = true;
            CSVFileManager csvFileManager = new CSVFileManager();
            try {
                csvFileManager.importFile(pathTxt.getText());
            }
            catch(IOException fileException){
                JOptionPane.showMessageDialog(null,"File non trovato, controlla percorso");
                fileFound = false;
            }
            if(fileFound) {
                List<String> result = csvFileManager.readLines();
                if(serverComunicator.addPhrases(result))
                    JOptionPane.showMessageDialog(null,"Frasi aggiunte correttamente." +
                            " Esci e rientra dal pannello per vedere modifiche");
                else
                    JOptionPane.showMessageDialog(null,"Potrebbero essersi verificati errori" +
                            " nell'aggiunta di alcune frasi al database");
            }
        });
        addPhrase.add(confirmBtn);
    }

    /**
     *Design della gui di cambio profilo: comune tra player e admin
     */
    public void designProfileChange(){
        JLabel advLbl = new JLabel("Modifica ci"  + (char) 242 + " che ti interessa e clicca conferma");
        advLbl.setBounds(5,5,800,20);
        profileChangeDesign.add(advLbl);

        JLabel nameLbl = new JLabel("NOME");
        nameLbl.setBounds(5,45,800,20);
        profileChangeDesign.add(nameLbl);
        JTextField nameTxt = new JTextField();
        nameTxt.setBounds(5,70,800,25);
        profileChangeDesign.add(nameTxt);

        JLabel surnameLbl = new JLabel("COGNOME");
        surnameLbl.setBounds(5,100,800,20);
        profileChangeDesign.add(surnameLbl);
        JTextField surnametxt = new JTextField();
        surnametxt.setBounds(5,125,800,25);
        profileChangeDesign.add(surnametxt);

        JLabel nickLbl = new JLabel("NICKNAME");
        nickLbl.setBounds(5,155,800,20);
        profileChangeDesign.add(nickLbl);
        JTextField nickTxt = new JTextField();
        nickTxt.setBounds(5,180,800,25);
        profileChangeDesign.add(nickTxt);

        JLabel passwLbl = new JLabel("PASSWORD");
        passwLbl.setBounds(5,210,800,20);
        profileChangeDesign.add(passwLbl);
        JPasswordField passwTxt = new JPasswordField();
        passwTxt.setBounds(5,235,800,25);
        profileChangeDesign.add(passwTxt);

        JButton confirmBtnProfile = new JButton("CONFERMA");
        confirmBtnProfile.setBounds(5,275,800,30);
        confirmBtnProfile.addActionListener(e -> {
            HashMap<String,String> changeValues = new HashMap<>();

            if(!isEmptyString(nameTxt.getText()))
                changeValues.put("NAME",nameTxt.getText());
            else
                changeValues.put("NAME","NONE");
            if(!isEmptyString(surnametxt.getText()))
                changeValues.put("SURNAME",surnametxt.getText());
            else
                changeValues.put("SURNAME","NONE");
            if(!isEmptyString(nickTxt.getText()))
                changeValues.put("NICK",nickTxt.getText());
            else
                changeValues.put("NICK","NONE");
            if(!isEmptyString(String.valueOf(passwTxt.getPassword()))){
                String oldPassword = JOptionPane.showInputDialog("Inserisci la tua vecchia password");
                oldPassword = Encryption.MD5(oldPassword);
                if(oldPassword.equals(playerData.getPassword())) {
                    String pwd = new String (passwTxt.getPassword());
                    pwd = Encryption.MD5(pwd);
                    changeValues.put("PWD", pwd);
                }
                else{
                    changeValues.put("PWD","NONE");
                    JOptionPane.showMessageDialog(null,"La password inserita "  + (char) 232 +" errata" +
                            " e perciò non verrà aggiornata: Gli altri dati saranno modificati");
                }
            }
            else
                changeValues.put("PWD","NONE");
            changeValues.put("MYNICK",playerData.getNickname());
            if(serverComunicator.changeProfile(changeValues)) {
                JOptionPane.showMessageDialog(null, "Dati aggiornati");
                
                if (!changeValues.get("NAME").equals("NONE"))
                    playerData.setName(changeValues.get("NAME"));
                if (!changeValues.get("SURNAME").equals("NONE"))
                    playerData.setSurname(changeValues.get("SURNAME"));
                if (!changeValues.get("NICK").equals("NONE"))
                    playerData.setNickname(changeValues.get("NICK"));
                if (!changeValues.get("PWD").equals("NONE"))
                    playerData.setPassword(changeValues.get("PWD"));
                    
            }
            else
                JOptionPane.showMessageDialog(null,"Errore durante aggiornamento dati:" +
                        " il tuo nickname o la tua mail potrebbero gi" + (char) 224 +" essere usati");
        });
        profileChangeDesign.add(confirmBtnProfile);
        profileChangeDesign.add(backButton());
    }


    private JButton confirmButton() {
        JButton confirmBtn = new JButton("CONFERMA");
        confirmBtn.setBounds(5, 415, 799, 35);
        confirmBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deletePhraseList = phraseListDel.getSelectedValuesList();
                serverComunicator.delPhrase(deletePhraseList);
            }
        });
        return confirmBtn;
    }

    private JButton backButton() {
        JButton backBtn = new JButton("BACK");
        backBtn.setBounds(22,505,97,25);
        backBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentPanel.show(cardsContainer, "MENU");
            }
        });
        return backBtn;
    }

    private boolean isEmptyString(String s){
        boolean isEmpty = true;
        for(int i = 0; i<s.length(); i++)
            if(!s.substring(i,i+1).equals(" ")) {
                isEmpty = false;
                break;
            }
        return isEmpty;
    }

    /**
     * fornisce la chiusura della connessione
     * @param matchId
     */
    public void closeGameConnection(int matchId) {
        serverComunicator.closeGameConnection(matchId);
    }
}
