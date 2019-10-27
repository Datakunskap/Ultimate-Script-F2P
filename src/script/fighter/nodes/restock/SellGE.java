package script.fighter.nodes.restock;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.ui.Log;
import script.Beggar;
import script.beg.TradePlayer;
import script.data.Location;
import script.fighter.Fighter;
import script.fighter.QuestingDriver;
import script.fighter.config.Config;
import script.fighter.framework.Node;
import script.fighter.models.Progressive;
import script.fighter.wrappers.*;
import script.tanner.ExGrandExchange;

public class SellGE extends Node {

    private Item[] itemsToSell;
    private InterfaceComponent restrictedMsg = Interfaces.getComponent(465, 25);
    private String status;
    private Fighter main;
    private int gpStart;
    private boolean triedTeleport;

    public SellGE(Fighter main) {
        this.main = main;
    }

    @Override
    public boolean validate() {
        if (!GEWrapper.isSellItems())
            return false;

        if (itemsToSell == null) {
            return true;
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
        main.invalidateTask(this);
        Player me = Players.getLocal();

        if (!Location.GE_AREA_LARGE.getBegArea().contains(me)) {
            if (!triedTeleport && (OgressWrapper.CORSAIR_COVE[0].contains(me)
                    || SplashWrapper.getSplashArea().contains(me))) {

                TeleportWrapper.tryTeleport(false);
                triedTeleport = true;
            }
            status = "Walking to GE";
            GEWrapper.walkToGE();
            return Fighter.getLoopReturn();
        }

        if (itemsToSell == null) {
            Progressive p = Config.getProgressive();
            if (p.isOgress() && !OgressWrapper.has7QuestPoints()) {
                new QuestingDriver(main.getScript()).startSPXQuesting(Beggar.randInt(20, 30));
            }

            BankWrapper.openAndDepositAll(true);
            BankWrapper.withdrawSellableItems();

            Item[] sellableItems = Inventory.getItems(i -> i.getId() != 995
                    && !p.getRunes().contains(i.getName().toLowerCase())
                    && (!p.isOgress() || Config.getLoot().contains(i.getName())));

            if (sellableItems != null && sellableItems.length > 0) {
                Log.info("Selling");
                itemsToSell = sellableItems;
                gpStart = Inventory.getCount("Coins");
            } else {
                Log.severe("Nothing To Sell");
                Bank.close();
                GEWrapper.setSellItems(false);
                return Fighter.getLoopReturn();
            }
        }

        if (!GrandExchange.isOpen()) {
            GEWrapper.openGE();
            Log.fine("Selling " + itemsToSell.length + " Item(s)");
            return 1000;
        }

        if (itemsLeftToSell()) {
            for (int i = 0; i < itemsToSell.length; i++) {
                status = "Selling";
                if (itemsToSell[i] != null && GrandExchange.getOffers(RSGrandExchangeOffer.Type.SELL).length < 3) {
                    if (ExGrandExchange.sell(itemsToSell[i].getId(), itemsToSell[i].getStackSize(), Beggar.randInt(1, 2), false)) {
                        Log.info("Selling: " + itemsToSell[i].getName());
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
            Time.sleep(300, 600);
            Keyboard.pressEnter();
        }

        return Random.high(600, 1800);
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
        triedTeleport = false;
        itemsToSell = null;
        GEWrapper.setSellItems(false);
        Interfaces.closeAll();
        Time.sleepUntil(() -> !GrandExchange.isOpen() && !Bank.isOpen(), 2000, 6000);
        BankWrapper.updateInventoryValue();
        super.onInvalid();
    }

    @Override
    public String status() {
        return status;
    }
}
