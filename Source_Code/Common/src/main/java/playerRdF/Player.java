package playerRdF;

import useful.User;

public class Player extends User {
    protected String name;
    protected String surname;
    protected String nickname;
    protected String password;
    protected String email;

    public  Player(){}
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
