package script.beg;

import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.script.task.Task;
import script.Script;

public class ToggleRun extends Task {

    private Script main;

    public ToggleRun(Script script){
        main = script;
    }

    @Override
    public boolean validate() {
        return !Movement.isRunEnabled() && Movement.getRunEnergy() > 4 && !main.trading;
    }

    @Override
    public int execute() {
        Movement.toggleRun(true);
        return 1000;
    }
}
