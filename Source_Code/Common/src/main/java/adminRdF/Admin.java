package adminRdF;

import useful.User;

/**
 *This class defines an admin
 *
 */

public class Admin extends User {
    private String name;
    private String surname;
    private String nickname;
    private String password;
    private String email;

    public  Admin(){}
    //GETTER
    @Override
    public String getName() {
        return name;
    }
    @Override
    public String getSurname() {
        return surname;
    }
    @Override
    public String getNickname() {
        return nickname;
    }
    @Override
    public String getPassword() {
        return password;
    }
    @Override
    public String getEmail() {
        return email;
    }
    //SETTER
    @Override
    public void setName(String name) {
        this.name = name;
    }
    @Override
    public void setSurname(String surname) {
        this.surname = surname;
    }
    @Override
    public void setNickname(String nick) {
        this.nickname = nick;
    }
    @Override
    public void setPassword(String pwd) {
        this.password = pwd;
    }
    @Override
    public void setEmail(String email) {
        this.email = email;
    }
    //SETALL
    @Override
    public void setAll(String name, String surname, String nick, String pwd, String email) {
        setName(name);
        setSurname(surname);
        setNickname(nick);
        setPassword(pwd);
        setEmail(email);
    }
}
