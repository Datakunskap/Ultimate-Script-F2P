package script.fighter.nodes.restock;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.Definitions;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.runetek.providers.RSItemDefinition;
import script.data.Location;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.debug.Logger;
import script.fighter.framework.Node;
import script.tanner.ExGrandExchange;
import script.tanner.ExPriceChecker;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

public class BuyGE extends Node {

    private Fighter main;
    private String status;
    private Iterator runesIterator;
    private int quantity;
    private String itemToBuy;

    public BuyGE(Fighter main) { this.main = main; }

    @Override
    public boolean validate() {
        if ((runesIterator != null && runesIterator.hasNext()) || itemsStillBuying())
            return true;

        HashSet runes = Config.getProgressive().getRunes();
        if (runes != null && runes.size() > 0 &&
                Config.getProgressive().getSpell() != null && !Magic.canCast(Config.getProgressive().getSpell())) {
            runesIterator = runes.iterator();
            itemToBuy = (String) runesIterator.next();
            quantity = 1;
            return true;
        }
        return false;
    }

    @Override
    public int execute() {
        invalidateTask(main.getActive());

        if (!Location.GE_AREA.getBegArea().contains(Players.getLocal())) {
            Movement.walkToRandomized(Location.GE_AREA.getBegArea().getCenter());
            status = "Walking to GE";
            return Fighter.getLoopReturn();
        }

        if (!GrandExchange.isOpen()) {
            status = "restocking";
            openGE();
            return Fighter.getLoopReturn();
        }

        if (ExGrandExchange.buy(itemToBuy, quantity, getPrice(), false)) {
            if (Time.sleepUntil(() -> GrandExchange.getFirst(x -> x.getItemName().toLowerCase().equals(itemToBuy)) != null, 8000)) {
                status = "Buying: " + itemToBuy;
                itemToBuy = (String) runesIterator.next();
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

        return Fighter.getLoopReturn();
    }

    private int getPrice() {
        RSItemDefinition item = Definitions.getItem(itemToBuy, x -> x.isTradable() || x.isNoted());
        try {
            int price = ExPriceChecker.getOSBuddyBuyPrice(item.getId(), true);
            if (price < 1) {
                price = ExPriceChecker.getRSBuddyBuyPrice(item.getId(), true);
            }
            if (price < 1) {
                price = Inventory.getCount(true, 995) / 2;
            }
            return price;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void openGE() {
        Npc n = Npcs.getNearest(x -> x != null && x.getName().contains("Grand Exchange Clerk"));
        if (n != null) {
            Time.sleepUntil(() -> n.interact("Exchange"), 1000, 10000);
            Time.sleep(700, 1300);
        }
    }

    private boolean itemsStillBuying() {
        return (GrandExchange.isOpen() || GrandExchangeSetup.isOpen()) &&
                (GrandExchange.getOffers(RSGrandExchangeOffer.Type.SELL).length > 0 || GrandExchange.getFirstActive() != null);
    }

    public void onInvalid() {
        runesIterator = null;
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
