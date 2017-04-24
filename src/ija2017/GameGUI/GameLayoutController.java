package ija2017.GameGUI;

import ija2017.model.*;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

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
    private ArrayList<ImageView> targetPlaceholders = new ArrayList<>(4);
    private ArrayList<ImageView> stackPlaceholders = new ArrayList<>(7);
    private ArrayList<ArrayList<ImageView>> cardStacks = new ArrayList<>(7);
    private ArrayList<ArrayList<ImageView>> cardTargetPiles = new ArrayList<>(4);
    private ImageView faceDownPilePlaceholder;
    private ImageView sourcePilePlaceholder;
    private ArrayList<ImageView> faceDownPile = new ArrayList<>(54);
    private ArrayList<ImageView> sourcePileCards = new ArrayList<>(54);

    public BorderPane gameRootPane;
    public Pane playingTable;
    private Game game;

    private List<GameExitHandler> gameExitHandlers = new ArrayList<>();

    public void addExitHandler(GameExitHandler handler) {
        gameExitHandlers.add(handler);
    }

    private void OnGameExit() {
        for(GameExitHandler h : gameExitHandlers)
            h.removeGame(gameRootPane);
    }

    public void cancelGameClick(ActionEvent actionEvent) {
        OnGameExit(); // fire game exit event
    }

    private double xPixelsInside;
    private double yPixelsInside;
    private ArrayList<ImageView> dropSites = new ArrayList<>(11);
    private ArrayList<Point> dropsiteCenters = new ArrayList<>(11);
    private int sourcePileIndex;
    private int sourceCardIndex;

    private void beginDragDrop(MouseEvent mouseEvent) {
        ImageView sender = (ImageView)mouseEvent.getSource();
        xPixelsInside = mouseEvent.getSceneX() - sender.getLayoutX();
        yPixelsInside = mouseEvent.getSceneY() - sender.getLayoutY();
        sourcePileIndex = senderPileIndex((ImageView)mouseEvent.getSource());
        sourceCardIndex = -1;
        if(sourcePileIndex < 7 && sourcePileIndex >= 0) // stack
            sourceCardIndex = senderStackCardIndex(sourcePileIndex, sender);
        calculateDropSites();
        sender.toFront();
        if(sourceCardIndex >= 0)
            for(int i = sourceCardIndex + 1;  i < cardStacks.get(sourcePileIndex).size(); i++)
                cardStacks.get(sourcePileIndex).get(i).toFront();
        if(sourcePileIndex >= 0)
            dropsiteCenters.set(sourcePileIndex, null); // prevent highlighting of source pile
    }

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
        }
        reconstructTable();
    }

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
                //site.setVisible(true);
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
                //site.setVisible(true);
                site.toFront();
                site.setLayoutX(xCoords[i - 4]);
                site.setLayoutY(SPACING + (maxSizeY - cardHeight) / 2);
                dropsiteCenters.add(new Point((xCoords[i - 4] + cardWidth / 2), (SPACING + (maxSizeY - cardHeight) / 2) + cardHeight / 2));
            } else dropsiteCenters.add(null);
        }
    }

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

    private int senderStackCardIndex(int i, ImageView sender){
        return cardStacks.get(i).indexOf(sender);
    }

    /********** Drag&drop logic end *********/

    private void turnOver(MouseEvent mouseEvent){
        game.turnCard();
        constructFaceDownPile();
        SourcePile pile = game.getSourcePile();
        if(pile.isEmpty()) {
            constructSourcePile();
        } else {
            ImageView img = new ImageView(CardImages.get().cardImages.get(pile.forcePeek(pile.size()-1).toString()));
            sourcePileCards.add(img);
            playingTable.getChildren().add(img);
            img.setOnMousePressed(this::beginDragDrop);
            img.setOnMouseDragged(this::dragDrop);
            img.setOnMouseReleased(this::endDragDrop);
        }
        placeAll();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /* bind window resize listeners */
        playingTable.heightProperty().addListener(e -> placeAll());
        playingTable.widthProperty().addListener(e -> placeAll());

        /* FaceDownPile */
        faceDownPilePlaceholder = new ImageView(CardImages.get().cardImages.get("X(X)"));
        faceDownPilePlaceholder.setOnMouseClicked(this::turnOver);
        playingTable.getChildren().add(faceDownPilePlaceholder);

        /* SourcePile */
        sourcePilePlaceholder = new ImageView(CardImages.get().cardPlaceholder);
        playingTable.getChildren().add(sourcePilePlaceholder);

        /* Foundations */
        for(int i = 0; i < 4; i++){
            targetPlaceholders.add(new ImageView(CardImages.get().cardPlaceholder));
            playingTable.getChildren().add(targetPlaceholders.get(i));
            cardTargetPiles.add(new ArrayList<>(13)); // initialize card arraylist
        }

        /* Stacks */
        for(int i = 0; i < 7; i++) {
        	stackPlaceholders.add(new ImageView(CardImages.get().cardPlaceholder));
        	playingTable.getChildren().add(stackPlaceholders.get(i));
        	cardStacks.add(new ArrayList<>(19)); // initialize card arraylist
        }

        /* Drop site overlays */
        for(int i = 0; i < 11; i++) {
            ImageView site;
            site = new ImageView(CardImages.get().cardHoverOverlay);
            dropSites.add(site);
            site.setVisible(false);
            playingTable.getChildren().add(site);
        }
    }

    public void initializeNewGame() {
        game = new Game();
        reconstructTable();
    }

    public void initializeWithFile(File savegame) {
        /* create new game with given file */
        game = new Game();
        reconstructTable();
    }

    public void initializeWithSeed(String seed) {
        /* create new game with given seed*/
        game = new Game();
        reconstructTable();
    }

    private void constructTargetPile(int i) {
    	ArrayList<ImageView> targetPile = cardTargetPiles.get(i);
        for(ImageView image : cardTargetPiles.get(i))
            playingTable.getChildren().remove(image);
    	targetPile.clear();
    	FoundationPile pile = game.getFoundationPile(i);
    	for(int j = 0; j < pile.size(); j++) {
    		Card c = pile.forcePeek(j); // forcePeek to override game rules
            ImageView card = new ImageView(CardImages.get().cardImages.get(c.toString()));
        	targetPile.add(card);
            card.setOnMousePressed(this::beginDragDrop);
            card.setOnMouseDragged(this::dragDrop);
            card.setOnMouseReleased(this::endDragDrop);
            playingTable.getChildren().add(card);
    	}
    }

    private void constructStack(int i) {
		ArrayList<ImageView> cardStack = cardStacks.get(i);
        for(ImageView image : cardStacks.get(i))
            playingTable.getChildren().remove(image);
        cardStack.clear();
        CardStack stack = game.getCardStack(i);
        for(int j = 0; j < stack.size(); j++) {
            Card c = stack.forcePeek(j); // forcePeek to override game rules
            ImageView card = new ImageView(CardImages.get().cardImages.get(c.toString()));
            cardStack.add(card);
            if(!c.toString().equals("X(X)")) {
                card.setOnMousePressed(this::beginDragDrop);
                card.setOnMouseDragged(this::dragDrop);
                card.setOnMouseReleased(this::endDragDrop);
            }
            playingTable.getChildren().add(card);
        }
    }

    private void constructFaceDownPile() {
        FaceDownPile pile = game.getFaceDownPile();
        if(pile.size() == 0)
            faceDownPilePlaceholder.setImage(CardImages.get().cardPlaceholder);
        else
            faceDownPilePlaceholder.setImage(CardImages.get().cardImages.get("X(X)"));
    }

    private void constructSourcePile() {
        for(ImageView image : sourcePileCards)
            playingTable.getChildren().remove(image);
        sourcePileCards.clear();
        SourcePile pile = game.getSourcePile();
        for(int i = 0; i < pile.size(); i++) {
            Card c = pile.forcePeek(i);
            ImageView img = new ImageView(CardImages.get().cardImages.get(c.toString()));
            sourcePileCards.add(img);
            playingTable.getChildren().add(img);
            img.setOnMousePressed(this::beginDragDrop);
            img.setOnMouseDragged(this::dragDrop);
            img.setOnMouseReleased(this::endDragDrop);
        }
    }

    private void placeAll() {
        calculateProportions();
        for(int i = 0; i < 7; i++)
            placeStack(i);
        for(int i = 0; i < 4; i++)
            placeTargetPile(i);
        placeFaceDownPlaceholder();
        placeSourcePile();
    }

    private void placeFaceDownPlaceholder() {
        faceDownPilePlaceholder.setFitHeight(cardHeight);
        faceDownPilePlaceholder.setFitWidth(cardWidth);
        faceDownPilePlaceholder.setLayoutY(firstRowDecksY);
        faceDownPilePlaceholder.setLayoutX(xCoords[0]);
    }

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

    /* Calculation of proportions for drawing. */

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

    private int[] getXcoords(int width) {
        int slice_size = (width - SPACING) / 7;
        int[] arr = new int[7];
        for (int i = 0; i < 7; i++)
            arr[i] = SPACING + i * slice_size;
        return arr;
    }

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

    public void undo(ActionEvent actionEvent) {
        game.undo();
        reconstructTable();
    }

    private void reconstructTable() {
        for(int i = 0; i < 7; i++)
            constructStack(i);

        for(int i = 0; i < 4; i++)
            constructTargetPile(i);

        constructSourcePile();
        constructFaceDownPile();
        placeAll();
    }

    public void hintClick() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hint");
        alert.setHeaderText(null);
        alert.setContentText("Hint text.");
        alert.showAndWait();
    }

    private class Point {
        int x,y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        int distance(Point p) {
            int dx = this.x - p.x, dy = this.y - p.y;
            return dx*dx + dy*dy;
        }
    }
}
