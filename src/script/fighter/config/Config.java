package script.fighter.config;

import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.movement.position.Position;
import script.fighter.debug.LogLevel;
import script.fighter.models.Progressive;

import java.util.HashSet;

public class Config {

    private static LogLevel logLevel;
    private static Progressive NULL_SAFE_PROGRESSIVE = new Progressive();

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
}
