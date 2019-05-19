package script.tasks;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class Banking extends Task {

    @Override
    public boolean validate() {
        return (Inventory.isFull() || !Beggar.banked) && !Beggar.trading &&
                Beggar.location.getBegArea().contains(Players.getLocal()) && Inventory.getCount(true, 995) < 25;
    }

    @Override
    public int execute() {
        Log.info("Banking");
        if (!Bank.isOpen()) {
            Bank.open();
            return 1000;
        }
        if (Bank.isOpen()) {
            Bank.depositInventory();
            Time.sleep(2000);
            Bank.withdrawAll(995);
            Time.sleep(5000);
            Beggar.banked = true;
        }
        Bank.close();
        Time.sleepUntil(() -> !Bank.isOpen(), 2000);
        Beggar.startC = Inventory.getCount(true, 995);
        return 1000;
    }
}
