package script.tasks;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class Banking extends Task {

    @Override
    public boolean validate() {
        return (Inventory.getCount(true, "Coins") < 25 ||
                Inventory.isFull()) &&
                !Beggar.trading;
    }

    @Override
    public int execute() {
        Log.info("Banking");
        if (!Bank.isOpen()) {
            Bank.open();
            return 1000;
        }
        if(Inventory.getCount(true, "Coins") < 25) {
            Bank.withdraw(995, Integer.MAX_VALUE);
            Time.sleep(3000);
        }
        return 1000;
    }
}
