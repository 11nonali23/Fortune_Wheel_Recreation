package useful;

import adminRdF.SubAdmin;
import dbRdF.SubDbRdF;
import playerRdF.SubPlayer;
import serverRdF.Proxy;
import serverRdF.ServerRdFCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Classe per gestire l'iscrizione dell'utente
 */


public abstract class Subscription extends JFrame {
    protected Container cardsContainer;
    protected CardLayout cards;
    protected JPanel subPanel;
    protected JPanel codeConfirmPanel;
    protected JPanel dbRegistrationPanel;

    protected JLabel regAdv;

    protected int emailCode = 0;

    protected Subscription myIstance;
    protected boolean subbed;
    protected boolean closed;
    protected JLabel nameLbl;
    protected JLabel surnameLbl;
    protected JLabel nickNameLbl;
    protected JLabel emailLbl;
    protected JLabel pwdLbl;

    protected JTextField nameTxt;
    protected JTextField surnameTxt;
    protected JTextField nicknameTxt;
    protected JTextField emailTxt;
    protected JPasswordField passwTxt;
    protected Proxy serverComunicator;

    private Subscription getMyIstance(){
        return myIstance;
    }

    /**
     * Metodo per il desgin delle interfacce di subscription. Ha un design diverso in base al chiamate (Player, admin, database)
     */
    protected void setFrame(){
        this.getContentPane().setBackground(Color.WHITE);
        this.getContentPane().setLayout(null);
        this.setBounds(200,200,381,518);
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closed = true;
                if (getMyIstance() instanceof SubDbRdF) {
                    SubDbRdF istance = (SubDbRdF) getMyIstance();
                    istance.getServer().notifyMe();
                }
                dispose();
            }
        });
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }

        cards = new CardLayout();
        cardsContainer = getContentPane();
        cardsContainer.setSize(381,518);
        cardsContainer.setLayout(cards);

        subPanel = new JPanel();
        codeConfirmPanel = new JPanel();
        dbRegistrationPanel = new JPanel();
        subPanel.setLayout(null);
        codeConfirmPanel.setLayout(null);
        dbRegistrationPanel.setLayout(null);

        subPanel.setBackground(Color.WHITE);
        codeConfirmPanel.setBackground(Color.WHITE);
        dbRegistrationPanel.setBackground(Color.WHITE);

        cardsContainer.add("LOGIN", subPanel);
        cardsContainer.add("CODE",codeConfirmPanel);
        cardsContainer.add("DBREG",dbRegistrationPanel);
    }

    protected void designDbRegPanel(){
        JLabel urlLbl = new JLabel("Inserisci url database");
        urlLbl.setBounds(36,77,282,15);
        urlLbl.setFont(new Font("Arial", Font.PLAIN, 13));
        dbRegistrationPanel.add(urlLbl);

        JTextField urlTxt = new JTextField("jdbc:postgresql://localhost/");
        urlTxt.setFont(new Font("Arial", Font.BOLD, 11));
        urlTxt.setBounds(36, 92, 282, 38);
        dbRegistrationPanel.add(urlTxt);

        JLabel userLbl = new JLabel("Inserisci username database");
        userLbl.setBounds(36,135,282,15);
        userLbl.setFont(new Font("Arial", Font.PLAIN, 13));
        dbRegistrationPanel.add(userLbl);

        JTextField userTxt = new JTextField();
        userTxt.setFont(new Font("Arial", Font.BOLD, 11));
        userTxt.setText("");
        userTxt.setBounds(36, 150, 282, 38);
        dbRegistrationPanel.add(userTxt);

        JLabel pwdLbl = new JLabel("Inserisci password database");
        pwdLbl.setBounds(36,193,282,15);
        pwdLbl.setFont(new Font("Arial", Font.PLAIN, 13));
        dbRegistrationPanel.add(pwdLbl);

        JPasswordField pwdTxt = new JPasswordField();
        pwdTxt.setFont(new Font("Arial", Font.BOLD, 11));
        pwdTxt.setBounds(36, 208, 282, 38);
        dbRegistrationPanel.add(pwdTxt);

        JButton dbReg = new JButton("REGISTRA DATABASE");
        dbReg.addActionListener(new ActionListener() {
            @Override
            synchronized public void actionPerformed(ActionEvent e) {
                SubDbRdF instance = (SubDbRdF) getMyIstance();
                if(instance.getServer().getDatabase().openConnection(urlTxt.getText(),
                        userTxt.getText(),String.valueOf(pwdTxt.getPassword()))){
                    JOptionPane.showMessageDialog(null,"Connessione al database avvenuta" +
                            " con successo, se " + (char)232 +
                            " registrato almeno un admin l'applicazione si avvier" + (char)224);
                    if (instance.getServer().getDatabase().oneAdmin()) {
                        subbed = true;
                        instance.getServer().notifyMe();
                    }
                    else{
                        JOptionPane.showMessageDialog(null,"Nessun admin " + (char)232 + " presente nel database" +
                                ". Dovrai iscriverti per inizializzare il server");
                        cards.show(cardsContainer,"LOGIN");
                    }
                }
                else
                    JOptionPane.showMessageDialog(null,"Connessione al database non riuscita" +
                            " controlla i parametri inseriti");
            }
        });

        dbReg.setFont(new Font("Arial", Font.BOLD, 13));
        dbReg.setBounds(36, 435, 282, 38);
        dbRegistrationPanel.add(dbReg);
    }

    protected void designSub(){
            regAdv = new JLabel("");
            regAdv.setForeground(Color.ORANGE);
            regAdv.setIcon(null);
            regAdv.setToolTipText("");
            Color c = new Color(0, 0, 0, 0);
            regAdv.setBackground(c);
            regAdv.setHorizontalAlignment(SwingConstants.CENTER);
            regAdv.setFont(new Font("Showcard Gothic", Font.PLAIN, 20));
            regAdv.setBounds(36, 26, 307, 43);
            regAdv.setOpaque(true);
            subPanel.add(regAdv);

            nameLbl = new JLabel("Inserisci il tuo nome");
            nameLbl.setBounds(36,77,282,15);
            nameLbl.setFont(new Font("Arial", Font.PLAIN, 13));
            subPanel.add(nameLbl);

            nameTxt = new JTextField();
            nameTxt.setFont(new Font("Arial", Font.BOLD, 11));
            nameTxt.setBounds(36, 92, 282, 38);
            subPanel.add(nameTxt);
            nameTxt.setColumns(10);

            surnameLbl = new JLabel("Inserisci il tuo cognome");
            surnameLbl.setBounds(36,137,282,15);
            surnameLbl.setFont(new Font("Arial", Font.PLAIN, 13));
            subPanel.add(surnameLbl);

            surnameTxt = new JTextField();
            surnameTxt.setFont(new Font("Arial", Font.BOLD, 11));
            surnameTxt.setText("");
            surnameTxt.setBounds(36, 152, 282, 38);
            subPanel.add(surnameTxt);
            surnameTxt.setColumns(10);

            nickNameLbl = new JLabel("Inserisci un nickname");
            nickNameLbl.setBounds(36,197,282,15);
            nickNameLbl.setFont(new Font("Arial", Font.PLAIN, 13));
            subPanel.add(nickNameLbl);

            nicknameTxt = new JTextField();
            nicknameTxt.setFont(new Font("Arial", Font.BOLD, 11));
            nicknameTxt.setBounds(36, 212, 282, 38);
            subPanel.add(nicknameTxt);
            nicknameTxt.setColumns(10);

            emailLbl = new JLabel("Inserisci la tua email");
            emailLbl.setBounds(36,257,282,15);
            emailLbl.setFont(new Font("Arial", Font.PLAIN, 13));
            subPanel.add(emailLbl);

            emailTxt = new JTextField();
            emailTxt.setFont(new Font("Arial", Font.BOLD, 11));
            emailTxt.setBounds(36, 272, 282, 38);
            subPanel.add(emailTxt);
            emailTxt.setColumns(10);

            pwdLbl = new JLabel("Inserisci password");
            pwdLbl.setBounds(36,317,282,15);
            pwdLbl.setFont(new Font("Arial", Font.PLAIN, 13));
            subPanel.add(pwdLbl);

            passwTxt = new JPasswordField();
            passwTxt.setFont(new Font("Arial", Font.PLAIN, 13));
            passwTxt.setText("");
            passwTxt.setBounds(36, 332, 282, 38);
            subPanel.add(passwTxt);

            JButton adminReg = new JButton("REGISTRAMI");
            adminReg.addActionListener(new ActionListener() {
                @Override
                synchronized public void actionPerformed(ActionEvent e) {
                    if(getMyIstance() instanceof SubAdmin || getMyIstance() instanceof SubPlayer) {
                        System.out.println(serverComunicator.checkEmailUsername(emailTxt.getText(), nicknameTxt.getText()));
                        if (serverComunicator.checkEmailUsername(emailTxt.getText(), nicknameTxt.getText()) == false) {
                            emailCode = serverComunicator.getEmailCode();
                            if (!ServerRdFCreator.getSendMailServer().requestRegistration(nameTxt.getText(),
                                    emailTxt.getText(), emailCode))
                                JOptionPane.showMessageDialog(null, "Non "  + (char) 232 +" stato possibile inviare il" +
                                        " codice al tuo indirizzo email, controlla che sia corretto");
                            else {
                                JOptionPane.showMessageDialog(null, "Ti " + (char)232 + " stato mandato un" +
                                        " codice all'indirizzo email segnato.");
                                cards.show(cardsContainer, "CODE");
                            }
                        } else
                            JOptionPane.showMessageDialog(null, "Errore username o password gi" + (char)224 + " esistenti");
                    }
                    else{
                        SubDbRdF instance = (SubDbRdF) getMyIstance();
                        emailCode = instance.getServer().getEmailCode();
                        if (!instance.getServer().getSendMailServer().requestRegistration(nameTxt.getText(),
                                emailTxt.getText(), emailCode))
                            JOptionPane.showMessageDialog(null, "Non " + (char) 232 + " stato possibile inviare il" +
                                    " codice al tuo indirizzo email, controlla che sia corretto");
                        else {
                            JOptionPane.showMessageDialog(null, "Ti " + (char) 232 + " stato mandato un" +
                                    " codice all'indirizzo email segnato.");
                            cards.show(cardsContainer, "CODE");
                        }
                    }
                }
            });

            adminReg.setFont(new Font("Arial", Font.BOLD, 13));
            adminReg.setBounds(36, 402, 282, 38);
            subPanel.add(adminReg);
    }

    protected void designEnterCode(){
        JLabel codeLbl = new JLabel("INSERISCI CODICE DI CONFERMA");
        codeLbl.setFont(new Font("Showcard Gothic", Font.PLAIN, 18));
        codeLbl.setForeground(Color.ORANGE);
        codeLbl.setBounds(5, 35, 348, 58);
        codeConfirmPanel.add(codeLbl);

        JTextField codeTxt = new JTextField();
        codeTxt.setFont(new Font("Arial", Font.PLAIN, 16));
        codeTxt.setBounds(5, 106, 160, 52);
        codeConfirmPanel.add(codeTxt);
        codeTxt.setColumns(10);

        JButton confirmBtn = new JButton("OK");
        confirmBtn.setFont(new Font("Arial", Font.PLAIN, 15));
        confirmBtn.setBounds(170, 106, 70, 52);
        codeConfirmPanel.add(confirmBtn);
        confirmBtn.addActionListener(e -> {
            int code = 0;
            try{
                code = Integer.parseInt(codeTxt.getText());
            }
            catch (NumberFormatException ne){
                JOptionPane.showMessageDialog(null,"Il codice puo essere solo numerico");
            }
            if(emailCode==code) {
                if (getMyIstance() instanceof SubAdmin) {
                    String pwd = new String(passwTxt.getPassword());
                    pwd = Encryption.MD5(pwd);
                    subbed = serverComunicator.sub(nicknameTxt.getText(), emailTxt.getText(), nameTxt.getText(),
                            surnameTxt.getText(), pwd,"ADMIN");
                    if (subbed) {
                        SubAdmin istance = (SubAdmin) getMyIstance();
                        istance.getAdminMenu().setSigned(subbed);
                        istance.getAdminMenu().setPlayerData(nicknameTxt.getText(), emailTxt.getText(),
                                nameTxt.getText(), surnameTxt.getText(), new String(passwTxt.getPassword()));
                        dispose();
                        JOptionPane.showMessageDialog(null, "Registrazione avvenuta con successo");
                    } else
                        JOptionPane.showMessageDialog(null, "Registrazione errata. Prova cambiando nickname");
                }
                if (getMyIstance() instanceof SubPlayer) {
                    String pwd = new String(passwTxt.getPassword());
                    pwd = Encryption.MD5(pwd);
                    subbed = serverComunicator.sub(nicknameTxt.getText(), emailTxt.getText(), nameTxt.getText(),
                            surnameTxt.getText(), pwd,"PLAYER");
                    if (subbed) {
                        SubPlayer instance = (SubPlayer) getMyIstance();
                        instance.getPlayerMenu().setSigned(subbed);
                        instance.getPlayerMenu().setPlayerData(nicknameTxt.getText(), emailTxt.getText(),
                                nameTxt.getText(), surnameTxt.getText(), new String(passwTxt.getPassword()));
                        dispose();
                        JOptionPane.showMessageDialog(null, "Registrazione avvenuta con successo");
                    } else
                        JOptionPane.showMessageDialog(null, "Registrazione errata. Prova cambiando nickname");
                }
                if (getMyIstance() instanceof SubDbRdF) {
                    //Faccio query:
                    SubDbRdF instance = (SubDbRdF) getMyIstance();
                    
                    String pwd = new String(passwTxt.getPassword());
                    pwd = Encryption.MD5(pwd);
                    
                    subbed = instance.getServer().getDatabase().registrazione(nicknameTxt.getText(),emailTxt.getText(),
                            nameTxt.getText(),surnameTxt.getText(),pwd,"ADMIN");
                    if (subbed) {
                        JOptionPane.showMessageDialog(null, "Registrazione avvenuta con successo" );
                        instance.getServer().notifyMe();
                    } else
                        JOptionPane.showMessageDialog(null, "Registrazione fallita, controlla nickname e email");
                }
            }
            else {
                JOptionPane.showMessageDialog(null,"Codice errato, riprova");
            }
        });

        JButton backBtn = new JButton("BACK TO LOGIN");
        backBtn.setFont(new Font("Arial", Font.PLAIN, 12));
        backBtn.setBounds(5, 415, 150, 50);
        backBtn.addActionListener(e -> {
            cards.show(cardsContainer,"LOGIN");
        });
        codeConfirmPanel.add(backBtn);
    }

    protected abstract boolean isSubbed();
    protected abstract boolean isClosed();
    protected abstract void outTimeClose();

    public void notifyServer() {
        if(getMyIstance() instanceof SubDbRdF){
            SubDbRdF instance = (SubDbRdF) getMyIstance();
            instance.getServer().notifyMe();
        }

    }
}
