package script.tasks;

import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.script.task.Task;
import script.Beggar;

public class ToggleRun extends Task {

    private Beggar main;

    public ToggleRun(Beggar beggar){
        main = beggar;
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
