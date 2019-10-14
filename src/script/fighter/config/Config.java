package script.fighter.config;

import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import script.fighter.debug.LogLevel;
import script.fighter.models.Progressive;

import java.util.Collection;
import java.util.HashSet;

public class Config {

    private static LogLevel logLevel;
    private static Progressive NULL_SAFE_PROGRESSIVE = new Progressive();
    private static final String[] SPLASH_GEAR = new String[]{"cursed goblin staff",
            "bronze platebody", "bronze full helm", "bronze platelegs", "bronze kiteshield"};
    private static final Area SPLASH_AREA = Area.rectangular(3010, 3187, 3020, 3182);;


    public static Progressive getProgressive() {
        Progressive curr = ProgressiveSet.getCurrent();
        return curr != null ? curr : NULL_SAFE_PROGRESSIVE;
    }

    public static boolean buryBones() {
        return getProgressive().isBuryBones();
    }

    public static HashSet<String> getNpcs() {
        return getProgressive().getEnemies();
    }

    public static HashSet<String> getFood() {
        return getProgressive().getFood();
    }

    public static boolean isLooting() {
        return getLoot().size() > 0;
    }

    public static HashSet<String> getLoot() {
        return getProgressive().getLoot();
    }

    public static int getRadius() {
        return getProgressive().getRadius();
    }

    public static Position getStartingTile() {
        return getProgressive().getPosition();
    }

    public static LogLevel getLogLevel() {
        return logLevel;
    }

    public static void setLogLevel(LogLevel logLevel) {
        Config.logLevel = logLevel;
    }

    public static boolean hasRunes(){
        Progressive p = getProgressive();
        HashSet<String> runes = p.getRunes();
        for (String rune : runes) {
            if (!Inventory.contains(rune)) {
                return false;
            }
        }
        return Magic.canCast(p.getSpell());
    }

    public static boolean hasEquipment() {
        Progressive p = getProgressive();
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

    public static String[] getSplashGear(boolean getStaff){
        if (getStaff) {
            return SPLASH_GEAR;
        } else {
            String[] noStaff = new String[SPLASH_GEAR.length - 1];
            for (int i = 1; i < SPLASH_GEAR.length; i ++) {
                noStaff[i - 1] = SPLASH_GEAR[i];
            }
            return noStaff;
        }
    }

    public static boolean hasSplashGear(boolean checkStaff) {
        for (String item : SPLASH_GEAR) {
            if (!checkStaff && item.contains("staff"))
                continue;
            if (!Inventory.contains(item) && !Equipment.contains(item)) {
                return false;
            }
        }
        return true;
    }

    public static Area getSplashArea() {
        return SPLASH_AREA;
    }
}
