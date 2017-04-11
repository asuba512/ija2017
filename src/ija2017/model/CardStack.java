package ija2017.model;

public class CardStack extends CardDeck {

    public boolean flipTopCard(){
        return this.deck.peekLast().turnFaceUp();
    }

    @Override
    public boolean put(Card c){
        if(!c.isTurnedFaceUp()) // inserting cards during game initialization
            return super.put(c);
        else{ // inserting cards from SourcePile or from TargetPile
            if(this.deck.peekLast().value() - 1 == c.value() && !c.similarColorTo(this.deck.peekLast()))
                return super.put(c);
            else return false;
        }
    }

    public CardStack peek(int index){
        return new CardStack(); // TODO
    }

    public boolean canAccept(Card c) {
        if(this.deck.peekLast().value() - 1 == c.value() && !c.similarColorTo(this.deck.peekLast()))
            return true;
        else return false;
    }

    public boolean canAccept(CardStack cs){
        return false; // TODO
    }
}
