package script.fighter.nodes.restock;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Definitions;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.runetek.providers.RSItemDefinition;
import org.rspeer.ui.Log;
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
    private Iterator<String> runesIterator;
    private int quantity;
    private String itemToBuy;
    private Spell spell;

    public BuyGE(Fighter main) {
        this.main = main;
    }

    @Override
    public boolean validate() {
        if (runesIterator != null || itemsStillBuying())
            return true;

        HashSet<String> runes = Config.getProgressive().getRunes();
        spell = Config.getProgressive().getSpell();
        if (runes != null && runes.size() > 0 &&
                spell != null && !hasRunes(runes)) {
            Log.fine("Restocking");
            runesIterator = runes.iterator();
            itemToBuy = runesIterator.next();
            quantity = 35;
            return true;
        }

        return false;
    }

    @Override
    public int execute() {
        invalidateTask(main.getActive());

        if (!Location.GE_AREA.getBegArea().contains(Players.getLocal())) {
            if (!Movement.walkToRandomized(Location.GE_AREA.getBegArea().getCenter())) {
                SceneObject stairs = SceneObjects.getNearest("Staircase");
                if (stairs != null) {
                    if (stairs.interact("Climb-down")) {
                        Logger.debug("Climbing stairs");
                    } else if (stairs.getPosition().randomize(3).isPositionWalkable()) {
                        Movement.walkTo(stairs);
                    } else {
                        SceneObject door = SceneObjects.getNearest("Door");
                        if (door != null && door.isPositionInteractable()) {
                            door.interact("Open");
                        }
                    }
                }
            }
            status = "Walking to GE";
            return Fighter.getLoopReturn();
        }

        if (!GrandExchange.isOpen()) {
            status = "Restocking";
            openGE();
            return Fighter.getLoopReturn();
        }

        if (runesIterator != null && !itemsStillBuying()) {
            if (ExGrandExchange.buy(itemToBuy, quantity, getPrice(), false)) {
                if (Time.sleepUntil(() -> GrandExchange.getFirst(x -> x.getItemName().toLowerCase().equals(itemToBuy)) != null, 8000)) {
                    Logger.debug("Buying: " + itemToBuy);
                    if (runesIterator.hasNext()) {
                        itemToBuy = runesIterator.next();
                    } else {
                        runesIterator = null;
                    }
                }
            }
        }

        if (!GrandExchange.getView().equals(GrandExchange.View.OVERVIEW)) {
            GrandExchange.open(GrandExchange.View.OVERVIEW);
        }

        if (itemsStillBuying()) {
            GrandExchange.collectAll();
            Time.sleep(Random.mid(300, 600));
            Keyboard.pressEnter();
        }

        if (!itemsStillBuying() && runesIterator == null) {
            closeGE();
            if (!Tabs.isOpen(Tab.MAGIC)) {
                Tabs.open(Tab.MAGIC);
                Time.sleepUntil(() -> Tabs.isOpen(Tab.MAGIC) && Magic.canCast(spell), 8000);
            }
            Log.fine("Done restocking");
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
            return (price + 5) * quantity;
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

    private void closeGE(){
        if (GrandExchange.isOpen()) {
            Movement.walkToRandomized(Players.getLocal().getPosition().randomize(4));
            Time.sleepUntil(() -> !GrandExchange.isOpen(), 2000, 6000);
        }
    }

    private boolean itemsStillBuying() {
        return (GrandExchange.isOpen() || GrandExchangeSetup.isOpen()) &&
                (GrandExchange.getOffers(RSGrandExchangeOffer.Type.BUY).length > 0 || GrandExchange.getFirstActive() != null);
    }

    private boolean hasRunes(HashSet<String> runes){
        for (String rune : runes) {
            if (!Inventory.contains(rune)) {
                return false;
            }
        }
        if (Tabs.isOpen(Tab.MAGIC))
            return Magic.canCast(spell);
        return true;
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
