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
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

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
    Label winnerLabel;

    //volatile boolean myTurn;
    AtomicBoolean myTurn = new AtomicBoolean(false);
    AtomicBoolean gameOver = new AtomicBoolean(false);

    Button save = new Button("Save");
    HBox top;

    @Override
    public void start(Stage primaryStage) throws Exception{
        BorderPane pane = new BorderPane();
        top = new HBox(50);
        top.setPadding(new Insets(15,15,15,15));
        myName = new Label("Waiting for other Player");
        winnerLabel = new Label("...");
        top.getChildren().addAll(myName, winnerLabel);
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

        //Server communication Thread
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
                    myTurn.set(true);
                    top.setBackground(new Background(new BackgroundFill(Color.rgb(0, 165, 205), CornerRadii.EMPTY, Insets.EMPTY)));
                }
                else{
                    myTurn.set(false);
                }

                Platform.runLater(() -> myName.setText("I am Player " + playerNum));
            }
            catch (IOException ex){
                System.out.println(ex);
            }

            while(!gameOver.get()){

                System.out.println(gameOver.get() + " <- game over? - my turn? -> " + myTurn);

                synchronized (this) {
                    while(myTurn.get()){
                        try{
                            System.out.println("Waiting for move..");
                            this.wait();
                        }catch (InterruptedException e){
                            //error
                        }
                    }
                }


                    try {
                        score = (Integer[][])fromServer.readObject();
                        if(checkVictory(1)){
                            //p1 won
                            System.out.println("Player 1 won");
                            Platform.runLater(() -> {
                                setOtherColor();
                                gameOver.set(true);
                                winnerLabel.setText("You Lost :( Player 1 Won");
                                pane.setBackground(new Background(new BackgroundFill(Color.rgb(255, 0, 0), CornerRadii.EMPTY, Insets.EMPTY)));
                            });
                        }
                        else if(checkVictory(2)){
                            //p2 won
                            System.out.println("Player 2 Won");
                            Platform.runLater(() -> {
                                setOtherColor();
                                gameOver.set(true);
                                winnerLabel.setText("You Lost :( Player 1 Won");
                                pane.setBackground(new Background(new BackgroundFill(Color.rgb(255, 0, 0), CornerRadii.EMPTY, Insets.EMPTY)));
                            });
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    myTurn.set(true);

                    Platform.runLater(() -> {
                        setOtherColor();
                        if(!gameOver.get()){
                            top.setBackground(new Background(new BackgroundFill(Color.rgb(0, 165, 205), CornerRadii.EMPTY, Insets.EMPTY)));
                        }


                    });


            }

        }).start();


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

            //Writing move to server thread
            new Thread(()->{

                try {


                    //Sends board after move to server
                    toServer.writeObject(score);

                    if(checkVictory(playerNum)){
                        gameOver.set(true);
                        System.out.println("I won!");

                    }

                    synchronized (this) {
                        if(gameOver.get()){
                            winnerLabel.setText("You Win!");
                            top.setBackground(new Background(new BackgroundFill(Color.rgb(50, 205, 50), CornerRadii.EMPTY, Insets.EMPTY)));
                        }
                        myTurn.set(false);
                        this.notifyAll();
                    }
                        //System.out.println("My turn: " + myTurn);

                        Platform.runLater(() -> {
                            setOtherColor();
                            if(!gameOver.get()){
                                top.setBackground(new Background(new BackgroundFill(Color.rgb(220, 220, 220), CornerRadii.EMPTY, Insets.EMPTY)));
                            }

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


        //System.out.println("Game over: " + gameOver.get() + " My turn: " + myTurn);
        //makes sure the player isn't trying to click a taken slot
        if(score[x][y] != 1 && score[x][y] != 2 && myTurn.get() && !gameOver.get()){

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

    public boolean checkVictory(int p){

        int counter = 0;

        //check across victory
        for(int i=0;i<6;i++){

            counter = 0;

            for(int j=0;j<7;j++){

                if(score[i][j] == p){
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

                if(score[j][i] == p) {
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
                if(score[j][i] == p && score[j-1][i+1] == p && score[j-2][i+2] == p && score[j-3][i+3] == p){
                    return true;
                    //won by diagonal upward
                }

            }
        }

        //check diagonal down victory
        for(int i=0;i<4;i++){
            for(int j=0;j<3;j++){
                if(score[j][i] == p && score[j+1][i+1] == p && score[j+2][i+2] == p && score[j+3][i+3] == p){
                    return true;
                    //won by diagonal downward
                }


            }
        }

        return false; //did not win

    }

}
