package script.fighter.wrappers;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.ui.Log;
import script.beg.TradePlayer;
import script.fighter.config.Config;
import script.fighter.nodes.combat.BackToFightZone;

public class BankWrapper {

    public static boolean openNearest() {
        if(Bank.isOpen()) {
            return true;
        }
        if (BackToFightZone.shouldEnableRun()) {
            BackToFightZone.enableRun();
        }
        return Bank.open();
    }

    private static void openAndDepositAll(boolean keepAllCoins, int numCoinsToKeep, String... itemsToKeep) {
        Log.fine("Depositing Inventory");
        while (!openNearest()) {
            Time.sleep(1000);
        }

        Bank.depositInventory();
        Time.sleepUntil(Inventory::isEmpty, 5000);

        if (numCoinsToKeep > 0) {
            Bank.withdraw(995, numCoinsToKeep);
            Time.sleepUntil(() -> Inventory.contains(995) && Inventory.getCount(true, 995) >= numCoinsToKeep, 5000);
        }
        if (keepAllCoins) {
            Bank.withdrawAll(995);
            Time.sleepUntil(() -> Inventory.contains(995), 5000);
        }
        if (itemsToKeep != null && itemsToKeep.length > 0) {
            for (String i : itemsToKeep) {
                i = i.toUpperCase().charAt(0) + i.substring(1).toLowerCase();
                Bank.withdrawAll(i);
                String finalI = i;
                Time.sleepUntil(() -> Inventory.contains(finalI), 5000);
            }
        }
    }
    public static void openAndDepositAll(boolean keepAllCoins, String... itemsToKeep) {
        openAndDepositAll(keepAllCoins, 0, itemsToKeep);
    }
    public static void openAndDepositAll(int numCoinsToKeep, String... itemsToKeep) {
        openAndDepositAll(false, numCoinsToKeep, itemsToKeep);
    }
    public static void openAndDepositAll(boolean keepAllCoins) {
        openAndDepositAll(keepAllCoins, 0, (String) null);
    }
    public static void openAndDepositAll(int numCoinsToKeep) {
        openAndDepositAll(false, numCoinsToKeep, (String) null);
    }
    public static void openAndDepositAll(String... itemsToKeep) {
        openAndDepositAll(false, 0, itemsToKeep);
    }

    public static void withdrawSellableItems() {
        if (!Bank.getWithdrawMode().equals(Bank.WithdrawMode.NOTE)) {
            Bank.setWithdrawMode(Bank.WithdrawMode.NOTE);
        }
        Bank.withdrawAll(i -> i.isExchangeable() && !TradePlayer.isTradeRestrictedItem(i.getName()) &&
                !Config.getProgressive().getRunes().contains(i.getName().toLowerCase()));
        Time.sleep(2000);
    }
}
