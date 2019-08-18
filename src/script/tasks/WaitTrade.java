package script.tasks;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.runetek.api.input.menu.ActionOpcodes;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;
import script.chocolate.Main;
import script.data.Chocolate;

public class WaitTrade extends Task {

    private int min;
    private int max;
    private boolean waitSet = false;

    private Beggar main;
    private Chocolate chocolate;

    public WaitTrade(Beggar beggar, Chocolate chocolate){
        main = beggar;
        this.chocolate = chocolate;
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
        int grindTimeout = timeout - 16;
        timeout -= grindTimeout;

        /*while (!main.trading && !Trade.isOpen(false) && validateGrind()) {
            Time.sleep(executeGrind());
        }*/

        if (Time.sleepUntil(() -> main.trading || Trade.isOpen(false), grindTimeout) ||
                Time.sleepUntil(() -> executeGrind() && (main.trading || Trade.isOpen(false)), 500, timeout)) {
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

    private boolean validateGrind() {
        return !chocolate.restock && !main.isMuling && chocolate.barCount > 0;
    }

    private boolean executeGrind() {
        if (validateGrind()) {
            chocolate.closeGE();

            if (Inventory.containsAnyExcept(Chocolate.KNIFE, Chocolate.BAR, Chocolate.DUST, 995) ||
                    Inventory.containsOnly(Chocolate.KNIFE, Chocolate.DUST, 995) || Inventory.isEmpty()) {
                if (!Bank.isOpen()) {
                    Bank.open();
                } else {
                    Bank.depositInventory();
                    Time.sleepUntil(Inventory::isEmpty, 5000);
                    if (Bank.contains(Chocolate.BAR)) {
                        Time.sleepUntil(() -> Bank.getCount(Chocolate.BAR) > 0, 5000);
                        chocolate.barCount = Bank.getCount(Chocolate.BAR);
                        Log.info("Bars left: " + chocolate.barCount + "  |  Dust made: " + chocolate.totalMade);
                        getSupplies();
                    } else {
                        Log.fine("Grind done  |  Total dust made: " + chocolate.totalMade);
                        chocolate.barCount = 0;
                        chocolate.restock = true;
                        Bank.close();
                    }
                }
            } else if (Inventory.contains(Chocolate.KNIFE, Chocolate.BAR, 995) && Bank.isClosed()) {
                Item knife = Inventory.getFirst(Chocolate.KNIFE);
                for (Item i : Inventory.getItems(x -> x.getId() == Chocolate.BAR)) {

                    knife.interact(ActionOpcodes.USE_ITEM);
                    i.interact(ActionOpcodes.ITEM_ON_ITEM);

                    chocolate.totalMade ++;

                    Time.sleep(600);

                    if (main.trading || Trade.isOpen(false)) {
                        break;
                    }
                    Time.sleep(600);
                }
            } else {
                Log.severe("FAILED Grinding");
            }

            //return Random.high(500, 800);
        }
        return true;
    }

    private void getSupplies() {
        Bank.withdrawAll(995);
        Time.sleepUntil(() -> Inventory.contains(995), 5000);
        Bank.withdraw(Chocolate.KNIFE, 1);
        Time.sleepUntil(() -> Inventory.contains(Chocolate.KNIFE), 5000);
        Bank.withdrawAll(Chocolate.BAR);
        Time.sleepUntil(() -> Inventory.contains(Chocolate.BAR), 5000);
        Bank.close();
        Time.sleepUntil(Bank::isClosed, 5000);
    }

    private void setMinMaxWait() {
        min = main.minWait * 1000;
        max = main.maxWait * 1000;
    }
}
