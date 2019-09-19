package script.tutorial_island;

import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;

public class EnableRun extends Task {

    @Override
    public boolean validate() {
        return Varps.get(406) >= TutorialIsland.randomSectionRun && !Movement.isRunEnabled() && (Movement.getRunEnergy() > Random.nextInt(8, 30) || Random.nextInt(1, 200) == 1);
    }

    @Override
    public int execute() {
        Log.info("Run enabled");
        Movement.toggleRun(true);
        Time.sleepUntil(Movement::isRunEnabled, 500);
        Movement.walkToRandomized(Players.getLocal().getPosition().randomize(6));
        Time.sleep(3000, 6000);
        return TutorialIsland.getRandSleep();
    }
}
