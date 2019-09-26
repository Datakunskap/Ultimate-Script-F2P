package script.fighter.nodes.combat;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.debug.Logger;
import script.fighter.framework.Node;

public class BackToFightZone extends Node {

    private Position startTileRandom;
    private int distRandom;

    private Fighter main;

    public BackToFightZone(Fighter main){
        this.main = main;
    }

    @Override
    public boolean validate() {
        return Players.getLocal().distance(Config.getStartingTile()) > Config.getRadius();
    }

    @Override
    public int execute() {
        invalidateTask(main.getActive());

        //Log.info("Walking back to fight zone.");
        if(startTileRandom == null) {
            startTileRandom = Config.getStartingTile().randomize(3);
            distRandom = Random.nextInt(1, 4);
        }
        if(Movement.getDestinationDistance() >= distRandom) {
            Time.sleep(200, 450);
            if (!Players.getLocal().isMoving() || !Movement.isDestinationSet()) {
                Logger.debug("Walking to: " + startTileRandom.toString());
                main.daxWalker.walkTo(startTileRandom, () -> startTileRandom == null);
            }
        }
        return Fighter.getLoopReturn();
    }

    @Override
    public void onInvalid() {
        Logger.debug("Disposing back to fight zone.");
        startTileRandom = null;
        distRandom = -1;
    }

    public void invalidateTask(Node active) {
        if (active != null && !this.equals(active)) {
            Logger.debug("Node has changed.");
            active.onInvalid();
        }
        main.setActive(this);
    }

    @Override
    public String status() {
        return "Walking back to fight zone.";
    }
}
