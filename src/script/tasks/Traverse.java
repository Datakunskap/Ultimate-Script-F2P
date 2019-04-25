package script.tasks;

import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import script.Beggar;

public class Traverse extends Task {

    @Override
    public boolean validate() {
        return !Beggar.location.getBegArea().contains(Players.getLocal());
    }

    @Override
    public int execute() {
        Movement.walkTo(Beggar.location.getBegArea().getCenter());
        return 1000;
    }
}