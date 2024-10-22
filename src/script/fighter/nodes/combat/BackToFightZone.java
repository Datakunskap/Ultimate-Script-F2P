package script.fighter.nodes.combat;

import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import script.data.CheckTutIsland;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.debug.Logger;
import script.fighter.framework.Node;

public class BackToFightZone extends Node {

    private Position startTileRandom;
    private int distRandom;
    private final CheckTutIsland checkTutIsland;

    private Fighter main;

    public BackToFightZone(Fighter main){
        this.main = main;
        checkTutIsland = new CheckTutIsland(main.getScript());
    }

    @Override
    public boolean validate() {
        if (Players.getLocal().distance(Config.getStartingTile()) > Config.getRadius()) {
            return true;
        }
        return false;
    }

    @Override
    public int execute() {
        main.invalidateTask(this);
        if (!Game.isLoggedIn() || Players.getLocal() == null)
            return 2000;

        //Log.info("Walking back to fight zone.");
        if(startTileRandom == null) {
            startTileRandom = Config.getStartingTile().randomize(3);
            distRandom = Random.nextInt(1, 4);
        }
        if(Movement.getDestinationDistance() >= distRandom) {
            Logger.debug("Walking to: " + startTileRandom.toString());
            if (shouldEnableRun()) {
                enableRun();
            }
            if (!Movement.walkTo(startTileRandom)) {
                startTileRandom = null;
                Movement.walkTo(Players.getLocal().getPosition().randomize(3));
            }
            Time.sleep(200, 450);
            /*if (!Players.getLocal().isMoving()) {
                Logger.debug("Walking to: " + startTileRandom.toString());
                main.daxWalker.walkTo(startTileRandom, () -> startTileRandom == null);
                startTileRandom = Config.getStartingTile().randomize(3);
            }*/
        }
        return Fighter.getLoopReturn();
    }

    public static boolean shouldEnableRun() {
        if (Movement.isRunEnabled()) {
            return false;
        }
        if (Random.nextInt(1, 100) == 1) {
            // sometimes I like to random enable run
            return true;
        }
        return Movement.getRunEnergy() > Random.nextInt(12, 30);
    }

    public static void enableRun() {
        Movement.toggleRun(true);
        Time.sleepUntil(Movement::isRunEnabled, 500);
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
