package script.tasks;

import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class Traverse extends Task {

    @Override
    public boolean validate() {
        return !Beggar.location.getBegArea().contains(Players.getLocal());
    }

    @Override
    public int execute() {
        Movement.walkToRandomized(Beggar.location.getBegArea().getTiles().get(randInt(0, Beggar.location.getBegArea().getTiles().size()-1)));
        Log.info("Walking to location");
        return 1000;
    }

    public static int randInt(int min, int max) {
        java.util.Random rand = new java.util.Random();
        int randomNum = rand.nextInt(max - min + 1) + min;
        return randomNum;
    }
}