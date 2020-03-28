package sample;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{


        new Thread(() ->{
            try {
                ServerSocket server = new ServerSocket(8888);
                //server.setReuseAddress(true);

                // The main thread is just accepting new connections
                while (true) {

                    Socket player1 = server.accept();
                    System.out.println("Player1 connected " + player1.getInetAddress().getHostAddress());
                    Socket player2 = server.accept();
                    System.out.println("Player2 connected " + player1.getInetAddress().getHostAddress());
                    ClientHandler clientSocket = new ClientHandler(player1, player2);

                    // The background thread will handle each client separately
                    new Thread(clientSocket).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();



    }

    public static void main(String[] args) {
        launch(args);
    }

}
