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
import org.rspeer.script.Script;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

import java.io.*;
import java.util.ArrayList;

public class WorldHop extends Task {

    private static boolean atMinPop = false;

    @Override
    public boolean validate() {
        return (Beggar.worldHop || Beggar.worldHopf2p) && Beggar.hopTimeExpired;
    }

    @Override
    public int execute() {
        resetMinPop();
        Beggar.OTHER_BEG_WORLDS = getWorldsFromFile();

        openWorldSwitcher();

        if (Beggar.worldHop) {
            WorldHopper.randomHop(x -> x != null && !Beggar.OTHER_BEG_WORLDS.contains(x.getId()) && x.getPopulation() >= Beggar.worldPop &&
                    x.isMembers() && !x.isSkillTotal());
        }

        if (Beggar.worldHopf2p) {
            if(Beggar.currWorld != 301 && !Beggar.OTHER_BEG_WORLDS.contains(301)){
                WorldHopper.hopTo(301);
            }
            else if(Beggar.currWorld != 308 && !Beggar.OTHER_BEG_WORLDS.contains(308)){
                WorldHopper.hopTo(308);
            }
            else if(Beggar.currWorld != 393 && !Beggar.OTHER_BEG_WORLDS.contains(393)){
                WorldHopper.hopTo(393);
            }
            else {
                WorldHopper.randomHop(x -> x != null && !Beggar.OTHER_BEG_WORLDS.contains(x.getId()) && x.getPopulation() >= Beggar.worldPop &&
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

        if (Time.sleepUntil(() -> Worlds.getCurrent() != Beggar.currWorld && !Beggar.OTHER_BEG_WORLDS.contains(Worlds.getCurrent()), 10000)) {
            Log.fine("World hopped to world: " + Worlds.getCurrent());
            Time.sleep(3000, 5000);

            removeCurrBegWorld();

            Beggar.startTime = System.currentTimeMillis();
            Beggar.currWorld = Worlds.getCurrent();
            Beggar.hopTimeExpired = false;
            Beggar.hopTryCount = 0;

            Beggar.worldPop = 800;
            writeWorldToFile();
        } else {
            Log.info("World hop failed... Retrying");
            Beggar.hopTryCount++;
        }

        if (Beggar.hopTryCount > 10 || atMinPop) {
            Log.severe("World Hop Failed");
            resetMinPop();

            Beggar.startTime = System.currentTimeMillis();
            Beggar.hopTimeExpired = false;
            Beggar.hopTryCount = 0;
        }
        return 500;
    }

    private void openWorldSwitcher() {
        //Initiate interfaces
        InterfaceComponent worldSwitcher = Interfaces.getComponent(182, 3);
        InterfaceComponent worldSelect = Interfaces.getComponent(69, 0);
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

    private static void resetMinPop() {
        Beggar.OTHER_BEG_WORLDS = getWorldsFromFile();

        RSWorld[] f2pCriteriaWorlds = null;
        while (f2pCriteriaWorlds == null || f2pCriteriaWorlds.length == 0) {
            f2pCriteriaWorlds = Worlds.getLoaded(x -> x != null && !Beggar.OTHER_BEG_WORLDS.contains(x.getId()) && x.getId() != Worlds.getCurrent() &&
                    x.getPopulation() >= Beggar.worldPop && !x.isMembers() && !x.isBounty() && !x.isSkillTotal());

            if (f2pCriteriaWorlds == null || f2pCriteriaWorlds.length == 0) {
                if (Beggar.worldPop >= 300) {
                    Beggar.worldPop -= 10;
                    Log.info("Decreasing set world pop");
                } else {
                    Log.severe("Min World Pop Reached");
                    atMinPop = true;
                    break;
                }
            }
        }
    }

    private void writeWorldToFile() {
        try (FileWriter fw = new FileWriter(new File(Beggar.CURR_WORLD_PATH), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.println(Beggar.currWorld);
        } catch (IOException e) {
            Log.severe("File not found");
        }
    }

    public static ArrayList<Integer> getWorldsFromFile() {
        Log.info("Checking file");

        ArrayList<Integer> worlds = new ArrayList<Integer>();
        try {
            File file = new File(Beggar.CURR_WORLD_PATH);

            if (!file.exists()) {
                return new ArrayList<Integer>();
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
            return new ArrayList<Integer>();
        }

        return worlds;
    }

    public static void removeCurrBegWorld(){
        BufferedReader reader = null;
        try {
            File inputFile = new File(Beggar.CURR_WORLD_PATH);
            File tempFile = new File(Script.getDataDirectory() + "\\TEMPCurrBegWorld.txt");

            if (!inputFile.exists())
                return;

            reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String lineToRemove = Integer.toString(Beggar.currWorld);
            String currentLine;

            while((currentLine = reader.readLine()) != null) {
                // trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if(trimmedLine.equals(lineToRemove)) continue;
                writer.write(currentLine + System.getProperty("line.separator"));
            }
            writer.close();
            reader.close();

            if (inputFile.exists() && !inputFile.delete()) {
                Log.severe("Could not delete file | Retrying...");
                Thread.sleep(5000);
                removeCurrBegWorld();
            }

            if (tempFile.exists() && !tempFile.renameTo(inputFile)) {
                Log.severe("Could not rename file | Retrying...");
                Thread.sleep(5000);
                removeCurrBegWorld();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
