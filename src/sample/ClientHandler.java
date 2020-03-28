package sample;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket p1Socket;
    private final Socket p2Socket;

    Double[][] boardState = new Double[6][7];

    public ClientHandler(Socket p1, Socket p2) {
        this.p1Socket = p1;
        this.p2Socket = p2;
    }

    @Override
    public void run() {

        ObjectInputStream inputFromP1 = null;
        ObjectOutputStream outP1 = null;


        ObjectInputStream inputFromP2 = null;
        ObjectOutputStream outP2 = null;

        try{
            inputFromP1 = new ObjectInputStream(p1Socket.getInputStream());
            outP1 = new ObjectOutputStream(p1Socket.getOutputStream());

            inputFromP2 = new ObjectInputStream(p2Socket.getInputStream());
            outP2 = new ObjectOutputStream(p2Socket.getOutputStream());
        }catch (IOException e){
            System.out.println("Error");
        }

/*
        while(true){

            try {



            } catch (IOException e) {
                e.printStackTrace();
            }

        }
*/



    }
}