package ija2017.model;

import java.io.Serializable;
import java.util.LinkedList;

/**
 * Class representing general card deck
 * @author Adam Suba (xsubaa00)
 */
public class CardDeck implements Serializable {
    /** Represents deck of cards */
    LinkedList<Card> deck;

    /**
     * Construction initializes LinkedList that stores cards.
     */
    public CardDeck(){
        this.deck = new LinkedList<>();
    }

    /**
     * Inserts card to the end of deck. Without any restrictions.
     * @param c Card to be inserted.
     * @return True if card was successfully inserted, false if not.
     */
    public boolean put(Card c){
        return this.deck.offerLast(c);
    }

    /**
     * Removes card from the end of deck. Without any restrictions.
     * @return Removed card.
     */
    Card remove(){
        return this.deck.pollLast();
    }

    /**
     * Returns last card in deck without removing it. Without any restrictions.
     * @return Last card in deck.
     */
    Card peek(){
        return this.deck.peekLast();
    }

    /**
     * Returns card at specified index. Useful in for building UI.
     * @param index Index of card to be return.
     * @return Card at specified index.
     */
    public Card forcePeek(int index){
        return this.deck.get(index);
    }

    /**
     * Returns size of card deck.
     * @return Size of deck.
     */
    public int size(){
        return this.deck.size();
    }

    /**
     * Checks whether is deck empty.
     * @return True if it is empty, false if not.
     */
    public boolean isEmpty(){
        return this.deck.isEmpty();
    }

    /**
     * Initializes ordered deck of 52 cards. Used for generating cards before dealing.
     * @return List of cards.
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
