package script.tasks;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.WorldHopper;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.runetek.providers.RSWorld;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

import java.io.*;
import java.util.ArrayList;

public class WorldHop extends Task {

    private Beggar main;

    public WorldHop(Beggar beggar) {
        main = beggar;
    }

    @Override
    public boolean validate() {
        return (main.worldHop || main.worldHopf2p) && main.hopTimeExpired;
    }

    @Override
    public int execute() {
        int actualWorld = Worlds.getCurrent();
        main.atMinPop = false;
        resetMinPop();
        main.OTHER_BEG_WORLDS = getWorldsFromFile();

        openWorldSwitcher();

        if (main.worldHop) {
            WorldHopper.randomHop(x -> x != null && !main.OTHER_BEG_WORLDS.contains(x.getId()) && x.getPopulation() >= main.worldPop &&
                    x.isMembers() && !x.isSkillTotal());
        }

        if (main.worldHopf2p) {
            if(main.currWorld != 301 && actualWorld != 301 && !containsTwoWorlds(301)){
                WorldHopper.hopTo(301);
            }
            else if(main.currWorld != 308 && actualWorld != 308 && (!containsTwoWorlds(308) &&
                    Worlds.get(308).getPopulation() > 1000) ||
                    (!main.OTHER_BEG_WORLDS.contains(308) && Worlds.get(308).getPopulation() > main.worldPop)){
                WorldHopper.hopTo(308);
            }
            else if(main.currWorld != 393 && actualWorld != 393 &&
                    (!containsTwoWorlds(393) && Worlds.get(393).getPopulation() > 1000) ||
                    (!main.OTHER_BEG_WORLDS.contains(393) && Worlds.get(393).getPopulation() > main.worldPop)){
                WorldHopper.hopTo(393);
            }
            else {
                WorldHopper.randomHop(x -> x != null && !main.OTHER_BEG_WORLDS.contains(x.getId()) && x.getPopulation() >= main.worldPop &&
                        !x.isMembers() && !x.isBounty() && !x.isSkillTotal());
            }
        }

        Time.sleep(1000);

        if (Dialog.isOpen()) {
            if (Dialog.canContinue()){
                Dialog.processContinue();
            }
            Dialog.process("Switch to it");
            Time.sleepUntil(() -> !Dialog.isProcessing(), 10000);
            Time.sleep(5000);
        }

        if (Time.sleepUntil(() -> (main.currWorld > 0) ? Worlds.getCurrent() != main.currWorld : Worlds.getCurrent() != actualWorld, 10000)) {
            Log.fine("World hopped to world: " + Worlds.getCurrent());
            Time.sleep(3000, 5000);

            main.removeCurrBegWorld();

            main.startTime = System.currentTimeMillis();
            main.currWorld = Worlds.getCurrent();
            main.hopTimeExpired = false;
            main.hopTryCount = 0;

            main.worldPop = 800;
            writeWorldToFile();
            return 1000;
        } else {
            Log.info("World hop failed... Retrying");
            main.hopTryCount++;
        }

        if (main.hopTryCount > 10 || main.atMinPop) {
            Log.severe("World Hop Failed");
            resetMinPop();

            main.startTime = System.currentTimeMillis();
            main.hopTimeExpired = false;
            main.hopTryCount = 0;
        }
        return 500;
    }

    private void openWorldSwitcher() {
        //Initiate interfaces
        InterfaceComponent worldSwitcher = Interfaces.getComponent(182, 3);
        //Open logout tab
        Tabs.open(Tab.LOGOUT);
        //Open world selector
        if (worldSwitcher != null) {
            if (worldSwitcher.getMaterialId() == -1) {
                worldSwitcher.interact("World Switcher");
                //Wait untill the world select screen is loaded
                Time.sleepUntil(() -> worldSwitcher.getMaterialId() != -1, 2000);
            }
        }
    }

    private void resetMinPop() {
        main.OTHER_BEG_WORLDS = getWorldsFromFile();

        RSWorld[] f2pCriteriaWorlds = null;
        while (f2pCriteriaWorlds == null || f2pCriteriaWorlds.length == 0) {
            f2pCriteriaWorlds = Worlds.getLoaded(x -> x != null && !main.OTHER_BEG_WORLDS.contains(x.getId()) && x.getId() != Worlds.getCurrent() &&
                    x.getPopulation() >= main.worldPop && !x.isMembers() && !x.isBounty() && !x.isSkillTotal());

            if (f2pCriteriaWorlds == null || f2pCriteriaWorlds.length == 0) {
                if (main.worldPop >= 300) {
                    main.worldPop -= 10;
                    Log.info("Decreasing set world pop");
                } else {
                    Log.severe("Min World Pop Reached");
                    main.atMinPop = true;
                    break;
                }
            }
        }
    }

    private void writeWorldToFile() {
        try (FileWriter fw = new FileWriter(new File(Beggar.CURR_WORLD_PATH), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.println(main.currWorld);
        } catch (IOException e) {
            Log.severe("File not found");
        }
    }

    private ArrayList<Integer> getWorldsFromFile() {
        Log.info("Checking file");

        ArrayList<Integer> worlds = new ArrayList<>();
        try {
            File file = new File(Beggar.CURR_WORLD_PATH);

            if (!file.exists()) {
                return new ArrayList<>();
            }

            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            String line = br.readLine();
            while (line != null) {
                line = line.trim();
                if(!line.equals("") && !line.equals(" "))
                    worlds.add(Integer.parseInt(line));

                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            Log.info("No other beggar FNF");
            return new ArrayList<>();
        }

        return worlds;
    }

    private boolean containsTwoWorlds(int world) {
        if (main.OTHER_BEG_WORLDS == null)
            return false;

        boolean one = false;
        for (int x : main.OTHER_BEG_WORLDS) {
            if (x == world) {
                if (one) {
                    return true;
                }
                one = true;
            }
        }
        return false;
    }
}
