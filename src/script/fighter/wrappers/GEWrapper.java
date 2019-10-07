package script.fighter.wrappers;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.ui.Log;

public class GEWrapper {

    private static boolean needSellItems;

    public static void setSellItems(boolean needSellItems) { GEWrapper.needSellItems = needSellItems; }

    public static boolean getSellItems() { return needSellItems; }

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

    public static void teleportHome() {
        if (Tabs.open(Tab.MAGIC)) {
            Time.sleep(1500);
            Log.fine("Teleporting Home");
            Magic.cast(Spell.Modern.HOME_TELEPORT);
            Time.sleep(18000);
        }
    }
}
