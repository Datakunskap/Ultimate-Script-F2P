package script.tasks;

import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.WorldHopper;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class WorldHop extends Task {

    private int curr;

    @Override
    public boolean validate() {
        return Beggar.worldHop && Beggar.hopTimeExpired;
    }

    @Override
    public int execute() {
        curr = Worlds.getCurrent();

        WorldHopper.randomHop(x -> x != null && x.getPopulation() >= Beggar.worldPop);
        if(Time.sleepUntil(() -> Worlds.getCurrent() != curr, 20000)){
            Log.fine("World hopped to world: " + Worlds.getCurrent());
        } else {
            Log.severe("World hop failed...");
        }
        Beggar.hopTimeExpired = false;
        Beggar.startTime = System.currentTimeMillis();
        return 1000;
    }

//    public void hopToRandomWorld(){
//        //Initiate interfaces
//        InterfaceComponent worldSwitcher = Interfaces.getComponent(182,3);
//        InterfaceComponent worldSelect = Interfaces.getComponent(69,0);
//        //Open logout tab
//        Tabs.open(Tab.LOGOUT);
//        //Open world selector
//        if(worldSwitcher != null){
//            if(worldSwitcher.getMaterialId() == -1){
//                worldSwitcher.interact("World Switcher");
//                //Wait untill the world select screen is loaded
//                Time.sleepUntil( () -> worldSwitcher.getMaterialId() != -1, 2000);
//            }
//        }
//        //Hop to random world
//        if(worldSelect != null){
//            WorldHopper.randomHopInP2p();       //Hop to members world
//            //WorldHopper.randomHopInF2p();     //Hop to Free 2 Play world
//        }
//    }
}
