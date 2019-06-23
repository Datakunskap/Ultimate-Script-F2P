package script.casino;

import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class GTraverse extends Task {

    @Override
    public boolean validate() {
        return (Beggar.walk || (!Beggar.location.getBegArea().contains(Players.getLocal()))) && !Beggar.trading;
    }

    @Override
    public int execute() {
        int rand = Beggar.randInt(1, 8);

        if(!Beggar.location.getBegArea().contains(Players.getLocal())){
            Log.info("Walking to GE");
            if (GWalkingHelper.shouldSetDestination()) {
                if (Movement.walkToRandomized(BankLocation.GRAND_EXCHANGE.getPosition())) {
                    Time.sleepUntil(() -> BankLocation.GRAND_EXCHANGE.getPosition().distance(Players.getLocal().getPosition()) <= 7, Random.mid(1800, 2400));
                }
            }
        } else if (rand == 1) {
            Movement.walkToRandomized(Beggar.location.getBegArea().getTiles().get(Beggar.randInt(0, Beggar.location.getBegArea().getTiles().size() - 1)));
            Log.info("Walking to random GE location");
            Beggar.walk = false;
            return Beggar.randInt(4000, 5000);
        } else {
            Beggar.walk = false;
            Beggar.sendTrade = false;
            return Beggar.randInt(2000, 3000);
        }
        return 600;
    }
}