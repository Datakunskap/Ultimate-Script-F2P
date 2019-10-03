package script.fighter.nodes.combat;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.PathingEntity;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.runetek.event.types.DeathEvent;
import org.rspeer.runetek.event.types.TargetEvent;
import org.rspeer.ui.Log;
import script.fighter.CombatStore;
import script.fighter.NodeSupplier;
import script.fighter.Stats;
import script.fighter.config.Config;
import script.fighter.config.ProgressiveSet;
import script.fighter.debug.Logger;
import script.fighter.models.NpcResult;
import script.fighter.models.Progressive;
import script.fighter.nodes.idle.IdleNode;
import script.fighter.wrappers.CombatWrapper;

import java.util.HashMap;
import java.util.HashSet;

public class CombatListener {

    public static void onTargetEvent(TargetEvent e) {
        PathingEntity source = e.getSource();
        PathingEntity target = e.getTarget();
        PathingEntity oldTarget = e.getOldTarget();
        //source is local player.
        if(source instanceof Player && source.equals(Players.getLocal())) {
            if(target instanceof Npc) {
                if(!target.containsAction("Attack")) {
                    return;
                }
                CombatStore.setCurrentTarget(new NpcResult((Npc) target, true));
            }
        }
        //source is another player
        if(source instanceof Player && !source.equals(Players.getLocal())) {
            if(target instanceof Npc) {
                Npc current = CombatStore.getCurrentTargetNpc();
                if(target.equals(current)) {
                    Logger.debug("Another player is targeting our target.");
                    CombatStore.setCurrentTarget(null);
                }
                NpcResult next = CombatStore.getNextTarget();
                if(next != null && target.equals(next.getNpc())) {
                    Logger.debug("Another player is targeting our next target.");
                    CombatStore.setNextTarget(null);
                }
            }
        }

        if(target instanceof Player && target.equals(Players.getLocal())) {
            if(source instanceof Npc) {
                if(source.containsAction("Attack")) {
                    CombatStore.addTargetingMe((Npc) source);
                }
            }
        }

        if(oldTarget instanceof Player && oldTarget.equals(Players.getLocal())) {
            if(source instanceof Npc) {
                CombatStore.removeTargetingMe((Npc) source);
            }
        }
    }

    public static void onChatMessage(ChatMessageEvent e) {
        if(e.getType() == ChatMessageType.PUBLIC || e.getType() == ChatMessageType.PRIVATE_RECEIVED)
            return;
        else if(e.getMessage().toLowerCase().contains("someone else is fighting that")) {
            Logger.debug("Someone else is fighting our target. Clearing.");
            CombatStore.setCurrentTarget(Config.isLooting() ? null : CombatWrapper.findTarget(false));
        }
        else if(e.getMessage().toLowerCase().contains("already under attack")) {
            Logger.debug("Already under attack.");
            HashSet<Npc> targetingMe = CombatStore.getTargetingMe();
            if(targetingMe.size() > 0) {
                CombatStore.setCurrentTarget(new NpcResult(targetingMe.iterator().next(), true));
            } else {
                CombatStore.setCurrentTarget(null);

            }
        }
        else if(e.getMessage().toLowerCase().contains("reach that!")) {
            Config.getProgressive().setPosition(Config.getProgressive().getPosition().randomize(3));
            CombatStore.resetTargetingValues();
        }
        else if(e.getMessage().toLowerCase().contains("the door seems to be stuck")) {
            CombatStore.resetTargetingValues();
        }
    }

    public static void onDeathEvent(DeathEvent e, NodeSupplier supplier) {
        PathingEntity source = e.getSource();
        if (source.equals(Players.getLocal())) {
            Log.info("You Died");
            Progressive p = ProgressiveSet.getCurrent();
            p.setEquipmentMap(new HashMap<>());
        }
        NpcResult current = CombatStore.getCurrentTarget();
        if(current != null && source.equals(current.getNpc())) {
            Stats.onKilled(source.getName());
            Logger.debug("Current target has died.");
            CombatStore.setCurrentTarget(null);
            IdleNode idleNode = (IdleNode) supplier.IDLE;
            idleNode.onTargetKill();
        }
        NpcResult next = CombatStore.getNextTarget();
        if(next != null && source.equals(next.getNpc())) {
            Logger.debug("Next target has died.");
            CombatStore.setNextTarget(null);
        }
        if(source instanceof Npc && CombatStore.getTargetingMe().contains(source)) {
            CombatStore.removeTargetingMe((Npc) source);
        }
    }

}
