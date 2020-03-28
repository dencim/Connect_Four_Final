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
            outP1.writeChar('1');
            outP1.flush();

            inputFromP2 = new ObjectInputStream(p2Socket.getInputStream());
            outP2 = new ObjectOutputStream(p2Socket.getOutputStream());
            outP2.writeChar('2');
            outP2.flush();

        }catch (IOException e){
            System.out.println("Error");
        }


        while(true){

            try {

                boardState = (Double[][])inputFromP1.readObject();
                outP2.writeObject(boardState);
                outP2.flush();
                if(!checkVictory()){
                    boardState = (Double[][])inputFromP2.readObject();
                    outP1.writeObject(boardState);
                    outP1.flush();
                }
                else{
                    System.out.println("P1 won");
                }



            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }




    }

    public boolean checkVictory(){
        return true;
    }
}