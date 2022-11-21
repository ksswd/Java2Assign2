package application;

import application.controller.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();

            fxmlLoader.setLocation(getClass().getClassLoader().getResource("resources/mainUI.fxml"));
            Pane root = fxmlLoader.load();
            primaryStage.setTitle("Tic Tac Toe");
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        Controller.printWriter.println("Disconnect");
        Controller.printWriter.flush();
        Controller.printWriter.close();
        Controller.bufferedReader.close();
        Controller.socket.close();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
