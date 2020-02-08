package useful;

import adminRdF.LogAdmin;
import playerRdF.LogPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe per gestire login
 */

public abstract class Login extends JFrame {
    protected Login myIstance;
    protected boolean accessed; //traccia l'accesso o meno di un utente
    protected boolean closed;
    protected JLabel emailLbl;
    protected JLabel pwdLbl;
    protected JTextField emailTxt;
    protected JPasswordField passwTxt;
    protected JButton loginBtn;
    protected serverRdF.Proxy serverConnection;

    /**
     * Metodo che chiude la finestra dopo 10 secondi
     */
    protected abstract void outTimeClose();

    private Login getMyIstance(){
        return myIstance;
    }

    /**
     * Metodo per il desgin delle interfacce di login. Ha un design diverso in base al chiamate (Player, admin, database)
     */
    protected void designLogin(){
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e1) {
            e1.printStackTrace();
        }
        getContentPane().setBackground(Color.WHITE);
        getContentPane().setLayout(null);
        setBounds(200,200,381,518);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                closed = true;
                dispose();
            }
        });
        JLabel regAdv = new JLabel("LOGIN");
        regAdv.setForeground(Color.ORANGE);
        regAdv.setBackground(Color.white);
        regAdv.setIcon(null);
        regAdv.setToolTipText("");
        regAdv.setHorizontalAlignment(SwingConstants.CENTER);
        regAdv.setFont(new Font("Showcard Gothic", Font.PLAIN, 20));
        regAdv.setBounds(36, 26, 307, 43);
        regAdv.setOpaque( true );
        this.getContentPane().add(regAdv);

        emailLbl = new JLabel("Inserisci la tua email");
        emailLbl.setBounds(36,257,282,15);
        emailLbl.setFont(new Font("Arial", Font.PLAIN, 13));
        getContentPane().add(emailLbl);

        emailTxt = new JTextField();
        emailTxt.setFont(new Font("Arial", Font.PLAIN, 13));
        emailTxt.setBounds(36, 272, 282, 38);
        getContentPane().add(emailTxt);
        emailTxt.setColumns(10);

        pwdLbl = new JLabel("Inserisci password");
        pwdLbl.setBounds(36,317,282,15);
        pwdLbl.setFont(new Font("Arial", Font.PLAIN, 13));
        getContentPane().add(pwdLbl);

        passwTxt = new JPasswordField();
        passwTxt.setFont(new Font("Arial", Font.PLAIN, 13));
        passwTxt.setBounds(36, 332, 282, 38);
        getContentPane().add(passwTxt);
        passwTxt.setColumns(10);

        loginBtn = new JButton("ESEGUI LOGIN");
        loginBtn.addActionListener(e -> {
            if(getMyIstance() instanceof LogPlayer){
                List<String> listaDati = new ArrayList<>();
                try {
                
                    String pwd = new String(passwTxt.getPassword());
                    pwd = Encryption.MD5(pwd);
                    
                    listaDati= serverConnection.log_piu_dati(emailTxt.getText(),
                            pwd,"PLAYER");
                    accessed = listaDati.size()>1;
                } catch (IOException | ClassNotFoundException exc){
                    System.err.println("Errore di comunicazione con server");
                }
                if(accessed) {
                    JOptionPane.showMessageDialog(null,"Login avvenuto con successo");
                    //i dati sono disordinati perchè il database ha un ordine diverso
                    LogPlayer istance = (LogPlayer) getMyIstance();
                    istance.getPlayerMenu().setSigned(accessed);
                    System.out.println(listaDati.get(0));
                    istance.getPlayerMenu().setPlayerData(listaDati.get(2),listaDati.get(3),listaDati.get(0),listaDati.get(4),listaDati.get(1));
                    dispose();
                }
                else
                    JOptionPane.showMessageDialog(null,"Login fallito,controlla email e password");
            }
            else if(getMyIstance() instanceof LogAdmin){
                List<String> listaDati = new ArrayList<>();
                try {
                
                    String pwd = new String(passwTxt.getPassword());
                    pwd = Encryption.MD5(pwd);
                    
                    listaDati= serverConnection.log_piu_dati(emailTxt.getText(),
                            pwd,"ADMIN");
                    accessed = listaDati.size()>1;
                } catch (IOException | ClassNotFoundException exc){
                    System.err.println("Errore di comunicazione con server");
                }
                if(accessed) {
                    JOptionPane.showMessageDialog(null,"Login avvenuto con successo");
                    //i dati sono disordinati perchè il database ha un ordine diverso
                    LogAdmin istance = (LogAdmin) getMyIstance();
                    istance.getAdminMenu().setSigned(accessed);
                    istance.getAdminMenu().setPlayerData(listaDati.get(2),listaDati.get(3),
                            listaDati.get(0),listaDati.get(4),listaDati.get(1));
                    dispose();
                }
                else
                    JOptionPane.showMessageDialog(null,"Login fallito, controlla email e password");
            }
        });

        loginBtn.setFont(new Font("Arial", Font.BOLD, 13));
        loginBtn.setBounds(36, 402, 282, 37);
        getContentPane().add(loginBtn);

        JButton resetPwdButton = new JButton("hai dimenticato la password? clicca qui per reset!");
        resetPwdButton.setFont(new Font("Arial", Font.ITALIC, 9));
        resetPwdButton.setBounds(36,150,282,30);
        resetPwdButton.addActionListener(e -> {
            String mail = JOptionPane.showInputDialog(null,"Inserisci la tua email");
            if(mail != null) {
                if (getMyIstance() instanceof LogPlayer) {
                    if(serverConnection.resetPassword(mail))
                        JOptionPane.showMessageDialog(null,"password resettata");
                    else
                        JOptionPane.showMessageDialog(null,"Non " + (char) 232 +" stato possi" +
                                "bile resettare la tua password, controlla che l'email inserita" +
                                " corrisponda a quella dell' account che hai creato");
                } else if (getMyIstance() instanceof LogAdmin) {
                    if(serverConnection.resetPassword(mail))
                        JOptionPane.showMessageDialog(null,"password resettata");
                    else
                        JOptionPane.showMessageDialog(null,"Non " + (char) 232 +"stato possi" +
                                "bile resettare la tua password, controlla che l'email inserita" +
                                " corrisponda a quella dell' account che hai creato");
                }
            }
        });
        getContentPane().add(resetPwdButton);
    }
}
