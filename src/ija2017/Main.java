package ija2017;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class that includes main() method.
 * @author Jakub Paliesek (xpalie00)
 */
public class Main extends Application {

    /**
     * Starts GUI application.
     * @param primaryStage Main FXML stage.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("MainWindowLayout.fxml"));
        primaryStage.setTitle("Klondike Solitaire");
        primaryStage.setScene(new Scene(root, 1280, 760));
        primaryStage.show();
    }

    /**
     * Main method that is launched.
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
