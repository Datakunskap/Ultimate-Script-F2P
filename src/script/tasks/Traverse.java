package script.tasks;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.path.Path;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class Traverse extends Task {

    private Path pathToGE;

    @Override
    public boolean validate() {
        return (Beggar.walk || !Beggar.atGE) && !Beggar.trading;
    }

    @Override
    public int execute() {
        if(!Beggar.atGE){
            Log.info("Walking to GE");
            if(Beggar.buildGEPath){
                pathToGE = Movement.buildPath(Beggar.location.getBegArea().getCenter());

                if (pathToGE == null){
                    for (Position p : Beggar.location.getBegArea().getTiles()){
                        if (p != null){
                            pathToGE = Movement.buildPath(p);
                        }
                    }
                }
                Beggar.buildGEPath = false;
            }
            pathToGE.walk();
            if(Beggar.location.getBegArea().contains(Players.getLocal())){
                Beggar.atGE = true;
            }
        } else if (Beggar.randInt(1, Beggar.walkChance) == 1) {
            Movement.walkToRandomized(Beggar.location.getBegArea().getTiles().get(Beggar.randInt(0, Beggar.location.getBegArea().getTiles().size() - 1)));
            Log.info("Walking to random GE location");
            Beggar.walk = false;
            return Beggar.randInt(4000, 5000);
        } else {
            Log.info("Not walking");
            Beggar.walk = false;
            return Beggar.randInt(2000, 3000);
        }
        return 1000;
    }
}