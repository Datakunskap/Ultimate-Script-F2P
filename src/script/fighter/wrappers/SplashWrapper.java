package script.fighter.wrappers;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.ui.Log;
import script.Script;
import script.fighter.config.Config;

import java.util.HashMap;

public class SplashWrapper {

    public static final String STAFF = "cursed goblin staff";
    private static final Area SPLASH_AREA_MUGGER = Area.rectangular(3015, 3187, 3020, 3184);
    private static final Area SPLASH_AREA_THIEF = Area.rectangular(3011, 3195, 3012, 3191); //Area.rectangular(3011, 3195, 3012, 3194);
    public static final Area DRAYNOR_MARKET_AREA = Area.rectangular(3078, 3252, 3083, 3248);
    private static final int RAND_AREA_INDEX = Script.randInt(0, 1);

    public static String getStaff() {
        return STAFF;
    }

    public static boolean hasStaff() {
        return Inventory.contains(STAFF) || Equipment.contains(STAFF);
    }

    public static void equipEquipment() {
        if (Bank.isOpen() || GrandExchange.isOpen()) {
            Bank.close();
            GEWrapper.closeGE();
            Time.sleep(500, 800);
        }

        HashMap<EquipmentSlot, String> map = Config.getProgressive().getEquipmentMap();

        for (String e : map.values()) {
            if (Inventory.contains(e)) {
                Log.fine("Equipping: " + e);
                Inventory.getFirst(e).interact(a -> true);
                Time.sleepUntil(() -> Equipment.contains(e), 1000, 8000);
            }
        }
    }

    public static Area getSplashArea() {
        if (RAND_AREA_INDEX == 0 && Script.SPLASH_USE_EQUIPMENT) {
            return SPLASH_AREA_MUGGER;
        } else {
            return SPLASH_AREA_THIEF;
        }
    }

    public static int getSplashStartAmnt(boolean isUseGear) {
        int gear = isUseGear ? 1052 : 0;
        switch (Skills.getLevel(Skill.MAGIC)) {
            case 1:
                return 4008 + gear;
            case 2:
                return 3820 + gear;
            case 3:
                return 3624 + gear;
            case 4:
                return 3408 + gear;
            case 5:
                return 3156 + gear;
            case 6:
                return 2892 + gear;
            case 7:
                return 2592 + gear;
            case 8:
                return 2256 + gear;
            case 9:
                return 1896 + gear;
            case 10:
                return 1488 + gear;
            case 11:
                return 1044 + gear;
            case 12:
                return 552 + gear;
        }
        return Integer.MAX_VALUE;
    }
}
