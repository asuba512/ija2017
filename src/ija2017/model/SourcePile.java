package ija2017.model;

import java.util.LinkedList;

/**
 * Created by xsubaa00 on 10/04/17.
 */
public class SourcePile extends CardPile {
    @Override
    public boolean put(Card c){
        if(super.put(c)) {
            this.deck.peekLast().turnFaceUp();
            return true;
        }
        else return false;
    }
}
