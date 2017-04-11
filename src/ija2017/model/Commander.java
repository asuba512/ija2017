package ija2017.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xsubaa00 on 10/04/17.
 */
public class Commander {
    public static interface Command {
        void execute();
        void undo();
    }

    public static class Invoker {
        List<Command> commands = new ArrayList<>();
        public void execute(Command cmd){
            commands.add(0, cmd);
            cmd.execute();
        }

        public void undo(){
            if (commands.size() > 0){
                Command cmd = commands.remove(0);
                cmd.undo();
            }
        }
    }
}
