package ija2017.model;

/**
 * Created by xsubaa00 on 10/04/17.
 */
public class TargetPile extends CardPile {
    @Override
    public boolean put(Card c){
        if (this.deck.isEmpty())
            return c.value() == 1 && super.put(c);
        if(this.deck.peekLast().value() + 1 == c.value() && this.deck.peekLast().color() == c.color())
            return super.put(c);
        return false;
    }

    public boolean canAccept(Card c){
        if(c == null)
            return false;
        if(this.deck.isEmpty())
            return c.value() == 1;
        if(this.deck.peekLast().value() + 1 == c.value() && this.deck.peekLast().color() == c.color())
            return true;
        return false;
    }
}
