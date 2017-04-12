package ija2017.GameGUI;

import ija2017.model.Card;
import ija2017.model.CardStack;
import ija2017.model.Game;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
    private ImageView faceDownPile;
    private ImageView sourcePilePlaceholder;
    private ArrayList<ImageView> sourceDeckCards = new ArrayList<>(54);
    private ArrayList<ImageView> sourcePileCards = new ArrayList<>(54);
    private int[] xCoords;
    private int[] yCoords;
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

    private void begindragdrop(MouseEvent mouseEvent) {
        ImageView sender = (ImageView)mouseEvent.getSource();
        xPixelsInside = mouseEvent.getSceneX() - sender.getLayoutX();
        yPixelsInside = mouseEvent.getSceneY() - sender.getLayoutY();
        calculateDropSites();
        sender.toFront();
        sourcePileIndex = senderPileIndex((ImageView)mouseEvent.getSource());
        if(sourcePileIndex >= 0)
            dropsiteCenters.set(sourcePileIndex, null); // prevent higlighting of source pile
        this.dragdrop = true;
    }

    private void dragdrop(MouseEvent mouseEvent) {
        ImageView source = ((ImageView)mouseEvent.getSource());
        source.setLayoutX(mouseEvent.getSceneX() - xPixelsInside);
        source.setLayoutY(mouseEvent.getSceneY() - yPixelsInside);
        int overlayIndex = getOverlayIndex(new Point((int)(source.getLayoutX() + cardWidth / 2), (int)(source.getLayoutY() + cardHeight /2)));
        idLabel.setText(overlayIndex + "");
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
        idLabel.setText(targetIndex + "");
        boolean moved = false;
        if(targetIndex >= 0) {
            if(targetIndex < 7) {
                if(cardStacks.get(targetIndex).size() < 19) {
                    cardStacks.get(targetIndex).add(source);
                    moved = true;
                }
            } else if(targetIndex > 6 && targetIndex < 11) {
                if(cardTargetPiles.get(targetIndex - 7).size() < 13) {
                    cardTargetPiles.get(targetIndex - 7).add(source);
                    moved = true;
                }
            }
        }
        if(moved) {
            if(sourcePileIndex >= 0 && sourcePileIndex < 7)
                cardStacks.get(sourcePileIndex).remove(source);
            else if(sourcePileIndex >= 7) {
                cardTargetPiles.get(sourcePileIndex - 7).remove(source);
            }
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
        Random rnd = new Random(System.nanoTime());
        for(int i = 0; i < 7; i++) {
            if(rnd.nextInt(10) >= 4) {
                site = dropSites.get(i);
                //site.setVisible(true);
                site.toFront();
                site.setLayoutX(xCoords[i]);
                if (cardStacks.get(i).size() == 0)
                    site.setLayoutY(yCoords[0]);
                else
                    site.setLayoutY(yCoords[cardStacks.get(i).size() - 1]);

                dropsiteCenters.add(new Point(xCoords[i] + cardWidth / 2, (int) site.getLayoutY() + cardHeight / 2));
            }
            else
                dropsiteCenters.add(null);
        }
        for(int i = 7; i < 11; i++) {
            if(rnd.nextInt(10) >= 4) {
                site = dropSites.get(i);
                //site.setVisible(true);
                site.toFront();
                site.setLayoutX(xCoords[i - 4]);
                site.setLayoutY(SPACING + (maxSizeY - cardHeight) / 2);
                dropsiteCenters.add(new Point((xCoords[i - 4] + cardWidth / 2), (SPACING + (maxSizeY - cardHeight) / 2) + cardHeight / 2));
            }
            else
                dropsiteCenters.add(null);
        }
    }

    private int senderPileIndex(ImageView sender) {
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

    /********** Drag&drop logic end *********/

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // set listeners on window resize
        game = new Game();
        Card c;
        CardStack stack;
        playingTable.heightProperty().addListener(e -> draw());
        playingTable.widthProperty().addListener(e -> draw());
        faceDownPile = new ImageView(CardImages.get().cardImages.get("X(X)"));
        playingTable.getChildren().add(faceDownPile);
        sourcePilePlaceholder = new ImageView(CardImages.get().cardPlaceholder);
        playingTable.getChildren().add(sourcePilePlaceholder);
        for(int i = 0; i < 4; i++){
            targetPlaceholders.add(new ImageView(CardImages.get().cardPlaceholder));
            cardTargetPiles.add(new ArrayList<>(13));
            playingTable.getChildren().add(targetPlaceholders.get(i));
        }
        for(int i = 0; i < 7; i++) {
            stackPlaceholders.add(new ImageView(CardImages.get().cardPlaceholder));
            playingTable.getChildren().add(stackPlaceholders.get(i));
            // getcardStack(index), getFoundationPile(index), getSourcePile(), getFaceDownPileSize()
            stack = game.getCardStack(i);
            ArrayList<ImageView> cardStack = new ArrayList<>(19);
            for(int j = 0; j < i + 1; j++) {
                c = stack.forcePeek(j); // forcePeek to override game rules
                // card.setImage(CardImages.get().cardImages.get(stack.forcePeek(index).toString()))
                ImageView card = new ImageView(CardImages.get().cardImages.get(c.toString()));
                cardStack.add(card);
                card.setOnMousePressed(this::begindragdrop);
                card.setOnMouseDragged(this::dragdrop);
                card.setOnMouseReleased(this::enddragdrop);
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

    public void setLoadFile(File f) {
        loadFile = f;
    }

    public void cancelGameClick(ActionEvent actionEvent) {
        OnGameExit(); // fire game exit event
    }

    public void testClick(ActionEvent actionEvent) {

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
        faceDownPile.setFitHeight(cardHeight);
        faceDownPile.setFitWidth(cardWidth);
        faceDownPile.setLayoutY(SPACING + (maxSizeY - cardHeight) / 2);
        faceDownPile.setLayoutX(xCoords[0]);
        sourcePilePlaceholder.setFitHeight(cardHeight);
        sourcePilePlaceholder.setFitWidth(cardWidth);
        sourcePilePlaceholder.setLayoutY(SPACING + (maxSizeY - cardHeight) / 2);
        sourcePilePlaceholder.setLayoutX(xCoords[1]);
        for(int i = 0; i < 7; i++)
            drawStack(i);
        for(int i = 0; i < 4; i++)
            drawTargetPile(i);
    }

    private void drawStack(int i) {
        stackPlaceholders.get(i).setFitWidth(cardWidth);
        stackPlaceholders.get(i).setFitHeight(cardHeight);
        stackPlaceholders.get(i).setLayoutX(xCoords[i]);
        stackPlaceholders.get(i).setLayoutY(yCoords[0]);
        //CardStack stack = game.getCardStack(i);
        for(int j = 0; j < cardStacks.get(i).size(); j++) {
            //String filename = stack.forcePeek(j).toString();
            ImageView img = cardStacks.get(i).get(j);
            img.setLayoutX(xCoords[i]);
            img.setLayoutY(yCoords[j]);
            img.setFitHeight(cardHeight);
            img.setFitWidth(cardWidth);
            img.toFront();
//            if (img.getImage() != CardImages.get().cardImages.get(filename))
//                img.setImage(CardImages.get().cardImages.get(filename));
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
        for(int j = 0; j < cardTargetPiles.get(i).size(); j++) {
            ImageView card;
            card = cardTargetPiles.get(i).get(j);
            card.toFront();
            card.setLayoutY(SPACING + (maxSizeY - cardHeight) / 2);
            card.setLayoutX(xCoords[i+3]);
            card.setFitWidth(cardWidth);
            card.setFitHeight(cardHeight);
        }
        ImageView site = dropSites.get(i+7);
        site.setFitHeight(cardHeight);
        site.setFitWidth(cardWidth);
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
        int step = (arr[12] - arr[0]) / 12; // interpolation step
        for(int i = 1; i < 19; i++)
            arr[i] = arr[0] + i * step;
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
