package script.tasks;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;
import script.tanner.ExGrandExchange;
import script.tanner.data.Location;

public class SellGE extends Task {

    private Item[] itemsToSell;
    private InterfaceComponent restrictedMsg = Interfaces.getComponent(465, 25);
    private Beggar beggar;
    private int startGP;

    public SellGE(Beggar beggar) {
        this.beggar = beggar;
    }

    @Override
    public boolean validate() {
        if (!Location.GE_AREA.containsPlayer() || beggar.isMuling || !beggar.equipped) {
            itemsToSell = null;
            return false;
        }

        if (itemsToSell == null) {
            Item[] sellableItems = Inventory.getItems(i -> i.getId() != 995 && i.isExchangeable() && !TradePlayer.isTradeRestrictedItem(i.getName()));
            if (sellableItems != null && sellableItems.length > 0) {
                itemsToSell = sellableItems;
                startGP = Inventory.getCount(true, 995);
                return true;
            }
            closeGE();
            itemsToSell = null;
            startGP = 0;
            return false;
        }

        if (itemsLeftToSell() || itemsStillSelling()) {
            return true;
        }

        closeGE();
        itemsToSell = null;
        startGP = 0;
        return false;
    }

    @Override
    public int execute() {
        if (!GrandExchange.isOpen()) {
            Npc n = Npcs.getNearest(x -> x != null && x.getName().contains("Grand Exchange Clerk"));
            if (n != null) {
                Time.sleepUntil(() -> n.interact("Exchange"), 1000, 10000);
                Log.fine("Selling " + itemsToSell.length + " Item(s)");
                Time.sleep(700, 1300);
            }
            return 1000;
        }

        if (itemsLeftToSell()) {
            for (int i = 0; i < itemsToSell.length; i++) {
                if (itemsToSell[i] != null && GrandExchange.getOffers(RSGrandExchangeOffer.Type.SELL).length < 3) {
                    if (ExGrandExchange.sell(itemsToSell[i].getId(), itemsToSell[i].getStackSize(), Beggar.randInt(1, 5), false)) {
                        final int index = i;
                        if (Time.sleepUntil(() -> GrandExchange.getFirst(x -> x.getItemId() == itemsToSell[index].getId()) != null,8000)) {
                            itemsToSell[i] = null;
                        }
                    } else {
                        itemsToSell = Inventory.getItems(x -> x.getId() != 995 && x.isExchangeable() && !TradePlayer.isTradeRestrictedItem(x.getName()));
                    }
                }
            }
        }

        if (!GrandExchange.getView().equals(GrandExchange.View.OVERVIEW)) {
            GrandExchange.open(GrandExchange.View.OVERVIEW);
        }

        if (GrandExchange.getOffers(RSGrandExchangeOffer.Type.SELL).length > 0) {
            GrandExchange.collectAll();
            Time.sleep(Random.mid(300, 600));
            Keyboard.pressEnter();
        }

        return Beggar.randInt(1000, 2000);
    }

    private boolean itemsLeftToSell() {
        if (itemsToSell == null || itemsToSell.length < 1) {
            return false;
        }
        for (Item i : itemsToSell) {
            if (i != null) {
                return true;
            }
        }
        return false;
    }

    private boolean itemsStillSelling() {
         return (GrandExchange.isOpen() || GrandExchangeSetup.isOpen()) &&
                 (GrandExchange.getOffers(RSGrandExchangeOffer.Type.SELL).length > 0 || GrandExchange.getFirstActive() != null);
    }

    private void closeGE(){
        if (GrandExchange.isOpen()) {
            Movement.walkToRandomized(Players.getLocal().getPosition().randomize(4));
            Time.sleepUntil(() -> !GrandExchange.isOpen(), 2000, 6000);
            if (startGP > 0 && Inventory.contains(995)) {
                Beggar.itemsSoldProfitAmount += (Inventory.getCount(true, 995) - startGP);
            }
        }
    }
}
