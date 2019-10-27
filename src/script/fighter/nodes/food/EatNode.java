package script.fighter.nodes.food;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.Inventory;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.debug.Logger;
import script.fighter.framework.Node;
import script.fighter.wrappers.CombatWrapper;

import java.util.function.Predicate;

public class EatNode extends Node {

    private Item food;
    private int eatTillPercent;
    private final Predicate<Item> FOOD = i -> Config.getFood().contains(i.getName().toLowerCase());

    private Fighter main;

    public EatNode(Fighter main){
        this.main = main;
    }

    @Override
    public boolean validate() {
        int current = CombatWrapper.getHealthPercent();
        if (eatTillPercent != -1 && current < eatTillPercent) {
            food = Inventory.getFirst(FOOD);
            return true;
        }
        boolean lowHealth = current < 40;
        if (!lowHealth) {
            eatTillPercent = -1;
            return false;
        }
        food = Inventory.getFirst(FOOD);
        return food != null;
    }

    @Override
    public int execute() {
        main.invalidateTask(this);

        Logger.debug("Attempting to eat.");
        if (food == null) {
            Logger.severe("No food?");
            eatTillPercent = -1;
            return Fighter.getLoopReturn();
        }
        if(eatTillPercent == -1) {
            eatTillPercent = Random.high(55, 75);
        }
        food.interact("Eat");

        return Fighter.getLoopReturn();
    }

    @Override
    public void onInvalid() {
        eatTillPercent = -1;
        food = null;
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
        return null;
    }
}
