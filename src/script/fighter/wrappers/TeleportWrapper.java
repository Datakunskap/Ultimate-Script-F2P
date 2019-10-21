package script.fighter.wrappers;

import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.ui.Log;

import java.time.Duration;

public class TeleportWrapper {

    private static StopWatch timer;

    public static boolean teleportHome() {
        if (checkTime(Spell.Modern.HOME_TELEPORT)) {
            Tabs.open(Tab.MAGIC);
            Time.sleepUntil(() -> Tabs.getOpen() == Tab.MAGIC, 1000, 5000);
            Log.fine("Teleporting Home");
            Magic.cast(Spell.Modern.HOME_TELEPORT);
            Time.sleep(18000);
            return true;
        }
        return false;
    }

    private static boolean checkTime(Spell spell) {
        if (timer == null) {
            timer = StopWatch.start();
            return true;
        }
        if (timer.exceeds(Duration.ofMinutes(29))
                && spell == Spell.Modern.HOME_TELEPORT) {

            timer = StopWatch.start();
            return true;
        }
        return false;
    }
}
