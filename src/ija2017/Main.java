package ija2017;

import ija2017.model.Game;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        int opt;
        int tmp, tmp2;
        Game gm1 = new Game();
        String menu = "Options:\n\t0 Print game\n\t-1 Undo\n\t1 Turn Next Card\n\t2 Move SourcePile to TargetPile\n\t" +
                "3 Move SourcePile to CardStack";
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
                    System.out.print("TargetPile? 1-4 ");
                    if(gm1.canMoveToFoundation(tmp = scan.nextInt()-1))
                        gm1.moveToFoundation(tmp);
                    else
                        System.out.println(false);
                    break;
                case 3:
                    System.out.print("CardStack? 1-7 ");
                    if(gm1.canMoveToStack((tmp = scan.nextInt()-1)))
                        gm1.moveToStack(tmp);
                    else
                        System.out.println(false);
                    break;
                case 4:
                    System.out.println("TargetPile? 1-4 ");
                    tmp = scan.nextInt() - 1;
                    System.out.println("CardStack? 1-7 ");
                    if(gm1.canMoveToStack((tmp2 = scan.nextInt() - 1), tmp))
                        gm1.moveToStack(tmp2, tmp);
                    else
                        System.out.println(false);
                    break;
            }
            System.out.println(menu);
        }
    }
}
