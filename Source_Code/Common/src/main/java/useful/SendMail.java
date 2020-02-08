package useful;

import serverRdF.ServerRdFCreator;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Properties;

/**
 *Classe utilizzata per inviare email
 * @author Andrea Nonali
 */

public class SendMail {
    private Properties properties;
    private static final int randomPasswordLength = 10;
    private static final String ALPHA_NUMERIC_STRING = "abcdefghijklmnopqrstuvwxyz" +
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final String username = "infofortunewheel@gmail.com";
    private final String password = "";
    private final String HOST = "smtp.gmail.com";
    private final String PORT = "587";

    /**
     *Constructor set new properties
     *
     */
    public SendMail(){
        //Setting properties
        properties = new Properties();
        properties.put("mail.smtp.host",HOST);
        properties.put("mail.smtp.port",PORT);
        properties.put("mail.smtp.auth","true");
        properties.put("mail.smtp.starttls.enable","true");
    }

    /**
     *Method to complete registration: sends code to the user
     *
     */
    public boolean requestRegistration(String userName, String email,int code){
        String messageText = "Ciao "+ userName +
                ".\nBenvenuto nella piattaforma RdF.\n" +
                "Per confermare la sua iscrizione " +
                "digiti sull'interfaccia il seguente codice: " + code;
        return sendMail(messageText,"RdF registrazione",email);
    }


    /**
     *Method to change password
     *
     */
    public boolean requestResetPassword(String name,String email, String newPassword){
        String messageText = "Ciao "  +" name!"+
                ".\nLa tua richiesta di cambio dati è stata presa in carico.\n" +
                "La tua nuova password è: "+ newPassword;
        return sendMail(messageText, "RdF change password",email);
    }

    /**
     *Method used to advise admin to update phrases database
     * Takes an arraylist with the odd values corresponding to email of admin
     * and the even corresponding to the name of the admin
     */
    public boolean adviseAdminUpdatePhraseDb(ArrayList<String> nameEmail){
        boolean messageoK = false;
        String messageText;
        for(int i =0; i < nameEmail.size(); i+=2){
            messageText = "Ciao "+ nameEmail.get(i) +
                    ".\nÈ stata terminata una partita per mancanza di frasi" +
                    " nel catalogo ancora non viste dai giocatori.\n" +
                    "In qualità di amministratore del gioco contiamo sul tuo aiuto per aggiornare il " +
                    "prima possibile il database delle frasi.\n" +
                    "Per farlo ti basterà accedere mediante modulo adminRdF, registarti e cliccare sul" +
                    " bottone phrases.\n" +
                    "Grazie in anticipo, lo staff di RdF";
            messageoK = sendMail(messageText,"Aggiornamento catologo frasi",nameEmail.get(i+1));
        }
        return messageoK;
    }

    /**
     *This method when given object text and adress send an email
     *
     */
    private boolean sendMail(String text,String object,String email) {
        boolean messageOk = true;
        //Starting session with authentication
        Session session = Session.getInstance(
                properties,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        //Preparing message
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(email)
            );message.setSubject(object);
            message.setText(text);
            //Sending message
            Transport.send(message);
            System.out.println("Message sent");
        } catch (MessagingException e) {
            messageOk = false;
            System.err.println("Message server mail not found ");
        }
        return messageOk;
    }
    /**
     *This method create a random password
     *
     */
    public String generateRandomPassword(){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i<randomPasswordLength;i++) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }
}
