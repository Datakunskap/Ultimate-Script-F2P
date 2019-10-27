package script.fighter.nodes.mule;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.*;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.Script;
import org.rspeer.ui.Log;
import script.Beggar;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.framework.Node;
import script.fighter.nodes.combat.BackToFightZone;
import script.fighter.wrappers.BankWrapper;
import script.fighter.wrappers.GEWrapper;
import script.fighter.wrappers.TeleportWrapper;
import script.tanner.data.Location;

import java.io.*;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Mule extends Node {

    private boolean trading;
    private int begWorld = -1;
    private static final String MULE_FILE_PATH = Script.getDataDirectory() + "\\mule.txt";
    private boolean banked;
    private String status2;
    private boolean soldItems;

    private Fighter main;

    public Mule(Fighter main) {
        this.main = main;
    }

    private void loginMule() {
        String status1;
        try {
            File file = new File(MULE_FILE_PATH);

            if (!file.exists()) {
                file.createNewFile();
            }
            PrintWriter pw = new PrintWriter(file);
            pw.println("mule");
            pw.close();

            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            while (((status1 = br.readLine())) != null) {
                Log.info(status1);
            }

            br.close();
        } catch (IOException e) {
            Log.info("File not found");
        }

    }

    public static void logoutMule() {
        try {
            File file = new File(MULE_FILE_PATH);

            if (!file.exists()) {
                Log.info("Logout file not found");
            }
            PrintWriter pw = new PrintWriter(file);
            pw.println("done");
            pw.close();

            Log.info("done");

        } catch (IOException e) {
            Log.info("File not found");
        }
    }

    @Override
    public boolean validate() {
        return (!GEWrapper.isSellItems() && BankWrapper.getTotalValue() >= Beggar.OGRESS_MULE_AMOUNT) || trading;
    }

    @Override
    public int execute() {
        main.invalidateTask(this);
        Config.isMuleing = true;

        if (!Location.GE_AREA.containsPlayer() && !banked) {
            status2 = "Walking to GE";
            TeleportWrapper.tryTeleport(false);
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
            HashSet<String> runes = Config.getProgressive().getRunes();

            BankWrapper.openAndDepositAll();
            Time.sleepUntil(Inventory::isEmpty, 2000, 8000);
            Bank.setWithdrawMode(Bank.WithdrawMode.NOTE);
            Time.sleepUntil(() -> Bank.getWithdrawMode().equals(Bank.WithdrawMode.NOTE), 2000, 8000);

            if (BankWrapper.isTradeRestricted()) {
                Log.fine("Withdrawing Coins");
                Item coins = Bank.getFirst("Coins");
                if (coins != null) {
                    Bank.withdraw("Coins", coins.getStackSize() - Beggar.OGRESS_START_GP);
                }
            } else {
                Log.fine("Withdrawing Trade Restricted Items!!!");
                Item[] restrictedItems = Bank.getItems(i -> !runes.contains(i.getName().toLowerCase()));

                for(Item i : restrictedItems) {
                    Bank.withdrawAll(i.getId());
                    Time.sleepUntil(() -> !Inventory.contains(i.getId()), 2000, 10_000);
                    if (i.getName().equals("Coins")) {
                        Bank.deposit(i.getId(), Beggar.OGRESS_START_GP);
                    }
                }
            }


            Time.sleep(300, 800);
            BankWrapper.updateBankValue();
            BankWrapper.updateInventoryValue();
            Bank.close();
            return Fighter.getLoopReturn();
        }

        if (Worlds.getCurrent() != Beggar.MULE_WORLD) {
            begWorld = Worlds.getCurrent();
            WorldHopper.hopTo(Beggar.MULE_WORLD);

            if (Dialog.isOpen()) {
                if (Dialog.canContinue()) {
                    Dialog.processContinue();
                }
                Dialog.process(x -> x != null && x.toLowerCase().contains("future"));
                Dialog.process(x -> x != null && (x.toLowerCase().contains("switch") || x.toLowerCase().contains("yes")));
                Time.sleepUntil(() -> !Dialog.isProcessing(), 10000);
            }

            Time.sleepUntil(() -> Worlds.getCurrent() == Beggar.MULE_WORLD && Players.getLocal() != null, 10000);
            return Fighter.getLoopReturn();
        }

        if (Dialog.canContinue()) {
            Dialog.processContinue();
            Time.sleep(1000);
        }

        loginMule();

        if (!Beggar.MULE_AREA.getMuleArea().contains(Players.getLocal())) {

            if (BackToFightZone.shouldEnableRun()) {
                BackToFightZone.enableRun();
            }
            Movement.walkTo(Beggar.MULE_AREA.getMuleArea().getCenter());

            return Fighter.getLoopReturn();
        }


        Player mulePlayer = Players.getNearest(Beggar.MULE_NAME);

        if (mulePlayer != null && Beggar.MULE_AREA.getMuleArea().contains(mulePlayer)) {

            /*boolean notRestricted = main.getRuntime().exceeds(Duration.ofHours(24));
            Item[] dropItems = null;

            if (!notRestricted) {
                dropItems = Inventory.getItems(i -> TradePlayer.isTradeRestrictedItem(i.getName()));
            }

            if (dropItems != null && dropItems.length > 0) {
                for (Item i : dropItems) {
                    mulePlayer = Players.getNearest(Beggar.MULE_NAME);
                    if (mulePlayer != null && Beggar.MULE_AREA.getMuleArea().contains(mulePlayer)) {
                        i.interact("Drop");
                        Time.sleep(600, 1200);
                    } else {
                        break;
                    }
                }
                return Fighter.getLoopReturn();
            }*/

            if (!Inventory.isEmpty()) {

                if (Players.getNearest(Beggar.MULE_NAME) != null && !Trade.isOpen()) {
                    Players.getNearest(Beggar.MULE_NAME).interact("Trade with");
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
                                Trade.offerAll(o.getId());
                                Time.sleepUntil(() -> Trade.contains(true, o.getId()), 2000, 8000);
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
                            logoutMule();
                            trading = false;
                            BankWrapper.updateInventoryValue();
                            //main.amntMuled += (Coins - main.muleKeep);
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
        super.onInvalid();
    }

    @Override
    public String status() {
        return status2;
    }
}

