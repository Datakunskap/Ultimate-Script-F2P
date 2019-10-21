package script.fighter.wrappers;

import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.movement.position.Area;
import script.fighter.config.Config;

public class SplashWrapper {

    private static final String[] SPLASH_GEAR = new String[]{"cursed goblin staff",
            "bronze platebody", "bronze full helm", "bronze platelegs", "bronze kiteshield"};
    private static final Area SPLASH_AREA_MUGGER = Area.rectangular(3015, 3187, 3020, 3184);
    private static final Area SPLASH_AREA_THIEF = Area.rectangular(3011, 3195, 3012, 3191); //Area.rectangular(3011, 3195, 3012, 3194);
    public static final Area DRAYNOR_MARKET_AREA = Area.rectangular(3078, 3252, 3083, 3248);

    public static boolean isSplash() {
        return Config.getProgressive().isSplash();
    }

    public static void setSplash(boolean splash) {
        Config.getProgressive().setSplash(splash);
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
        if (hasSplashGear(false)) {
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
