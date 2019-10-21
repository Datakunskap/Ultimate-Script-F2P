package script.fighter.wrappers;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import script.fighter.config.Config;
import script.fighter.models.Progressive;

import java.util.Collection;
import java.util.HashSet;

public class GEWrapper {

    private static boolean needSellItems;

    public static void setSellItems(boolean needSellItems) { GEWrapper.needSellItems = needSellItems; }

    public static boolean isSellItems() { return needSellItems; }

    public static boolean hasRunes(Spell spell){
        Progressive p = Config.getProgressive();
        HashSet<String> runes = p.getRunes();
        for (String rune : runes) {
            if (!Inventory.contains(rune))
                return false;
        }

        if (spell.equals(Spell.Modern.FIRE_STRIKE)) {
            if (!Inventory.contains(x
                    -> x.getName().equalsIgnoreCase("Air rune") && x.getStackSize() >= 2)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasEquipment() {
        Progressive p = Config.getProgressive();
        Collection<String> items = p.getEquipmentMap().values();
        if (items.size() < 1)
            return true;
        for (String item : items) {
            if (!Inventory.contains(item) && !Equipment.contains(item)) {
                return false;
            }
        }
        return true;
    }

    public static void openGE() {
        Npc n = Npcs.getNearest(x -> x != null && x.getName().contains("Grand Exchange Clerk"));
        if (n != null) {
            Time.sleepUntil(() -> n.interact("Exchange"), 1000, 10000);
            Time.sleep(700, 1300);
        }
    }

    public static void closeGE(){
        if (GrandExchange.isOpen()) {
            Movement.walkToRandomized(Players.getLocal().getPosition().randomize(4));
            Time.sleepUntil(() -> !GrandExchange.isOpen(), 2000, 6000);
        }
    }

    public static boolean itemsStillActive(RSGrandExchangeOffer.Type offerType) {
        return (GrandExchange.isOpen() || GrandExchangeSetup.isOpen()) &&
                (GrandExchange.getOffers(offerType).length > 0 || GrandExchange.getFirstActive() != null);
    }
}
