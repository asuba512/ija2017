package ija2017.model;

import b.c.T;

import java.io.IOException;
import java.util.*;

/**
 * Created by xsubaa00 on 10/04/17.
 */
public class Game implements java.io.Serializable {
    private CardStack[] cardStack = new CardStack[7];
    private TargetPile[] targetPile = new TargetPile[4];
    private SourcePile sourcePile;
    private FaceDownPile faceDownPile;

    private transient Commander.Invoker invoker = new Commander.Invoker();

    public Game(){
        this.sourcePile = new SourcePile();
        this.faceDownPile = new FaceDownPile();
        LinkedList<Card> deck = CardDeck.createDeck();
        long seed = System.nanoTime();
        Collections.shuffle(deck, new Random(seed)); // shuffle created deck
        for(int i = 0; i < 4; i++)
            this.targetPile[i] = new TargetPile();
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
                "TargetPile 1: " + this.targetPile[0] + "\n" +
                "TargetPile 2: " + this.targetPile[1] + "\n" +
                "TargetPile 3: " + this.targetPile[2] + "\n" +
                "TargetPile 4: " + this.targetPile[3] + "\n";
        for(int i = 0; i < 7; i++)
            game += this.cardStack[i] + "\n";
        return game;
    }

    public boolean canMoveToFoundation(int targetFoundation){
        if(this.sourcePile.isEmpty())
            return false;
        return this.targetPile[targetFoundation].canAccept(this.sourcePile.peek());
    }
    public boolean canMoveToFoundation(int targetFoundation, int foundation){
        if(this.targetPile[foundation].isEmpty())
            return false;
        return this.targetPile[targetFoundation].canAccept(this.targetPile[foundation].peek());
    }
    public boolean canMoveToFoundation(int targetFoundation, int stack, int index){
        if(this.cardStack[stack].isEmpty())
            return false;
        if(index != this.cardStack[stack].size() - 1) // foundation can accept only single card
            return false;
        return this.targetPile[targetFoundation].canAccept(this.cardStack[stack].peek());
    }


    public boolean canMoveToStack(int targetStack){
        if(this.sourcePile.isEmpty())
            return false;
        return this.cardStack[targetStack].canAccept(this.sourcePile.peek());
    }
    public boolean canMoveToStack(int targetStack, int foundation){
        if(this.targetPile[foundation].isEmpty())
            return false;
        return this.cardStack[targetStack].canAccept(this.targetPile[foundation].peek());
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
        invoker.execute(new Commander.Command() {;
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

    private void turnOverSource(){
        SourcePile sp = this.sourcePile;
        FaceDownPile fp = this.faceDownPile;
        invoker.execute(new Commander.Command() {;
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
        TargetPile tp = this.targetPile[targetFoundation];
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
        TargetPile sp = this.targetPile[foundation];
        TargetPile tp = this.targetPile[targetFoundation];
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
        TargetPile tp = this.targetPile[targetFoundation];
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
        TargetPile tp = this.targetPile[foundation];
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

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException{
        in.defaultReadObject();
        this.invoker = new Commander.Invoker();
    }
}
