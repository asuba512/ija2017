package ija2017.GameGUI;

import javafx.scene.image.Image;

import java.util.HashMap;

/**
 * Singleton holding loaded image resources.
 *
 * @author Jakub Paliesek (xpalie00)
 */
class ImageResources {
    /** holds cards that can be searched by their ingame names (from hw1/hw2 */
    HashMap<String,Image> cardImages = new HashMap<>();
    /** holds placeholder image */
    Image cardPlaceholder = new Image(getClass().getResource("img/card_placeholder.png").toExternalForm());
    /** holds highlight overlay image */
    Image cardHoverOverlay = new Image(getClass().getResource("img/card_overlay.png").toExternalForm());
    /** holds firework image used when player wins a game */
    Image firework = new Image(getClass().getResource("img/firework.png").toExternalForm());

    private static ImageResources ourInstance = new ImageResources();

    /**
     * @return instance of singleton
     */
    static ImageResources get() {
        return ourInstance;
    }

    /** constructor that is called when module is loaded */
    private ImageResources() {
        final String cardNames[] = {"A", "2", "3" , "4", "5", "6",
                "7", "8", "9", "10", "J", "Q", "K"};
        final String[] colors = {"C", "S", "H", "D"};
        cardImages.put("X(X)", new Image(getClass().getResource("img/card_back.png").toExternalForm()));
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 13; j++) {
                cardImages.put(cardNames[j] + "(" + colors[i] + ")", new Image(getClass().getResource("img/card_"+cardNames[j]+"("+colors[i]+").png").toExternalForm()));
                //cardImages[i][j] = new Image(getClass().getResource("img/card_"+cardNames[j]+"("+colors[i]+").png").toExternalForm());
            }
        }
    }
}
