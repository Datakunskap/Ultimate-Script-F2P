package script.fighter.wrappers;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.ui.Log;
import script.Beggar;
import script.fighter.config.Config;

import java.util.HashSet;

public class OgressWrapper {

    public static final Position TOCK_QUEST_POSITION = new Position(3030, 3273, 0);
    public static final Position TOCK_BOAT_TO_COVE_POSITION = new Position(2911, 3226, 0);
    public static final Area CORSAIR_COVE_DUNGEON = Area.rectangular(1995, 9004, 2026, 8982, 1);
    public static final Area[] CORSAIR_COVE = { Area.rectangular(2508, 2878, 2604, 2831, 0),
            Area.rectangular(2508, 2878, 2604, 2831, 1) };
    public static final Position DUNGEON_ENTRANCE =  new Position(2524, 2860, 0);
    public static final Position TOCK_BOAT_FROM_COVE_POSITION = new Position(2578, 2841, 0); //new Position(2578, 2841, 0);
    private static final Tab[] SWITCH_TABS = new Tab[] {
            Tab.INVENTORY, Tab.MAGIC, Tab.COMBAT, Tab.SKILLS, Tab.FRIENDS_LIST, Tab.PRAYER, Tab.QUEST_LIST, Tab.ACCOUNT_MANAGEMENT } ;

    public static int deaths;
    public static int itemsAlched;

    public static void openRandomTab() {
        Tabs.open(SWITCH_TABS[Beggar.randInt(0, SWITCH_TABS.length - 1)]);
    }

    public static boolean has7QuestPoints() {
        if (Quest.Free.SHEEP_SHEARER.isComplete()
                && Quest.Free.RUNE_MYSTERIES.isComplete()
                && Quest.Free.ROMEO_AND_JULIET.isComplete()) {
            return true;
        }
        if (Quest.Free.SHEEP_SHEARER.isComplete()
                && Quest.Free.THE_RESTLESS_GHOST.isComplete()
                && Quest.Free.ROMEO_AND_JULIET.isComplete()) {
            return true;
        }
        if (Quest.Free.RUNE_MYSTERIES.isComplete()
                && Quest.Free.THE_RESTLESS_GHOST.isComplete()
                && Quest.Free.ROMEO_AND_JULIET.isComplete()) {
            return true;
        }
        return false;
    }

    public static void equipEquipment() {
        if (Bank.isOpen() || GrandExchange.isOpen()) {
            Bank.close();
            GEWrapper.closeGE();
            Time.sleep(500, 800);
        }

        HashSet<String> equip = new HashSet<>(Config.getProgressive().getEquipmentMap().values());

        for (String e : equip) {
            if (Inventory.contains(e)) {
                Log.fine("Equipping: " + e);
                Inventory.getFirst(e).interact(a -> true);
                Time.sleepUntil(() -> Equipment.contains(e), 1000, 8000);
            } else {
                Log.severe("Can't Find Equipment: " + e);
            }
        }
    }

    public static void unequipAll(boolean dropAll) {
        Tabs.open(Tab.EQUIPMENT);
        Time.sleep(2000, 2500);
        EquipmentSlot[] equipped = Equipment.getOccupiedSlots();
        HashSet<String> itemNames = new HashSet<>();

        for (EquipmentSlot slot : equipped) {
            itemNames.add(slot.getItemName());
            slot.unequip();
            Time.sleep(1500, 2000);
        }

        if (dropAll) {
            dropAllEquipment(itemNames);
        }
    }

    private static void dropAllEquipment(HashSet<String> itemNames) {
        Tabs.open(Tab.INVENTORY);
        Time.sleepUntil(() -> Tabs.getOpen() == Tab.INVENTORY, 1000, 5000);
        for (String name : itemNames) {
            Item i = Inventory.getFirst(n -> n.getName().equalsIgnoreCase(name));
            if (i != null) {
                i.interact("Drop");
            }
            Time.sleep(1500, 2000);
        }
    }
}
