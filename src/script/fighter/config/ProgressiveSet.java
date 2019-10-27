package script.fighter.config;

import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.scene.Players;
import script.fighter.Fighter;
import script.fighter.models.Progressive;
import script.fighter.wrappers.BankWrapper;
import script.fighter.wrappers.OgressWrapper;

import java.time.Duration;
import java.util.HashSet;

public class ProgressiveSet {

    private static HashSet<Progressive> progressives = new HashSet<>();
    private static Progressive current;

    public static void add(Progressive progressive) {
        progressives.add(progressive);
    }

    public static void remove(Progressive progressive){progressives.remove(progressive);}

    public static void removeAll(){progressives.clear();}

    public static boolean isEmpty(){return progressives.isEmpty();}

    public static Progressive getCurrent() {
        return current;
    }

    public static void setCurrent(Progressive current) {
        ProgressiveSet.current = current;
    }

    public static Progressive getBest() {
        if(Players.getLocal() == null || !Game.isLoggedIn()) {
            return null;
        }
        if (OgressWrapper.has7QuestPoints()
                || (Fighter.getRuntime() != null && Fighter.getRuntime().exceeds(Duration.ofHours(18)))) {
            BankWrapper.setTradeRestricted(false);
        }

        Progressive best = null;
        for (Progressive progressive : progressives) {
            int level = Skills.getLevel(progressive.getSkill());
            if(level >= progressive.getMinimumLevel() && level < progressive.getMaximumLevel()) {
                best = progressive;
            }
        }
        return best;
    }
}
