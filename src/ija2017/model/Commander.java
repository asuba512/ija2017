package ija2017.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Commander implements Serializable {
    public interface Command extends Serializable {
        void execute();
        void undo();
    }

    public static class Invoker implements Serializable {
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
