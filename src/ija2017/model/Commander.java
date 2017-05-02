package ija2017.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing Command design pattern with invoker. Has unlimited storage of commands.
 * @author Adam Suba (xsubaa00)
 */
class Commander implements Serializable {
    /**
     * Command interface.
     */
    public interface Command extends Serializable {
        void execute();
        void undo();
    }

    /**
     * Static class implementing Invoker of commands.
     */
    static class Invoker implements Serializable {
        /** Stores executed commands. */
        List<Command> commands = new ArrayList<>();

        /**
         * Stores command and executes it.
         * @param cmd Command to be executed.
         */
        void execute(Command cmd){
            commands.add(0, cmd);
            cmd.execute();
        }

        /**
         * Undoes last executed command.
         */
        void undo(){
            if (commands.size() > 0){
                Command cmd = commands.remove(0);
                cmd.undo();
            }
        }
    }
}
