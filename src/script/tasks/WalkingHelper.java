package script.tasks;

import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Players;
import script.Beggar;

class WalkingHelper {

    private Beggar main;

    WalkingHelper(Beggar main) {
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
        } catch (Exception e) {
            main.setStopping(true);
            e.printStackTrace();
        }

        if (!Movement.isDestinationSet()) {
            return true;
        }

        // almost at destination
        return Movement.getDestinationDistance() <= Random.nextInt(2, 3);
    }
}
