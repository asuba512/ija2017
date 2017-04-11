package ija2017.GUI.GameGUI;

import javafx.scene.image.Image;

public class CardImages {
    Image[][] cardImages = new Image[4][13];
    Image cardPlaceholder = new Image(getClass().getResource("img/card_placeholder.png").toExternalForm());
    Image cardHoverOverlay = new Image(getClass().getResource("img/card_overlay.png").toExternalForm());
    private static CardImages ourInstance = new CardImages();

    public static CardImages get() {
        return ourInstance;
    }

    private CardImages() {
        final String cardNames[] = {"A", "2", "3" , "4", "5", "6",
                "7", "8", "9", "10", "J", "Q", "K"};
        final String[] colors = {"C", "S", "H", "D"};

        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 13; j++) {
                cardImages[i][j] = new Image(getClass().getResource("img/card_"+cardNames[j]+"("+colors[i]+").png").toExternalForm());
            }
        }
    }
}
