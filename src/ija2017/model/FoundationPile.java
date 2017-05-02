package ija2017.model;

/**
 * Class representing single foundation deck of cards.
 * @author Adam Suba (xsubaa00)
 */
public class FoundationPile extends CardDeck {
    /**
     * Puts single card onto foundation. Respects rules of foundation. First Ace may be of any color.
     * @param c Card to be inserted.
     * @return True if card was inserted, false otherwise.
     */
    @Override
    public boolean put(Card c){
        if (this.deck.isEmpty())
            return c.value() == 1 && super.put(c);
        if(this.deck.peekLast().value() + 1 == c.value() && this.deck.peekLast().color() == c.color())
            return super.put(c);
        return false;
    }

    /**
     * Checks whether card can be placed on foundation deck.
      * @param c Card to be checked.
     * @return True if it is possible, false otherwise.
     */
    boolean canAccept(Card c){
        if(c == null)
            return false;
        if(this.deck.isEmpty())
            return c.value() == 1;
        if(this.deck.peekLast().value() + 1 == c.value() && this.deck.peekLast().color() == c.color())
            return true;
        return false;
    }
}
