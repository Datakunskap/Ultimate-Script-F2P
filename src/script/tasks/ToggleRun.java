package script.tasks;

import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.script.task.Task;
import script.Beggar;

public class ToggleRun extends Task {
    @Override
    public boolean validate() {
        return !Movement.isRunEnabled() && Movement.getRunEnergy() > 4 && !Beggar.trading;
    }

    @Override
    public int execute() {
        Movement.toggleRun(true);
        return 1000;
    }
}
