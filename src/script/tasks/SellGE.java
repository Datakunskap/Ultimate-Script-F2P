package script.tasks;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;
import script.data.Chocolate;
import script.tanner.data.Location;

import java.util.Objects;

public class SellGE extends Task {

    private Chocolate chocolate;
    private Banking banking;

    private Beggar beggar;
    //private StartChocolate startBegging;

    public SellGE(Chocolate chocolate, Beggar beggar) {
        this.chocolate = chocolate;
        this.beggar = beggar;
        banking = new Banking(chocolate);
    }

    /*private boolean checkStartBegging() {
        startBegging = new StartChocolate(chocolate, beggar, banking);
        return startBegging.execute();
    }*/

    @Override
    public boolean validate() {
        return beggar.startChocolate && chocolate.restock && !chocolate.sold && Location.GE_AREA.containsPlayer() && !beggar.isMuling;
    }

    @Override
    public int execute() {
        if (!chocolate.checkedBank) {
            chocolate.checkedBank = true;
            return banking.executeChocolate();
        }

        if (!GrandExchange.isOpen()) {
            Log.fine("Selling");
            Npc n = Npcs.getNearest(x -> x != null && x.getName().contains("Grand Exchange Clerk"));
            if (n != null) {
                Time.sleepUntil(() -> n.interact("Exchange"), 1000, 10000);
                Time.sleep(700, 1300);
            }
            return 1000;
        }

        if (GrandExchangeSetup.getItem() == null) {
            chocolate.geSet = false;
        }

        // needs older account
        /*if (!sellRechocolateingHides()) {
            GrandExchange.collectAll();
            return 1000;
        }*/

        // bc issues with Buraks ExGrandExchange when selling
        if (Inventory.contains(Chocolate.DUST_NOTE) || Inventory.contains(Chocolate.DUST)) {
            Log.fine("Selling dust");
            GrandExchange.createOffer(RSGrandExchangeOffer.Type.SELL);
            Time.sleep(800);
            GrandExchangeSetup.setItem(Chocolate.DUST_NOTE);
            if (Inventory.contains(Chocolate.DUST))
                GrandExchangeSetup.setItem(Chocolate.DUST);
            Time.sleepUntil(() -> GrandExchangeSetup.getItem() != null,5000);
            fallbackGEPrice();
            GrandExchangeSetup.setPrice(chocolate.sellPrice - chocolate.decSellPrice);
            Time.sleep(600);
            GrandExchangeSetup.setQuantity(9999999);
            Time.sleep(600);
            GrandExchangeSetup.confirm();
            Time.sleep(600);
            if(GrandExchangeSetup.getItem() != null) {
                chocolate.geSet = true;
            }

            GrandExchange.collectAll();
            Time.sleep(Random.mid(300, 600));
            GrandExchange.collectAll();
            Time.sleep(Random.mid(300, 600));
            Keyboard.pressEnter();
            chocolate.startTime = System.currentTimeMillis();
        }

        if (GrandExchange.getFirst(Objects::nonNull).getProgress().equals(RSGrandExchangeOffer.Progress.FINISHED) &&
                !Inventory.contains(Chocolate.DUST_NOTE) && !Inventory.contains(Chocolate.DUST)) {
            GrandExchange.collectAll();
            Time.sleep(Random.mid(300, 600));
            GrandExchange.collectAll();
            Time.sleep(Random.mid(300, 600));

            Log.info("Done selling");
            chocolate.sold = true;
            chocolate.checkedBank = false;
            chocolate.geSet = false;
            chocolate.decSellPrice = 0;
            chocolate.timesPriceChanged = 0;
            //chocolate.setHighestProfitLeather(false);

            /*if (checkStartBegging())
                return 1000;*/

        }

        if (GrandExchange.getFirstActive() == null && !GrandExchange.getFirst(Objects::nonNull).getProgress().equals(RSGrandExchangeOffer.Progress.FINISHED) && !Inventory.contains(Chocolate.DUST_NOTE) && !Inventory.contains(Chocolate.DUST)){
            Log.info("Done selling 2");
            chocolate.sold = true;
            chocolate.checkedBank = false;
            chocolate.geSet = false;
            chocolate.decSellPrice = 0;
            chocolate.timesPriceChanged = 0;
            //chocolate.setHighestProfitLeather(false);

            /*if (checkStartBegging())
                return 1000;*/

        }

        // Decreases sell price if over time
        chocolate.checkTime();
        Log.info( "Waiting to complete  |  Time: " + chocolate.elapsedSeconds / 60 + "min(s)  |  Price changed " + chocolate.timesPriceChanged + " time(s)");
        if(chocolate.elapsedSeconds > chocolate.resetGeTime * 60 &&
                GrandExchange.getFirstActive() != null) {
            Log.fine("Decreasing leather price by: " + chocolate.intervalAmnt);
            while(!Inventory.contains(Chocolate.DUST) && GrandExchange.getFirstActive() != null) {
                Time.sleepUntil(() -> GrandExchange.getFirst(Objects::nonNull).abort(), 1000, 5000);
                GrandExchange.collectAll();
                Time.sleep(5000);
                GrandExchange.collectAll();
            }
            chocolate.decSellPrice += chocolate.intervalAmnt;
            chocolate.setPrices(true);
            chocolate.startTime = System.currentTimeMillis();
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

    private void fallbackGEPrice() {
        if (chocolate.usingSellFallback) {
            Time.sleepUntil(() -> Interfaces.getComponent(465, 24, 39).getText() != null, 5000);
            chocolate.sellPrice = Integer.parseInt(Interfaces.getComponent(465, 24, 39).getText().split(" ")[0]);
            chocolate.lastPrices[0] = chocolate.sellPrice;
            Log.fine("GE sell price set: " + chocolate.sellPrice);
        }
    }
}
