package ija2017;

import ija2017.model.Game;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        int opt;
        int tmp;
        Game gm1 = new Game();
        String menu = "Options:\n\t0 Print game\n\t-1 Undo\n\t1 Turn Next Card\n\t2 Move SourcePile to TargetPile";
        System.out.println(menu);
        while((opt = scan.nextInt()) != -2){
            switch (opt){
                case 0:
                    System.out.println(gm1);
                    break;
                case -1:
                    gm1.undo();
                    break;
                case 1:
                    gm1.turnCard();
                    break;
                case 2:
                    System.out.print("TargetPile? 1-4");
                    if(gm1.canMoveToFoundation(tmp = scan.nextInt()-1))
                        gm1.moveToFoundation(tmp);
                    else
                        System.out.println(false);
            }
            System.out.println(menu);
        }
    }
}
