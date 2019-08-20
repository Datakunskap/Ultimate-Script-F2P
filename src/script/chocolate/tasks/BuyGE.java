package script.chocolate.tasks;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;
import script.chocolate.Main;
import script.tanner.ExGrandExchange;
import script.tanner.data.Location;

import java.util.Objects;

public class BuyGE extends Task {

    private Main main;
    private Beggar beggar;
    private Banking banking;
    private int buyQuantity;

    public BuyGE (Main main, Beggar beggar) {
        this.main = main;
        this.beggar = beggar;
        banking = new Banking(main);
    }

    @Override
    public boolean validate() {
        return main.sold && main.restock && Location.GE_AREA.containsPlayer() && !main.isMuling;
    }

    @Override
    public int execute() {

        if (!main.checkedBank) {
            if (main.atGELimit) {
                StartTanning.execute(main, beggar);
            }

            main.checkedBank = true;
            return banking.execute();
        }

        if (Inventory.contains(Main.KNIFE) || Inventory.contains(Main.KNIFE + 1)) {
            main.hasKnife = true;
        }

        if (!GrandExchange.isOpen()) {
            Npc n = Npcs.getNearest(x -> x != null && x.getName().contains("Grand Exchange Clerk"));
            if (n != null) {
                Time.sleepUntil(() -> n.interact("Exchange"), 1000, 10000);
                Time.sleep(700, 1300);
            }
            return 1000;
        }

        if (!main.hasKnife && GrandExchange.getFirst(x -> x != null && x.getItemId() == Main.KNIFE) == null) {
            buyKnife();
            main.justBoughtKnife = true;
        }

        // sets quantity to buy
        buyQuantity = main.gp / (main.buyPrice + main.incBuyPrice);

        // Checks if at GE limit
        if (main.timesPriceChanged >= 3 && main.elapsedSeconds > 60) {
            buyQuantity = (main.totalMade + buyQuantity) - Main.BAR_GE_LIMIT;
            Log.severe("AT GE LIMIT");
            while(GrandExchange.getFirstActive() != null) {
                Time.sleepUntil(() -> GrandExchange.getFirst(Objects::nonNull).abort(), 1000, 5000);
                GrandExchange.collectAll();
                Time.sleep(5000);
                GrandExchange.collectAll();
            }
            main.atGELimit = true;
            main.sold = false;
        }

        // Checks if done buying
        if (GrandExchange.getFirstActive() == null && (Inventory.contains(Main.BAR) ||
                Inventory.contains(Main.BAR + 1)) && main.hasKnife) {

            if (Time.sleepUntil(() -> (Inventory.getCount(true, Main.BAR) +
                    Inventory.getCount(true, Main.BAR + 1)) >= buyQuantity, 5000)) {
                Log.fine("Done buying");
                main.sold = false;
                main.checkedBank = false;
                main.restock = false;
                main.closeGE();
                main.startTime = System.currentTimeMillis();
                if (main.buyPriceChng) {
                    main.incBuyPrice = main.incBuyPrice / (main.timesPriceChanged * main.intervalAmnt);
                    main.buyPriceChng = false;
                }

                banking.openAndDepositAll();
                fallbackGEPrice();
                Bank.close();
                Time.sleepUntil(() -> !Bank.isOpen(), 5000);
                main.justBoughtKnife = false;
                //main.teleportHome();
                return 800;
            }
        }

        // Buys hides -> having issues with Buraks toBank param so handled manually
        if (GrandExchange.getFirstActive() == null && ExGrandExchange.buy(Main.BAR, buyQuantity, (main.buyPrice + main.incBuyPrice), false)) {
            Log.fine("Buying Bars");
        } else {
            Log.info("Waiting to complete  |  Time: " + main.elapsedSeconds / 60 + "min(s)  |  Price changed " + main.timesPriceChanged + " time(s)");
            if (!GrandExchange.isOpen()) {
                Npcs.getNearest("Grand Exchange Clerk").interact("Exchange");
                Time.sleep(main.randInt(700, 1300));
            }
            Time.sleepUntil(() -> GrandExchange.getFirst(Objects::nonNull).getProgress().equals(RSGrandExchangeOffer.Progress.FINISHED), 2000, 10000);
            GrandExchange.collectAll();
            Keyboard.pressEnter();
            Time.sleep(1500);
        }

        // Increases buy price if over time
        main.checkTime();
        if (main.elapsedSeconds > main.resetGeTime * 60 && GrandExchange.getFirstActive() != null && main.hasKnife) {
            Log.fine("Increasing bar price by: " + main.intervalAmnt);
            while(GrandExchange.getFirstActive() != null) {
                Time.sleepUntil(() -> GrandExchange.getFirst(Objects::nonNull).abort(), 1000, 5000);
                GrandExchange.collectAll();
                Time.sleep(5000);
                GrandExchange.collectAll();
            }
            main.incBuyPrice += main.intervalAmnt;
            //main.setPrices(true);
            banking.calcSpendAmount((Inventory.getCount(true, x -> x != null && x.getId() == Main.BAR) +
                    Inventory.getCount(true, x -> x != null && x.getId() == Main.BAR + 1)));
            main.startTime = System.currentTimeMillis();
            main.buyPriceChng = true;
            main.timesPriceChanged++;
        }

        // Checks and handles stuck in setup
        if (main.elapsedSeconds > (main.resetGeTime + 1) * 60 && GrandExchange.getFirstActive() == null && GrandExchangeSetup.isOpen()) {
            GrandExchange.open(GrandExchange.View.OVERVIEW);
            if (!Time.sleepUntil(() -> !GrandExchangeSetup.isOpen(), 5000)) {
                main.closeGE();
            }
            main.startTime = System.currentTimeMillis();
        }

        GrandExchange.collectAll();
        Keyboard.pressEnter();
        return 1000;
    }

    private void buyKnife() {
        Log.fine("Buying Knife");
        if (ExGrandExchange.buy(Main.KNIFE, 1, main.knifePrice, false)) {
            Time.sleep(600);
            GrandExchange.collectAll();
            Time.sleep(Random.mid(300, 600));
            GrandExchange.collectAll();
            Time.sleep(Random.mid(300, 600));
        }
    }

    private void fallbackGEPrice() {

        if (main.usingBuyFallback && Bank.isOpen() && Bank.contains(995)) {
            int expectedGP = main.gp - (buyQuantity * main.buyPrice);
            int actualGP = main.justBoughtKnife ? Bank.getCount(995) - main.knifePrice : Bank.getCount(995);
            if (actualGP > expectedGP) {
                main.lastPrices[1] = main.buyPrice - ((actualGP - expectedGP) / buyQuantity);
                Log.fine("GE buy price found: " + main.lastPrices[1]);
            }
        }
    }
}
