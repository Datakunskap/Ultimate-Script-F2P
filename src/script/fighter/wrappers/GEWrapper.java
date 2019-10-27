package script.fighter.wrappers;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.ui.Log;
import script.fighter.config.Config;
import script.fighter.models.Progressive;
import script.fighter.nodes.combat.GoToCove;

import java.util.Collection;
import java.util.HashSet;

public class GEWrapper {

    private static boolean sellItems;

    public static void setSellItems(boolean sellItems) {
        GEWrapper.sellItems = sellItems;
    }

    public static boolean isSellItems() {
        return sellItems;
    }

    public static boolean hasRunes(Spell spell) {
        try {
            Progressive p = Config.getProgressive();
            if (p.getRunes() == null || !Game.isLoggedIn() || Players.getLocal() == null)
                return true;

            HashSet<String> runes = p.getRunes();

            for (String rune : runes) {
                if (!Inventory.contains(rune)) {
                    return false;
                }
            }

            if (spell == Spell.Modern.FIRE_STRIKE) {
                if (!Inventory.contains(x
                        -> x.getName().equalsIgnoreCase("air rune") && x.getStackSize() >= 2)) {
                    return false;
                }
            }
            return true;
        } catch (Exception ignored) {
            return true;
        }
    }

    public static boolean hasEquipment() {
        try {
            Progressive p = Config.getProgressive();
            if (!Game.isLoggedIn() || Players.getLocal() == null)
                return true;

            Collection<String> items = p.getEquipmentMap().values();
            if (items.size() < 1)
                return true;
            for (String item : items) {
                if (!Inventory.contains(item) && !Equipment.contains(item)) {
                    return false;
                }
            }
            return true;
        } catch (Exception ignored) {
            Log.severe(ignored);
            return true;
        }
    }

    public static HashSet<String> getEquipmentNeeded() {
        Progressive p = Config.getProgressive();
        HashSet<String> needed = new HashSet<>();
        Collection<String> items = p.getEquipmentMap().values();
        if (items.size() < 1)
            return needed;
        for (String item : items) {
            if (!Inventory.contains(item) && !Equipment.contains(item)) {
                needed.add(item);
            }
        }
        return needed;
    }

    public static void openGE() {
        Npc n = Npcs.getNearest(x -> x != null && x.getName().contains("Grand Exchange Clerk"));
        if (n != null) {
            Time.sleepUntil(() -> n.interact("Exchange"), 1000, 10000);
            Time.sleep(700, 1300);
        }
    }

    public static void closeGE() {
        if (GrandExchange.isOpen()) {
            Movement.walkToRandomized(Players.getLocal().getPosition().randomize(4));
            Time.sleepUntil(() -> !GrandExchange.isOpen(), 2000, 6000);
        }
    }

    public static boolean itemsStillActive(RSGrandExchangeOffer.Type offerType) {
        return (GrandExchange.isOpen() || GrandExchangeSetup.isOpen()) &&
                (GrandExchange.getOffers(offerType).length > 0 || GrandExchange.getFirstActive() != null);
    }

    public static void walkToGE() {
        if (GoToCove.shouldEnableRun())
            Movement.toggleRun(true);

        if (OgressWrapper.CORSAIR_COVE_DUNGEON.contains(Players.getLocal()) ||
                OgressWrapper.CORSAIR_COVE[0].contains(Players.getLocal())) {

            Movement.walkTo(OgressWrapper.TOCK_BOAT_FROM_COVE_POSITION);
            handleObstacles();

        } else if (OgressWrapper.CORSAIR_COVE[1].contains(Players.getLocal())) {
            Npc tock = Npcs.getNearest("Captain Tock");
            if (tock != null) {
                if (!Dialog.isOpen()) {
                    tock.interact("Talk-to");
                } else {
                    Dialog.process(d -> d.contains("Rimmington"));
                    Dialog.processContinue();
                }
            }
        } else if (!Movement.walkToRandomized(BankLocation.GRAND_EXCHANGE.getPosition())) {
            handleObstacles();
        }
    }

    private static void handleObstacles() {
        SceneObject ladder = (OgressWrapper.CORSAIR_COVE_DUNGEON.contains(Players.getLocal())) ?
                SceneObjects.getNearest("Vine ladder") : null;
        SceneObject plank1 = SceneObjects.getFirstAt(new Position(2578, 2839, 0));
        SceneObject plank2 = SceneObjects.getFirstAt(new Position(2909, 3228, 1));

        if (ladder != null) {
            if (ladder.interact("Climb")) {
                Time.sleep(1500, 2000);
            } else {
                Movement.walkTo(ladder);
            }
        } else if (plank1 != null) {
            if (plank1.interact("Cross")) {
                Time.sleep(3000, 5000);
            } else {
                Movement.walkTo(plank1);
            }
        } else if (plank2 != null) {
            if (plank2.interact("Cross")) {
                Time.sleep(3000, 5000);
            } else {
                Movement.walkTo(plank2);
            }
        }
    }

}
