package assignment4;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Tic Tac Toe Client
 * @author Chen Borun
 * @version 1.0
 * @since 2023/12/03
 */

public class Client {
    boolean GameEndFlag;
    PrintWriter writer;
    Socket sock;
    JMenuBar MBar = new JMenuBar();
    JMenu Control_Menu = new JMenu("Control");
    JMenu Help_Menu = new JMenu("Help");
    JPanel Grid_Panel = new JPanel();
    JButton[] Buttons = new JButton[9];
    JPanel Name_Panel = new JPanel();
    JMenuItem Exit_MenuItem = new JMenuItem("Exit");
    JMenuItem Instruction_MenuItem = new JMenuItem("Instruction");
    JLabel Info_Label = new JLabel("Enter your player name...");
    JPanel Main_Panel = new JPanel();
    JFrame frame = new JFrame();
    JTextField Name_TextField = new JTextField(15);
    JButton Name_SubmitBtn = new JButton("Submit");
    String PlayerPos = "O";
    
    public static void main(String[] args) {
        Client gui = new Client();
        gui.go();
    }
    
    public void go(){
    	//Initialize panel
        Info_Label.setPreferredSize(new Dimension(300,15));
        Grid_Panel.setLayout(new GridLayout(3,3));
        for (int i = 0; i < 9; i++){
            Buttons[i] = new JButton();
            Buttons[i].setPreferredSize(new Dimension(100,100));
            Buttons[i].setBorder(BorderFactory.createLineBorder(Color.black));
            Buttons[i].setBackground(Color.white);
            Buttons[i].setFont(new Font("Consolas", Font.BOLD,50));
            Buttons[i].addActionListener(new BtnAction());
            Buttons[i].setEnabled(false);
            Grid_Panel.add(Buttons[i]);
            Buttons[i].setFocusable(false);
        }
        Name_Panel.add(Name_TextField);
        Name_SubmitBtn.addActionListener(new SubmitBtnAction());
        Name_Panel.add(Name_SubmitBtn);
        Main_Panel.add(Info_Label);
        Main_Panel.add(Grid_Panel);
        Main_Panel.add(Name_Panel);

        MBar.add(Control_Menu);
        MBar.add(Help_Menu);
        Exit_MenuItem.addActionListener(new Exit_menuAction());
        Control_Menu.add(Exit_MenuItem);
        Instruction_MenuItem.addActionListener(new Instruction_menuAction());
        Help_Menu.add(Instruction_MenuItem);

        frame.setJMenuBar(MBar);
        frame.getContentPane().add(Main_Panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle("Tic Tac Toe");
        frame.setSize(315, 425);
        frame.setResizable(false);
        frame.setVisible(true);

        //Socket
        try {
            sock = new Socket("127.0.0.1", 12345);
            writer = new PrintWriter(sock.getOutputStream(), true);
            InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
            BufferedReader reader = new BufferedReader(streamReader);
            String command;

            while ((command = reader.readLine()) != null) {
                System.out.println("Received Command: " + command);
                if (command.startsWith("PlayerX")) {
                    System.out.println("Get Player pos from server: " + command);
                    PlayerPos = "X";
                    System.out.println("Player pos set to " + command); 
                }
                else if(command.startsWith("PlayerO")) {
                    System.out.println("Get Player pos from server: " + command);
                    PlayerPos = "O";
                    System.out.println("Player pos set to " + command); 
                }
                else if(command.startsWith("Invalid")) {
                    System.out.println("Error!!!!"); 
                    DisableAllBtn();
                    Name_TextField.setEnabled(false);
                    Name_SubmitBtn.setEnabled(false);
                }

                if (command.startsWith("LEFT")){
                    JOptionPane.showMessageDialog(null, "Game Ends. One of the players left.", "Message", JOptionPane.INFORMATION_MESSAGE);
                    DisableAllBtn();
                    Name_TextField.setEnabled(false);
                    Name_SubmitBtn.setEnabled(false);
                }
                if (!GameEndFlag){
                    if ((command.equals("XTurn") && PlayerPos.equals("X")) || (command.equals("OTurn") && PlayerPos.equals("O"))){
                        EnableAllBtn();
                    }
                    else{
                    	DisableAllBtn();
                    }
                }
                if (command.startsWith("RX") || command.startsWith("RO")){
                    String[] ResString = command.split(",");
                    if (ResString[0].equals("RX") && PlayerPos.equals("X") || ResString[0].equals("RO") && PlayerPos.equals("O")){
                        Info_Label.setText("Valid move, wait for your opponent");
                    }
                    else {
                        Info_Label.setText("Your opponent has moved, now is your turn");
                    }
                    if (ResString[0].equals("RX")){
                        Buttons[Integer.parseInt(ResString[1])].setText("X");
                        Buttons[Integer.parseInt(ResString[1])].setForeground(Color.GREEN);
                    }
                    else{
                        Buttons[Integer.parseInt(ResString[1])].setText("O");
                        Buttons[Integer.parseInt(ResString[1])].setForeground(Color.RED);
                    }
                    State_Check();
                }

            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public void State_Check(){
    	//return type:
        //0: Not yet finish
        //1: PlayerX win
        //2: PlayerO win
        //3: Draw
        int state = Check_Win();
        if (state != 0){
        	int response = 0;
            if (state == 3){
            	int temp = JOptionPane.showConfirmDialog(null, "Draw. Do you want to play again?", "Message", JOptionPane.YES_NO_OPTION);
            	response = temp ;
            }
            else if (state == 1 && PlayerPos.equals("X") || state == 2 && PlayerPos.equals("O")){
            	int temp = JOptionPane.showConfirmDialog(null, "Congratulations. You Win. Do you want to play again?", "Message", JOptionPane.YES_NO_OPTION);
            	response = temp ;
            }
            else{
            	int temp = JOptionPane.showConfirmDialog(null, "You lose. Do you want to play again?", "Message", JOptionPane.YES_NO_OPTION);
            	response = temp ;
            }
            DisableAllBtn();
            if (response == JOptionPane.YES_OPTION) {
            	GameEndFlag = true;
            	resetGame();
            } else if (response == JOptionPane.NO_OPTION) {
                GameEndFlag = true;
                frame.dispose();
            }
        }
    }
    
    public void EnableAllBtn(){for (int i = 0; i < 9; i++){Buttons[i].setEnabled(true);}}

    public void DisableAllBtn(){for (int i = 0; i < 9; i++){Buttons[i].setEnabled(false);}}
    
    private void resetGame() {
        for (JButton button : Buttons) {
            button.setText("");
            button.setEnabled(false); 
        }
        GameEndFlag = false;
        Name_TextField.setEnabled(true);
        Name_TextField.setText("");
        Name_SubmitBtn.setEnabled(true);
        Info_Label.setText("Enter your player name...");
        frame.setTitle("Tic Tac Toe");
    }
    
    public int Check_Win(){
    	//return type:
        //0: Not finish
        //1: PlayerX win
        //2: PlayerO win
        //3: Draw
        //Check X (Player1)
        //Vertical
        if ((Buttons[0].getText().equals("X")) && (Buttons[3].getText().equals("X")) && (Buttons[6].getText().equals("X"))){
            return 1;
        }
        if ((Buttons[1].getText().equals("X")) && (Buttons[4].getText().equals("X")) && (Buttons[7].getText().equals("X"))){
            return 1;
        }
        if ((Buttons[2].getText().equals("X")) && (Buttons[5].getText().equals("X")) && (Buttons[8].getText().equals("X"))){
            return 1;
        }
    	//Horizontal
        if ((Buttons[0].getText().equals("X")) && (Buttons[1].getText().equals("X")) && (Buttons[2].getText().equals("X"))){
            return 1;
        }
        if ((Buttons[3].getText().equals("X")) && (Buttons[4].getText().equals("X")) && (Buttons[5].getText().equals("X"))){
            return 1;
        }
        if ((Buttons[6].getText().equals("X")) && (Buttons[7].getText().equals("X")) && (Buttons[8].getText().equals("X"))){
            return 1;
        }
        //Oblique
        if ((Buttons[0].getText().equals("X")) && (Buttons[4].getText().equals("X")) && (Buttons[8].getText().equals("X"))){
            return 1;
        }
        if ((Buttons[2].getText().equals("X")) && (Buttons[4].getText().equals("X")) && (Buttons[6].getText().equals("X"))){
            return 1;
        }
        
        //Check O (Player2)
        //Vertical
        if ((Buttons[0].getText().equals("O")) && (Buttons[3].getText().equals("O")) && (Buttons[6].getText().equals("O"))){
            return 2;
        }
        if ((Buttons[1].getText().equals("O")) && (Buttons[4].getText().equals("O")) && (Buttons[7].getText().equals("O"))){
            return 2;
        }
        if ((Buttons[2].getText().equals("O")) && (Buttons[5].getText().equals("O")) && (Buttons[8].getText().equals("O"))){
            return 2;
        }
        //Horizontal
        if ((Buttons[0].getText().equals("O")) && (Buttons[1].getText().equals("O")) && (Buttons[2].getText().equals("O"))){
            return 2;
        }
        if ((Buttons[3].getText().equals("O")) && (Buttons[4].getText().equals("O")) && (Buttons[5].getText().equals("O"))){
            return 2;
        }
        if ((Buttons[6].getText().equals("O")) && (Buttons[7].getText().equals("O")) && (Buttons[8].getText().equals("O"))){
            return 2;
        }
        //Oblique
        if ((Buttons[0].getText().equals("O")) && (Buttons[4].getText().equals("O")) && (Buttons[8].getText().equals("O"))){
            return 2;
        }
        if ((Buttons[2].getText().equals("O")) && (Buttons[4].getText().equals("O")) && (Buttons[6].getText().equals("O"))){
            return 2;
        }
        
        //Check Draw
        int temp = 0;
        for (int i = 0; i < 9; i++){
            if (!Buttons[i].getText().equals("")){
                temp = 3;
            }
            else{
                temp = 0;
                return temp;
            }
        }
        return temp;
    }
    
    class Exit_menuAction implements ActionListener{
        /**
         * Exit when this menu item is clicked
         * @param e the event to be processed
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            writer.println("LOST");
            System.exit(0);
        }
    }
    
    class Instruction_menuAction implements ActionListener{
        /**
         * Show a instruction message box when this menu item is clicked
         * @param e the event to be processed
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(null, "Some information about the game:\nCriteria for a valid move:\n-The move is not occupied by any mark.\n-The move is made in the player's turn.-The move is made within the 3 x 3 board.\nThe game would continue and switch among the opposite player until it reaches either one of the following conditions:\n-Player 1 wins.\n-Player2 wins.\n-Draw.", "Message", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    class BtnAction implements ActionListener{
        /**
         * @param e the event to be processed
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            for (int i = 0; i < 9; i++){
                if (e.getSource() == Buttons[i]){
                    if (Buttons[i].getText().equals("")){
                        Buttons[i].setText(PlayerPos);
                        DisableAllBtn();
                        Info_Label.setText("Valid move, wait for your opponent");
                        writer.println("DONE,"+PlayerPos + ","+ i); 
                        if (PlayerPos.equals("X")){
                            Buttons[i].setForeground(Color.GREEN);	
                        }
                        else if (PlayerPos.equals("O")){
                            Buttons[i].setForeground(Color.RED);	
                        }
                        else{
                            System.out.println(PlayerPos);
                        }
                    }
                }
                else {
                    Info_Label.setText("InValid move, Pick another one");
                }
            }
        }
    }
    
    class SubmitBtnAction implements ActionListener{

        /**
         * Name submit handler
         * @param e the event to be processed
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = Name_TextField.getText();
            Info_Label.setText("WELCOME " + name);
            frame.setTitle("Tic Tac Toe-Player: " + name);
            Name_TextField.setEnabled(false);
            Name_SubmitBtn.setEnabled(false);		
            writer.println("PlayerNameSet");
            
            // Enable the board after name submission
            for (JButton button : Buttons) {
                button.setEnabled(true);
            }
        }
    }
}