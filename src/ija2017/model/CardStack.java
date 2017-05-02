package ija2017.model;

import java.util.LinkedList;

/**
 * Class representing single stack of cards on tableau.
 * @author Adam Suba (xsubaa00)
 */
public class CardStack extends CardDeck {

    /**
     * Turns card at the end of stack face up.
     * @return True if card was turned, false if deck is empty or card wasn't turned.
     */
    boolean turnFaceUpTopCard() {
        return !this.deck.isEmpty() && this.deck.peekLast().turnFaceUp();
    }

    /**
     * Turns card at the end of stack face down.
     * @return True if card was turned, false if deck is empty or card wasn't turned.
     */
    boolean turnFaceDownTopCard(){
        if(this.deck.isEmpty()) return false;
        return this.deck.peekLast().turnFaceDown();
    }

    /**
     * Inserts card into card stack. Respects rules of stacks on tableau.
     * @param c Card to be inserted.
     * @return True if card was inserted, false otherwise.
     */
    @Override
    public boolean put(Card c){
        if(!c.isTurnedFaceUp()) // inserting cards during game initialization
            return super.put(c);
        else{ // inserting cards from SourcePile or from FoundationPile
            if (this.deck.isEmpty())
                return c.value() == 13 && super.put(c);
            if(this.deck.peekLast().value() - 1 == c.value() && !c.similarColorTo(this.deck.peekLast()))
                return super.put(c);
            return false;
        }
    }

    /**
     * Inserts whole stack of cards into card stack. Respects rules of stacks on tableau.
     * @param cs Represents list of cards to be inserted. Should be obtained by CardStack.remove(int index) method.
     * @return True if cards were inserted, false otherwise.
     */
    boolean put(LinkedList<Card> cs){
        while(!cs.isEmpty())
            if(!this.deck.offerLast(cs.removeLast()))
                return false;
        return true;
    }

    /**
     * Force-inserts card indo card stack. Does not respect rules. Useful for undo operation.
     * @param c Card to be inserted.
     * @return True if card is inserted, false otherwise.
     */
    boolean forcePut(Card c){
        return super.put(c);
    }

    /**
     * Removes whole stack of cards from card stack.
     * @param index Index of first card to be removed. Expects valid index.
     * @return List of cards representing removed stack.
     */
    LinkedList<Card> remove(int index){
        LinkedList<Card> stack = new LinkedList<>();
        for(int i = this.deck.size() - 1; i >= index; i--)
            stack.offerLast(this.deck.remove(i));
        return stack;
    }

    /**
     * Peeks at card at specified index. Useful for checking whether stack starting with this card can be moved somewhere.
     * @param index Index of card to be peeked at. Expects valid index.
     * @return Card at specified index.
     */
    Card peek(int index){
        if(this.deck.get(index).isTurnedFaceUp())
            return this.deck.get(index);
        return null;
    }

    /**
     * Checks whether card can be placed at top of the stack.
     * @param c Card to be checked.
     * @return True if it is possible, false otherwise.
     */
    boolean canAccept(Card c) {
        if(c == null)
            return false;
        if(this.deck.isEmpty())
            return c.value() == 13;
        if(this.deck.peekLast().value() - 1 == c.value() && !c.similarColorTo(this.deck.peekLast()))
            return true;
        return false;
    }

    /**
     * Gets index of first card in stack that faces up.
     * @return Index of first face-up card. -1 if stack is empty or none of the cards faces up.
     */
    int getFirstTurnedFaceUpIndex() {
        if(this.isEmpty())
            return -1;
        int size = this.size();
        for(int i = 0; i < size; i++) {
            if(this.deck.get(i).isTurnedFaceUp())
                return i;
        }
        return -1; // card with this index is certainly not in the stack nor has any followers
    }
}
