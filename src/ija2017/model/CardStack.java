package ija2017.model;

public class CardStack extends CardDeck {

    public void flipTopCard(){
        this.deck.peekLast().turnFaceUp();
    }
}
