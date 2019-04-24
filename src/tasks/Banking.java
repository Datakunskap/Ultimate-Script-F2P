package tasks;

import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.script.task.Task;

public class Banking extends Task {

    @Override
    public boolean validate() {
        return Inventory.isFull() && Inventory.getCount(true, "Coins") > 20000;
    }

    @Override
    public int execute() {
        if (!Bank.isOpen()) {
            Bank.open();
            return 1000;
        }

        Bank.depositInventory();
        return 1000;
    }
}
