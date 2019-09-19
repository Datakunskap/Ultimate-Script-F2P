package script.fighter.backgroundTasks;

import org.rspeer.runetek.adapter.scene.Npc;
import script.fighter.CombatStore;
import script.fighter.config.Config;
import script.fighter.framework.BackgroundTaskExecutor;
import script.fighter.wrappers.CombatWrapper;

public class TargetChecker {

    public TargetChecker() {
        Runnable checkTargetHealth = () -> {
            Npc npc = CombatStore.getCurrentTargetNpc();
            if (npc == null) {
                CombatStore.setCurrentTarget(Config.isLooting() ? null : CombatWrapper.findTarget(false));
                return;
            }
            if (npc.getHealthPercent() <= 0) {
                CombatStore.setCurrentTarget(Config.isLooting() ? null : CombatWrapper.findTarget(false));
            }
        };
        BackgroundTaskExecutor.submit(checkTargetHealth, 100);
    }

}
