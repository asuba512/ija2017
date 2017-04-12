package ija2017;

import ija2017.model.Game;

import java.util.Scanner;

public class MainCLI {

    static Scanner scan = new Scanner(System.in);
    public static void main(String[] args) {
        int opt;
        int tmp, tmp2, tmp3;
        Game gm1 = new Game();
        String menu = "Options:\n\t0 Print game\n\t-1 Undo\n\t1 Turn Next Card\n\t2 Move SourcePile to FoundationPile\n\t" +
                "3 Move SourcePile to CardStack\n\t4 Move FoundationPile to CardStack\n\t5 Move CardStack to CardStack\n\t" +
                "6 Move CardStack to FoundationPile\n\t7 Move FoundationPile to FoundationPile\n\t-2 End";
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
                    if((tmp = getFoundationIndex()) >= 0 && gm1.canMoveToFoundation(tmp))
                        gm1.moveToFoundation(tmp);
                    else
                        System.out.println(false);
                    break;
                case 3:
                    if((tmp = getStackIndex()) >= 0 && gm1.canMoveToStack(tmp))
                        gm1.moveToStack(tmp);
                    else
                        System.out.println(false);
                    break;
                case 4:
                    tmp = getFoundationIndex();
                    if(tmp >= 0 && (tmp2 = getStackIndex()) >= 0 && gm1.canMoveToStack(tmp2, tmp))
                        gm1.moveToStack(tmp2, tmp);
                    else
                        System.out.println(false);
                    break;
                case 5:
                    tmp = getStackIndex();
                    tmp2 = getCardIndex();
                    tmp3 = getStackIndex();
                    if(tmp >= 0 && tmp2 >= 0 && tmp3 >= 0 &&gm1.canMoveToStack(tmp3, tmp, tmp2))
                        gm1.moveToStack(tmp3, tmp, tmp2);
                    else
                        System.out.println(false);
                    break;
                case 6:
                    tmp = getStackIndex();
                    tmp2 = getCardIndex();
                    tmp3 = getFoundationIndex();
                    if(tmp >= 0 && tmp2 >= 0 && tmp3 >= 0 && gm1.canMoveToFoundation(tmp3, tmp, tmp2))
                        gm1.moveToFoundation(tmp3, tmp, tmp2);
                    else
                        System.out.println(false);
                    break;
                case 7:
                    tmp = getFoundationIndex();
                    if(tmp >= 0 && (tmp2 = getFoundationIndex()) >= 0 && gm1.canMoveToFoundation(tmp2, tmp))
                        gm1.moveToFoundation(tmp2, tmp);
                    else
                        System.out.println(false);
                    break;
            }
            System.out.println(menu);
        }
    }

    public static int getFoundationIndex(){
        System.out.println("FoundationPile? 1-4 ");
        int x = scan.nextInt() - 1;
        if(x >= 0 && x < 4)
            return x;
        else
            return -1;
    }

    public static int getStackIndex(){
        System.out.println("CardStack? 1-7 ");
        int x = scan.nextInt() - 1;
        if(x >= 0 && x < 7)
            return x;
        else
            return -1;
    }

    public static int getCardIndex(){
        System.out.println("Card? (from top, starting with 1) ");
        int x = scan.nextInt() - 1;
        if(x >= 0)
            return x;
        else
            return -1;
    }

}