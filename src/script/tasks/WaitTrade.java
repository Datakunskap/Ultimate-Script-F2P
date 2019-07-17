package script.tasks;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class WaitTrade extends Task {

    private int min;
    private int max;
    private boolean waitSet = false;

    private Beggar main;

    public WaitTrade(Beggar beggar){
        main = beggar;
    }

    @Override
    public boolean validate() {
        return !main.beg && !main.walk && !main.trading; //&& !main.sendTrade;
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

        if (Time.sleepUntil(() -> main.trading || Trade.isOpen(false), timeout)) {
            if(Trade.isOpen()){
                main.sentTradeInit = true;
            }
            main.trading = true;
            main.processSentTrade = true;
            main.walk = false;
            main.sendTrade = false;
            main.beg = false;
            return Beggar.randInt(1500, 2500);
        }

        Tabs.open(Tab.INVENTORY);
        main.walk = true;
        main.beg = true;
        main.sendTrade = true;

        main.checkWorldHopTime();
        return 500;
    }

    private void setMinMaxWait() {
        min = main.minWait * 1000;
        max = main.maxWait * 1000;
    }
}
