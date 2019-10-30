package script.fighter.wrappers;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.tab.EquipmentSlot;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;
import script.beg.TradePlayer;
import script.fighter.config.Config;
import script.fighter.nodes.combat.BackToFightZone;
import script.fighter.services.PriceCheckService;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;

public class BankWrapper {

    private static int bankValue = -1;
    private static int inventoryValue = -1;
    private static int startingValue;
    private static boolean tradeRestricted = true;
    private static StopWatch startValueTimer;

    public static int getTotalValue() {
        return bankValue + inventoryValue;
    }

    public static int getBankValue() {
        return bankValue;
    }

    public static int getInventoryValue() {
        return inventoryValue;
    }

    public static int getTotalValueGained() {
        return (getTotalValue() - (startingValue > 0 ? startingValue : getTotalValue()));
    }

    public static boolean isTradeRestricted() {
        return tradeRestricted;
    }

    public static void setTradeRestricted(boolean isTradeRestricted) {
        tradeRestricted = isTradeRestricted;
    }

    public static void updateBankValue() {
        boolean includeTradeRestricted = !isTradeRestricted();
        int newValue = PriceCheckService.getBankValue(includeTradeRestricted);

        if (bankValue == -1) {
            startingValue += newValue;
        }

        bankValue = newValue;
    }

    public static void updateInventoryValue() {
        boolean includeTradeRestricted = !isTradeRestricted();
        int newValue = PriceCheckService.getInventoryValue(includeTradeRestricted);

        if (inventoryValue == -1) {
            startValueTimer = StopWatch.start();
        }
        if (startValueTimer != null && startValueTimer.exceeds(Duration.ofSeconds(10))) {
            startingValue += newValue;
            startValueTimer = null;
        }

        inventoryValue = newValue;
    }

    private static void openAndDepositAll(boolean keepAllCoins, int numCoinsToKeep,
                                          boolean keepRunes, boolean keepEquipment, String... itemsToKeep) {
        //Log.fine("Depositing Inventory");
        while (!openNearest() && Game.isLoggedIn()) {
            Time.sleep(1000);
        }

        Bank.depositInventory();
        Time.sleepUntil(Inventory::isEmpty, 2000, 8000);
        Time.sleep(300, 600);
        inventoryValue = 0;
        updateBankValue();


        if (numCoinsToKeep > 0) {
            Bank.withdraw(995, numCoinsToKeep);
            Time.sleepUntil(()
                            -> Inventory.contains(995) && Inventory.getCount(true, 995) >= numCoinsToKeep,
                    1000, 5000);
        }
        if (keepAllCoins) {
            Bank.withdrawAll(995);
            Time.sleepUntil(() -> Inventory.contains(995), 1000, 5000);
        }
        if (itemsToKeep != null && itemsToKeep.length > 0) {
            for (String i : itemsToKeep) {
                Bank.withdrawAll(x -> x.getName().toLowerCase().equals(i.toLowerCase()));
                Time.sleepUntil(() -> Inventory.contains(x -> x.getName().toLowerCase().equals(i.toLowerCase())),
                        2000, 8000);
            }
        }
        if (keepRunes) {
            Log.info("Withdrawing Runes");
            HashSet<String> runes = Config.getProgressive().getRunes();
            for (String rune : runes) {
                Bank.withdrawAll(x -> x.getName().toLowerCase().equals(rune.toLowerCase()));
                Time.sleepUntil(() -> Inventory.contains(x -> x.getName().toLowerCase().equals(rune.toLowerCase())),
                        2000, 8000);
            }
        }
        if (keepEquipment) {
            Log.info("Withdrawing Equipment");
            HashMap<EquipmentSlot, String> equipmentMap = Config.getProgressive().getEquipmentMap();
            for (String equipment : equipmentMap.values()) {
                Bank.withdraw(x -> x.getName().toLowerCase().equals(equipment.toLowerCase()), 1);
                Time.sleepUntil(() -> Inventory.contains(x -> x.getName().toLowerCase().equals(equipment.toLowerCase())),
                        2000, 8000);
            }
        }

        updateBankValue();
    }

    public static void openAndDepositAll(int numCoinsToKeep, boolean keepRunes, boolean keepEquipment) {
        openAndDepositAll(false, numCoinsToKeep, keepRunes, keepEquipment);
    }

    public static void openAndDepositAll(boolean keepAllCoins, boolean keepRunes, boolean keepEquipment) {
        openAndDepositAll(keepAllCoins, 0, keepRunes, keepEquipment);
    }

    public static void openAndDepositAll(boolean keepAllCoins, String... itemsToKeep) {
        openAndDepositAll(keepAllCoins, 0, false, false, itemsToKeep);
    }

    public static void openAndDepositAll(int numCoinsToKeep, String... itemsToKeep) {
        openAndDepositAll(false, numCoinsToKeep, false, false, itemsToKeep);
    }

    public static void openAndDepositAll(boolean keepAllCoins) {
        openAndDepositAll(keepAllCoins, 0, false, false);
    }

    public static void openAndDepositAll(int numCoinsToKeep) {
        openAndDepositAll(false, numCoinsToKeep, false, false);
    }

    public static void openAndDepositAll(String... itemsToKeep) {
        openAndDepositAll(false, 0, false, false, itemsToKeep);
    }

    public static void withdrawSellableItems() {
        if (!Bank.getWithdrawMode().equals(Bank.WithdrawMode.NOTE)) {
            Bank.setWithdrawMode(Bank.WithdrawMode.NOTE);
            Time.sleep(800, 1250);
        }

        Item[] sellables = Bank.getItems(i -> i.isExchangeable()
                && !Config.getProgressive().getEquipmentMap().containsValue(i.getName().toLowerCase())
                && (!isTradeRestricted() || !TradePlayer.isTradeRestrictedItem(i.getName())));

        for (Item s : sellables) {
            Bank.withdrawAll(s.getName());
            Time.sleepUntil(() -> Inventory.contains(s.getName()), 1500, 8000);
        }

        updateBankValue();
        updateInventoryValue();
    }

    public static boolean openNearest() {
        if (Bank.isOpen()) {
            return true;
        }
        if (BackToFightZone.shouldEnableRun()) {
            BackToFightZone.enableRun();
        }
        if (Config.getProgressive().isOgress()) {
            if (OgressWrapper.CORSAIR_COVE[0].contains(Players.getLocal())
                    || OgressWrapper.CORSAIR_COVE[1].contains(Players.getLocal())
                    || BankLocation.getNearest() == BankLocation.CORSAIR_COVE) {

                if (TeleportWrapper.tryTeleport(Spell.Modern.FALADOR_TELEPORT)) {

                }
                if (TeleportWrapper.tryTeleport(Spell.Modern.TELEOTHER_FALADOR)) {

                }
                if (TeleportWrapper.tryTeleport(true)) {

                }
                if (TeleportWrapper.tryTeleport(Spell.Modern.LUMBRIDGE_TELEPORT)) {

                }
                if (TeleportWrapper.tryTeleport(Spell.Modern.TELEOTHER_LUMBRIDGE)) {

                }
            }
            GEWrapper.walkToGE();
        }

        return Bank.open();
    }
}
