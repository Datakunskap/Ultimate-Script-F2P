package script.fighter.nodes.food;

import org.rspeer.runetek.api.component.Bank;
import script.fighter.Fighter;
import script.fighter.debug.Logger;
import script.fighter.framework.Node;
import script.fighter.wrappers.BankWrapper;
import script.fighter.wrappers.CombatWrapper;

public class GetFoodNode extends Node {

    private boolean running;

    private Fighter main;

    public GetFoodNode(Fighter main){
        this.main = main;
    }

    @Override
    public boolean validate() {
        return running || CombatWrapper.getHealthPercent() < 40;
    }

    @Override
    public int execute() {
        invalidateTask(main.getActive());

        if(!BankWrapper.openNearest()) {
            return Fighter.getLoopReturn();
        }
        Bank.withdrawAll("Bread");
        Bank.withdrawAll("Shrimps");
        return Fighter.getLoopReturn();
    }

    @Override
    public void onInvalid() {
        running = false;
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
