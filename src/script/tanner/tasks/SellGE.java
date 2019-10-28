package script.tanner.tasks;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.input.menu.ActionOpcodes;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Script;
import script.tanner.Main;

import java.util.Objects;

public class SellGE extends Task {

    private Main main;
    private Banking banking;

    private Script script;
    private StartChocolate startChocolate;

    public SellGE (Main main, Script script) {
        this.main = main;
        this.script = script;
        banking = new Banking(main);
    }

    private boolean checkStartChocolate() {
        startChocolate = new StartChocolate(main, script, banking);
        return startChocolate.validate();
    }

    @Override
    public boolean validate() {
        return main.restock && !main.sold && main.GE_LOCATION.containsPlayer() && !main.isMuling;
    }

    @Override
    public int execute() {
        if (!main.checkedBank) {
            banking.execute();
            main.checkedBank = true;
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
            main.geSet = false;
        }

        // needs older account
        /*if (!sellRemainingHides()) {
            GrandExchange.collectAll();
            return 1000;
        }*/

        // bc issues with Buraks ExGrandExchange when selling
        if (Inventory.contains(main.LEATHER_NOTE) || Inventory.contains(main.LEATHER)) {
            Log.fine("Selling Leathers");
            GrandExchange.createOffer(RSGrandExchangeOffer.Type.SELL);
            Time.sleep(800);
            GrandExchangeSetup.setItem(main.LEATHER_NOTE);
            if (Inventory.contains(main.LEATHER))
                GrandExchangeSetup.setItem(main.LEATHER);
            Time.sleep(600);
            GrandExchangeSetup.setPrice(main.leatherPrice - main.decSellPrice);
            Time.sleep(600);
            GrandExchangeSetup.setQuantity(9999999);
            Time.sleep(600);
            GrandExchangeSetup.confirm();
            Time.sleep(600);
            if(GrandExchangeSetup.getItem() != null) {
                main.geSet = true;
            }

            GrandExchange.collectAll();
            Time.sleep(Random.mid(300, 600));
            GrandExchange.collectAll();
            Time.sleep(Random.mid(300, 600));
            Keyboard.pressEnter();
            main.startTime = System.currentTimeMillis();
        }

        if (GrandExchange.getFirst(Objects::nonNull).getProgress().equals(RSGrandExchangeOffer.Progress.FINISHED) &&
                !Inventory.contains(main.LEATHER_NOTE) && !Inventory.contains(main.LEATHER)) {
            GrandExchange.collectAll();
            Time.sleep(Random.mid(300, 600));
            GrandExchange.collectAll();
            Time.sleep(Random.mid(300, 600));

            Log.info("Done selling");
            main.sold = true;
            main.checkedBank = false;
            main.geSet = false;
            main.decSellPrice = 0;
            main.timesPriceChanged = 0;
            main.setHighestProfitLeather(false);

            if (checkStartChocolate()) {
                startChocolate.execute();
                return 1000;
            }

        }

        if (GrandExchange.getFirstActive() == null && !GrandExchange.getFirst(Objects::nonNull).getProgress().equals(RSGrandExchangeOffer.Progress.FINISHED) &&
                !Inventory.contains(main.LEATHER_NOTE) && !Inventory.contains(main.LEATHER)){
            Log.info("Done selling 2");
            main.sold = true;
            main.checkedBank = false;
            main.geSet = false;
            main.decSellPrice = 0;
            main.timesPriceChanged = 0;
            main.setHighestProfitLeather(false);

            if (checkStartChocolate()) {
                startChocolate.execute();
                return 1000;
            }
        }

        // Decreases sell price if over time
        main.checkTime();
        Log.info( "Waiting to complete  |  Time: " + main.elapsedSeconds / 60 + "min(s)  |  Price changed " + main.timesPriceChanged + " time(s)");
        if(main.elapsedSeconds > main.resetGeTime * 60 &&
                GrandExchange.getFirstActive() != null) {
            Log.fine("Decreasing leather price by: " + main.intervalAmnt);
            while(!Inventory.contains(main.LEATHER) && GrandExchange.getFirstActive() != null) {
                Time.sleepUntil(() -> GrandExchange.getFirst(Objects::nonNull).abort(), 1000, 5000);
                GrandExchange.collectAll();
                Time.sleep(5000);
                GrandExchange.collectAll();

                InterfaceComponent i = Interfaces.getComponent(465,23,2);
                Time.sleepUntil(() -> i != null && i.isVisible() &&
                        (i.getName().toLowerCase().contains("coins") || i.getName().toLowerCase().contains("leather") || i.getName().toLowerCase().contains("hard leather")), 5000);
                if (i != null) {
                    i.interact(ActionOpcodes.INTERFACE_ACTION);
                }
                InterfaceComponent i2 = Interfaces.getComponent(465,23,3);
                Time.sleepUntil(() -> i2 != null && i2.isVisible() &&
                        (i2.getName().toLowerCase().contains("coins") || i2.getName().toLowerCase().contains("leather") || i2.getName().toLowerCase().contains("hard leather")), 5000);
                if (i2 != null) {
                    i2.interact(ActionOpcodes.INTERFACE_ACTION);
                }
                GrandExchange.open();
                GrandExchange.open(GrandExchange.View.OVERVIEW);
            }
            main.decSellPrice += main.intervalAmnt;
            main.setPrices(true);
            main.startTime = System.currentTimeMillis();
            main.timesPriceChanged++;
        }

        // Checks and handles stuck in setup
        if (main.elapsedSeconds > 30 && GrandExchange.getFirstActive() == null && GrandExchangeSetup.isOpen()) {
            GrandExchange.open(GrandExchange.View.OVERVIEW);
            GrandExchange.collectAll();
            if (!Time.sleepUntil(() -> !GrandExchangeSetup.isOpen(), 5000)) {
                main.closeGE();
            }
            main.startTime = System.currentTimeMillis();
        }

        GrandExchange.collectAll();
        Keyboard.pressEnter();
        return 1000;
    }
}
