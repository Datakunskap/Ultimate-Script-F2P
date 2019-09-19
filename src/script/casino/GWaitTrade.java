/*
package script.casino;

import org.fighter.runetek.api.commons.Time;
import org.fighter.runetek.api.component.Bank;
import org.fighter.runetek.api.component.Trade;
import org.fighter.runetek.api.component.tab.Tab;
import org.fighter.runetek.api.component.tab.Tabs;
import org.fighter.script.task.Task;
import org.fighter.ui.Log;
import script.Beggar;

public class GWaitTrade extends Task {

    private int min;
    private int max;
    private boolean waitSet = false;

    @Override
    public boolean validate() {
        return !Beggar.beg && !Beggar.walk && !Beggar.trading; //&& !Beggar.sendTrade;
    }

    @Override
    public int execute() {

        if (Bank.isOpen()) {
            Bank.close();
        }

        if (!waitSet) {
            setMinMaxWait();
            waitSet = true;
        }

        int timeout = Beggar.randInt(min, max);
        Log.info("Waiting " + (timeout / 1000) + "s for a trade");

        if (Time.sleepUntil(() -> Beggar.trading || Trade.isOpen(), timeout)) {
            if(Trade.isOpen()){
                Beggar.sentTradeInit = true;
            }
            Beggar.trading = true;
            Beggar.processSentTrade = true;
            Beggar.walk = false;
            Beggar.sendTrade = false;
            Beggar.beg = false;
            return Beggar.randInt(1500, 2500);
        }

        Tabs.open(Tab.INVENTORY);
        Beggar.walk = true;
        Beggar.beg = true;
        Beggar.sendTrade = true;

        Beggar.checkWorldHopTime();
        return 500;
    }

    private void setMinMaxWait() {
        min = Beggar.minWait * 1000;
        max = Beggar.maxWait * 1000;
    }
}
*/
