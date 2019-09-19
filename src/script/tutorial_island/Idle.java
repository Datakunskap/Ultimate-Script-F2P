package script.tutorial_island;

import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.ui.Log;
import script.fighter.Fighter;

import java.util.concurrent.TimeUnit;

public class Idle extends TutorialSection {

    private int max;
    private long idleTill;
    private TutorialIsland main;

    public Idle(TutorialIsland main) {
        super(null);
        this.main = main;
    }

    public long getIdleFor() {
        if(idleTill == 0) {
            return 0;
        }
        return TimeUnit.MILLISECONDS.toSeconds(idleTill - System.currentTimeMillis());
    }

    private boolean isIdling() {
        return getIdleFor() > 0;
    }

    @Override
    public boolean validate() {
        if(isIdling()) {
            return true;
        }
        if(max == 0) {
            max = main.idleTutSection;
        }
        return getTutorialSection() >= max && !main.hasIdled;
    }

    @Override
    public int execute() {
        Log.fine("Idling");

        if(idleTill == 0) {
            idleTill = System.currentTimeMillis() + Random.low(20000, 180000);
            return Fighter.getLoopReturn();
        }
        long timeout = getIdleFor();
        if(timeout > 60 && Game.isLoggedIn()) {
            Log.fine("Logging out....");
            Game.logout();
            Time.sleep(200, 500);
        }
        if(timeout > 0) {
            Log.fine("Idling for " + getIdleFor() + " seconds.");
            return Fighter.getLoopReturn();
        }
        max = 0;
        main.hasIdled = true;
        //main.totalTanned = 0;
        //main.idleTanNum += Random.high(main.idleTanNum - 100, main.idleTanNum + 100);
        return Fighter.getLoopReturn();
    }
}
