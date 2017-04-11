package ija2017.model;

public class Card implements java.io.Serializable {
    private Color color;
    private int value;
    private boolean isUp = false;
    private static String valueToString[] = {"", "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};

    public Card(Card.Color color, int value){
        this.value = value;
        this.color = color;
    }

    public int compareValue(Card c){
        return this.value - c.value();
    }

    public boolean isTurnedFaceUp(){
        return this.isUp;
    }

    public boolean turnFaceUp(){
        if(!this.isUp){
            this.isUp = true;
            return true;
        }
        return false;
    }

    public boolean turnFaceDown() {
        if(this.isUp){
            this.isUp = false;
            return true;
        }
        return false;
    }

    public boolean similarColorTo(Card card){
        if(this.color == Color.HEARTS || this.color == Color.DIAMONDS) {
            if (card.color() == Color.HEARTS || card.color() == Color.DIAMONDS)
                return true;
        }
        else
        if(card.color() == Color.CLUBS || card.color() == Color.SPADES)
            return true;
        return false;
    }

    public Card.Color color(){
        return this.color;
    }

    public int value(){
        return this.value;
    }

    public String toString() {
        if(!this.isUp)
            return "X(X)";
        return valueToString[this.value] + "(" + color.toString() + ")";
    }

    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        Card card = (Card) object;

        if (value != card.value) return false;
        if (color != card.color) return false;
        return true;
    }

    public int hashCode() {
        int result = color.hashCode();
        result = 31 * result + value;
        return result;
    }

    enum Color{
        CLUBS("C"), DIAMONDS("D"), HEARTS("H"), SPADES("S");

        private String value;

        Color(String value){
            this.value = value;
        }

        public String toString(){
            return this.value;
        }
    }
}
