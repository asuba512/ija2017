package ija2017.model;

import java.io.Serializable;

/**
 * Class representing single Card
 * @author Adam Suba (xsubaa00)
 */

public class Card implements Serializable {
    /** Holds card's color */
    private Color color;
    /** Holds card's value */
    private int value;
    /** Holds card's orientation (face up/face down) */
    private boolean isUp = false;

    /** Holds string representation of card value */
    private static final String valueToString[] = {"", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    /** Holds long string representation of card value */
    private static final String valueToHumanReadableString[] = {"", "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King"};

    /**
     * Constructor sets card's color and value.
     * @param color card's color
     * @param value card's value
     */
    public Card(Card.Color color, int value){
        this.value = value;
        this.color = color;
    }

    /**
     * Checks whether card is turned up
     * @return True if card is faced up, false if faced down.
     */
    boolean isTurnedFaceUp(){
        return this.isUp;
    }

    /**
     * Turns card face up.
     * @return True if card was turned, false if card was already face up.
     */
    boolean turnFaceUp(){
        if(!this.isUp){
            this.isUp = true;
            return true;
        }
        return false;
    }

    /**
     * Turns card face down.
     * @return True if card was turned, false if card was already face down.
     */
    boolean turnFaceDown() {
        if(this.isUp){
            this.isUp = false;
            return true;
        }
        return false;
    }

    /**
     * Checks whether card has similar color to another card.
     * @param card Card to be compared to.
     * @return True if cards have similar color, false if not.
     */
    boolean similarColorTo(Card card){
        if(this.color == Color.HEARTS || this.color == Color.DIAMONDS) {
            if (card.color() == Color.HEARTS || card.color() == Color.DIAMONDS)
                return true;
        }
        else
        if(card.color() == Color.CLUBS || card.color() == Color.SPADES)
            return true;
        return false;
    }

    /**
     * Returns card's color.
     * @return Card.Color Color of the card.
     */
    Card.Color color(){
        return this.color;
    }

    /**
     * Returns card's value.
     * @return Value of the card.
     */
    int value(){
        return this.value;
    }

    /**
     * Converts card into string representation.
     * @return String representation of card.
     */
    public String toString() {
        if(!this.isUp)
            return "X(X)";
        return valueToString[this.value] + "(" + color.toString() + ")";
    }

    /**
     * Converts card into long string representation.
     * @return Long string representation of card.
     */
    String toHumanReadableString() {
        if(!this.isUp)
            return "X(X)";
        return valueToHumanReadableString[this.value] + " of " + color.toHumanReadableString();
    }

    /**
     * Checks whether two cards are equal.
     * @param object Object which is supposed to be checked.
     * @return True if cards are equal, false if not.
     */
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Card card = (Card) object;
        return value == card.value && color == card.color;
    }

    /**
     * Calculates hash value of card.
     * @return Hash value of card.
     */
    public int hashCode() {
        int result = color.hashCode();
        result = 31 * result + value;
        return result;
    }

    /**
     * Represents card colors.
     */
    enum Color implements Serializable {
        CLUBS("C"), DIAMONDS("D"), HEARTS("H"), SPADES("S");

        private String value;

        /**
         * Constructor sets string representation of color.
         * @param value String representation of color.
         */
        Color(String value){
            this.value = value;
        }

        /**
         * Returns string representation of color.
         * @return String representation of color.
         */
        public String toString(){
            return this.value;
        }

        /**
         * Converts String representation of color into long representation.
         * @return Long representation of color.
         */
        public String toHumanReadableString() {
            switch(this.value) {
                case "C": return "clubs";
                case "H": return "hearts";
                case "S": return "spades";
                case "D": return "diamonds";
            }
            return null;
        }
    }
}
