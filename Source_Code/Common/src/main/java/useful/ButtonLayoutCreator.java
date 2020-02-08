package useful;

import javax.swing.*;

public class ButtonLayoutCreator extends JPanel {
    private static final int sizeX = 100;
    private static final int sizeY = 100;
    private final int buttonSize = 20;
    private String[] modifersName;
    private JButton[] modifiersButton;

    public ButtonLayoutCreator(){
        panelLayout();
    }

    private void panelLayout() {
        setLayout(null);
        setBounds(0,0,sizeX, sizeY);
    }

    private void initButtons(){
        modifersName = new String[]{"600", "400","500","ecc"};
        modifiersButton = new JButton[4];
        for (int i =0; i<4;i++){
            modifiersButton[i].setText(modifersName[i]);
            modifiersButton[i].setSize(buttonSize,buttonSize);
        }
    }
}
