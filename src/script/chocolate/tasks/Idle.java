package script.chocolate.tasks;

import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.script.events.LoginScreen;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Script;
import script.chocolate.Main;
import script.fighter.Fighter;

import java.util.concurrent.TimeUnit;

public class Idle extends Task {

    private int max;
    private long idleTill;

    private Main main;

    public Idle(Main main){
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
            max = main.idleChocNum;
        }
        return main.totalMade >= max;
    }

    @Override
    public int execute() {
        Log.info("Idling");

        if(idleTill == 0) {
            idleTill = System.currentTimeMillis() + Random.high(20000, 180000);
            return Fighter.getLoopReturn();
        }
        long timeout = getIdleFor();
        if(timeout > 60 && Game.isLoggedIn() && Script.IDLE_LOGOUT) {
            Log.fine("Logging out....");
            main.script.removeBlockingEvent(LoginScreen.class);
            Game.logout();
            Time.sleep(200, 500);
        }
        if(timeout > 0) {
            Log.fine("Idling for " + getIdleFor() + " seconds.");
            return Fighter.getLoopReturn();
        }
        max = 0;
        //main.totalMade = 0;
        main.idleChocNum += Random.high(main.idleChocNum - 250, main.idleChocNum + 250);
        Script.timesIdled ++;
        if (main.script.getBlockingEvent(LoginScreen.class) == null) {
            main.script.addBlockingEvent(new LoginScreen(main.script));
        }
        return Fighter.getLoopReturn();
    }
}
