package script.fighter.wrappers;

import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.WorldHopper;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.providers.RSWorld;
import org.rspeer.ui.Log;
import script.Script;

import java.io.*;
import java.time.Duration;
import java.util.HashSet;

public class WorldhopWrapper {

    private static HashSet<Integer> ogressWorlds;
    public static HashSet<Integer> OTHER_BEG_WORLDS;
    private static StopWatch worldHopTimer;
    private static long elapsedMinutes;
    public static int currentWorld;

    public static void resetChecker() {
        worldHopTimer = null;
        ogressWorlds = null;
    }

    public static void checkWorldhop(boolean targetCheck) {
        if (worldHopTimer == null) {
            worldHopTimer = StopWatch.start();
        }
        if (ogressWorlds == null) {
            ogressWorlds = getWorldsFromFile(Script.OGRESS_WORLD_PATH);
        }

        if (ogressWorlds.contains(Worlds.getCurrent())
                && ((!targetCheck || Players.getLocal().getTargetIndex() == -1))) {

            if (worldHopTimer.exceeds(Duration.ofMinutes(Script.OGRESS_WORLD_HOP_MINS))) {
                Log.fine("World-Hopping");

                removeWorld(Worlds.getCurrent(), Script.OGRESS_WORLD_PATH);

                hopToLowPopWorld(50, Worlds.getCurrent(), getWorldsFromFile(Script.OGRESS_WORLD_PATH));

                writeWorldToFile(Worlds.getCurrent(), Script.OGRESS_WORLD_PATH);
                currentWorld = Worlds.getCurrent();

                resetChecker();
            }
            else {
                if (worldHopTimer.getElapsed().toMinutes() != elapsedMinutes) {
                    elapsedMinutes = worldHopTimer.getElapsed().toMinutes();
                    Log.fine("World Hopping in: "
                            + (Script.OGRESS_WORLD_HOP_MINS - worldHopTimer.getElapsed().toMinutes()) + "min(s)");
                }
            }
        } else {
            worldHopTimer = StopWatch.start();
        }
    }

    public static void hopToLowPopWorld(int pop, int current) {
        hopToLowPopWorld(pop, current, new HashSet<>());
    }

    public static void hopToLowPopWorld(int pop, int current, HashSet<Integer> excludeWorlds) {
        RSWorld newWorld = null;
        while (newWorld == null && pop <= 1100) {
            final int finalPop = pop;
            newWorld = Worlds.get(x -> !excludeWorlds.contains(x.getId()) && x.getPopulation() <= finalPop
                                            && !x.isMembers() && !x.isBounty() && !x.isSkillTotal());
            pop += 50;
        }
        if (newWorld == null) {
            Log.severe("World Hop Failed -> Cant Find World");
            return;
        }

        final RSWorld world = newWorld;
        if (Time.sleepUntil(() -> WorldHopper.hopTo(world), 5_000, 20_000)) {

            if (Time.sleepUntil(() -> Worlds.getCurrent() != current && Game.getState() == Game.STATE_IN_GAME
                    && !Game.isLoadingRegion() && Game.isLoggedIn() && Game.getState() != Game.STATE_LOADING_REGION
                    && Players.getLocal() != null, 2000, 30_000)) {

                Log.fine("Hopped to Low Population World: " + world.getId());
                Time.sleep(1000, 3000);
            }
        } else {
            Log.severe("World Hop Failed -> Cant Hop To World: " + world.getId());
        }
    }

    public static void removeWorld(int currentWorld, String filePath) {
        BufferedReader reader;
        try {
            File inputFile = new File(filePath);
            File tempFile = new File(org.rspeer.script.Script.getDataDirectory() + "\\TEMP" + inputFile.getName());

            if (!inputFile.exists())
                return;

            reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String lineToRemove = Integer.toString(currentWorld);
            String currentLine;

            boolean rm = false;
            while ((currentLine = reader.readLine()) != null) {
                // trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if (trimmedLine.contains(lineToRemove) && !rm) {
                    rm = true;
                    continue;
                }
                writer.write(currentLine + System.getProperty("line.separator"));
            }
            writer.close();
            reader.close();

            if (inputFile.exists() && !inputFile.delete()) {
                Log.severe("Could not delete file | Retrying...");
                Thread.sleep(5000);
                removeWorld(currentWorld, filePath);
            }

            if (tempFile.exists() && !tempFile.renameTo(inputFile)) {
                Log.severe("Could not rename file | Retrying...");
                Thread.sleep(5000);
                removeWorld(currentWorld, filePath);
            }

        } catch (Exception e) {
            Log.severe("Exception: removeCurrBegWorld(" + currentWorld + "): " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void writeWorldToFile(int currentWorld, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.println(currentWorld);
        } catch (IOException e) {
            Log.severe("File not found");
            Log.severe("FNF: writeWorldToFile()");
        }
    }

    public static HashSet<Integer> getWorldsFromFile(String filePath) {

        HashSet<Integer> worlds = new HashSet<>();
        try {
            File file = new File(filePath);

            if (!file.exists()) {
                return new HashSet<>();
            }

            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            String line = br.readLine();
            while (line != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.equals(" ") && !line.equals(System.lineSeparator())) {
                    worlds.add(Integer.parseInt(line));
                }
                line = br.readLine();
            }
            br.close();
        } catch (IOException e) {
            Log.severe("FNF getting Worlds");
            return new HashSet<>();
        }

        return worlds;
    }

    public static boolean containsTwoOrMoreWorlds(int world, HashSet<Integer> otherWorlds) {
        if (otherWorlds == null) {
            Log.severe("OTHER_BEG_WORLDS == null: containsTwoWorlds(int world)");
            return false;
        }

        boolean one = false;
        for (int x : otherWorlds) {
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
