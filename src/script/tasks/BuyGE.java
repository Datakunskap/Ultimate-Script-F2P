package script.tasks;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
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
import script.ExGrandExchange;
import script.ExPriceChecker;

import java.io.IOException;
import java.util.Objects;

public class BuyGE extends Task {

    private boolean bought = false;
    private final int X = Beggar.item;

    @Override
    public boolean validate() {
        return Inventory.getCount(true, 995) >= Beggar.randBuyGP && !Beggar.equipped;
    }

    @Override
    public int execute() {
        Log.info("Buying from GE & Equipping");
        if(!bought)
            openGE();

        if (Inventory.contains(X)) {
            bought = true;
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
                    Beggar.equipped = true;
                    Beggar.walk = true;
                    return 1000;
                }
            }
        }

        // Buys
        try {
            if (!bought && GrandExchange.getFirstActive() == null && ExGrandExchange.buy(X, 1, ExPriceChecker.getOSBuddyBuyPrice(X) + 500, false)) {
                Log.fine("Buying");
            } if(!bought) {
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

        if (Equipment.contains(X)) {
            Beggar.equipped = true;
            Beggar.walk = true;
            return 1000;
        }

        GrandExchange.collectAll();
        return 1000;
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

    public void closeGE() {
        while(GrandExchange.isOpen() || GrandExchangeSetup.isOpen()) {
            InterfaceComponent X = Interfaces.getComponent(465, 2, 11);
            X.interact(ActionOpcodes.INTERFACE_ACTION);
            Time.sleepUntil(() -> !GrandExchange.isOpen() && !GrandExchangeSetup.isOpen(), 5000);
        }
    }
}
