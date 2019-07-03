package script.tasks;

import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.events.LoginScreen;
import script.Beggar;

class WalkingHelper {

    private Beggar main;

    public WalkingHelper(Beggar main){
        this.main = main;
    }

    boolean shouldSetDestination() {
        // small chance to force new destination in case of the rare problem:
        // having a destination set but player is not moving towards it
        // I don't trust Players.getLocal().isMoving() for this
        if (Random.nextInt(1, 200) == 1) {
            return true;
        }
        try {
            if (Game.isLoggedIn() && !Players.getLocal().isMoving()) {
                return true;
            }
        } catch (Exception e){
            main.setStopping(true);
            e.printStackTrace();
        }

        if (!Movement.isDestinationSet()) {
            return true;
        }

        // almost at destination
        if (Movement.getDestinationDistance() <= Random.nextInt(2,3)) {
            return true;
        }
        return false;
    }

    boolean shouldEnableRun() {
        if (Movement.isRunEnabled()) {
            return false;
        }
        if (Random.nextInt(1, 1000) == 1) {
            // sometimes I like to random enable run, so my bot should too
            return true;
        }
        return Movement.getRunEnergy() > Random.nextInt(4, 25);
    }

    boolean enableRun() {
        Movement.toggleRun(true);
        return Time.sleepUntil(Movement::isRunEnabled, 500);
    }
}
