package ija2017.model;

/**
 * Class representing deck of cards from which player removes cards to foundation and tableau.
 * @author Adam Suba (xsubaa00)
 */
public class SourcePile extends CardDeck {
    /**
     * Inserts card into this deck. Turns the card face up.
     * @param c Card to be inserted.
     * @return True if card is inserted, false otherwise.
     */
    @Override
    public boolean put(Card c){
        if(super.put(c)) {
            this.deck.peekLast().turnFaceUp();
            return true;
        }
        else return false;
    }
}
