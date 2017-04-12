package ija2017.model;

import java.util.LinkedList;

public class CardDeck {
    protected LinkedList<Card> deck;

    public CardDeck(){
        this.deck = new LinkedList<>();
    }

    public boolean put(Card c){
        return this.deck.offerLast(c);
    }

    public Card remove(){
        return this.deck.pollLast();
    }

    public Card peek(){
        return this.deck.peekLast();
    }

    public Card forcePeek(int index){
        return this.deck.get(index);
    }

    public int size(){
        return this.deck.size();
    }

    public boolean isEmpty(){
        return this.deck.isEmpty();
    }

    /*
    Creates numberOfDecks decks of cards
     */
    static LinkedList<Card> createDeck(){
        LinkedList<Card> deck = new LinkedList<>();
        for(int i = 1; i <= 13; i++){
            deck.add(new Card(Card.Color.CLUBS, i));
            deck.add(new Card(Card.Color.DIAMONDS, i));
            deck.add(new Card(Card.Color.SPADES, i));
            deck.add(new Card(Card.Color.HEARTS, i));
        }
        return deck;
    }

    @Override
    public String toString() {
        return this.deck.toString();
    }
}
