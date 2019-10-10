package script.fighter.nodes.restock;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.ui.Log;
import script.Beggar;
import script.beg.TradePlayer;
import script.fighter.Fighter;
import script.fighter.debug.Logger;
import script.fighter.framework.Node;
import script.fighter.wrappers.BankWrapper;
import script.fighter.wrappers.GEWrapper;
import script.tanner.ExGrandExchange;
import script.tanner.data.Location;

public class SellGE extends Node {

    private Item[] itemsToSell;
    private InterfaceComponent restrictedMsg = Interfaces.getComponent(465, 25);
    private String status;
    private Fighter main;

    public SellGE(Fighter main) {
        this.main = main;
    }

    @Override
    public boolean validate() {
        if (!GEWrapper.getSellItems())
            return false;

        if (!Location.GE_AREA.containsPlayer()) {
            GEWrapper.setSellItems(false);
            itemsToSell = null;
            return false;
        }

        if (GEWrapper.getSellItems() && itemsToSell == null) {
            BankWrapper.openAndDepositAll(true);
            BankWrapper.withdrawSellableItems();

            Item[] sellableItems = Inventory.getItems(i -> i.getId() != 995 &&
                    i.isExchangeable() && !TradePlayer.isTradeRestrictedItem(i.getName()));
            if (sellableItems != null && sellableItems.length > 0) {
                itemsToSell = sellableItems;
                return true;
            }
            GEWrapper.closeGE();
            GEWrapper.setSellItems(false);
            itemsToSell = null;
            return false;
        }

        if (itemsLeftToSell() || GEWrapper.itemsStillActive(RSGrandExchangeOffer.Type.SELL)) {
            return true;
        }

        GEWrapper.closeGE();
        GEWrapper.setSellItems(false);
        itemsToSell = null;
        return false;
    }

    @Override
    public int execute() {
        invalidateTask(main.getActive());

        if (!GrandExchange.isOpen()) {
            GEWrapper.openGE();
            Log.fine("Selling " + itemsToSell.length + " Item(s)");
            return 1000;
        }

        if (itemsLeftToSell()) {
            for (int i = 0; i < itemsToSell.length; i++) {
                if (itemsToSell[i] != null && GrandExchange.getOffers(RSGrandExchangeOffer.Type.SELL).length < 3) {
                    if (ExGrandExchange.sell(itemsToSell[i].getId(), itemsToSell[i].getStackSize(), Beggar.randInt(1, 5), false)) {
                        status = "Selling: " + itemsToSell[i].getName();
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
            Time.sleep(Random.low(300, 600));
            Keyboard.pressEnter();
        }

        return Beggar.randInt(800, 1400);
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

    @Override
    public void onInvalid() {
        itemsToSell = null;
        GEWrapper.setSellItems(false);
        super.onInvalid();
    }

    public void invalidateTask(Node active) {
        if (active != null && !this.equals(active)) {
            Logger.debug("Node has changed.");
            active.onInvalid();
        }
        main.setActive(this);
    }

    @Override
    public String status() {
        return status;
    }
}
