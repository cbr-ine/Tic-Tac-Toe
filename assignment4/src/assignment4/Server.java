package assignment4;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Tic Tac Toe game server
 * @author Chen Borun
 * @version 1.0
 * @since 2023/12/03
 */

public class Server {
    ServerSocket serverSock;
    ArrayList<PrintWriter> writers = new ArrayList<PrintWriter>();
    int i = 1;
    int Player_Prepared = 0;
    public static void main(String[] args) {
        Server server = new Server();
        server.go();
    }
    
    class ClientHandler implements Runnable{
        private Socket sock;
        public ClientHandler(Socket sock) {
            this.sock = sock;
        }
        @Override
        public void run() {
            try {
                PrintWriter writer = new PrintWriter(sock.getOutputStream(), true);
                if (writers.size() == 0){
                    writer.println("PlayerX");
                }
                else if(writers.size() == 1){
                    writer.println("PlayerO");
                }
                else{
                    writer.println("Invalid");
                }
                writers.add(writer);
                InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
                BufferedReader reader = new BufferedReader(streamReader);
                String command;
                while ((command = reader.readLine()) != null) {
                    System.out.println("Command from client: " + command);
                    if (command.startsWith("PlayerNameSet")){
                        Player_Prepared++;
                        if (Player_Prepared == 2){
                            for (PrintWriter eachWriter: writers) {
                                eachWriter.println("XTurn");
                            }
                        }
                    }
                    if (command.startsWith("DONE")){
                        String[] ResultStr = command.split(",");
                        if (ResultStr[1].equals("X")){
                            for (PrintWriter eachWriter: writers) {
                                eachWriter.println("RX," + ResultStr[2]);
                            }
                        }
                        else{
                            for (PrintWriter eachWriter: writers) {
                                eachWriter.println("RO," + ResultStr[2]);
                            }
                        }
                        if (i % 2 == 0){
                            for (PrintWriter eachWriter: writers) {
                                eachWriter.println("XTurn");
                            }
                        }
                        else{
                            for (PrintWriter eachWriter: writers) {
                                eachWriter.println("OTurn");
                            }
                        }
                        i++;
                    }
                }
            } 
            catch (Exception ex) {
                ex.printStackTrace();
            } 
            finally {
                for (PrintWriter eachWriter: writers) {
                    eachWriter.println("LEFT");
                }
                System.out.println("One User Left...");
            }
        }

    }
    
    public void go() {
        try {
            serverSock = new ServerSocket(12345);
            System.out.println("Server is running...");
            while (true) {
                Socket sock = serverSock.accept();
                System.out.println("Client" + writers.size()+1 +" is connected to server");
                ClientHandler clientHandler = new ClientHandler(sock);
                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        } 
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}