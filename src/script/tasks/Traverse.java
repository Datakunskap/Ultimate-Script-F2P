package script.tasks;

import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;
import script.data.CheckTutIsland;

public class Traverse extends Task {

    private Beggar main;
    private WalkingHelper walk;

    public Traverse(Beggar main){
        this.main = main;
        walk = new WalkingHelper(main);
    }

    @Override
    public boolean validate() {
        return (main.walk || (!main.location.getBegArea().contains(Players.getLocal()))) && !main.trading;
    }

    @Override
    public int execute() {

        int rand = Beggar.randInt(1, main.walkChance);

        if(!main.location.getBegArea().contains(Players.getLocal())){
            //Log.info("Walking to GE");

            tutIslandCheck();

            if (walk.shouldSetDestination()) {
                if (Movement.walkToRandomized(BankLocation.GRAND_EXCHANGE.getPosition())) {
                    Time.sleepUntil(() -> BankLocation.GRAND_EXCHANGE.getPosition().distance(Players.getLocal().getPosition()) <= 7, Random.mid(1800, 2400));
                }
            }
        } else if (rand != 1 && rand != 2 && rand != 3 && rand != 4 && rand != 5 && rand != 6 && rand != 7) {
            Movement.walkToRandomized(main.location.getBegArea().getTiles().get(Beggar.randInt(0, main.location.getBegArea().getTiles().size() - 1)));
            Log.info("Walking to random GE location");
            main.walk = false;
            return Beggar.randInt(4000, 5000);
        } else {
            main.walk = false;
            main.sendTrade = false;
            return Beggar.randInt(2000, 3000);
        }
        return 600;
    }

    private void tutIslandCheck() {
        CheckTutIsland checkT = new CheckTutIsland(main);

        if (checkT.onTutIsland()) {
            checkT.execute();
        }
    }
}