package ija2017;

import ija2017.GameGUI.GameExitHandler;
import ija2017.GameGUI.GameLayoutController;
import ija2017.model.Game;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainWindowController implements Initializable, GameExitHandler {
    public GridPane gameGrid;
    public Button newGameButton;
    public Button loadGameButton;
    public Button seedGameButton;

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
            GameLayoutController ctr = loader.getController();
            ctr.addExitHandler(this);
            ctr.initializeNewGame();
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
            GameLayoutController ctr = loader.getController();
            ctr.addExitHandler(this);
            numberOfGames++;
            if(numberOfGames == 4) {
                newGameButton.setDisable(true);
                loadGameButton.setDisable(true);
            }
            if(numberOfGames == 1)
                setBiggerGame(freespot[0], freespot[1]);
            ctr.initializeNewGame();
        }
    }

    @FXML
    public void loadGameClick() throws IOException {
        int[] freeSpot = findFreeSpot();
        if(freeSpot != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open saved game");
            File selectedFile = fileChooser.showOpenDialog(gameGrid.getScene().getWindow());
            if (selectedFile == null)
                return;
            Game g;
            try {
                FileInputStream fileIn = new FileInputStream(selectedFile);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                g = (Game) in.readObject();
                in.close();
                fileIn.close();
            } catch (Exception e) {
                showErrorDialog("File loading error", "Error loading savegame file. Is file in correct format and accessible?");
                return;
            }
            setSmallerGame();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GameGUI/GameLayout.fxml"));
            BorderPane newGame = loader.load();
            GameLayoutController ctr = loader.getController();
            ctr.addExitHandler(this);
            gamePanes[freeSpot[0]][freeSpot[1]] = newGame;
            gameGrid.add(newGame, freeSpot[1], freeSpot[0]);
            numberOfGames++;
            if(numberOfGames == 4) {
                newGameButton.setDisable(true);
                loadGameButton.setDisable(true);
            }
            if(numberOfGames == 1)
                setBiggerGame(freeSpot[0], freeSpot[1]);
            ctr.initializeWithGame(g);
        }
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

    public void seedGameClick() {
        int[] freespot = findFreeSpot();
        if(freespot != null) {
            TextInputDialog dialog = new TextInputDialog("42");
            dialog.setTitle("Seed prompt");
            dialog.setHeaderText("Please enter your seed");
            dialog.setContentText("Random text:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(s -> {
                setSmallerGame();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("GameGUI/GameLayout.fxml"));
                BorderPane newGame;
                try {
                    newGame = loader.load();
                } catch (IOException e) { return; }
                gamePanes[freespot[0]][freespot[1]] = newGame;
                gameGrid.add(newGame, freespot[1], freespot[0]);
                GameLayoutController ctr = loader.getController();
                ctr.addExitHandler(this);
                numberOfGames++;
                if(numberOfGames == 4) {
                    newGameButton.setDisable(true);
                    loadGameButton.setDisable(true);
                }
                if(numberOfGames == 1)
                    setBiggerGame(freespot[0], freespot[1]);
                ctr.initializeWithSeed(s);
            });
        }
    }

    private void showErrorDialog(String header, String text) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(header);
        alert.setHeaderText(null);
        alert.setContentText(text);
        alert.showAndWait();
    }
}
