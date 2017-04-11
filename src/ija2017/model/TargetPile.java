package ija2017.model;

/**
 * Created by xsubaa00 on 10/04/17.
 */
public class TargetPile extends CardPile {
    private Card.Color pileColor;
    private int sequence;

    public TargetPile(){
        sequence = 1;
    }

    @Override
    public boolean put(Card c){
        if(this.sequence == c.value()) {
            if (this.pileColor == null) {
                this.pileColor = c.color();
            }
            else if(this.pileColor != c.color())
                return false;
            if(super.put(c)) {
                this.sequence++;
                return true;
            }
        }
        return false;
    }

    public boolean canAccept(Card c){
        if(this.sequence == c.value()) {
            if (this.pileColor == null)
                return true;
            else if(this.pileColor != c.color())
                return false;
            return true;
        }
        return false;
    }
}
