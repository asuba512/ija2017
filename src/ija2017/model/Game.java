package ija2017.model;

import java.util.*;

/**
 * Created by xsubaa00 on 10/04/17.
 */
public class Game {
    private CardStack[] cardStack = new CardStack[7];
    private TargetPile[] targetPile = new TargetPile[4];
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
            this.targetPile[i] = new TargetPile();
        for(int i = 0; i < 7; i++) {
            this.cardStack[i] = new CardStack();
            for (int j = 0; j < i + 1; j++)
                this.cardStack[i].put(deck.pop());
            this.cardStack[i].flipTopCard(); // flip top card face up
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
        return this.targetPile[targetFoundation].canAccept(this.sourcePile.peek());
    }
    public boolean canMoveToFoundation(int targetFoundation, int stack, int index){
        return this.targetPile[targetFoundation].canAccept(this.cardStack[stack].peek(index));
    }


    public boolean canMoveToStack(int targetStack){
        return this.cardStack[targetStack].canAccept(this.sourcePile.peek());
    }
    public boolean canMoveToStack(int targetStack, int foundation){
        return this.cardStack[targetStack].canAccept(this.targetPile[foundation].peek());
    }
    public boolean canMoveToStack(int targetStack, int stack, int index){
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

    public void moveToFoundation(int targetFoundation, int stack, int index){
        invoker.execute(new Commander.Command() {

            @Override
            public void execute() {

            }

            @Override
            public void undo() {

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

        invoker.execute(new Commander.Command() {

            @Override
            public void execute() {

            }

            @Override
            public void undo() {

            }
        });
    }
}
