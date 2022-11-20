package application.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private static final int EMPTY = 0;
    private static final int BOUND = 90;
    private static final int OFFSET = 15;

    public static Socket socket;
    public static PrintWriter printWriter;
    public static BufferedReader bufferedReader;
    private static int PlayerID = 0;
    private static int CurrentPlayer = -1;
    @FXML
    public Pane base_square;
    @FXML
    private Button button_connect;
    @FXML
    private Rectangle game_panel;


    private static final int[][] chessBoard = new int[3][3];

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        game_panel.setOnMouseClicked(event -> {
            if (PlayerID != CurrentPlayer) {
                return;
            }
            int x = (int) (event.getX() / BOUND);
            int y = (int) (event.getY() / BOUND);
            if (chessBoard[x][y] != EMPTY){
                return;
            }
            // send the operation to the server
            printWriter.println(String.format("Gaming,%d,%d,%d", PlayerID, x, y));
            printWriter.flush();
        });
    }

    private void refreshBoard(int turn, int x, int y) {
        chessBoard[x][y] = turn + 1;
        if (turn == 0) {
            drawCircle(x, y);
        } else {
            drawLine(x, y);
        }
    }


    private void drawCircle(int i, int j) {
        Circle circle = new Circle();
        circle.setCenterX(i * BOUND + BOUND / 2.0 + OFFSET);
        circle.setCenterY(j * BOUND + BOUND / 2.0 + OFFSET);
        circle.setRadius(BOUND / 2.0 - OFFSET / 2.0);
        circle.setStroke(Color.RED);
        circle.setFill(Color.TRANSPARENT);
        try {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    base_square.getChildren().add(circle);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawLine(int i, int j) {
        Line line_a = new Line();
        Line line_b = new Line();
        line_a.setStartX(i * BOUND + OFFSET * 1.5);
        line_a.setStartY(j * BOUND + OFFSET * 1.5);
        line_a.setEndX((i + 1) * BOUND + OFFSET * 0.5);
        line_a.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_a.setStroke(Color.BLUE);

        line_b.setStartX((i + 1) * BOUND + OFFSET * 0.5);
        line_b.setStartY(j * BOUND + OFFSET * 1.5);
        line_b.setEndX(i * BOUND + OFFSET * 1.5);
        line_b.setEndY((j + 1) * BOUND + OFFSET * 0.5);
        line_b.setStroke(Color.BLUE);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                base_square.getChildren().add(line_a);
                base_square.getChildren().add(line_b);
            }
        });
    }

    public void connectToServer(ActionEvent actionEvent) {
        try {
            socket = new Socket(InetAddress.getLocalHost(), 12312);
            printWriter = new PrintWriter(socket.getOutputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            new Receive().start();
            System.out.println("Success!");
            button_connect.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class Receive extends Thread {
        public void run() {
            while (this.isAlive()) {
                try {
                    String readLine = bufferedReader.readLine();
                    String[] opcode = readLine.split(",");
                    // set ID of each Client
//                    System.out.println(opcode[0]);
                    if (Objects.equals(opcode[0], "SetID")) {
                        PlayerID = Integer.parseInt(opcode[1]);
                        System.out.println("Your ID is " + PlayerID);
                    }
                    else if (Objects.equals(opcode[0], "StartGame")) {
                        CurrentPlayer = Integer.parseInt(opcode[1]);
                        System.out.println("Game Start!");
                    }
                    else if(Objects.equals(opcode[0], "Gaming")){
                        int lastTurn = Integer.parseInt(opcode[1]);
                        refreshBoard(lastTurn, Integer.parseInt(opcode[2]), Integer.parseInt(opcode[3]));
                        CurrentPlayer = (lastTurn + 1) % 2;
                    }
                    else if (Objects.equals(opcode[0], "FinalCheck")) {
                        int res = Integer.parseInt(opcode[1]);
                        if(res==1||res==2)
                            System.out.println("Player:"+res+" win!");
                        else if(res==3)
                            System.out.println("Tie!");
                        break;
                    }
                    else if (Objects.equals(opcode[0], "Disconnect")){
                        System.out.println("Your opponent is disconnected!");
                        System.out.println("You win!");
                        break;
                    }
                    if (CurrentPlayer == PlayerID) {
                        System.out.println("Your turn!");
                    } else {
                        System.out.println("Waiting!");
                    }
                    Thread.sleep(100);
                } catch (Exception e) {
//                    e.printStackTrace();
                }
            }
        }
    }
}
