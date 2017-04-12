package ija2017.GameGUI;

import ija2017.model.*;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
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
    private static final int SPACING = 10;
    private static final double RATIO = 351.0 / 226.0;
    public BorderPane gameRootPane;
    public Label idLabel;
    public Pane playingTable;
    private File loadFile = null;
    private Game game;

    private ArrayList<ImageView> targetPlaceholders = new ArrayList<>(4);
    private ArrayList<ImageView> stackPlaceholders = new ArrayList<>(7);
    private ArrayList<ArrayList<ImageView>> cardStacks = new ArrayList<>(7);
    private ArrayList<ArrayList<ImageView>> cardTargetPiles = new ArrayList<>(4);
    private ImageView faceDownPilePlaceholder;
    private ImageView sourcePilePlaceholder;
    private ArrayList<ImageView> faceDownPile = new ArrayList<>(54);
    private ArrayList<ImageView> sourcePileCards = new ArrayList<>(54);
    private int[] xCoords;
    private int[] yCoords;
    private int cardOffsetInStack;
    private int maxSizeX;
    private int maxSizeY;
    int cardWidth;
    int cardHeight;


    private List<GameExitHandler> gameExitHandlers = new ArrayList<>();

    public void addExitHandler(GameExitHandler handler) {
        gameExitHandlers.add(handler);
    }

    private void OnGameExit() {
        for(GameExitHandler h : gameExitHandlers)
            h.removeGame(gameRootPane);
    }

    /********** Drag&drop logic *********/

    private boolean dragdrop = false;
    private double xPixelsInside;
    private double yPixelsInside;
    private ArrayList<ImageView> dropSites = new ArrayList<>(11);
    private ArrayList<Point> dropsiteCenters = new ArrayList<>(11);
    int sourcePileIndex;
    int sourceCardIndex;

    private void begindragdrop(MouseEvent mouseEvent) {
        ImageView sender = (ImageView)mouseEvent.getSource();
        xPixelsInside = mouseEvent.getSceneX() - sender.getLayoutX();
        yPixelsInside = mouseEvent.getSceneY() - sender.getLayoutY();
        sourcePileIndex = senderPileIndex((ImageView)mouseEvent.getSource());
        sourceCardIndex = -1;
        if(sourcePileIndex < 7 && sourcePileIndex >= 0) // stack
            sourceCardIndex = senderStackCardIndex(sourcePileIndex, sender);
        idLabel.setText(sourcePileIndex + ":" + sourceCardIndex);
        calculateDropSites();
        sender.toFront();
        if(sourceCardIndex >= 0)
            for(int i = sourceCardIndex + 1;  i < cardStacks.get(sourcePileIndex).size(); i++)
                cardStacks.get(sourcePileIndex).get(i).toFront();
        if(sourcePileIndex >= 0)
            dropsiteCenters.set(sourcePileIndex, null); // prevent highlighting of source pile
        this.dragdrop = true;
    }

    private void dragdrop(MouseEvent mouseEvent) {
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

    private void enddragdrop(MouseEvent mouseEvent) {
        ImageView source = ((ImageView)mouseEvent.getSource());
        this.dragdrop = false;
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

        draw();
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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // set listeners on window resize
        game = new Game();
        Card c;
        CardStack stack;
        FaceDownPile pile = game.getFaceDownPile();
        playingTable.heightProperty().addListener(e -> draw());
        playingTable.widthProperty().addListener(e -> draw());
        /* FaceDownPile */
        faceDownPilePlaceholder = new ImageView(CardImages.get().cardImages.get("X(X)"));
        faceDownPilePlaceholder.setOnMouseClicked(this::turnOver);
        playingTable.getChildren().add(faceDownPilePlaceholder);
        for(int i = 23; i >= 0; i--){
            c = pile.forcePeek(i);
            ImageView card = new ImageView(CardImages.get().cardImages.get(Card.valueToString[c.value()] + "(" + c.color() + ")"));
            card.setOnMousePressed(this::begindragdrop);
            card.setOnMouseDragged(this::dragdrop);
            card.setOnMouseReleased(this::enddragdrop);
            faceDownPile.add(card);
            card.setVisible(false);
            playingTable.getChildren().add(card);
        }
        /* SourcePile */
        sourcePilePlaceholder = new ImageView(CardImages.get().cardPlaceholder);
        playingTable.getChildren().add(sourcePilePlaceholder);
        /* Foundations */
        for(int i = 0; i < 4; i++){
            targetPlaceholders.add(new ImageView(CardImages.get().cardPlaceholder));
            cardTargetPiles.add(new ArrayList<>(13));
            playingTable.getChildren().add(targetPlaceholders.get(i));
        }
        /* Stacks */
        for(int i = 0; i < 7; i++) {
            stackPlaceholders.add(new ImageView(CardImages.get().cardPlaceholder));
            playingTable.getChildren().add(stackPlaceholders.get(i));
            // getcardStack(index), getFoundationPile(index), getSourcePile(), getFaceDownPile()
            stack = game.getCardStack(i);
            ArrayList<ImageView> cardStack = new ArrayList<>(19);
            for(int j = 0; j < i + 1; j++) {
                c = stack.forcePeek(j); // forcePeek to override game rules
                ImageView card = new ImageView(CardImages.get().cardImages.get(c.toString()));
                cardStack.add(card);
                if(c.toString() != "X(X)") {
                    card.setOnMousePressed(this::begindragdrop);
                    card.setOnMouseDragged(this::dragdrop);
                    card.setOnMouseReleased(this::enddragdrop);
                }
                playingTable.getChildren().add(card);
            }
            cardStacks.add(cardStack);
        }
        /*for(int i = 4; i < 7; i++)
            cardStacks.add(new ArrayList<>(13));*/
        for(int i = 0; i < 11; i++) {
            ImageView site;
            site = new ImageView(CardImages.get().cardHoverOverlay);
            dropSites.add(site);
            site.setVisible(false);
            playingTable.getChildren().add(site);
        }
    }

    private void turnOver(MouseEvent mouseEvent){
        game.turnCard();
        ImageView c;
        if(faceDownPile.isEmpty()) {
            int size = sourcePileCards.size();
            for (int i = 0; i < size; i++) {
                c = sourcePileCards.remove(0);
                faceDownPile.add(c);
                c.setVisible(false);
            }
            faceDownPilePlaceholder.setImage(CardImages.get().cardImages.get("X(X)"));
        } else {
            c = faceDownPile.remove(0);
            sourcePileCards.add(c);
            c.setVisible(true);
            if(faceDownPile.size() == 0)
                faceDownPilePlaceholder.setImage(CardImages.get().cardPlaceholder);
        }
        draw();
    }

    public void setLoadFile(File f) {
        loadFile = f;
    }

    public void cancelGameClick(ActionEvent actionEvent) {
        OnGameExit(); // fire game exit event
    }

    public void undo(ActionEvent actionEvent) {
        game.undo();
        draw();
    }

    private void draw() {
        int height = (int)playingTable.getHeight();
        int width = (int)playingTable.getWidth();
        maxSizeX = (width - SPACING) / 7 - SPACING;
        maxSizeY = (height - 3 * SPACING) / 3;
        if(maxSizeX * RATIO > maxSizeY) {
            cardHeight = maxSizeY;
            cardWidth = (int)(maxSizeY / RATIO);
        } else {
            cardHeight = (int)(maxSizeX * RATIO);
            cardWidth = maxSizeX;
        }
       // idLabel.setText(""+maxSizeX);
        int localShift = (maxSizeX - cardWidth) / 2;
        xCoords = getXcoords(width);
        for(int i = 0; i < 7; i++) {
            xCoords[i] += localShift;
        }
        yCoords = getYcoords(height, cardHeight);
        faceDownPilePlaceholder.setFitHeight(cardHeight);
        faceDownPilePlaceholder.setFitWidth(cardWidth);
        faceDownPilePlaceholder.setLayoutY(SPACING + (maxSizeY - cardHeight) / 2);
        faceDownPilePlaceholder.setLayoutX(xCoords[0]);
        sourcePilePlaceholder.setFitHeight(cardHeight);
        sourcePilePlaceholder.setFitWidth(cardWidth);
        sourcePilePlaceholder.setLayoutY(SPACING + (maxSizeY - cardHeight) / 2);
        sourcePilePlaceholder.setLayoutX(xCoords[1]);
        for(int i = 0; i < 7; i++)
            drawStack(i);
        for(int i = 0; i < 4; i++)
            drawTargetPile(i);
        drawSourcePile();
    }

    private void drawStack(int i) {
        stackPlaceholders.get(i).setFitWidth(cardWidth);
        stackPlaceholders.get(i).setFitHeight(cardHeight);
        stackPlaceholders.get(i).setLayoutX(xCoords[i]);
        stackPlaceholders.get(i).setLayoutY(yCoords[0]);
        CardStack stack = game.getCardStack(i);
        for(int j = 0; j < cardStacks.get(i).size(); j++) {
            String filename = stack.forcePeek(j).toString();
            ImageView img = cardStacks.get(i).get(j);
            img.setLayoutX(xCoords[i]);
            img.setLayoutY(yCoords[j]);
            img.setFitHeight(cardHeight);
            img.setFitWidth(cardWidth);
            img.toFront();
            if (img.getImage() != CardImages.get().cardImages.get(filename)) {
                img.setOnMousePressed(this::begindragdrop);
                img.setOnMouseDragged(this::dragdrop);
                img.setOnMouseReleased(this::enddragdrop);
                img.setImage(CardImages.get().cardImages.get(filename));
            }
        }
        ImageView site = dropSites.get(i);
        site.setFitHeight(cardHeight);
        site.setFitWidth(cardWidth);
    }

    private void drawTargetPile(int i) {
        ImageView img = targetPlaceholders.get(i);
        img.setLayoutX(xCoords[i+3]);
        img.setLayoutY(SPACING + (maxSizeY - cardHeight) / 2);
        img.setFitWidth(cardWidth);
        img.setFitHeight(cardHeight);
        FoundationPile pile = game.getFoundationPile(i);
        for(int j = 0; j < cardTargetPiles.get(i).size(); j++) {
            String filename = pile.forcePeek(j).toString();
            ImageView card;
            card = cardTargetPiles.get(i).get(j);
            card.toFront();
            card.setLayoutY(SPACING + (maxSizeY - cardHeight) / 2);
            card.setLayoutX(xCoords[i+3]);
            card.setFitWidth(cardWidth);
            card.setFitHeight(cardHeight);
            if (img.getImage() != CardImages.get().cardImages.get(filename))
                img.setImage(CardImages.get().cardImages.get(filename));
        }
        ImageView site = dropSites.get(i+7);
        site.setFitHeight(cardHeight);
        site.setFitWidth(cardWidth);
    }

    private void drawSourcePile() {
        sourcePilePlaceholder.setLayoutX(xCoords[1]);
        sourcePilePlaceholder.setLayoutY(SPACING + (maxSizeY - cardHeight) / 2);
        sourcePilePlaceholder.setFitWidth(cardWidth);
        sourcePilePlaceholder.setFitHeight(cardHeight);
        SourcePile pile = game.getSourcePile();
        for (int i = 0; i < sourcePileCards.size(); i++) {
            ImageView sourcePileCard = sourcePileCards.get(i);
            String filename = pile.forcePeek(i).toString();
            sourcePileCard.toFront();
            sourcePileCard.setLayoutY(SPACING + (maxSizeY - cardHeight) / 2);
            sourcePileCard.setLayoutX(xCoords[1]);
            sourcePileCard.setFitWidth(cardWidth);
            sourcePileCard.setFitHeight(cardHeight);
            if (sourcePileCard.getImage() != CardImages.get().cardImages.get(filename))
                sourcePileCard.setImage(CardImages.get().cardImages.get(filename));
        }
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
