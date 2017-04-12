package ija2017.model;

import java.util.*;

/**
 * Created by xsubaa00 on 10/04/17.
 */
public class Game {
    private CardStack[] cardStack = new CardStack[7];
    private FoundationPile[] foundationPile = new FoundationPile[4];
    private SourcePile sourcePile;
    private FaceDownPile faceDownPile;

    private final Commander.Invoker invoker = new Commander.Invoker();

    public Game(){
        this.sourcePile = new SourcePile();
        this.faceDownPile = new FaceDownPile();
        LinkedList<Card> deck = CardDeck.createDeck();
        long seed = System.nanoTime();
        Collections.shuffle(deck, new Random(seed)); // shuffle created deck
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

    @Override
    public String toString(){
        String game = "FaceDownPile: " + this.faceDownPile + "\n" +
                "SourcePile: " + this.sourcePile + "\n" +
                "FoundationPile 1: " + this.foundationPile[0] + "\n" +
                "FoundationPile 2: " + this.foundationPile[1] + "\n" +
                "FoundationPile 3: " + this.foundationPile[2] + "\n" +
                "FoundationPile 4: " + this.foundationPile[3] + "\n";
        for(int i = 0; i < 7; i++)
            game += this.cardStack[i] + "\n";
        return game;
    }

    public boolean canMoveToFoundation(int targetFoundation){
        if(this.sourcePile.isEmpty())
            return false;
        return this.foundationPile[targetFoundation].canAccept(this.sourcePile.peek());
    }
    public boolean canMoveToFoundation(int targetFoundation, int foundation){
        if(this.foundationPile[foundation].isEmpty())
            return false;
        return this.foundationPile[targetFoundation].canAccept(this.foundationPile[foundation].peek());
    }
    public boolean canMoveToFoundation(int targetFoundation, int stack, int index){
        if(this.cardStack[stack].isEmpty())
            return false;
        if(index != this.cardStack[stack].size() - 1) // foundation can accept only single card
            return false;
        return this.foundationPile[targetFoundation].canAccept(this.cardStack[stack].peek());
    }


    public boolean canMoveToStack(int targetStack){
        if(this.sourcePile.isEmpty())
            return false;
        return this.cardStack[targetStack].canAccept(this.sourcePile.peek());
    }
    public boolean canMoveToStack(int targetStack, int foundation){
        if(this.foundationPile[foundation].isEmpty())
            return false;
        return this.cardStack[targetStack].canAccept(this.foundationPile[foundation].peek());
    }
    public boolean canMoveToStack(int targetStack, int stack, int index){
        if(this.cardStack[stack].isEmpty() || this.cardStack[stack].size() <= index)
            return false;
        return this.cardStack[targetStack].canAccept(this.cardStack[stack].peek(index));
    }

    /* COMMANDS */
    public void undo(){
        this.invoker.undo();
    }

    public void turnCard(){
        if(this.faceDownPile.isEmpty()) {
            turnOverSource();
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

    public void turnOverSource(){
        SourcePile sp = this.sourcePile;
        FaceDownPile fp = this.faceDownPile;
        invoker.execute(new Commander.Command() {
            @Override
            public void execute() {
                while(!sp.isEmpty())
                    fp.put(sp.remove());
            }

            @Override
            public void undo() {
                while(!fp.isEmpty())
                    sp.put(fp.remove());
            }
        });
    }

    /*
     * Check using can*() methods must be performed before executing any following command
    */

    public void moveToFoundation(int targetFoundation){
        SourcePile sp = this.sourcePile;
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

    /* only one card is going to be moved, checked before using canMoveToFoundation() */
    public void moveToFoundation(int targetFoundation, int stack, int index){
        CardStack cs = this.cardStack[stack];
        FoundationPile tp = this.foundationPile[targetFoundation];
        invoker.execute(new Commander.Command() {
            boolean flipped;
            @Override
            public void execute() {
                tp.put(cs.remove());
                flipped = cs.turnFaceUpTopCard(); // true/false if was flipped or not
            }

            @Override
            public void undo() {
                if(flipped)
                    cs.turnFaceDownTopCard();
                cs.forcePut(tp.remove());
            }
        });
    }

    public void moveToStack(int targetStack){
        SourcePile sp = this.sourcePile;
        CardStack cs = this.cardStack[targetStack];
        invoker.execute(new Commander.Command() {
            @Override
            public void execute() {
                cs.put(sp.remove());
            }

            @Override
            public void undo() {
                sp.put(cs.remove());
            }
        });
    }

    public void moveToStack(int targetStack, int foundation){
        CardStack cs = this.cardStack[targetStack];
        FoundationPile tp = this.foundationPile[foundation];
        invoker.execute(new Commander.Command() {

            @Override
            public void execute() {
                cs.put(tp.remove());
            }

            @Override
            public void undo() {
                tp.put(cs.remove());
            }
        });
    }

    public void moveToStack(int targetStack, int stack, int index){
        CardStack ts = this.cardStack[targetStack];
        CardStack ss = this.cardStack[stack];
        invoker.execute(new Commander.Command() {
            boolean flipped;
            @Override
            public void execute() {
                ts.put(ss.remove(index));
                flipped = ss.turnFaceUpTopCard();
            }

            @Override
            public void undo() {
                if(flipped)
                    ss.turnFaceDownTopCard();
                ss.put(ts.remove(index));
            }
        });
    }

    public CardStack getCardStack(int i) {
        return this.cardStack[i];
    }

    public FoundationPile getFoundationPile(int i){
        return this.foundationPile[i];
    }

    public SourcePile getSourcePile(){
        return this.sourcePile;
    }

    public int getFaceDownPileSize(){
        return this.faceDownPile.size();
    }
}
