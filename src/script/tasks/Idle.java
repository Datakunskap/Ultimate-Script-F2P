package script.tasks;

import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;
import script.fighter.Fighter;

import java.util.concurrent.TimeUnit;

public class Idle extends Task {

    private int max;
    private long idleTill;

    private Beggar main;

    public Idle(Beggar main){
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
            max = main.idleBegNum;
        }
        return main.numBegs >= max;
    }

    @Override
    public int execute() {
        Log.info("Idling");

        if(idleTill == 0) {
            idleTill = System.currentTimeMillis() + Random.high(20000, 180000);
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
        main.numBegs = 0;
        main.idleBegNum = Random.high(main.idleBegNum - 10, main.idleBegNum + 10);
        return Fighter.getLoopReturn();
    }
}
