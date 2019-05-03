package script.tasks;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.path.Path;
import org.rspeer.runetek.api.movement.pathfinding.executor.PathExecutor;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class Traverse extends Task {

    @Override
    public boolean validate() {
        return (Beggar.walk || !Beggar.atGE) && !Beggar.trading;
    }

    @Override
    public int execute() {
        if(!Beggar.atGE){
            Log.info("Walking to GE");
            Movement.walkToRandomized(Beggar.location.getBegArea().getCenter());
            if(Beggar.location.getBegArea().contains(Players.getLocal())){
                Beggar.atGE = true;
            }
        } else{
            Movement.walkToRandomized(Beggar.location.getBegArea().getTiles().get(Beggar.randInt(0, Beggar.location.getBegArea().getTiles().size() - 1)));
            Log.info("Walking to random GE location");
            Beggar.walk = false;
            return Beggar.randInt(4000, 5000);
        }
        return Beggar.randInt(500, 2000);
    }
}