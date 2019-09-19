package script.fighter.nodes.loot;

import org.rspeer.runetek.adapter.scene.Pickable;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.scene.Players;
import script.fighter.CombatStore;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.debug.Logger;
import script.fighter.framework.Node;
import script.fighter.services.LootService;

public class LootNode extends Node {

    private Pickable[] items;

    private Fighter main;

    public LootNode(Fighter main){
        this.main = main;
    }

    @Override
    public boolean validate() {
        if(Config.getLoot().size() == 0) {
            return false;
        }
        final boolean hasTarget = CombatStore.hasTarget();
        if(hasTarget) {
            return false;
        }
        if(Inventory.isFull()) {
            return false;
        }
        if(!Config.getProgressive().isPrioritizeLooting() &&
                Players.getLocal().distance(Config.getStartingTile()) > Config.getRadius()){
            return false;
        }
        items = LootService.getItemsToLoot();
        for (Pickable i : items) {
            if (i.getPosition().distance(Config.getStartingTile()) <= Config.getRadius()) {
                return true;
            }
        }
        return false;
        //return items != null && items.length > 0;
    }

    @Override
    public int execute() {
        //Log.info("Looting");
        invalidateTask(main.getActive());

        if(items != null && items.length > 0) {
            for (Pickable item : items) {
                if(item != null) {
                    int count = Inventory.getCount(item.getId());
                    if(!item.interact("Take")) {
                        continue;
                    }
                    Time.sleep(100, 250);
                    if(Players.getLocal().isMoving()) {
                        Time.sleepUntil(() -> !Players.getLocal().isMoving(), 10000);
                    }
                    Time.sleepUntil(() -> Inventory.getCount(item.getId()) != count, 2500);
                }
            }
        }
        return Fighter.getLoopReturn();
    }

    @Override
    public void onInvalid() {
        items = null;
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
    public String status() {
        return "Looting";
    }
}
