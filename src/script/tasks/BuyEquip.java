package script.tasks;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.input.menu.ActionOpcodes;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;
import script.tanner.ExGrandExchange;
import script.tanner.ExPriceChecker;

import java.io.IOException;
import java.util.Objects;

public class BuyEquip extends Task {

    private Beggar main;

    public BuyEquip(Beggar beggar){
        main = beggar;
    }

    @Override
    public boolean validate() {
        return Inventory.getCount(true, 995) >= main.randBuyGP && !main.equipped;
    }

    @Override
    public int execute() {
        Log.info("Buying from GE & Equipping");
        final int X = main.item;
        if(!main.bought)
            openGE();

        if (Inventory.contains(X)) {
            main.bought = true;
            closeGE();
            if (!Tabs.isOpen(Tab.INVENTORY)){
                Tabs.open(Tab.INVENTORY);
                Time.sleepUntil(() -> Tabs.isOpen(Tab.EQUIPMENT), 5000);
            }
            if (Inventory.getFirst(X).interact("Wear")) {
                Log.info("Equipping");
                Time.sleep(1000);
                Tabs.open(Tab.EQUIPMENT);
                if (Time.sleepUntil(() -> Tabs.isOpen(Tab.EQUIPMENT) && Equipment.contains(X), 5000)) {
                    setRandBuyGP();
                    return 1000;
                }
            }
        }

        // Buys
        try {
            if (!main.bought && (GrandExchange.isOpen() || GrandExchangeSetup.isOpen()) && GrandExchange.getFirstActive() == null && ExGrandExchange.buy(X, 1,
                    ExPriceChecker.getOSBuddyBuyPrice(X, false) > 0 ? ExPriceChecker.getOSBuddyBuyPrice(X, false) + 500 : ExPriceChecker.getRSBuddyBuyPrice(X, false) + 500, false)) {
                Log.fine("Buying");
            } if(!main.bought) {
                Log.info("Waiting to complete");
                openGE();
                Time.sleepUntil(() -> GrandExchange.getFirst(Objects::nonNull).getProgress().equals(RSGrandExchangeOffer.Progress.FINISHED), 2000, 10000);
                GrandExchange.collectAll();
                Keyboard.pressEnter();
                Time.sleep(1500);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Checks and handles stuck in setup
        if (GrandExchange.getFirstActive() == null && GrandExchangeSetup.isOpen()) {
            GrandExchange.open(GrandExchange.View.OVERVIEW);
            if (!Time.sleepUntil(() -> !GrandExchangeSetup.isOpen(), 5000)) {
                closeGE();
            }
            main.startTime = System.currentTimeMillis();
        }

        if (Equipment.contains(X)) {
            setRandBuyGP();
            return 1000;
        }

        GrandExchange.collectAll();
        return 1000;
    }

    private void setRandBuyGP() {
        int newRand = Inventory.getCount(true, 995) + Beggar.randInt(20000, 30000);
        if (newRand < main.muleAmnt) {
            main.randBuyGP = newRand;
        } else {
            main.randBuyGP = Integer.MAX_VALUE;
        }
        main.equipped = true;
        main.walk = true;
        main.item = main.items[Beggar.randInt(0, main.items.length - 1)];
    }

    private void openGE() {
        while (!GrandExchange.isOpen() && !GrandExchangeSetup.isOpen()) {
            Npc n = Npcs.getNearest(x -> x != null && x.getName().contains("Grand Exchange Clerk"));
            if (n != null) {
                Time.sleepUntil(() -> n.interact("Exchange"), 1000, 10000);
                Time.sleep(700, 1300);
            }
        }
    }

    private void closeGE() {
        while(GrandExchange.isOpen() || GrandExchangeSetup.isOpen()) {
            InterfaceComponent X = Interfaces.getComponent(465, 2, 11);
            X.interact(ActionOpcodes.INTERFACE_ACTION);
            Time.sleepUntil(() -> !GrandExchange.isOpen() && !GrandExchangeSetup.isOpen(), 5000);
        }
    }
}
