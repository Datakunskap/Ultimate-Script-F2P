package script.fighter.nodes.loot;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Inventory;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.framework.Node;

public class BuryBones extends Node {

    private Item[] bones;

    private Fighter main;

    public BuryBones(Fighter main){
        this.main = main;
    }

    @Override
    public boolean validate() {
        if(!Config.buryBones()) {
            return false;
        }
        bones = Inventory.getItems(p ->
                p.getName().toLowerCase().contains("bones") && p.containsAction("Bury"));
        return bones.length > 0;
    }

    @Override
    public int execute() {
        //Log.info("Burying Bones");
        if(bones == null) {
            return Fighter.getLoopReturn();
        }
        for (Item bone : bones) {
            bone.click();
            Time.sleep(100, 350);
        }
        return Fighter.getLoopReturn();
    }

    @Override
    public String status() {
        return "Burying Bones";
    }
}
