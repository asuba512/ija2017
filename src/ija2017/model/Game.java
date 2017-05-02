package ija2017.model;

import java.io.*;
import java.util.*;

/**
 * Class representing a single game. Holds the state of a game and implements commands to play the game.
 */
public class Game implements Serializable {
    /** Array of CardStacks representing the tableau. */
    private CardStack[] cardStack = new CardStack[7];
    /** Array of FoundationPiles representing the foundation. */
    private FoundationPile[] foundationPile = new FoundationPile[4];
    /** Holds cards turned over from faceDownPile. */
    private SourcePile sourcePile;
    /** Holds cards left after dealing. */
    private FaceDownPile faceDownPile;
    /** Holds seed used to shuffle cards. */
    private String seed;
    /** Holds scored points. */
    private long points;
    /** Holds number of remaining redeals. */
    private int redealsLeft = 2;

    /** Instance of invoker for commands. */
    private final Commander.Invoker invoker = new Commander.Invoker();

    /**
     * Constructs game with specified seed string.
     * @param seed String used as seed. Uses hashCode() function of String class to convert to number.
     */
    public Game(String seed){
        this.seed = seed;
        LinkedList<Card> deck = CardDeck.createDeck();
        Collections.shuffle(deck, new Random((long)seed.hashCode())); // shuffle created deck
        initGame(deck);
    }

    /**
     * Constructs game with seed from current time.
     */
    public Game(){
        this(Long.toString(System.nanoTime()));
    }

    /**
     * Initializes game. Deals generated shuffled cards onto tableau.
     * @param deck Generated shuffled cards.
     */
    private void initGame(LinkedList<Card> deck){
        this.sourcePile = new SourcePile();
        this.faceDownPile = new FaceDownPile();
        for(int i = 0; i < 4; i++)
            this.foundationPile[i] = new FoundationPile();
        for(int i = 0; i < 7; i++) {
            this.cardStack[i] = new CardStack();
            for (int j = 0; j < i + 1; j++)
                this.cardStack[i].put(deck.pop());
            this.cardStack[i].turnFaceUpTopCard(); // flip top card face up
        }
        this.faceDownPile.put(deck);
    }

    /**
     * Converts game into string representation. For debug purposes.
     * @return String representation of game.
     */
    @Override
    public String toString(){
        StringBuilder game = new StringBuilder("FaceDownPile: " + this.faceDownPile + "\n" +
                "SourcePile: " + this.sourcePile + "\n" +
                "FoundationPile 1: " + this.foundationPile[0] + "\n" +
                "FoundationPile 2: " + this.foundationPile[1] + "\n" +
                "FoundationPile 3: " + this.foundationPile[2] + "\n" +
                "FoundationPile 4: " + this.foundationPile[3] + "\n");
        for(int i = 0; i < 7; i++)
            game.append(this.cardStack[i]).append("\n");
        return game.toString();
    }

    /**
     * Checks whether card at the top of source pile can be placed onto target foundation.
     * @param targetFoundation Self explanatory.
     * @return True if the move is possible, false otherwise.
     */
    public boolean canMoveToFoundation(int targetFoundation){
        if(this.sourcePile.isEmpty())
            return false;
        return this.foundationPile[targetFoundation].canAccept(this.sourcePile.peek());
    }

    /**
     * Checks whether card at the top of specified foundation can be placed onto target foundation.
     * @param targetFoundation Self explanatory.
     * @param foundation Source foundation of the card to be moved.
     * @return True if the move is possible, false otherwise.
     */
    public boolean canMoveToFoundation(int targetFoundation, int foundation){
        if(this.foundationPile[foundation].isEmpty())
            return false;
        return this.foundationPile[targetFoundation].canAccept(this.foundationPile[foundation].peek());
    }

    /**
     * Checks whether stack of cards at specified index and stack on the tableau can be placed onto foundation.
     * @param targetFoundation Self explanatory.
     * @param stack Source stack on the tableau.
     * @param index Index of first card of the stack to be moved.
     * @return True if the move is possible, false otherwise.
     */
    public boolean canMoveToFoundation(int targetFoundation, int stack, int index){
        if(this.cardStack[stack].isEmpty())
            return false;
        if(index != this.cardStack[stack].size() - 1) // foundation can accept only single card
            return false;
        return this.foundationPile[targetFoundation].canAccept(this.cardStack[stack].peek());
    }

    /**
     * Checks whether card from source pile can be moved to stack.
     * @param targetStack Self explanatory.
     * @return True if the move is possible, false otherwise.
     */
    public boolean canMoveToStack(int targetStack){
        if(this.sourcePile.isEmpty())
            return false;
        return this.cardStack[targetStack].canAccept(this.sourcePile.peek());
    }

    /**
     * Checks wheter card from foundation can be moved to stack.
     * @param targetStack Self explanatory.
     * @param foundation Source foundation of the card to be moved.
     * @return True if the move is possible, false otherwise.
     */
    public boolean canMoveToStack(int targetStack, int foundation){
        if(this.foundationPile[foundation].isEmpty())
            return false;
        return this.cardStack[targetStack].canAccept(this.foundationPile[foundation].peek());
    }

    /**
     * Checks whether stack of cards can be moved to another stack.
     * @param targetStack Self explanatory.
     * @param stack Source stack on the tableau.
     * @param index Index of the first card to be moved.
     * @return True if the move is possible, false otherwise.
     */
    public boolean canMoveToStack(int targetStack, int stack, int index){
        if(this.cardStack[stack].isEmpty() || this.cardStack[stack].size() <= index)
            return false;
        return this.cardStack[targetStack].canAccept(this.cardStack[stack].peek(index));
    }

    /**
     * Generates list of hints in the current state of the game.
     * @return List of hints.
     */
    public List<String> getHints() {
        LinkedList<String> hints = new LinkedList<>();
        // source -> foundation
        for(int i = 0; i < 4; i++) {
            if(canMoveToFoundation(i)) {
                hints.add("Move card from source deck to foundation.");
                break; // avoid multiple foundation hint
            }
        }

        // source -> stack
        for(int i = 0; i < 7; i++) {
            if(canMoveToStack(i)) {
                hints.add("Move card from source deck to stack number " + (i+1) + ".");
            }
        }

        // stack -> foundation (switched inner and outer cycles to avoid hint duplication to multiple foundations)
        for(int s = 0; s < 7; s++) {
            for(int f = 0; f < 4; f++) {
                if (canMoveToFoundation(f, s, this.cardStack[s].size() - 1)) {
                    hints.add("Move card from stack number " + (s + 1) + " to foundation.");
                }
            }
        }

        // stack -> stack
        for(int target = 0; target < 7; target++) {
            for(int source = 0; source < 7; source++) {
                if(source == target)
                    continue;
                int card;
                if((card = this.cardStack[source].getFirstTurnedFaceUpIndex()) == -1)
                    continue;
                if(card == 0 && this.cardStack[target].size() == 0)
                    continue; // eliminate moving kings between empty stacks
                if(canMoveToStack(target, source, card))
                    hints.add("Move " + this.cardStack[source].forcePeek(card).toHumanReadableString()
                            + " from stack number " + (source+1) + " to stack number " + (target+1) + ".");
            }
        }

        // new card
        if(!this.faceDownPile.isEmpty())
            hints.add("Try drawing new card from deck.");
        else if(!this.sourcePile.isEmpty() && this.redealsLeft > 0)
            hints.add("Try turning the source pile.");

        // foundation -> stack
        for(int s = 0; s < 7; s++) {
            for(int f = 0; f < 4; f++) {
                if(canMoveToStack(s, f)) {
                    hints.add("Try to move " + this.foundationPile[f].peek().toHumanReadableString() + " from foundation to stack number " + (s+1) + ".");
                }
            }
        }
        return hints;
    }

    /* COMMANDS */

    /**
     * Method calling undo on the invoker.
     */
    public void undo(){
        this.invoker.undo();
    }

    /**
     * Command that turns single card from faceDownPile to sourcePile.
     * If faceDownPile is empty, redeals sourcePile back to faceDownPile instead.
     */
    public void turnCard(){
        if(this.faceDownPile.isEmpty()) {
            if(this.redealsLeft > 0) {
                turnOverSource();
                redealsLeft--;
            }
            return;
        }
        SourcePile sp = this.sourcePile;
        FaceDownPile fp = this.faceDownPile;
        invoker.execute(new Commander.Command() {
            @Override
            public void execute() {
                sp.put(fp.remove());
            }

            @Override
            public void undo() {
                fp.put(sp.remove());
            }
        });
    }

    /**
     * Command that redeals cards back from sourcePile to faceDownPile.
     */
    private void turnOverSource() {
        SourcePile sp = this.sourcePile;
        FaceDownPile fp = this.faceDownPile;
        Game g = this;
        invoker.execute(new Commander.Command() {
            long points;
            @Override
            public void execute() {
                while(!sp.isEmpty())
                    fp.put(sp.remove());
                points = g.scorePoints(-100);
            }

            @Override
            public void undo() {
                while(!fp.isEmpty())
                    sp.put(fp.remove());
                g.scorePoints(points);
            }
        });
    }

    /**
     * Command that moves card from sourcePile onto specified foundation.
     * Corresponding canMoveToFoundation() method must be called before invoking this command.
     * @param targetFoundation Self explanatory.
     */
    public void moveToFoundation(int targetFoundation){
        SourcePile sp = this.sourcePile;
        FoundationPile tp = this.foundationPile[targetFoundation];
        Game g = this;
        invoker.execute(new Commander.Command() {
            @Override
            public void execute() {
                tp.put(sp.remove());
                g.scorePoints(10);
            }

            @Override
            public void undo() {
                sp.put(tp.remove());
                g.scorePoints(-10);
            }
        });
    }

    /**
     * Command that moves card from foundation onto specified foundation.
     * Corresponding canMoveToFoundation() method must be called before invoking this command.
     * @param targetFoundation Self explanatory.
     * @param foundation Source foundation of the card to be moved.
     */
    public void moveToFoundation(int targetFoundation, int foundation){
        FoundationPile sp = this.foundationPile[foundation];
        FoundationPile tp = this.foundationPile[targetFoundation];
        invoker.execute(new Commander.Command() {
            @Override
            public void execute() {
                tp.put(sp.remove());
            }

            @Override
            public void undo() {
                sp.put(tp.remove());
            }
        });
    }

    /**
     * Command that moves single card from tableau onto specified foundation.
     * Corresponding canMoveToFoundation() method must be called before invoking this command.
     * Method canMoveToFoundation(int, int, int) guarantees only single card is moved.
     * Index parameter is left to be consistent with moveToStack(int, int, int) and to allow overloading.
     * @param targetFoundation Self explanatory.
     * @param stack Source stack on the tableau.
     * @param index Index of the card to be moved.
     */
    public void moveToFoundation(int targetFoundation, int stack, int index){
        CardStack cs = this.cardStack[stack];
        FoundationPile tp = this.foundationPile[targetFoundation];
        Game g = this;
        invoker.execute(new Commander.Command() {
            boolean flipped;
            @Override
            public void execute() {
                tp.put(cs.remove());
                flipped = cs.turnFaceUpTopCard(); // true/false if was flipped or not
                if(flipped)
                    g.scorePoints(15); // 10 for stack->foundation, 5 for turning over card
                else
                    g.scorePoints(10); // only 10 for stack->foundation
            }

            @Override
            public void undo() {
                if(flipped) {
                    cs.turnFaceDownTopCard();
                    g.scorePoints(-15);
                }
                else g.scorePoints(-10);
                cs.forcePut(tp.remove());
            }
        });
    }

    /**
     * Command that moves card from sourcePile onto specified stack.
     * Corresponding canMoveToStack() method must be called before invoking this command.
     * @param targetStack Self explanatory.
     */
    public void moveToStack(int targetStack){
        SourcePile sp = this.sourcePile;
        CardStack cs = this.cardStack[targetStack];
        Game g = this;
        invoker.execute(new Commander.Command() {
            @Override
            public void execute() {
                cs.put(sp.remove());
                g.scorePoints(5);
            }

            @Override
            public void undo() {
                sp.put(cs.remove());
                g.scorePoints(-5);
            }
        });
    }

    /**
     * Command that moves card from specified foundation onto specified stack.
     * Corresponding canMoveToStack() method must be called before invoking this command.
     * @param targetStack Self explanatory.
     * @param foundation Source foundation of the card to be moved.
     */
    public void moveToStack(int targetStack, int foundation){
        CardStack cs = this.cardStack[targetStack];
        FoundationPile tp = this.foundationPile[foundation];
        Game g = this;
        invoker.execute(new Commander.Command() {
            long points;
            @Override
            public void execute() {
                cs.put(tp.remove());
                points = g.scorePoints(-15);
            }

            @Override
            public void undo() {
                tp.put(cs.remove());
                g.scorePoints(points);
            }
        });
    }

    /**
     * Command that moves stack of cards from specified card stack onto specified stack.
     * Corresponding canMoveToStack() method must be called before invoking this command.
     * @param targetStack Self explanatory.
     * @param stack Source stack on the tableau.
     * @param index Index of the first card to be moved.
     */
    public void moveToStack(int targetStack, int stack, int index){
        CardStack ts = this.cardStack[targetStack];
        CardStack ss = this.cardStack[stack];
        Game g = this;
        invoker.execute(new Commander.Command() {
            boolean flipped;
            int indexOfOriginalTargetPack = ts.size(); // index for undo
            @Override
            public void execute() {
                ts.put(ss.remove(index));
                flipped = ss.turnFaceUpTopCard();
                if(flipped)
                    g.scorePoints(5);
            }

            @Override
            public void undo() {
                if(flipped) {
                    ss.turnFaceDownTopCard();
                    g.scorePoints(-5);
                }
                ss.put(ts.remove(indexOfOriginalTargetPack));
            }
        });
    }

    /**
     * Scores points. Trims score if it is lower than zero.
     * @param pt Points to be scored. Negative number to remove scored points.
     * @return Actual number of points scored (after trimming).
     */
    private long scorePoints(long pt){
        this.points += pt;
        if(this.points < 0) {
            long tmp = this.points - pt;
            this.points = 0;
            return tmp;
        }
        return this.points;
    }

    /**
     * @return Game seed.
     */
    public String getSeed() {return this.seed; }

    /**
     * @return Scored points.
     */
    public long getPoints(){
        return this.points;
    }

    /**
     * @return Number of remaining redeals.
     */
    public int getRedealsLeft() {return this.redealsLeft; }

    /**
     * @param i Index of cardstack on the tableau.
     * @return Stack at specified index.
     */
    public CardStack getCardStack(int i) {
        return this.cardStack[i];
    }

    /**
     * @param i Index of foundation.
     * @return Foundation at specified index.
     */
    public FoundationPile getFoundationPile(int i){
        return this.foundationPile[i];
    }

    /**
     * @return SourcePile.
     */
    public SourcePile getSourcePile(){
        return this.sourcePile;
    }

    /**
     * @return FaceDownPile.
     */
    public FaceDownPile getFaceDownPile(){
        return this.faceDownPile;
    }
}
