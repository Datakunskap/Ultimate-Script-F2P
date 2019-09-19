package script.chocolate.tasks;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.runetek.api.input.menu.ActionOpcodes;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.chocolate.Main;

public class Grind extends Task {

    private Main main;

    public Grind(Main main) {
        this.main = main;
    }

    @Override
    public boolean validate() {
        return !main.restock && !main.isMuling && main.barCount >= 0;
    }

    @Override
    public int execute() {
        main.closeGE();

        if (Inventory.containsAnyExcept(Main.KNIFE, Main.BAR, Main.DUST) ||
                Inventory.containsOnly(Main.KNIFE, Main.DUST) ||
                Inventory.containsOnly(Main.KNIFE) || Inventory.containsOnly(Main.BAR) ||
                Inventory.isEmpty()) {

            if (!Bank.isOpen()) {
                Bank.open();
            } else {
                Bank.depositInventory();
                Time.sleepUntil(Inventory::isEmpty, 5000);
                if (Bank.contains(Main.BAR)) {
                    if (Time.sleepUntil(() ->  Bank.getCount(Main.BAR) > 0, 10000)) {
                        main.totalMade += main.barCount > Bank.getCount(Main.BAR) ? main.barCount - Bank.getCount(Main.BAR) : 0;
                        main.barCount = Bank.getCount(Main.BAR);
                    }
                    Log.info("Bars left: " + main.barCount + "  |  Dust made: " + main.totalMade);
                    getSupplies();
                } else {
                    Log.fine("Grind done  |  Total dust made: " + main.totalMade);
                    main.totalMade += main.barCount;
                    main.barCount = 0;
                    main.restock = true;
                    Bank.close();
                }
            }
        } else if (Inventory.contains(Main.KNIFE, Main.BAR) && Bank.isClosed()) {
            if (!Tabs.isOpen(Tab.INVENTORY))
                Tabs.open(Tab.INVENTORY);
            Item knife = Inventory.getFirst(Main.KNIFE);
            for (Item i : Inventory.getItems(x -> x.getId() == Main.BAR)) {

                knife.interact(ActionOpcodes.USE_ITEM);
                i.interact(ActionOpcodes.ITEM_ON_ITEM);

                Time.sleep(600);
            }
        }
        else {
            if (Bank.isOpen())
                Bank.close();
            Log.severe("FAILED Grinding");
        }

        return Random.high(500, 800);
    }

    private void getSupplies() {
        Bank.withdraw(Main.KNIFE, 1);
        Time.sleepUntil(() -> Inventory.contains(Main.KNIFE), 8000);
        Bank.withdrawAll(Main.BAR);
        Time.sleepUntil(() -> Inventory.contains(Main.BAR), 8000);
        Bank.close();
        Time.sleepUntil(Bank::isClosed, 5000);
    }
}
