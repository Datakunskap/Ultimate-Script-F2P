package script.tasks;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;
import script.chocolate.Main;

public class Banking extends Task {

    private Beggar main;
    private Main chocolate;

    public Banking(Beggar beggar){
        main = beggar;
    }

    public Banking(Main chocolate) { this.chocolate = chocolate; }

    @Override
    public boolean validate() {
        return (Inventory.isFull() || !main.banked) && !main.trading &&
                main.location.getBegArea().contains(Players.getLocal()) && Inventory.getCount(true, 995) < 25;
    }

    @Override
    public int execute() {
        Log.info("Banking");
        if (!Bank.isOpen()) {
            Bank.open();
            return 1000;
        }
        Bank.isOpen();
        Bank.depositInventory();
        Time.sleep(2000);
        Bank.withdrawAll(995);
        Time.sleep(5000);
/*        if (Bank.contains(Main.KNIFE) && Bank.contains(Main.DUST, Main.KNIFE))
            main.startChocBeg = true;
        if (Inventory.contains(Main.KNIFE) && Inventory.contains(Main.DUST, Main.KNIFE))
            main.startChocBeg = true;*/
        main.banked = true;
        Bank.close();
        Time.sleepUntil(() -> !Bank.isOpen(), 2000);
        main.startC = Inventory.getCount(true, 995);

        return 1000;
    }

    int executeChocolate() {
        Log.fine("Banking");
        openAndDepositAll();

        if (checkAtMuleAmnt()) {
            main.muleChocBeg = true;
            return 1000;
        }

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

        chocolate.startTime = System.currentTimeMillis();
        return 1000;
    }

    void calcSpendAmount(int qBought) {
        // Calculate GP to spend
        chocolate.gp = (Bank.isOpen()) ? Bank.getCount(995) : Inventory.getCount(true, 995);
        chocolate.gp -= Beggar.SAVE_BEG_GP;
        if (!chocolate.hasKnife)
            chocolate.gp -= chocolate.knifePrice;
        chocolate.setPrices(true);
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
            chocolate.barCount = Bank.getCount(Main.BAR);
        }
    }

    boolean needRestock() {
        if (Bank.contains(Main.KNIFE)) {
            chocolate.hasKnife = true;
            if (Bank.contains(Main.BAR)) {
                Log.info("Restock Unnecessary");
                chocolate.sold = false;
                chocolate.checkedBank = false;
                chocolate.restock = false;
                chocolate.closeGE();
                chocolate.startTime = System.currentTimeMillis();
                chocolate.barCount = Bank.getCount(Main.BAR);
                return false;
            }
        }
        return true;
    }

    boolean checkAtMuleAmnt() {
        if (Bank.contains(995) && Bank.getCount(995) >= main.muleAmnt) {
            return true;
        }
        return false;
    }
}
