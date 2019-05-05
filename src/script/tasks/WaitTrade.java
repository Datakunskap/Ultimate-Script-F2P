package script.tasks;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class WaitTrade extends Task {

    private int min;
    private int max;
    private boolean waitSet = false;

    @Override
    public boolean validate() {
        return !Beggar.beg && !Beggar.walk && !Beggar.trading;
    }

    @Override
    public int execute() {

        if (!waitSet){
            setMinMaxWait();
            waitSet = true;
        }

        int timeout = Beggar.randInt(min, max);
        Log.info("Waiting " + (timeout / 1000) + "s for a trade");

        if (Time.sleepUntil(() -> Beggar.trading, timeout)){
            return Beggar.randInt(1000, 2000);
        }
        else {
            Beggar.walk = true;
            Beggar.beg = true;

            Beggar.checkWorldHopTime();
            return 500;
        }
    }

    private void setMinMaxWait(){
        min = Beggar.minWait *1000;
        max = Beggar.maxWait *1000;
    }
}
