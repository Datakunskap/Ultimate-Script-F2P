package script.fighter.nodes.mule;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.*;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;
import script.Script;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.framework.Node;
import script.fighter.nodes.combat.BackToFightZone;
import script.fighter.wrappers.BankWrapper;
import script.fighter.wrappers.GEWrapper;
import script.fighter.wrappers.TeleportWrapper;
import script.tanner.data.Location;

import java.util.NavigableMap;
import java.util.TreeMap;

public class Mule extends Node {

    private boolean trading;
    private int begWorld = -1;
    private static final String MULE_FILE_PATH = org.rspeer.script.Script.getDataDirectory() + "\\mule.txt";
    private boolean banked;
    private String status;
    private boolean soldItems;
    private boolean triedTeleport;
    private int gp;

    private Fighter main;

    public Mule(Fighter main) {
        this.main = main;
    }

    @Override
    public boolean validate() {
        return (!GEWrapper.isSellItems() && BankWrapper.getTotalValue() >= Script.OGRESS_MULE_AMOUNT) || trading;
    }

    @Override
    public int execute() {
        main.invalidateTask(this);
        Config.isMuleing = true;

        if (!Location.GE_AREA.containsPlayer() && !banked) {
            status = "Walking to GE";
            if (!triedTeleport) {
                TeleportWrapper.tryTeleport(false);
                triedTeleport = true;
            }
            GEWrapper.walkToGE();
            return Fighter.getLoopReturn();
        }
        if (GrandExchange.isOpen()) {
            GEWrapper.closeGE();
        }

        if (!soldItems) {
            GEWrapper.setSellItems(true);
            soldItems = true;
            return Fighter.getLoopReturn();
        }

        if (!banked) {
            Log.info("Withdrawing Items To Mule");
            banked = true;

            BankWrapper.openAndDepositAll(false);
            Time.sleepUntil(Inventory::isEmpty, 2000, 8000);
            Bank.setWithdrawMode(Bank.WithdrawMode.NOTE);
            Time.sleepUntil(() -> Bank.getWithdrawMode().equals(Bank.WithdrawMode.NOTE), 2000, 8000);

            Log.fine("Withdrawing Coins");
            Item coins = Bank.getFirst("Coins");
            if (coins != null) {
                gp = coins.getStackSize() - Script.OGRESS_START_GP;
                Bank.withdraw("Coins", gp);
            } else {
                Log.severe("Cant find coins");
            }
            Time.sleepUntil(() -> Inventory.contains("Coins"), 1000, 5000);

            BankWrapper.updateBankValue();
            Bank.close();
            Time.sleep(300, 800);
            BankWrapper.updateInventoryValue();
            return Fighter.getLoopReturn();
        }

        if (Worlds.getCurrent() != Script.MULE_WORLD) {
            begWorld = Worlds.getCurrent();
            WorldHopper.hopTo(Script.MULE_WORLD);

            if (Dialog.isOpen()) {
                if (Dialog.canContinue()) {
                    Dialog.processContinue();
                }
                Dialog.process(x -> x != null && x.toLowerCase().contains("future"));
                Dialog.process(x -> x != null && (x.toLowerCase().contains("switch") || x.toLowerCase().contains("yes")));
                Time.sleepUntil(() -> !Dialog.isProcessing(), 10000);
            }

            Time.sleepUntil(() -> Worlds.getCurrent() == Script.MULE_WORLD && Players.getLocal() != null, 10000);
            return Fighter.getLoopReturn();
        }

        if (Dialog.canContinue()) {
            Dialog.processContinue();
            Time.sleep(1000);
        }

        script.beg.Mule.loginMule();

        if (!Script.MULE_AREA.getMuleArea().contains(Players.getLocal())) {

            if (BackToFightZone.shouldEnableRun()) {
                BackToFightZone.enableRun();
            }
            Movement.walkTo(Script.MULE_AREA.getMuleArea().getCenter());

            return Fighter.getLoopReturn();
        }


        Player mulePlayer = Players.getNearest(Script.MULE_NAME);

        if (mulePlayer != null && Script.MULE_AREA.getMuleArea().contains(mulePlayer)) {

            /*boolean notRestricted = main.getRuntime().exceeds(Duration.ofHours(24));
            Item[] dropItems = null;

            if (!notRestricted) {
                dropItems = Inventory.getItems(i -> TradePlayer.isTradeRestrictedItem(i.getName()));
            }

            if (dropItems != null && dropItems.length > 0) {
                for (Item i : dropItems) {
                    mulePlayer = Players.getNearest(Script.MULE_NAME);
                    if (mulePlayer != null && Script.MULE_AREA.getMuleArea().contains(mulePlayer)) {
                        i.interact("Drop");
                        Time.sleep(600, 1200);
                    } else {
                        break;
                    }
                }
                return Fighter.getLoopReturn();
            }*/

            if (!Inventory.isEmpty()) {

                if (Players.getNearest(Script.MULE_NAME) != null && !Trade.isOpen()) {
                    Players.getNearest(Script.MULE_NAME).interact("Trade with");
                    Time.sleep(3000, 5000);
                }
                if (!Inventory.isEmpty()) {
                    if (Trade.isOpen(false)) {
                        trading = true;
                        // handle first trade window...
                        int attempts = 0;
                        while (Trade.isOpen(false)) {
                            attempts++;
                            Log.info("Entering trade offer");
                            Item[] tradeItems = Inventory.getItems();

                            for (Item o : tradeItems) {
                                Trade.offerAll(i -> i.getId() == o.getId());
                                Time.sleepUntil(() -> Trade.contains(true, i -> i.getId() == o.getId() && i.getStackSize() == o.getStackSize()), 2000, 8000);
                            }
                            if (Inventory.isEmpty()) {
                                Log.info("Trade entered & accepted");
                                Trade.accept();
                                Time.sleepUntil(() -> Trade.isOpen(true), 5000);
                                break;
                            }
                            if (attempts > 6) {
                                break;
                            }
                        }
                    }
                    if (Trade.isOpen(true)) {
                        // handle second trade window...
                        Time.sleep(500, 1500);
                        if (Trade.accept()) {
                            Time.sleep(3000);
                            Log.fine("Trade completed shutting down mule");
                            soldItems = false;
                            script.beg.Mule.logoutMule(Script.MULE_IP);
                            trading = false;
                            BankWrapper.updateInventoryValue();
                            main.getScript().amntMuled += gp;
                            //main.setRandMuleKeep(main.minKeep, main.maxKeep);
                            if (begWorld != -1) {
                                WorldHopper.hopTo(begWorld);
                                Time.sleepUntil(() -> Worlds.getCurrent() == begWorld, 10_000);
                            }
                            Time.sleep(8000, 10000);
                            Config.isMuleing = false;
                        }
                        Time.sleep(700);
                    }

                }
            }
        }

        return 500;
    }

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "B");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    @Override
    public void onInvalid() {
        banked = false;
        triedTeleport = false;
        BankWrapper.openAndDepositAll(false, true, false);
        super.onInvalid();
    }

    @Override
    public String status() {
        return status;
    }
}

