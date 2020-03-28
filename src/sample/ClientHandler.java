package sample;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket p1Socket;
    private final Socket p2Socket;

    Integer[][] boardState = new Integer[6][7];

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

                boardState = (Integer[][])inputFromP1.readObject();
                outP2.writeObject(boardState);
                outP2.flush();
                if(!checkVictory()){
                    boardState = (Integer[][])inputFromP2.readObject();
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
//returns true if someone won
    public boolean checkVictory(){

        int counter = 0;

        //check across victory
        for(int i=0;i<6;i++){

            counter = 0;

            for(int j=0;j<7;j++){

                if(boardState[i][j] == 1){
                    counter++;
                } else {
                    counter = 0; //reset b/c not in order
                }

                if(counter > 3){
                    return true;
                }

            }
        }

        counter = 0;

        //check down victory
        for(int i=0;i<7;i++){
            counter = 0;

            for(int j=0;j<6;j++){

                if(boardState[j][i] == 1) {
                    counter++;
                }else {
                    counter = 0;
                }
                if(counter > 3){
                    return true;
                }


            }
        }

        //Check diagonal up victory
        for(int i=0;i<4;i++){
            for(int j=3;j<6;j++){
                //i is 4 and j is 3 to avoid Out of Bounds error when searching through array
                if(boardState[j][i] == 1 && boardState[j-1][i+1] == 1 && boardState[j-2][i+2] == 1 && boardState[j-3][i+3] == 1){
                    return true;
                    //won by diagonal upward
                }

            }
        }

        //check diagonal down victory
        for(int i=0;i<4;i++){
            for(int j=0;j<3;j++){
                if(boardState[j][i] == 1 && boardState[j+1][i+1] == 1 && boardState[j+2][i+2] == 1 && boardState[j+3][i+3] == 1){
                    return true;
                    //won by diagonal downward
                }


            }
        }

        return false; //nobody won

    }
}
