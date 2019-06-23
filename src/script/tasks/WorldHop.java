package script.tasks;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.WorldHopper;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.runetek.providers.RSWorld;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

import java.io.*;

public class WorldHop extends Task {

    private static final int OTHER_BEG_WORLD = checkWorldFromFile();

    @Override
    public boolean validate() {
        return (Beggar.worldHop || Beggar.worldHopf2p) && Beggar.hopTimeExpired;
    }

    @Override
    public int execute() {
        openWorldSwitcher();
        Beggar.currWorld = Worlds.getCurrent();

        if (Beggar.worldHop) {
            WorldHopper.randomHop(x -> x != null && x.getId() != OTHER_BEG_WORLD && x.getPopulation() >= Beggar.worldPop &&
                    x.isMembers() && !x.isSkillTotal());
        }
        if (Beggar.worldHopf2p) {
            WorldHopper.randomHop(x -> x != null && x.getId() != checkWorldFromFile() && x.getPopulation() >= Beggar.worldPop &&
                    !x.isMembers() && !x.isBounty() && !x.isSkillTotal());
        }

        if(Time.sleepUntil(() -> Worlds.getCurrent() != Beggar.currWorld && Worlds.getCurrent() != OTHER_BEG_WORLD, 10000)){
            Log.fine("World hopped to world: " + Worlds.getCurrent());
            Beggar.startTime = System.currentTimeMillis();
            Beggar.hopTimeExpired = false;
            Beggar.hopTryCount = 0;

            if (Beggar.worldPop < 850) {
                Beggar.worldPop += 50;
            }
            writeWorldToFile();
        } else {
            Log.info("World hop failed... Retrying");
            Beggar.hopTryCount++;
        }

        if (Beggar.hopTryCount > 15) {
            Log.severe("World Hop Failed");
            resetMinPop();

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
    }

    public static void resetMinPop() {
        RSWorld[] f2pCriteriaWorlds = null;
        while (f2pCriteriaWorlds == null || f2pCriteriaWorlds.length == 0) {
            f2pCriteriaWorlds = Worlds.getLoaded(x -> x != null && x.getId() != OTHER_BEG_WORLD && x.getId() != Worlds.getCurrent() &&
                    x.getPopulation() >= Beggar.worldPop && !x.isMembers() && !x.isBounty() && !x.isSkillTotal());

            if (f2pCriteriaWorlds == null || f2pCriteriaWorlds.length == 0) {
                if (Beggar.worldPop >= 500) {
                    Beggar.worldPop -= 10;
                } else {
                    Log.severe("Min World Pop Reached");
                    break;
                }
            }
        }
    }

    private void writeWorldToFile() {
        try {
            File file = new File(Beggar.CURR_WORLD_PATH);

            if (!file.exists()) {
                file.createNewFile();
            }
            PrintWriter pw = new PrintWriter(file);
            pw.println(Worlds.getCurrent());
            pw.close();

        } catch (IOException e) {
            Log.info("File not found");
        }
    }

    private static int checkWorldFromFile() {
        Log.info("Checking file");

        String world = "";
        try {
            File file = new File(Beggar.CURR_WORLD_PATH);

            if (!file.exists()) {
                return -1;
            }

            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            String line = br.readLine();
            while (line != null){
                world = line;
                line = br.readLine();
            }
            br.close();
            Log.info("Beggar on: " + world);
        } catch (IOException e) {
            Log.info("No other beggar FNF");
        }

        if (world != null && !world.equals("")) {
            world = world.trim();
            return Integer.parseInt(world);
        } else {
            return -1;
        }
    }
}
