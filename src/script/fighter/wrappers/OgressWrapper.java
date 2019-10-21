package script.fighter.wrappers;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;

import java.util.HashSet;

public class OgressWrapper {

    public static final Position TOCK_QUEST_POSITION = new Position(3030, 3273, 0);

    public static final Position TOCK_BOAT_TO_COVE_POSITION = new Position(2911, 3226, 0);

    public static final Area CORSAIR_COVE_DUNGEON = Area.rectangular(1995, 9004, 2026, 8982, 1);
    //Area.rectangular(1988, 9014, 2028, 8962, 1);

    public static final Area[] CORSAIR_COVE = {Area.rectangular(2508, 2878, 2604, 2831, 0),
            Area.rectangular(2508, 2878, 2604, 2831, 1)};

    public static final Position DUNGEON_ENTRANCE =  new Position(2524, 2860, 0);

    public static final Position TOCK_BOAT_FROM_COVE_POSITION = new Position(2578, 2841, 0); //new Position(2578, 2841, 0);

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
        Time.sleep(2000, 2500);
        for (String name : itemNames) {
            Inventory.getFirst(name).interact("Drop");
            Time.sleep(1500, 2000);
        }
    }

}
