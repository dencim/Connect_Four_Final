package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class Main extends Application {

    //IO streams
    Socket socket;
    public ObjectOutputStream toServer;
    public ObjectInputStream fromServer;

    Integer[][] score = new Integer[6][7];
    String player = "Player ";
    int playerNum;

    GridPane board;
    Label myName;
    Label turnLabel;

    volatile boolean myTurn;
    volatile boolean gameOver = false;
    Button save = new Button("Save");

    @Override
    public void start(Stage primaryStage) throws Exception{
        BorderPane pane = new BorderPane();
        HBox top = new HBox(50);
        top.setPadding(new Insets(15,15,15,15));
        myName = new Label("Waiting for other Player");
        turnLabel = new Label("...");
        top.getChildren().addAll(myName, turnLabel);
        pane.setTop(top);

        pane.setBottom(save);
        board = new GridPane();

        save.setMaxWidth(250);
        save.setPadding(new Insets(10, 10, 10, 10));
        save.setStyle("-fx-text-fill: #000fff");


        Button temp;
        for(int i=0;i<6;i++){

            for(int j=0;j<7;j++){

                temp = new Button(null,new ImageView(new Image(new FileInputStream("src/sample/empty.png"))));
                temp.setPadding(new Insets(0,0,0,0));
                temp.setStyle("-fx-border-color: #0000A0");
                temp.setId((i+"")+(j+"")); //id will be row then column

                temp.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        buttonClicked(actionEvent);
                    }
                });

                temp.setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        buttonHover(mouseEvent);
                    }
                });

                temp.setOnMouseExited(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        buttonHoverLeave(mouseEvent);
                    }
                });

                board.add(temp, j, i);
                score[i][j] = 0;

            }

        }



        //System.out.println(System.getProperty("user.dir"));
        pane.setCenter(board);
        primaryStage.setTitle("Connect Four");
        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.show();


        //Setup Done

        //Server Thread
        new Thread(()->{

            try {
                //Create a socket to connect to the server
                socket = new Socket("127.0.0.1" , 8888);

                //Create an output stream to send data to the server
                toServer = new ObjectOutputStream(socket.getOutputStream());
                //Create an input stream to receive data to the server
                fromServer = new ObjectInputStream(socket.getInputStream());

                playerNum = fromServer.readChar() - 48;
                System.out.println("Player: " + playerNum);

                if(playerNum==1){
                    myTurn=true;
                }
                else{
                    myTurn=false;
                }

                Platform.runLater(() -> myName.setText("I am Player " + playerNum));
            }
            catch (IOException ex){
                System.out.println(ex);
            }

            while(!gameOver){

                System.out.println(myTurn);

                synchronized (this) {
                    while(myTurn){
                        try{
                            this.wait();
                        }catch (InterruptedException e){
                            //error
                        }
                    }
                }


                    try {
                        System.out.println("Player 2 right away");
                        score = (Integer[][])fromServer.readObject();
                        System.out.println("Player 2 should have");
                        System.out.print(score);
                        if(score[0][0]==5){
                            //p1 won
                            System.out.println("Player 1 won");
                            Platform.runLater(() -> {
                                setOtherColor();
                                gameOver = true;
                            });
                        }
                        else if(score[0][0]==6){
                            //p2 won
                            System.out.println("Player 2 won");
                            Platform.runLater(() -> {
                                setOtherColor();
                                gameOver = true;
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    myTurn = true;

                    Platform.runLater(() -> {
                        setOtherColor();

                    });





            }

        }).start();

        System.out.println("Game OVER!");

    }
    //Show green or red outline depending on if allowed to place in slot
    private void buttonHover(MouseEvent mouseEvent){
        String event = mouseEvent.getSource().toString();
        int x = event.charAt(10) - 48;
        int y = event.charAt(11) - 48;

        if(allowedSquare(x,y)){
            try {
                Button temp = (Button)(mouseEvent.getSource());
                temp.setGraphic(new ImageView(new Image(new FileInputStream("src/sample/green.png"))));
                //board.add(new ImageView(new Image(new FileInputStream("src/sample/yellow.png"))),y,x); //image not button - change
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else{
            try {
                Button temp = (Button)(mouseEvent.getSource());
                temp.setGraphic(new ImageView(new Image(new FileInputStream("src/sample/red.png"))));
                //board.add(new ImageView(new Image(new FileInputStream("src/sample/red.png"))),y,x);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    //Set button back to empty slot image
    private void buttonHoverLeave(MouseEvent mouseEvent){

        try {
            Button temp = (Button)(mouseEvent.getSource());
            temp.setGraphic(new ImageView(new Image(new FileInputStream("src/sample/empty.png"))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    //Set move if is allowed
    private void buttonClicked(ActionEvent actionEvent){
        String event = actionEvent.getSource().toString();
        int x = event.charAt(10) - 48;
        int y = event.charAt(11) - 48;

        if(allowedSquare(x, y)){

            score[x][y] = playerNum;
            try {
                if(playerNum==1){
                    board.add(new ImageView(new Image(new FileInputStream("src/sample/yellowFilled.png"))),y,x);
                }
                else{
                    board.add(new ImageView(new Image(new FileInputStream("src/sample/redFilled.png"))),y,x);
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //Writing move to server
            new Thread(()->{

                try {


                        //Sends board after move to server
                        toServer.writeObject(score);

                    synchronized (this) {
                        myTurn = false;
                        this.notifyAll();
                    }
                        //System.out.println("My turn: " + myTurn);

                        Platform.runLater(() -> {
                            setOtherColor();
                        });


                }
                catch (IOException e){
                    System.out.println(e);
                }


            }).start();

        }
        else{
            System.out.println("Not good move");
        }
    }

    //Sets opponents pieces from server info
    private void setOtherColor(){

        for(int i=0;i<6;i++){
            for(int j=0;j<7;j++){
                if(score[i][j] == 1 && playerNum == 2){
                    try {
                        board.add(new ImageView(new Image(new FileInputStream("src/sample/yellowFilled.png"))),j,i);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }else if(score[i][j] == 2 && playerNum == 1){
                    try {
                        board.add(new ImageView(new Image(new FileInputStream("src/sample/redFilled.png"))),j,i);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private boolean allowedSquare(int x, int y){

        System.out.println(gameOver);
        //makes sure the player isn't trying to click a taken slot
        if(score[x][y] != 1 && score[x][y] != 2 && myTurn==true && !gameOver){

            //for bottom row
            if(score[5][y] == 0 && x == 5){

                return true;
            } //checks to make sure slot below is taken
            else if(score[x+1][y] == 1 || score[x+1][y] == 2){

                return true;

            }

        }

        return false;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
