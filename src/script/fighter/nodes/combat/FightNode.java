package script.fighter.nodes.combat;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.PathingEntity;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Players;
import script.fighter.CombatStore;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.debug.Logger;
import script.fighter.framework.BackgroundTaskExecutor;
import script.fighter.framework.Node;
import script.fighter.models.NpcResult;
import script.fighter.services.LootService;
import script.fighter.wrappers.CombatWrapper;

public class FightNode extends Node {

    private NpcResult result;
    private String status;
    private boolean running;

    private Fighter main;

    public FightNode(Fighter main){
        this.main = main;
        BackgroundTaskExecutor.submit(this::findNextTarget, 1000);
    }

    public FightNode() {
        BackgroundTaskExecutor.submit(this::findNextTarget, 1000);
    }

    @Override
    public boolean validate() {
        NpcResult target = CombatStore.getCurrentTarget();
        if(target != null) {
            return true;
        }
        if(Config.getProgressive().isPrioritizeLooting()) {
            //Item to loot, return.
            System.out.println("Prioritizing looting");
            if(LootService.getItemsToLoot().length > 0) {
                System.out.println("Found item to loot");
                return false;
            }
        }
        status = "Looking for target.";
        NpcResult res = CombatWrapper.findTarget(false);
        if(res == null || res.getNpc() == null) {
            status = "No targets around me, waiting...";
            return false;
        }
        Logger.fine("New Target Index: " + res.getNpc().getIndex());
        result = res;
        doAttack(result.getNpc());
        return true;
    }

    @Override
    public int execute() {
        invalidateTask(main.getActive());

        //Log.info(status);
        running = true;
        if(result != null && !CombatStore.hasTarget()) {
            doAttack(result.getNpc());
            return Fighter.getLoopReturn();
        }
        NpcResult target = CombatStore.getCurrentTarget();
        if(target == null || CombatWrapper.isDead(target.getNpc())) {
            status = "Target has died.";
            CombatStore.setCurrentTarget(null);
            return Fighter.getLoopReturn();
        }
        if(!CombatWrapper.isTargetingMe(target.getNpc())) {
            Logger.debug("Our current target is not targeting me.");
            if(CombatStore.getTargetingMe().size() > 0) {
               status = "Switching to target that is targeting me.";
               Npc first = CombatStore.getTargetingMe().stream().filter(n -> {
                   PathingEntity npcsTarget = n.getTarget();
                   return npcsTarget != null && npcsTarget.equals(Players.getLocal()) && n.getIndex() != target.getNpc().getIndex();
               }).findFirst().orElse(null);
               if(first == null) {
                   Logger.debug("Targeting me first is null, grabbing next.");
                   Npc next = CombatStore.getTargetingMe().iterator().next();
                   CombatStore.setCurrentTarget(new NpcResult(next, true));
                   doAttack(next);
               } else {
                   Logger.debug("Changing target to: " + first.getIndex());
                   CombatStore.setCurrentTarget(new NpcResult(first, true));
                   doAttack(first);
               }
               return Fighter.getLoopReturn();
            }
            doAttack(target.getNpc());
        }
        return Fighter.getLoopReturn();
    }

    public void onInvalid() {
        running = false;
        super.onInvalid();
    }

    public void invalidateTask(Node active) {
        if (active != null && !this.equals(active)) {
            Logger.debug("Node has changed.");
            active.onInvalid();
        }
        main.setActive(this);
    }

    @Override
    public void onScriptStop() {
        super.onScriptStop();
    }

    @Override
    public String status() {
        return status;
    }

    private void findNextTarget() {
        if(!running)
            return;
        CombatStore.setNextTarget(CombatWrapper.findTarget(true));
    }

    private void doAttack(Npc npc) {
        Player p = Players.getLocal();
        PathingEntity target = p.getTarget();
        PathingEntity targetsTarget = target == null ? null : target.getTarget();
        if(p.getTargetIndex() != -1 && target != null && targetsTarget != null && targetsTarget.equals(p)) {
            System.out.println("In combat.");
            return;
        }
        if(Movement.isInteractable(npc, false)) {
            status = "Attacking " + npc.getName() + " (" + npc.getIndex() + ").";
            Logger.debug("Attacking target: " + npc.getIndex());
            if (Config.getProgressive().getSpell() == null) {
                npc.interact("Attack");
            } else {
                castSpell(npc);
            }
            Time.sleepUntil(() -> Players.getLocal().getTargetIndex() > 0, 1500);
            return;
        }
        status = "Walking to target.";
        Movement.walkTo(npc);
    }

    private void castSpell(Npc npc) {
        if (!Tabs.isOpen(Tab.MAGIC)) {
            Tabs.open(Tab.MAGIC);
            Time.sleepUntil(() -> Tabs.isOpen(Tab.MAGIC), 100, 1000);
        }
        Magic.cast(Config.getProgressive().getSpell(), npc);
    }
}
