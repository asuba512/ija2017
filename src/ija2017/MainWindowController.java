package ija2017;

import ija2017.GameGUI.GameExitHandler;
import ija2017.GameGUI.GameLayoutController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable, GameExitHandler {
    public GridPane gameGrid;
    public Button newGameButton;
    public Button loadGameButton;

    private int numberOfGames = 0;
    private BorderPane[][] gamePanes = {{null, null}, {null, null}}; // [row][col]

    private int[] findFreeSpot() {
        for(int row = 0; row < 2; row++)
            for(int col = 0; col < 2; col++)
                if (gamePanes[row][col] == null)
                    return new int[] {row, col};
        return null;
    }

    private int[] getNextSpot(int[] spot) {
        if(spot[0] == 0 && spot[1] == 0)
            return new int[] {0,1};
        else if(spot[0] == 0 && spot[1] == 1)
            return new int[] {1,0};
        else if(spot[0] == 1 && spot[1] == 0)
            return new int[] {1,1};
        else
            return null;

    }

    private void shiftGamePanesFrom(int[] spot) {
        int[] nextSpot = getNextSpot(spot);
        if(nextSpot != null) {
            gamePanes[spot[0]][spot[1]] = gamePanes[nextSpot[0]][nextSpot[1]];
            gamePanes[nextSpot[0]][nextSpot[1]] = null;
            shiftGamePanesFrom(nextSpot);
        }
    }

    private void recreateScreen() {
        for(int row = 0; row < 2; row++)
            for(int col = 0; col < 2; col++)
                if(gamePanes[row][col] != null)
                    gameGrid.getChildren().remove(gamePanes[row][col]);
        for(int row = 0; row < 2; row++)
            for(int col = 0; col < 2; col++)
                if(gamePanes[row][col] != null)
                    gameGrid.add(gamePanes[row][col], col, row);
    }

    /* Handles exit of game */
    public void removeGame(BorderPane gamePane) {
        gameGrid.getChildren().remove(gamePane);
        int[] pos = new int[2];
        for(int row = 0; row < 2; row++)
            for(int col = 0; col < 2; col++)
                if (gamePanes[row][col] == gamePane) {
                    gamePanes[row][col] = null;
                    pos[0] = row;
                    pos[1] = col;
                }
        shiftGamePanesFrom(pos);
        recreateScreen();
        numberOfGames--;
        if(numberOfGames < 2) {
            setBiggerGame(0, 0);
        }
        if(numberOfGames < 4) {
            newGameButton.setDisable(false);
            loadGameButton.setDisable(false);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GameGUI/GameLayout.fxml"));
        BorderPane newGame;
        try {
            newGame = loader.load();
            gameGrid.add(newGame, 0, 0);
            gamePanes[0][0] = (BorderPane)gameGrid.getChildren().get(0);
            ((GameLayoutController)loader.getController()).addExitHandler(this);
            numberOfGames++;
        } catch (IOException ignored) {}
    }

    @FXML
    public void newGameClick(ActionEvent e) throws IOException {
        int[] freespot = findFreeSpot();
        if(freespot != null) {
            setSmallerGame();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GameGUI/GameLayout.fxml"));
            BorderPane newGame = loader.load();
            gamePanes[freespot[0]][freespot[1]] = newGame;
            gameGrid.add(newGame, freespot[1], freespot[0]);
            ((GameLayoutController)loader.getController()).addExitHandler(this);
            numberOfGames++;
            if(numberOfGames == 4) {
                newGameButton.setDisable(true);
                loadGameButton.setDisable(true);
            }
            if(numberOfGames == 1)
                setBiggerGame(freespot[0], freespot[1]);
        }
    }

    @FXML
    public void loadGameClick(ActionEvent e) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open saved game");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Solitaire JSON savegame", "*.sjson"));
        File selectedFile = fileChooser.showOpenDialog(gameGrid.getScene().getWindow());
        if(selectedFile == null)
            return;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("GameGUI/GameLayout.fxml"));
        BorderPane newGame = loader.load();
        GameLayoutController controller = loader.getController();
        controller.setLoadFile(selectedFile);
        gameGrid.add(newGame, 0, 0);
    }

    /* Splits screen into 4 separate areas for games */
    private void setSmallerGame() {
        for (RowConstraints r : gameGrid.getRowConstraints())
            r.setPercentHeight(50.0);
        for (ColumnConstraints c : gameGrid.getColumnConstraints())
            c.setPercentWidth(50.0);
        gameGrid.setVgap(3.0);
        gameGrid.setHgap(3.0);
    }

    /* Maximizes game at given [row, col] */
    private void setBiggerGame(int row, int col) {
        gameGrid.setVgap(0.0);
        gameGrid.setHgap(0.0);

        if(row == 0)
            gameGrid.getRowConstraints().get(1).setPercentHeight(0.0);
        else
            gameGrid.getRowConstraints().get(0).setPercentHeight(0.0);

        if(col == 0)
            gameGrid.getColumnConstraints().get(1).setPercentWidth(0.0);
        else
            gameGrid.getColumnConstraints().get(0).setPercentWidth(0.0);

        gameGrid.getRowConstraints().get(row).setPercentHeight(100.0);
        gameGrid.getColumnConstraints().get(col).setPercentWidth(100.0);
    }

}