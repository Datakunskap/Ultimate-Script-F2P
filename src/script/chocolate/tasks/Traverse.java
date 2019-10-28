package script.chocolate.tasks;

import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.chocolate.Main;
import script.tanner.data.Location;

public class Traverse extends Task {

    private Main main;

    public Traverse(Main main) {
        this.main = main;
    }

    @Override
    public boolean validate() {
        return !Location.GE_AREA.containsPlayer() && !main.isMuling;
    }

    @Override
    public int execute() {

        Log.info("Walking to GE");

        if (WalkingHelper.shouldSetDestination()) {
            if (Movement.walkToRandomized(BankLocation.GRAND_EXCHANGE.getPosition())) {
                Time.sleepUntil(() -> BankLocation.GRAND_EXCHANGE.getPosition().distance(Players.getLocal().getPosition()) <= 7, Random.mid(1800, 2400));
            }
        }
        return 1000;
    }
}