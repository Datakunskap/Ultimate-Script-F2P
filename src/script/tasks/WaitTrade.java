package script.tasks;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class WaitTrade extends Task {

    private int min = 1300;
    private int max = 2300;

    @Override
    public boolean validate() {
        return (!Beggar.beg || !Beggar.walk) && !Beggar.trading;
    }

    @Override
    public int execute() {
        Log.info("Waiting for a trade");
        Time.sleep(min, max);
        if(Beggar.trading){
            return 500;
        }
        Time.sleep(min, max);
        if(Beggar.trading){
            return 500;
        }
        Time.sleep(min, max);
        if(Beggar.trading){
            return 500;
        }
        Time.sleep(min, max);
        if(Beggar.trading){
            return 500;
        }
        Time.sleep(min, max);
        if(Beggar.trading){
            return 500;
        }
        Time.sleep(min, max);
        if(Beggar.trading){
            return 500;
        }
        Time.sleep(min, max);
        if(Beggar.trading){
            return 500;
        }
        Time.sleep(min, max);
        if(Beggar.trading){
            return 500;
        }
        Time.sleep(min, max);
        if(Beggar.trading){
            return 500;
        }
        Time.sleep(min, max);
        if(Beggar.trading){
            return 500;
        }
        Beggar.walk = true;
        Beggar.beg = true;
        return 500;
    }
}
