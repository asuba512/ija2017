package ija2017.model;

import java.util.LinkedList;

/**
 * Created by xsubaa00 on 10/04/17.
 */
public class FaceDownPile extends CardDeck {
    @Override
    public boolean put(Card c) {
        c.turnFaceDown();
        return super.put(c);
    }

    public boolean put(LinkedList<Card> cards){
        this.deck = cards;
        return true;
    }
}
