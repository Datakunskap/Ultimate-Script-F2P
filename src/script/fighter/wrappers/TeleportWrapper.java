package script.fighter.wrappers;

import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;

import java.time.Duration;

public class TeleportWrapper {

    private static StopWatch timer;

    public static boolean tryTeleport(Spell spell, boolean homeTeleport) {
         Spell teleport = homeTeleport ? Spell.Modern.HOME_TELEPORT : getTeleportSpell(spell);

        if (check(teleport)) {
            Log.fine("Teleporting: " + teleport.getName());
            Magic.cast(teleport);
            if (teleport == Spell.Modern.HOME_TELEPORT) {
                Time.sleep(18_000, 20_000);
            } else {
                Time.sleepUntil(() -> !Players.getLocal().isAnimating(), 1000, 15_000);
                Time.sleep(300, 600);
            }
            return true;
        }
        return false;
    }

    public static boolean tryTeleport(boolean homeTeleport) {
        return tryTeleport(null, homeTeleport);
    }

    public static boolean tryTeleport(Spell spell) {
        return tryTeleport(spell, false);
    }

    private static boolean check(Spell spell) {
        if (timer == null) {
            timer = StopWatch.start();
            return true;
        }
        if (spell == null) {
            return false;
        }
        else if (timer.exceeds(Duration.ofMinutes(29)) && spell == Spell.Modern.HOME_TELEPORT) {
            timer = StopWatch.start();
            return true;
        } else if (spell != Spell.Modern.HOME_TELEPORT) {
            return true;
        }
        return false;
    }

    private static Spell getTeleportSpell(Spell spell) {
        if (!Tabs.isOpen(Tab.MAGIC)) {
            Tabs.open(Tab.MAGIC);
            Time.sleepUntil(() -> Tabs.getOpen() == Tab.MAGIC, 1000, 5000);
        }

        if (spell != null && Magic.canCast(spell)) {
            return spell;
        } else if (spell != null) {
            return null;
        }

        if (Magic.canCast(Spell.Modern.VARROCK_TELEPORT)) {
            return Spell.Modern.VARROCK_TELEPORT;
        } else if (Magic.canCast(Spell.Modern.FALADOR_TELEPORT)) {
            return Spell.Modern.FALADOR_TELEPORT;
        } else if (Magic.canCast(Spell.Modern.LUMBRIDGE_TELEPORT)) {
            return Spell.Modern.LUMBRIDGE_TELEPORT;
        } else if (Magic.canCast(Spell.Modern.TELEOTHER_FALADOR)) {
            return Spell.Modern.TELEOTHER_FALADOR;
        } else if (Magic.canCast(Spell.Modern.TELEOTHER_LUMBRIDGE)) {
            return Spell.Modern.TELEOTHER_LUMBRIDGE;
        } else {
            return Spell.Modern.HOME_TELEPORT;
        }
    }
}
