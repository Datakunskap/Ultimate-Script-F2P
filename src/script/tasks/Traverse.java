package script.tasks;

import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class Traverse extends Task {

    private Beggar main;
    private WalkingHelper walk;

    public Traverse(Beggar main){
        this.main = main;
        walk = new WalkingHelper(main);
    }

    @Override
    public boolean validate() {
        return (Beggar.walk || (!Beggar.location.getBegArea().contains(Players.getLocal()))) && !Beggar.trading;
    }

    @Override
    public int execute() {
        int rand = Beggar.randInt(1, Beggar.walkChance);

        if(!Beggar.location.getBegArea().contains(Players.getLocal())){
            Log.info("Walking to GE");
            if (walk.shouldSetDestination()) {
                if (Movement.walkToRandomized(BankLocation.GRAND_EXCHANGE.getPosition())) {
                    Time.sleepUntil(() -> BankLocation.GRAND_EXCHANGE.getPosition().distance(Players.getLocal().getPosition()) <= 7, Random.mid(1800, 2400));
                }
            }
        } else if (rand != 1 || rand != 2 || rand != 3 || rand != 4 || rand != 5 || rand != 6 || rand != 7) {
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

    public void checkTutorialIsland(){
        if (Beggar.TUTORIAL_ISLAND_AREA.contains(Players.getLocal())) {

        }
    }
}