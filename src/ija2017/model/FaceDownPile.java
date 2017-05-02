package ija2017.model;

import java.util.LinkedList;

/**
 * Class representing remainder of cards that are not dealt onto tableau.
 * @author Adam Suba (xsubaa00)
 */
public class FaceDownPile extends CardDeck {
    /**
     * Inserts single card into this deck. Turns card face down.
     * @param c Card to be inserted.
     * @return True if card was inserted, false otherwise.
     */
    @Override
    public boolean put(Card c) {
        c.turnFaceDown();
        return super.put(c);
    }

    /**
     * Inserts whole list of cards into this deck. Used during game initialization, after all cards are dealt.
     * @param cards List of cards.
     * @return  True. Return value was left to be consistent with other put() methods.
     */
    boolean put(LinkedList<Card> cards){
        this.deck = cards;
        return true;
    }
}
