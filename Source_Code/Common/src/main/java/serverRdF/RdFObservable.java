package serverRdF;

import game.Match;
import game.RdFObserver;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface RdFObservable extends Remote{
    void addObserver(boolean isPlayer, RdFObserver observer) throws RemoteException;
    void deleteObserver(boolean isPlayer, RdFObserver observer, boolean gameStarted) throws RemoteException;
    String getModifier() throws RemoteException;
    void spinWheel(String modifier)throws RemoteException;
    void callLetter(String letter, int bonus) throws RemoteException;
    void callPhrase(String phrase) throws RemoteException;
    void outTimeCall() throws RemoteException;
    void useJolly() throws RemoteException;
    int getMyBonus(String nickname) throws RemoteException;
    Match getMatchData() throws RemoteException;
}