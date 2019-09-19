package script.fighter.paint;

import org.rspeer.runetek.adapter.scene.Npc;
import script.fighter.CombatStore;
import script.fighter.models.NpcResult;

import java.awt.*;
import java.util.HashSet;

public class CombatPaintRenderer {

    public static void onRenderEvent(Graphics g) {
        Npc target = CombatStore.getCurrentTargetNpc();
        HashSet<Npc> targetingMeSet = CombatStore.getTargetingMe();
        g.setColor(Color.yellow);
        if (target != null) {
            target.getPosition().outline(g);
        }
        NpcResult next = CombatStore.getNextTarget();
        if(next != null) {
            g.setColor(Color.PINK);
            next.getNpc().getPosition().outline(g);
        }
        for (Npc npc : targetingMeSet) {
            if (npc == null)
                continue;
            g.setColor(Color.red);
            npc.getPosition().outline(g);
        }

    }

}
