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

    public static boolean tryTeleport(boolean homeTeleport) {
        Spell spell = homeTeleport ? Spell.Modern.HOME_TELEPORT : getTeleportSpell();
        if (checkTime(spell)) {
            Log.fine("Teleporting: " + spell.getName());
            Magic.cast(spell);
            Time.sleep(18_000, 20_000);
            return true;
        }
        return false;
    }

    private static boolean checkTime(Spell spell) {
        if (timer == null) {
            timer = StopWatch.start();
            return true;
        }
        if (timer.exceeds(Duration.ofMinutes(29)) && spell == Spell.Modern.HOME_TELEPORT) {
            timer = StopWatch.start();
            return true;
        }
        if (spell == Spell.Modern.VARROCK_TELEPORT) {
            return true;
        }
        if (spell == Spell.Modern.FALADOR_TELEPORT) {
            return true;
        }
        return false;
    }

    private static Spell getTeleportSpell() {
        if (!Tabs.isOpen(Tab.MAGIC)) {
            Tabs.open(Tab.MAGIC);
            Time.sleepUntil(() -> Tabs.getOpen() == Tab.MAGIC, 1000, 5000);
        }

        if (Magic.canCast(Spell.Modern.VARROCK_TELEPORT)) {
            return Spell.Modern.VARROCK_TELEPORT;
        } else if (Magic.canCast(Spell.Modern.FALADOR_TELEPORT)) {
            return Spell.Modern.FALADOR_TELEPORT;
        } else {
            return Spell.Modern.HOME_TELEPORT;
        }
    }
}
