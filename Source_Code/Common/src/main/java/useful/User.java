package useful;

import java.io.Serializable;

/*
 *Classe che modella l'utente
 */

public abstract class User implements Serializable {
    private static final long serialVersionUID=1L;
    //GETTER
    public abstract String getName();
    public abstract String getSurname();
    public abstract String getNickname();
    public  abstract String getPassword();
    public abstract String getEmail();
    //SETTER
    public abstract void setName(String name);
    public abstract void setSurname(String surname);
    public abstract void setNickname(String nick);
    public  abstract void setPassword(String pwd);
    public abstract void setEmail(String email);
    //SETALL
    public  abstract void setAll(String name, String surname, String nick, String pwd, String email);


}
