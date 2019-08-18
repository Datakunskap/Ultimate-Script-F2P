package script.tasks;

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
import script.data.Chocolate;
import script.tanner.ExGrandExchange;
import script.tanner.data.Location;

import java.util.Objects;

public class BuyGE extends Task {

    private Beggar beggar;
    private Chocolate chocolate;
    private Banking banking;
    private int buyQuantity;

    public BuyGE(Beggar beggar, Chocolate chocolate) {
        this.chocolate = chocolate;
        this.beggar = beggar;
        banking = new Banking(chocolate);
    }

    @Override
    public boolean validate() {
        return chocolate.sold && chocolate.restock && Location.GE_AREA.containsPlayer() && !beggar.isMuling;
    }

    @Override
    public int execute() {

        if (!chocolate.checkedBank) {
            chocolate.checkedBank = true;
            return banking.executeChocolate();
        }

        if (Inventory.contains(Chocolate.KNIFE) || Inventory.contains(Chocolate.KNIFE + 1)) {
            chocolate.hasKnife = true;
        }

        if (!GrandExchange.isOpen()) {
            Npc n = Npcs.getNearest(x -> x != null && x.getName().contains("Grand Exchange Clerk"));
            if (n != null) {
                Time.sleepUntil(() -> n.interact("Exchange"), 1000, 10000);
                Time.sleep(700, 1300);
            }
            return 1000;
        }

        if (!chocolate.hasKnife && GrandExchange.getFirst(x -> x != null && x.getItemId() == Chocolate.KNIFE) == null) {
            buyKnife();
            chocolate.justBoughtKnife = true;
        }

        // sets quantity to buy
        buyQuantity = chocolate.gp / (chocolate.buyPrice + chocolate.incBuyPrice);

        // Checks if done buying
        if (GrandExchange.getFirstActive() == null && (Inventory.contains(Chocolate.BAR) ||
                Inventory.contains(Chocolate.BAR + 1)) && chocolate.hasKnife) {

            if (Time.sleepUntil(() -> (Inventory.getCount(true, Chocolate.BAR) +
                    Inventory.getCount(true, Chocolate.BAR + 1)) >= buyQuantity, 5000)) {
                Log.fine("Done buying");
                chocolate.sold = false;
                chocolate.checkedBank = false;
                chocolate.restock = false;
                chocolate.closeGE();
                chocolate.startTime = System.currentTimeMillis();
                if (chocolate.buyPriceChng) {
                    chocolate.incBuyPrice = chocolate.incBuyPrice / (chocolate.timesPriceChanged * chocolate.intervalAmnt);
                    chocolate.buyPriceChng = false;
                }

                banking.openAndDepositAll();
                fallbackGEPrice();
                Bank.close();
                Time.sleepUntil(() -> !Bank.isOpen(), 5000);
                chocolate.justBoughtKnife = false;
                //chocolate.teleportHome();
                return 800;
            }
        }

        // Buys hides -> having issues with Buraks toBank param so handled manually
        if (GrandExchange.getFirstActive() == null && ExGrandExchange.buy(Chocolate.BAR, buyQuantity, (chocolate.buyPrice + chocolate.incBuyPrice), false)) {
            Log.fine("Buying Bars");
        } else {
            Log.info("Waiting to complete  |  Time: " + chocolate.elapsedSeconds / 60 + "min(s)  |  Price changed " + chocolate.timesPriceChanged + " time(s)");
            if (!GrandExchange.isOpen()) {
                Npcs.getNearest("Grand Exchange Clerk").interact("Exchange");
                Time.sleep(chocolate.randInt(700, 1300));
            }
            Time.sleepUntil(() -> GrandExchange.getFirst(Objects::nonNull).getProgress().equals(RSGrandExchangeOffer.Progress.FINISHED), 2000, 10000);
            GrandExchange.collectAll();
            Keyboard.pressEnter();
            Time.sleep(1500);
        }

        // Increases buy price if over time
        chocolate.checkTime();
        if (chocolate.elapsedSeconds > chocolate.resetGeTime * 60 && GrandExchange.getFirstActive() != null && chocolate.hasKnife) {
            Log.fine("Increasing bar price by: " + chocolate.intervalAmnt);
            while(GrandExchange.getFirstActive() != null) {
                Time.sleepUntil(() -> GrandExchange.getFirst(Objects::nonNull).abort(), 1000, 5000);
                GrandExchange.collectAll();
                Time.sleep(5000);
                GrandExchange.collectAll();
            }
            chocolate.incBuyPrice += chocolate.intervalAmnt;
            //chocolate.setPrices(true);
            banking.calcSpendAmount((Inventory.getCount(true, x -> x != null && x.getId() == Chocolate.BAR) +
                    Inventory.getCount(true, x -> x != null && x.getId() == Chocolate.BAR + 1)));
            chocolate.startTime = System.currentTimeMillis();
            chocolate.buyPriceChng = true;
            chocolate.timesPriceChanged++;
        }

        // Checks and handles stuck in setup
        if (chocolate.elapsedSeconds > (chocolate.resetGeTime + 1) * 60 && GrandExchange.getFirstActive() == null && GrandExchangeSetup.isOpen()) {
            GrandExchange.open(GrandExchange.View.OVERVIEW);
            if (!Time.sleepUntil(() -> !GrandExchangeSetup.isOpen(), 5000)) {
                chocolate.closeGE();
            }
            chocolate.startTime = System.currentTimeMillis();
        }

        GrandExchange.collectAll();
        Keyboard.pressEnter();
        return 1000;
    }

    private void buyKnife() {
        Log.fine("Buying Knife");
        if (ExGrandExchange.buy(Chocolate.KNIFE, 1, chocolate.knifePrice, false)) {
            Time.sleep(600);
            GrandExchange.collectAll();
            Time.sleep(Random.mid(300, 600));
            GrandExchange.collectAll();
            Time.sleep(Random.mid(300, 600));
        }
    }

    private void fallbackGEPrice() {

        if (chocolate.usingBuyFallback && Bank.isOpen() && Bank.contains(995)) {
            int expectedGP = chocolate.gp - (buyQuantity * chocolate.buyPrice);
            int actualGP = chocolate.justBoughtKnife ? Bank.getCount(995) - chocolate.knifePrice : Bank.getCount(995);
            actualGP -= Beggar.SAVE_BEG_GP;

            if (actualGP > expectedGP) {
                chocolate.lastPrices[1] = chocolate.buyPrice - ((actualGP - expectedGP) / buyQuantity);
                Log.fine("GE buy price found: " + chocolate.lastPrices[1]);
            }
        }
    }
}
