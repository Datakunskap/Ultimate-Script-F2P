package script.chocolate.tasks;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.ui.Log;
import script.chocolate.Main;

class Banking {

    private Main main;

    Banking(Main main) {
        this.main = main;
    }

    int execute() {
        Log.fine("Banking");
        openAndDepositAll();

        if (!needRestock())
            return 1000;

        calcSpendAmount(0);

        // Withdraw GP
        Time.sleep(500, 1500);
        Bank.withdrawAll(995);
        Time.sleepUntil(() -> !Bank.contains(995), 5000);

        // Withdraw leathers to sell
        if (Bank.contains(Main.DUST)) {
            Bank.setWithdrawMode(Bank.WithdrawMode.NOTE);
            Time.sleepUntil(() -> Bank.getWithdrawMode().equals(Bank.WithdrawMode.NOTE), 5000);
            Bank.withdrawAll(Main.DUST);
            Time.sleepUntil(() -> !Bank.contains(Main.DUST), 5000);
        }

        Bank.close();
        Time.sleepUntil(() -> !Bank.isOpen(), 2000);

        main.startTime = System.currentTimeMillis();
        return 1000;
    }

    void calcSpendAmount(int qBought) {
        // Calculate GP to spend
        main.gp = (Bank.isOpen()) ? Bank.getCount(995) : Inventory.getCount(true, 995);
        if (!main.hasKnife)
            main.gp -= main.knifePrice;
        main.setPrices(true);
    }

    void openAndDepositAll() {
        Log.fine("Depositing Inventory");
        while (!Bank.isOpen()) {
            Bank.open();
            Time.sleep(1000);
        }

        Bank.depositInventory();
        Time.sleepUntil(Inventory::isEmpty, 5000);

        if (Bank.contains(Main.BAR)) {
            main.barCount = Bank.getCount(Main.BAR);
        }
    }

    boolean needRestock() {
        if (Bank.contains(Main.KNIFE)) {
            main.hasKnife = true;
            if (Bank.contains(Main.BAR)) {
                Log.info("Restock Unnecessary");
                main.sold = false;
                main.checkedBank = false;
                main.restock = false;
                main.closeGE();
                main.startTime = System.currentTimeMillis();
                main.barCount = Bank.getCount(Main.BAR);
                return false;
            }
        }
        return true;
    }
}
