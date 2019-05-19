package script.tasks;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.WorldHopper;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class WorldHopF2P extends Task {

    @Override
    public boolean validate() {
        return Beggar.worldHopf2p && Beggar.hopTimeExpired;
    }

    @Override
    public int execute() {
        openWorldSwitcher();
        Beggar.currWorld = Worlds.getCurrent();

        WorldHopper.randomHop(x -> x != null && x.getPopulation() >= Beggar.worldPop &&
                !x.isMembers() && !x.isBounty() && !x.isSkillTotal());

        if(Time.sleepUntil(() -> Worlds.getCurrent() != Beggar.currWorld, 10000)){
            Log.fine("World hopped to world: " + Worlds.getCurrent());
            Beggar.startTime = System.currentTimeMillis();
            Beggar.hopTimeExpired = false;
            Beggar.hopTryCount = 0;
        } else {
            Log.severe("World hop failed...");
            Beggar.hopTryCount++;
        }

        if (Beggar.hopTryCount > 15) {
            Beggar.startTime = System.currentTimeMillis();
            Beggar.hopTimeExpired = false;
            Beggar.hopTryCount = 0;
        }
        return 1000;
    }

    public void openWorldSwitcher(){
        //Initiate interfaces
        InterfaceComponent worldSwitcher = Interfaces.getComponent(182,3);
        InterfaceComponent worldSelect = Interfaces.getComponent(69,0);
        //Open logout tab
        Tabs.open(Tab.LOGOUT);
        //Open world selector
        if(worldSwitcher != null){
            if(worldSwitcher.getMaterialId() == -1){
                worldSwitcher.interact("World Switcher");
                //Wait untill the world select screen is loaded
                Time.sleepUntil( () -> worldSwitcher.getMaterialId() != -1, 2000);
            }
        }
//        //Hop to random world
//        if(worldSelect != null){
//            WorldHopper.randomHopInP2p();       //Hop to members world
//            //WorldHopper.randomHopInF2p();     //Hop to Free 2 Play world
//        }
    }
}
