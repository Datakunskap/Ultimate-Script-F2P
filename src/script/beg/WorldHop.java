package script.beg;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.WorldHopper;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.runetek.api.input.menu.ActionOpcodes;
import org.rspeer.runetek.providers.RSWorld;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

import java.io.File;

import static script.fighter.wrappers.WorldhopWrapper.*;

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
        main.atMinPop = false;
        resetMinPop();
        openWorldSwitcher();
        OTHER_BEG_WORLDS = getWorldsFromFile(Beggar.CURR_WORLD_PATH);

        if (main.runningClients != null && main.runningClients.size() > 0 &&
                main. runningClients.size() >= Beggar.ALLOWED_INSTANCES && OTHER_BEG_WORLDS.size() > main.runningClients.size() - 1)
            new File(Beggar.CURR_WORLD_PATH).delete();


        if (main.worldHop) {
            WorldHopper.randomHop(x -> x != null && !OTHER_BEG_WORLDS.contains(x.getId()) && x.getPopulation() >= main.worldPop &&
                    x.isMembers() && !x.isSkillTotal());
        }

        if (main.worldHopf2p) {
            if (main.currWorld != main.popWorldsArr[0] && !containsTwoOrMoreWorlds(main.popWorldsArr[0], OTHER_BEG_WORLDS)) {

                WorldHopper.hopTo(main.popWorldsArr[0]);
            } else if (main.currWorld != main.popWorldsArr[1] && ((!containsTwoOrMoreWorlds(main.popWorldsArr[1], OTHER_BEG_WORLDS) && Worlds.get(main.popWorldsArr[1]).getPopulation() > 950) ||
                    (!OTHER_BEG_WORLDS.contains(main.popWorldsArr[1]) && Worlds.get(main.popWorldsArr[1]).getPopulation() >= main.worldPop))) {

                WorldHopper.hopTo(main.popWorldsArr[1]);
            } else if (main.currWorld != main.popWorldsArr[2] && ((!containsTwoOrMoreWorlds(main.popWorldsArr[2], OTHER_BEG_WORLDS) && Worlds.get(main.popWorldsArr[2]).getPopulation() > 950) ||
                    (!OTHER_BEG_WORLDS.contains(main.popWorldsArr[2]) && Worlds.get(main.popWorldsArr[2]).getPopulation() >= main.worldPop))) {

                WorldHopper.hopTo(main.popWorldsArr[2]);
            } else {
                WorldHopper.randomHop(x -> x != null && !OTHER_BEG_WORLDS.contains(x.getId()) && x.getPopulation() >= main.worldPop &&
                        !x.isMembers() && !x.isBounty() && !x.isSkillTotal());
            }
        }

        Time.sleep(1000);

        InterfaceComponent sti = Interfaces.getComponent(193, 0, 3);
        if (sti != null && sti.isVisible()) {
            sti.interact(ActionOpcodes.INTERFACE_ACTION);
            Time.sleepUntil(() -> !sti.isVisible() && !Dialog.isProcessing(), 10000);
            Time.sleep(5000);
        }

        if (Dialog.isOpen()) {
            if (Dialog.canContinue()) {
                Dialog.processContinue();
            }
            Dialog.process(x -> x != null && x.toLowerCase().contains("future"));
            Dialog.process(x -> x != null && (x.toLowerCase().contains("switch") || x.toLowerCase().contains("yes")));
            Time.sleepUntil(() -> !Dialog.isProcessing(), 10000);
            Time.sleep(5000);
        }

        if (Time.sleepUntil(() -> Worlds.getCurrent() != main.currWorld, 12000)) {
            Log.fine("World hopped to world: " + Worlds.getCurrent());
            Time.sleep(3000, 5000);

            removeWorld(main.currWorld, Beggar.CURR_WORLD_PATH);
            main.currWorld = Worlds.getCurrent();

            main.startTime = System.currentTimeMillis();
            main.hopTimeExpired = false;
            main.hopTryCount = 0;

            main.worldPop = 800;
            writeWorldToFile(main.currWorld, Beggar.CURR_WORLD_PATH);
            return 1000;
        } else {
            Log.info("World hop failed... Retrying");
            main.hopTryCount++;
        }

        if ((main.hopTryCount > 10 || main.atMinPop) && Worlds.getCurrent() == main.currWorld) {
            Log.severe("World Hop Failed");
            resetMinPop();

            main.startTime = System.currentTimeMillis();
            main.hopTimeExpired = false;
            main.hopTryCount = 0;
        }
        return 800;
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
        OTHER_BEG_WORLDS = getWorldsFromFile(Beggar.CURR_WORLD_PATH);

        RSWorld[] f2pCriteriaWorlds = null;
        while (f2pCriteriaWorlds == null || f2pCriteriaWorlds.length == 0) {
            f2pCriteriaWorlds = Worlds.getLoaded(x -> x != null && !OTHER_BEG_WORLDS.contains(x.getId()) && x.getId() != Worlds.getCurrent() &&
                    x.getPopulation() >= main.worldPop && !x.isMembers() && !x.isBounty() && !x.isSkillTotal());

            if (f2pCriteriaWorlds == null || f2pCriteriaWorlds.length == 0) {
                if (main.worldPop >= main.minPop) {
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
}
