package ija2017;

import ija2017.model.Game;

import java.io.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        int opt;
        int tmp, tmp2, tmp3;
        //Game gm1 = new Game();
        Game gm1 = null;
        try {
            FileInputStream fileIn = new FileInputStream("/tmp/employee.ser");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            gm1 = (Game) in.readObject();
            in.close();
            fileIn.close();
        }catch(IOException i) {
            i.printStackTrace();
            return;
        }catch(ClassNotFoundException c) {
            System.out.println("Employee class not found");
            c.printStackTrace();
            return;
        }
        String menu = "Options:\n\t0 Print game\n\t-1 Undo\n\t1 Turn Next Card\n\t2 Move SourcePile to TargetPile\n\t" +
                "3 Move SourcePile to CardStack\n\t4 Move TargetPile to CardStack\n\t5 Move CardStack to CardStack\n\t" +
                "6 Move CardStack to TargetPile\n\t-2 End";
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
                    System.out.println("TargetPile? 1-4 ");
                    if(gm1.canMoveToFoundation(tmp = scan.nextInt()-1))
                        gm1.moveToFoundation(tmp);
                    else
                        System.out.println(false);
                    break;
                case 3:
                    System.out.println("CardStack? 1-7 ");
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
                case 5:
                    System.out.println("CardStack? 1-7 ");
                    tmp = scan.nextInt() - 1;
                    System.out.println("Index (from top, starting with 1)? ");
                    tmp2 = scan.nextInt() - 1;
                    System.out.println("Target CardStack? 1-7 ");
                    tmp3 = scan.nextInt() - 1;
                    if(gm1.canMoveToStack(tmp3, tmp, tmp2))
                        gm1.moveToStack(tmp3, tmp, tmp2);
                    else
                        System.out.println(false);
                    break;
                case 6:
                    System.out.println("CardStack? 1-7 ");
                    tmp = scan.nextInt() - 1;
                    System.out.println("Index (from top, starting with 1)? ");
                    tmp2 = scan.nextInt() - 1;
                    System.out.println("TargetPile? 1-4 ");
                    tmp3 = scan.nextInt() - 1;
                    if(gm1.canMoveToFoundation(tmp3, tmp, tmp2))
                        gm1.moveToFoundation(tmp3, tmp, tmp2);
                    else
                        System.out.println(false);
                    break;
                case 7:System.out.println("From TargetPile? 1-4 ");
                    tmp = scan.nextInt() - 1;
                    System.out.println("To TargetPile? 1-4 ");
                    if(gm1.canMoveToFoundation((tmp2 = scan.nextInt()-1), tmp))
                        gm1.moveToFoundation(tmp2, tmp);
                    else
                        System.out.println(false);
                    break;
            }
            System.out.println(menu);
        }
        try {
            FileOutputStream fileOut =
                    new FileOutputStream("/tmp/employee.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(gm1);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in /tmp/employee.ser");
        }catch(IOException i) {
            i.printStackTrace();
        }
    }
}
