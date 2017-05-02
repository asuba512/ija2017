package ija2017.GameGUI;

import ija2017.model.*;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller of single game scene.
 *
 * @author Jakub Paliesek (xpalie00)
 */
public class GameLayoutController implements Initializable {
    // geometry =  used by proportions calculation methods
    private static final int SPACING = 10;
    private static final double RATIO = 351.0 / 226.0;
    private int maxSizeY, maxSizeX; // max size of one column/row without spacing
    private int cardWidth, cardHeight; // calculated dimensions of one card with respect to game pane size
    private int[] xCoords, yCoords; // calculated coordinates for card positioning
    private int firstRowDecksY;
    private int cardOffsetInStack;

    // ImageViews
    /** arraylist holding placeholders for foundations */
    private ArrayList<ImageView> targetPlaceholders = new ArrayList<>(4);
    /** arraylist of placeholders for car stacks */
    private ArrayList<ImageView> stackPlaceholders = new ArrayList<>(7);
    /** arraylist of arraylist (array of cardstacks containing cards */
    private ArrayList<ArrayList<ImageView>> cardStacks = new ArrayList<>(7);
    /** arraylist of arraylist (array of foundations containing cards */
    private ArrayList<ArrayList<ImageView>> cardTargetPiles = new ArrayList<>(4);
    /** placeholder for facedown pile */
    private ImageView faceDownPilePlaceholder;
    /** placeholder for source pile (where dealt cards go) */
    private ImageView sourcePilePlaceholder;

    /** cards dealt from facedown pile */
    private ArrayList<ImageView> sourcePileCards = new ArrayList<>(54);

    public BorderPane gameContainer;
    public BorderPane gameRootPane;
    public Pane playingTable;
    /** shows number of remaining redeals */
    public Label redeals;
    /** shows number of earned points */
    public Label points;
    /** shows current seed */
    public Label seed;
    /** game model */
    private Game game;

    /** list of hints (newly acquired after hint click after each move) */
    private List<String> hints;
    /** used to cycle through list of acquired hints */
    private int hintIndex = -1; // repeated asking for hints rotates available hints

    /** list of substribers to our custom observer game exit event */
    private List<GameExitHandler> gameExitHandlers = new ArrayList<>();

    /**
     * Adds new subscriber of custom game exit event that is fired when Cancel game button is clicked
     */
    public void addExitHandler(GameExitHandler handler) {
        gameExitHandlers.add(handler);
    }

    /**
     * Fires OnGameExit Event to subscribers. By this event we are letting the enclosing scene know that
     * this game scene needs to be destroyed.
     */
    private void OnGameExit() {
        for(GameExitHandler h : gameExitHandlers)
            h.removeGame(gameContainer);
    }

    /**
     * Handles click event on cancel game click. Fires OnGameExit event to subscribers (in our case, the enclosing scene controller.
     */
    public void cancelGameClick() {
        OnGameExit(); // fire game exit event
    }


    /** numbers of pixels on x axis of cursor inside card when dragged */
    private double xPixelsInside;
    /** numbers of pixels on y axis of cursor inside card when dragged */
    private double yPixelsInside;
    /** array of hightlight imageviews. shown on demand when card is dragged */
    private ArrayList<ImageView> dropSites = new ArrayList<>(11);
    /** array of calculated centers of possible dragging targets. used to determine, which one is 'closest' */
    private ArrayList<Point> dropsiteCenters = new ArrayList<>(11);
    /** index of pile from which is the dragged card */
    private int sourcePileIndex;
    /** index of card in pile of dragged card */
    private int sourceCardIndex;

    /**
     * Handles onmousepressed event on card ImageView. Possible moves are calculated here and are valid until drop event.
     * @param mouseEvent self explanatory
     */
    private void beginDragDrop(MouseEvent mouseEvent) {
        ImageView sender = (ImageView)mouseEvent.getSource();
        xPixelsInside = mouseEvent.getSceneX() - sender.getLayoutX();
        yPixelsInside = mouseEvent.getSceneY() - sender.getLayoutY();
        sourcePileIndex = senderPileIndex((ImageView)mouseEvent.getSource());
        sourceCardIndex = -1;
        if(sourcePileIndex < 7 && sourcePileIndex >= 0) // stack
            sourceCardIndex = cardStacks.get(sourcePileIndex).indexOf(sender);
        calculateDropSites();
        sender.toFront();
        if(sourceCardIndex >= 0)
            for(int i = sourceCardIndex + 1;  i < cardStacks.get(sourcePileIndex).size(); i++)
                cardStacks.get(sourcePileIndex).get(i).toFront();
        if(sourcePileIndex >= 0)
            dropsiteCenters.set(sourcePileIndex, null); // prevent highlighting of source pile
    }

    /**
     * Handles onmousedragged event on card ImageViews. Highlights target stack, if card is hovering over target of legal move.
     * @param mouseEvent self explanatory.
     */
    private void dragDrop(MouseEvent mouseEvent) {
        ImageView source = ((ImageView)mouseEvent.getSource());
        if(sourceCardIndex >= 0)
            for(int i = sourceCardIndex + 1;  i < cardStacks.get(sourcePileIndex).size(); i++) {
                cardStacks.get(sourcePileIndex).get(i).setLayoutX(mouseEvent.getSceneX() - xPixelsInside);
                cardStacks.get(sourcePileIndex).get(i).setLayoutY(source.getLayoutY() + cardOffsetInStack*(i - sourceCardIndex));
                cardStacks.get(sourcePileIndex).get(i).toFront();
            }
        source.setLayoutX(mouseEvent.getSceneX() - xPixelsInside);
        source.setLayoutY(mouseEvent.getSceneY() - yPixelsInside);
        int overlayIndex = getOverlayIndex(new Point((int)(source.getLayoutX() + cardWidth / 2), (int)(source.getLayoutY() + cardHeight /2)));
        for(int i = 0; i < 11; i++) {
            if(i == overlayIndex)
                dropSites.get(i).setVisible(true);
            else
                dropSites.get(i).setVisible(false);
        }
    }

    /**
     * Handles onmousereleased event on card ImageView. If there is a visible highlight, it disappears. If card was hovering
     * over target that represents illegal move, card is moved to its source. If legas move was performed, game model
     * is notified.
     * @param mouseEvent self explanatory.
     */
    private void endDragDrop(MouseEvent mouseEvent) {
        ImageView source = ((ImageView)mouseEvent.getSource());
        for(int i = 0; i < 11; i++) {
            dropSites.get(i).setVisible(false);
        }
        int targetIndex = getOverlayIndex(new Point((int)(source.getLayoutX() + cardWidth / 2), (int)(source.getLayoutY() + cardHeight /2)));
        boolean moved = false;
        if(targetIndex >= 0) {
            if(targetIndex < 7) {
                if(cardStacks.get(targetIndex).size() < 19) {
                    if(sourcePileIndex >= 0 && sourceCardIndex >=0)
                        game.moveToStack(targetIndex, sourcePileIndex, sourceCardIndex);
                    else if(sourcePileIndex >= 0)
                        game.moveToStack(targetIndex, sourcePileIndex - 7);
                    else
                        game.moveToStack(targetIndex);
                    if(sourcePileIndex >= 0 && sourcePileIndex < 7) {
                        for(int i = sourceCardIndex; i < cardStacks.get(sourcePileIndex).size(); i++)
                            cardStacks.get(targetIndex).add(cardStacks.get(sourcePileIndex).get(i));
                    }
                    else
                        cardStacks.get(targetIndex).add(source);
                    moved = true;
                }
            } else if(targetIndex > 6 && targetIndex < 11) {
                if(sourcePileIndex >= 0 && sourceCardIndex >=0)
                    game.moveToFoundation(targetIndex - 7, sourcePileIndex, sourceCardIndex);
                else if(sourcePileIndex >= 0)
                    game.moveToFoundation(targetIndex - 7, sourcePileIndex - 7);
                else
                    game.moveToFoundation(targetIndex - 7);
                if(cardTargetPiles.get(targetIndex - 7).size() < 13) {
                    cardTargetPiles.get(targetIndex - 7).add(source);
                    moved = true;
                }
            }
        }
        if(moved) {
            if(sourcePileIndex >= 0 && sourcePileIndex < 7)
                if(targetIndex < 7){
                    int sizeOfSource = cardStacks.get(sourcePileIndex).size();
                    for(int i = sourceCardIndex; i < sizeOfSource; i++)
                        cardStacks.get(sourcePileIndex).remove(sourceCardIndex);
                }
                else
                    cardStacks.get(sourcePileIndex).remove(source);
            else if(sourcePileIndex >= 7) {
                cardTargetPiles.get(sourcePileIndex - 7).remove(source);
            }
            else
                sourcePileCards.remove(source);
            hintIndex = -1; // reset hints
        }
        reconstructTable();
        if(cardTargetPiles.get(0).size() == 13 && cardTargetPiles.get(1).size() == 13 &&
                cardTargetPiles.get(2).size() == 13 && cardTargetPiles.get(3).size() == 13)
            gameWon();
    }

    /**
     * Shows fancy messagebox to user, when the game is over.
     */
    private void gameWon() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Done!");
        alert.setHeaderText(null);
        alert.setContentText("Congratulations!\nYou have won the game!");
        alert.getDialogPane().getStyleClass().add("congratulations");
        alert.getDialogPane().getStylesheets().add(getClass().getResource("GameLayout.css").toExternalForm());
        ImageView firework = new ImageView(ImageResources.get().firework);
        firework.setFitWidth(200);
        firework.setFitHeight(200);
        alert.setGraphic(firework);
        alert.showAndWait();
        OnGameExit();
    }

    /**
     * Decides over which possible target the card is currently hovering.
     * @param pos Position of hovering card.
     * @return index to array of ImageViews representing highlight, so that the highlight can be shown.
     */
    private int getOverlayIndex(Point pos) {
        int min = Integer.MAX_VALUE;
        int minIndex = -1;
        for(int i = 0; i < 11; i++) {
            if(dropsiteCenters.get(i) != null)
                if(dropsiteCenters.get(i).distance(pos) < min) {
                    minIndex = i;
                    min = dropsiteCenters.get(i).distance(pos);
                }
        }
        if(minIndex < 0) return -1;
        if(pos.x < dropsiteCenters.get(minIndex).x) {
            if(pos.x + cardWidth >= dropsiteCenters.get(minIndex).x)
                return minIndex;
        } else {
            if(pos.x <= dropsiteCenters.get(minIndex).x + cardWidth)
                return minIndex;
        }
        return -1;
    }

    /**
     * Calculates centers of drop places for valid moves. For invalid moves, null is added at drop place's index.
     */
    private void calculateDropSites() {
        ImageView site;
        dropsiteCenters.clear();
        boolean canMove;
        for(int i = 0; i < 7; i++) {
            if(sourcePileIndex >= 0 && sourceCardIndex >=0)
                canMove = game.canMoveToStack(i, sourcePileIndex, sourceCardIndex);
            else if(sourcePileIndex >= 0)
                canMove = game.canMoveToStack(i, sourcePileIndex - 7);
            else
                canMove = game.canMoveToStack(i);
            if(canMove) {
                site = dropSites.get(i);
                site.toFront();
                site.setLayoutX(xCoords[i]);
                if (cardStacks.get(i).size() == 0)
                    site.setLayoutY(yCoords[0]);
                else
                    site.setLayoutY(yCoords[cardStacks.get(i).size() - 1]);
                dropsiteCenters.add(new Point(xCoords[i] + cardWidth / 2, (int) site.getLayoutY() + cardHeight / 2));
            } else dropsiteCenters.add(null);
        }
        for(int i = 7; i < 11; i++) {
            if(sourcePileIndex >= 0 && sourceCardIndex >=0)
                canMove = game.canMoveToFoundation(i - 7, sourcePileIndex, sourceCardIndex);
            else if(sourcePileIndex >= 0)
                canMove = game.canMoveToFoundation(i - 7, sourcePileIndex - 7);
            else
                canMove = game.canMoveToFoundation(i - 7);
            if(canMove) {
                site = dropSites.get(i);
                site.toFront();
                site.setLayoutX(xCoords[i - 4]);
                site.setLayoutY(SPACING + (maxSizeY - cardHeight) / 2);
                dropsiteCenters.add(new Point((xCoords[i - 4] + cardWidth / 2), (SPACING + (maxSizeY - cardHeight) / 2) + cardHeight / 2));
            } else dropsiteCenters.add(null);
        }
    }

    /**
     * Determines index of stack/foundation for specified card ImageView.
     * @param sender input imageview
     * @return
     */
    private int senderPileIndex(ImageView sender) {
        if(sourcePileCards.contains(sender))
            return -1; // sourcePile
        for(int i = 0; i < 7; i++) {
            if(cardStacks.get(i).contains(sender))
                return i;
        }
        for(int i = 0; i < 4; i++) {
            if(cardTargetPiles.get(i).contains(sender))
                return i+7;
        }
        return -1;
    }

    /* ********* Drag&drop logic end ******** */

    /**
     * Handles click event on face down pile placeholder. Sends message to the game object to deal new card
     * (or to turn over the pile). Facedown pile is recreated according to game model. When a card is dealed, its
     * ImageView is created with appropriate handlers set.
     *
     * @param e unused
     */
    private void turnOver(MouseEvent e) {
        game.turnCard();
        constructFaceDownPile();
        SourcePile pile = game.getSourcePile();
        if(pile.isEmpty()) {
            constructSourcePile();
        } else {
            ImageView img = new ImageView(ImageResources.get().cardImages.get(pile.forcePeek(pile.size()-1).toString()));
            sourcePileCards.add(img);
            playingTable.getChildren().add(img);
            img.setOnMousePressed(this::beginDragDrop);
            img.setOnMouseDragged(this::dragDrop);
            img.setOnMouseReleased(this::endDragDrop);
        }
        hintIndex = -1; // reset hints
        placeAll();
    }

    /**
     * Initialization method of controller. Does the work that is independent on Game model initialization method.
     * @param url unused
     * @param rb unused
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /* bind window resize listeners */
        playingTable.heightProperty().addListener(e -> placeAll());
        playingTable.widthProperty().addListener(e -> placeAll());

        /* FaceDownPile */
        faceDownPilePlaceholder = new ImageView(ImageResources.get().cardImages.get("X(X)"));
        faceDownPilePlaceholder.setOnMouseClicked(this::turnOver);
        playingTable.getChildren().add(faceDownPilePlaceholder);

        /* SourcePile */
        sourcePilePlaceholder = new ImageView(ImageResources.get().cardPlaceholder);
        playingTable.getChildren().add(sourcePilePlaceholder);

        /* Foundations */
        for(int i = 0; i < 4; i++){
            targetPlaceholders.add(new ImageView(ImageResources.get().cardPlaceholder));
            playingTable.getChildren().add(targetPlaceholders.get(i));
            cardTargetPiles.add(new ArrayList<>(13)); // initialize card arraylist
        }

        /* Stacks */
        for(int i = 0; i < 7; i++) {
        	stackPlaceholders.add(new ImageView(ImageResources.get().cardPlaceholder));
        	playingTable.getChildren().add(stackPlaceholders.get(i));
        	cardStacks.add(new ArrayList<>(19)); // initialize card arraylist
        }

        /* Drop site overlays */
        for(int i = 0; i < 11; i++) {
            ImageView site;
            site = new ImageView(ImageResources.get().cardHoverOverlay);
            dropSites.add(site);
            site.setVisible(false);
            playingTable.getChildren().add(site);
        }
    }

    /**
     * Initializes UI with newly created game. Random seed is used.
     */
    public void initializeNewGame() {
        game = new Game();
        reconstructTable();
    }

    /**
     * Initializes UI with existing instance of Game model. This is used when loading previously saved game.
     * @param savedGame Instance of game to be used.
     */
    public void initializeWithGame(Game savedGame) {
        /* create new game with given file */
        game = savedGame;
        reconstructTable();
    }

    /**
     * Initializes game with given seed that user entered in enclosing scene ( MainWindow ).
     * @param seed user-entered seed
     */
    public void initializeWithSeed(String seed) {
        /* create new game with given seed*/
        if(seed.isEmpty())
            game = new Game();
        else
            game = new Game(seed);
        reconstructTable();
    }

    /**
     * Creates new ImageViews of cards inside target pile with specified index according to game model
     * @param i Index of target pile to be reconstructed
     */
    private void constructTargetPile(int i) {
    	ArrayList<ImageView> targetPile = cardTargetPiles.get(i);
        for(ImageView image : cardTargetPiles.get(i))
            playingTable.getChildren().remove(image);
    	targetPile.clear();
    	FoundationPile pile = game.getFoundationPile(i);
    	for(int j = 0; j < pile.size(); j++) {
    		Card c = pile.forcePeek(j); // forcePeek to override game rules
            ImageView card = new ImageView(ImageResources.get().cardImages.get(c.toString()));
        	targetPile.add(card);
            card.setOnMousePressed(this::beginDragDrop);
            card.setOnMouseDragged(this::dragDrop);
            card.setOnMouseReleased(this::endDragDrop);
            playingTable.getChildren().add(card);
    	}
    }

    /**
     * Creates new ImageViews of cards inside card stack with specified index according to game model
     * @param i Index of card stack to be reconstructed
     */
    private void constructStack(int i) {
		ArrayList<ImageView> cardStack = cardStacks.get(i);
        for(ImageView image : cardStacks.get(i))
            playingTable.getChildren().remove(image);
        cardStack.clear();
        CardStack stack = game.getCardStack(i);
        for(int j = 0; j < stack.size(); j++) {
            Card c = stack.forcePeek(j); // forcePeek to override game rules
            ImageView card = new ImageView(ImageResources.get().cardImages.get(c.toString()));
            cardStack.add(card);
            if(!c.toString().equals("X(X)")) {
                card.setOnMousePressed(this::beginDragDrop);
                card.setOnMouseDragged(this::dragDrop);
                card.setOnMouseReleased(this::endDragDrop);
            }
            playingTable.getChildren().add(card);
        }
    }

    /**
     * Sets image of facedown pile placeholder according to game model (either empty placeholder or cardback).
     */
    private void constructFaceDownPile() {
        FaceDownPile pile = game.getFaceDownPile();
        if(pile.size() == 0)
            faceDownPilePlaceholder.setImage(ImageResources.get().cardPlaceholder);
        else
            faceDownPilePlaceholder.setImage(ImageResources.get().cardImages.get("X(X)"));
    }

    /**
     * Creates new ImageViews of cards in source pile according to game model.
     */
    private void constructSourcePile() {
        for(ImageView image : sourcePileCards)
            playingTable.getChildren().remove(image);
        sourcePileCards.clear();
        SourcePile pile = game.getSourcePile();
        for(int i = 0; i < pile.size(); i++) {
            Card c = pile.forcePeek(i);
            ImageView img = new ImageView(ImageResources.get().cardImages.get(c.toString()));
            sourcePileCards.add(img);
            playingTable.getChildren().add(img);
            img.setOnMousePressed(this::beginDragDrop);
            img.setOnMouseDragged(this::dragDrop);
            img.setOnMouseReleased(this::endDragDrop);
        }
    }

    /**
     * Method that does placement of all ImageViews inside pane. It is intended to be used after calculateProportions.
     */
    private void placeAll() {
        calculateProportions();
        redeals.setText("Redeals left: " + game.getRedealsLeft());
        points.setText("Points: " + game.getPoints());
        seed.setText("Current seed: " + game.getSeed());
        for(int i = 0; i < 7; i++)
            placeStack(i);
        for(int i = 0; i < 4; i++)
            placeTargetPile(i);
        placeFaceDownPlaceholder();
        placeSourcePile();
    }

    /**
     * Moves placeholder of facedown pile according to calculated variables related to positioning.
     */
    private void placeFaceDownPlaceholder() {
        faceDownPilePlaceholder.setFitHeight(cardHeight);
        faceDownPilePlaceholder.setFitWidth(cardWidth);
        faceDownPilePlaceholder.setLayoutY(firstRowDecksY);
        faceDownPilePlaceholder.setLayoutX(xCoords[0]);
    }

    /**
     * Moves card stack placeholder and cards inside stack with specified index according to proportions
     * calculated in variables related to positioning. This method is intended to be used after calling
     * calculateProportions.
     * @param i index of target pile to be moved
     */
    private void placeStack(int i) {
        stackPlaceholders.get(i).setFitWidth(cardWidth);
        stackPlaceholders.get(i).setFitHeight(cardHeight);
        stackPlaceholders.get(i).setLayoutX(xCoords[i]);
        stackPlaceholders.get(i).setLayoutY(yCoords[0]);
        for(int j = 0; j < cardStacks.get(i).size(); j++) {
            ImageView img = cardStacks.get(i).get(j);
            img.setLayoutX(xCoords[i]);
            img.setLayoutY(yCoords[j]);
            img.setFitHeight(cardHeight);
            img.setFitWidth(cardWidth);
            img.toFront();
        }
        ImageView site = dropSites.get(i);
        site.setFitHeight(cardHeight);
        site.setFitWidth(cardWidth);
    }


    /**
     * Moves target pile placeholder and cards inside target pile with specified index according to proportions
     * calculated in variables related to positioning. This method is intended to be used after calling
     * calculateProportions.
     * @param i index of target pile to be moved
     */
    private void placeTargetPile(int i) {
        ImageView img = targetPlaceholders.get(i);
        img.setLayoutX(xCoords[i+3]);
        img.setLayoutY(firstRowDecksY);
        img.setFitWidth(cardWidth);
        img.setFitHeight(cardHeight);
        for(int j = 0; j < cardTargetPiles.get(i).size(); j++) {
            ImageView card;
            card = cardTargetPiles.get(i).get(j);
            card.toFront();
            card.setLayoutY(firstRowDecksY);
            card.setLayoutX(xCoords[i+3]);
            card.setFitWidth(cardWidth);
            card.setFitHeight(cardHeight);
        }
        ImageView site = dropSites.get(i+7);
        site.setFitHeight(cardHeight);
        site.setFitWidth(cardWidth);
    }

    /**
     * Moves ImageViews of card placeholder and cards inside source pile according to proportions
     * calculated in variables related to positioning. This method is intended to be used after calling
     * calculateProportions.
     */
    private void placeSourcePile() {
        sourcePilePlaceholder.setLayoutX(xCoords[1]);
        sourcePilePlaceholder.setLayoutY(firstRowDecksY);
        sourcePilePlaceholder.setFitWidth(cardWidth);
        sourcePilePlaceholder.setFitHeight(cardHeight);
        for(int i = 0; i < sourcePileCards.size(); i++) {
            ImageView sourcePileCard = sourcePileCards.get(i);
            sourcePileCard.toFront();
            sourcePileCard.setLayoutY(firstRowDecksY);
            sourcePileCard.setLayoutX(xCoords[1]);
            sourcePileCard.setFitWidth(cardWidth);
            sourcePileCard.setFitHeight(cardHeight);
        }
    }

    /**
     * Recalculates every position and size needed for card placement inside window.
     */
    private void calculateProportions() {
        int height = (int)playingTable.getHeight();
        int width = (int)playingTable.getWidth();
        maxSizeX = (width - SPACING) / 7 - SPACING;
        maxSizeY = (height - 3 * SPACING) / 3;
        // recalculate card dimensions with respect to smaller dimension
        if(maxSizeX * RATIO > maxSizeY) {
            cardHeight = maxSizeY;
            cardWidth = (int)(maxSizeY / RATIO);
        } else {
            cardHeight = (int)(maxSizeX * RATIO);
            cardWidth = maxSizeX;
        }
        int localShift = (maxSizeX - cardWidth) / 2;
        xCoords = getXcoords(width);
        for(int i = 0; i < 7; i++) {
            xCoords[i] += localShift;
        }
        yCoords = getYcoords(height, cardHeight);
        firstRowDecksY = SPACING + (maxSizeY - cardHeight) / 2;
    }

    /**
     * Recalculates array of X coordinates for each card placeholder in horizontal direction.
     * @param width width of whole window
     * @return array of X coordinates
     */
    private int[] getXcoords(int width) {
        int slice_size = (width - SPACING) / 7;
        int[] arr = new int[7];
        for (int i = 0; i < 7; i++)
            arr[i] =  SPACING + i * slice_size;
        return arr;
    }

    /**
     * Recalculates array of Y coordinaes which represent positions of cards in card stacks.
     * @param height    height of window/stage
     * @param cardHeight    height of card ImageView
     * @return  array of Y coordinates
     */
    private int[] getYcoords(int height, int cardHeight) {
        int startPos = (height - 3 * SPACING) / 3 + 2 * SPACING;
        int[] arr = new int[19];
        arr[0] = startPos;
        arr[12] = height - cardHeight - 1; // last card lies here
        cardOffsetInStack = (arr[12] - arr[0]) / 12; // interpolation step
        for(int i = 1; i < 19; i++)
            arr[i] = arr[0] + i * cardOffsetInStack;
        return arr;
    }

    /**
     * Handles click event on undo button. Undo is done in the game model using command pattern.
     * Whole game is GUI does not know anything, so entire table is recreated every time undo is
     * demanded.
     */
    public void undo() {
        game.undo();
        hintIndex = -1;
        reconstructTable();
    }

    /**
     * Clears card placeholders and creates new ImageViews according to current state of the game.
     */
    private void reconstructTable() {
        for(int i = 0; i < 7; i++)
            constructStack(i);

        for(int i = 0; i < 4; i++)
            constructTargetPile(i);

        constructSourcePile();
        constructFaceDownPile();
        placeAll();
    }

    /**
     * Handles click event on hint button. More information on finding possible moves is explained
     * in docs for Game.getHints method. Cycles through available hints when clicks are repeated.
     */
    public void hintClick() {
        if(hintIndex < 0) { // if we haven't asked for hints in this turn yet
            hints = game.getHints();
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hint");
        alert.setHeaderText(null);

        if(hints.size() != 0) {
            hintIndex = ++hintIndex % hints.size();
            alert.setContentText(hints.get(hintIndex));
        } else {
            hintIndex = 0;
            alert.setContentText("This game is lost. There's no hope.");
        }
        alert.showAndWait();
    }

    /**
     * Handles event when user clicks on save button. Game is saved using serializable.
     */
    public void saveClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Game");
        File file = fileChooser.showSaveDialog(gameRootPane.getScene().getWindow());
        if (file != null) {
            try {
                file.createNewFile();
                FileOutputStream fileOut = new FileOutputStream(file, false); // false = do not append
                ObjectOutputStream oos = new ObjectOutputStream(fileOut);
                oos.writeObject(game);
                oos.close();
                fileOut.close();
            } catch(IOException e) {
                return;
            }
        }
    }

    /**
     * Handles onclick event on clipboard button, inserts given seed into clipboard.
     */
    public void copySeed() {
        Clipboard cb = Clipboard.getSystemClipboard();
        ClipboardContent c = new ClipboardContent();
        c.putString(game.getSeed());
        cb.setContent(c);
    }

    /**
     *  Small class representing 2D points in calculations.
     */
    private class Point {
        int x,y;

        /**
         * Initializes new point with given coordinates
         * @param x x coordinate
         * @param y y coordinate
         */
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Calculates discance between current point and point passed in parameter.
         * @param p other point
         * @return distance
         */
        int distance(Point p) {
            int dx = this.x - p.x, dy = this.y - p.y;
            return dx*dx + dy*dy;
        }
    }
}
