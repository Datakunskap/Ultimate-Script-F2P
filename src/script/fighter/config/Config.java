package script.fighter.config;

import org.rspeer.runetek.api.component.tab.*;
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

    public static int getSplashStartAmnt(boolean withGear) {
        int gear = withGear ? 1052 : 0;
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
