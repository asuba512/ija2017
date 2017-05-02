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

/**
 * Controller of main scene (wrapper of games)
 *
 * @author Jakub Paliesek (xpalie00)
 */
public class MainWindowController implements Initializable, GameExitHandler {
    /** grid holding games */
    public GridPane gameGrid;
    /** button that creates new games from random seed */
    public Button newGameButton;
    /** button that opens up file dialog and initializes new game from file */
    public Button loadGameButton;
    /** button that pops up a dialog for entering seed and creates new game afterwards */
    public Button seedGameButton;

    /** number of currently active games */
    private int numberOfGames = 0;
    /** matrix of game panes */
    private BorderPane[][] gamePanes = {{null, null}, {null, null}}; // [row][col]

    /**
     * Finds first free spot for new game, starting at top right corner.
     * @return x coordinate in game grid at index 0, y coordinate at index 1
     */
    private int[] findFreeSpot() {
        for(int row = 0; row < 2; row++)
            for(int col = 0; col < 2; col++)
                if (gamePanes[row][col] == null)
                    return new int[] {row, col};
        return null;
    }

    /**
     * Determines coordinates of next spot to the spot from given coords
     * @param spot coordinates of spot to find next one
     * @return coordinates of spot next to given spot
     */
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

    /**
     * Every shift every game from its spot to previous spot. Shifting is performed only in
     * internal matrix, not in GUI.
     * @param spot coordinates of spot from which shifting begins
     */
    private void shiftGamePanesFrom(int[] spot) {
        int[] nextSpot = getNextSpot(spot);
        if(nextSpot != null) {
            gamePanes[spot[0]][spot[1]] = gamePanes[nextSpot[0]][nextSpot[1]];
            gamePanes[nextSpot[0]][nextSpot[1]] = null;
            shiftGamePanesFrom(nextSpot);
        }
    }

    /**
     * Removes game panes from grid and inserts them again following their positions in internal matrix.
     */
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

    /**
     * Handles closing event from one of children (games).
     * @param gamePane pane of game to be removed
     */
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
            buttonsSetDisable(false);
        }
    }

    /**
     * Method performed when game grid is created. Creates new game that spans on all 4 spots (maximized game)
     * @param url unused
     * @param resourceBundle unused
     */
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

    /**
     * Handles click event on button to create a quick game. Loads GameLayout using FXML loader,
     * initializes game from random seed and disables game creation buttons when grid is full.
     * @param e unused
     * @throws IOException this can occur when FXML layout is missing
     */
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
                buttonsSetDisable(true);
            }
            if(numberOfGames == 1)
                setBiggerGame(freespot[0], freespot[1]);
            ctr.initializeNewGame();
        }
    }

    /**
     * Handles click event on load game button, prompts user to select savegame,
     * loads gamelayout from FXML, creates new Game model object  and initializes new Game UI with it.
     * @throws IOException Can occur when FXML layout is missing.
     */
    @FXML
    public void loadGameClick() throws IOException {
        int[] freeSpot = findFreeSpot();
        if(freeSpot != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open saved game");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Solitaire saved game", "*.save"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );
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
                showErrorDialog();
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
                buttonsSetDisable(true);
            }
            if(numberOfGames == 1)
                setBiggerGame(freeSpot[0], freeSpot[1]);
            ctr.initializeWithGame(g);
        }
    }

    /**
     * Splits screen into 4 separate areas for games.
     */
    private void setSmallerGame() {
        for (RowConstraints r : gameGrid.getRowConstraints())
            r.setPercentHeight(50.0);
        for (ColumnConstraints c : gameGrid.getColumnConstraints())
            c.setPercentWidth(50.0);
        gameGrid.setVgap(3.0);
        gameGrid.setHgap(3.0);
    }

    /**
     * Maximizes game at given [row, col]
     * @param row row of game
     * @param  col column of game
     */
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


    /**
     * Handles click event for new game button (from given seed). Prompts user to enter custom seed,
     * loads FXML game layout and initializes game UI.
     */
    public void seedGameClick() {
        int[] freespot = findFreeSpot();
        if(freespot != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Seed prompt");
            dialog.setHeaderText("Please enter your seed.");
            dialog.setContentText("Enter Seed:");
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
                    buttonsSetDisable(true);
                }
                if(numberOfGames == 1)
                    setBiggerGame(freespot[0], freespot[1]);
                ctr.initializeWithSeed(s);
            });
        }
    }

    /**
     * Pops up a generic error dialog
     */
    private void showErrorDialog() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("File loading error");
        alert.setHeaderText(null);
        alert.setContentText("Error loading savegame file. Is file in correct format and accessible?");
        alert.showAndWait();
    }

    /**
     * Enable/disable game creation buttons (quick, new from seed, load)
     * @param b use true to disable game, false for enabling
     */
    private void buttonsSetDisable(boolean b) {
        newGameButton.setDisable(b);
        loadGameButton.setDisable(b);
        seedGameButton.setDisable(b);
    }
}
