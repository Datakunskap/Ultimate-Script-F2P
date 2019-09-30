package script.tutorial_island;

import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;

public class EnableRun extends Task {

    @Override
    public boolean validate() {
        return Varps.get(406) >= TutorialIsland.randomSectionRun && !Movement.isRunEnabled() && (Movement.getRunEnergy() > Random.nextInt(15, 40) || Random.nextInt(1, 500) == 1);
    }

    @Override
    public int execute() {
        Log.info("Run enabled");
        Movement.toggleRun(true);
        /*Movement.walkToRandomized(Players.getLocal().getPosition().randomize(6));
        Time.sleep(1000);
        Time.sleepUntil(() -> !Players.getLocal().isMoving(), 2000, Beggar.randInt(2000, 4000));*/
        return TutorialIsland.getRandSleep();
    }
}
