package ija2017.model;

import java.util.LinkedList;

public class CardStack extends CardDeck {

    public boolean turnFaceUpTopCard(){
        if(this.deck.isEmpty()) return false;
        return this.deck.peekLast().turnFaceUp();
    }

    public boolean turnFaceDownTopCard(){
        if(this.deck.isEmpty()) return false;
        return this.deck.peekLast().turnFaceDown();
    }

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

    public boolean put(LinkedList<Card> cs){
        while(!cs.isEmpty())
            if(!this.deck.offerLast(cs.removeLast()))
                return false;
        return true;
    }

    public boolean forcePut(Card c){
        return super.put(c);
    }

    public LinkedList<Card> remove(int index){
        LinkedList<Card> stack = new LinkedList<>();
        for(int i = this.deck.size() - 1; i >= index; i--)
            stack.offerLast(this.deck.remove(i));
        return stack;
    }

    /* index must be checked before */
    public Card peek(int index){
        if(this.deck.get(index).isTurnedFaceUp())
            return this.deck.get(index);
        return null;
    }

    public boolean canAccept(Card c) {
        if(c == null)
            return false;
        if(this.deck.isEmpty())
            return c.value() == 13;
        if(this.deck.peekLast().value() - 1 == c.value() && !c.similarColorTo(this.deck.peekLast()))
            return true;
        return false;
    }

    public int getFirstTurnedFaceUpIndex() {
        int size = this.size();
        for(int i = 0; i < size; i++) {
            if(this.deck.get(i).isTurnedFaceUp())
                return i;
        }
        return size; // card with this index is certainly not in the stack nor has any followers
    }
}
