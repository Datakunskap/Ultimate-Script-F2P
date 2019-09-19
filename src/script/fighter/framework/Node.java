package script.fighter.framework;

import org.rspeer.script.task.Task;

public abstract class Node extends Task {

    /*public abstract boolean validate();

    public abstract void execute();*/

    public void onInvalid() {

    }

    public void onScriptStop() {

    }

    public abstract String status();
}
