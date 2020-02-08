package game;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RdFObserver extends Remote {
    void spinWheel(String modifier) throws RemoteException;
    void setWheelSpinned(boolean isWheelToBeSpinned) throws RemoteException;
    void setStartMatch(String[] phrases, String[] themes) throws RemoteException;
    void setTimer(boolean set) throws RemoteException;
    void setJollyPoppedOut(boolean set) throws RemoteException;
    void changePhrase() throws RemoteException;
    void setMyTurn(boolean isMyTurn) throws RemoteException;
    void showEquals(String letter) throws  RemoteException;
    void setConsoleText(String text) throws RemoteException;
    void setCurrentPlayerTextField(String text) throws RemoteException;
    PlayerInGame getPlayerInGame() throws RemoteException;
    void endGameLeaveMatch() throws RemoteException;
    boolean canCallPhraseOrVocal() throws RemoteException;
    void setCanCallPhraseOrVocal(boolean can) throws RemoteException;
    void notEnoughPhrases() throws RemoteException;
    void setPlayerLabels(String player1, String player2) throws RemoteException;
}
