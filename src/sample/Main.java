package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;

public class Main extends Application {

    //IO streams
    Socket socket;
    public ObjectOutputStream toServer;
    public ObjectInputStream fromServer;

    FileInputStream green;
    FileInputStream red;
    FileInputStream empty;

    Button[][] buttons = new Button[6][7];
    Integer[][] score = new Integer[6][7];
    String player = "Player ";
    int playerNum;

    GridPane board;
    Label turnLabel;

    @Override
    public void start(Stage primaryStage) throws Exception{
        BorderPane pane = new BorderPane();
        turnLabel = new Label("Waiting for other Player");
        pane.setTop(turnLabel);
        board = new GridPane();

        //greenSrc = new FileInputStream("src/sample/yellowFilled.png");
        //redSrc = new FileInputStream("src/sample/redFilled.png");
        //emptySrc = new FileInputStream("src/sample/empty.png");

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
                buttons[i][j] = temp;

            }

        }


        System.out.println(System.getProperty("user.dir"));
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

                Platform.runLater(() -> turnLabel.setText("I am " + playerNum));
            }
            catch (IOException ex){
                System.out.println(ex);
            }

        }).start();


    }

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

    private void buttonHoverLeave(MouseEvent mouseEvent){

        try {
            Button temp = (Button)(mouseEvent.getSource());
            temp.setGraphic(new ImageView(new Image(new FileInputStream("src/sample/empty.png"))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

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


            new Thread(()->{

                try {

                    //Create an output stream to send data to the server
                    toServer.writeObject(score);
                    //Create an input stream to receive data to the server
                    fromServer = new ObjectInputStream(socket.getInputStream());

                }
                catch (IOException ex){
                    System.out.println(ex);
                }

            }).start();

        }
        else{
            System.out.println("Not good move");
        }
        //System.out.println(x + "" + y);
    }

    private boolean allowedSquare(int x, int y){

        //makes sure the player isn't trying to click a taken slot
        if(score[x][y] != 1 && score[x][y] != 2){

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
