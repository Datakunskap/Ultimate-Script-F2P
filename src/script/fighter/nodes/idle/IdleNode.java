package script.fighter.nodes.idle;

import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.ui.Log;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.debug.Logger;
import script.fighter.framework.Node;

import java.util.concurrent.TimeUnit;

public class IdleNode extends Node {

    private int max;
    private int kills;
    private long idleTill;

    private Fighter main;

    public IdleNode(Fighter main){
        this.main = main;
    }

    public void onTargetKill() {
        kills++;
    }

    public int getKills() {
        return kills;
    }

    public int getMax() {
        return max;
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
        if(!Config.getProgressive().isRandomIdle()) {
            return false;
        }
        if(isIdling()) {
            return true;
        }
        if(max == 0) {
            int buffer = Config.getProgressive().getRandomIdleBuffer();
            max = Random.high(buffer - 6, buffer + 8);
            max = buffer;
        }
        return kills >= max;
    }

    @Override
    public int execute() {
        invalidateTask(main.getActive());
        //Log.info("Idling");

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
        kills = 0;
        return Fighter.getLoopReturn();
    }

    @Override
    public void onInvalid() {
        if(!isIdling()) {
            kills = 0;
            max = 0;
            idleTill = 0;
        }
        super.onInvalid();
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
        return "Idling";
    }
}
