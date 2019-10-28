package script.data;

import org.rspeer.RSPeer;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;
import script.Script;
import script.fighter.wrappers.WorldhopWrapper;
import script.tutorial_island.TutorialIsland;

import java.io.*;

public class CheckTutIsland {

    private Script main;
    private static final int MAX_TUT_PROGRESS = 670;
    private static final int MAX_TUT_SECTION = 20;

    public CheckTutIsland(Script script) {
        main = script;
    }

    public boolean onTutIsland() {
        InterfaceComponent tutBar = Interfaces.getComponent(614, 21);

        return (Varps.get(281) <= MAX_TUT_PROGRESS && Varps.get(406) <= MAX_TUT_SECTION) ||
                (tutBar != null && tutBar.isVisible()) ||
                main.TUTORIAL_ISLAND_AREA.contains(Players.getLocal());
    }

    public void execute() {

        if (true) {
            TutorialIsland.getInstance(main).start();

        } else {

            if (main.currWorld != -1 && !main.isTanning) {
                Log.info("World Removed");
                WorldhopWrapper.removeWorld(main.currWorld, Script.CURR_WORLD_PATH);
            }

            File file1 = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\EXTutIsland\\TutIsland1.json");
            File file2 = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\EXTutIsland\\Beggar1.json");
            int[] IDs = writeJson(file1, file2, RSPeer.getGameAccount().getUsername());

            String path1 = "C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\EXTutIsland\\TutIsland";
            String path2 = "C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\EXTutIsland\\Script";
            int sleep = Script.randInt(360000, 660000);
            String javaVersion = "java";//"\"C:\\Program Files\\Java\\jdk1.8.0_201\\bin\\java.exe\"";
            String launcher = javaVersion + " -jar C:\\Users\\bllit\\OneDrive\\Desktop\\BegLauncher.jar "
                    + IDs[0] + " " + IDs[1] + " " + path1 + " " + path2 + " " + sleep + " && exit";

            try {
                Runtime.getRuntime().exec(
                        "cmd /c start cmd.exe /K \"" + launcher + "\"");

                System.exit(0);

            } catch (Exception e) {
                System.out.println("HEY Buddy ! U r Doing Something Wrong ");
                e.printStackTrace();
            }
        }
    }

    private void addWorldToFile() {
        main.currWorld = Worlds.getCurrent();
        WorldhopWrapper.writeWorldToFile(main.currWorld, Script.CURR_WORLD_PATH);
    }

    private int[] writeJson(File file1, File file2, String account) {
        String[] arr1 = editAccount(file1, account);
        String[] arr2 = editAccount(file2, account);
        int[] IDs = new int[2];

        PrintWriter pw = null;
        try {
            int tutID = 1;
            while (file1.exists()) {
                tutID++;
                file1 = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\EXTutIsland\\TutIsland" + tutID + ".json");
            }
            file1.createNewFile();
            IDs[0] = tutID;

            pw = new PrintWriter(file1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String s : arr1) {
            assert pw != null;
            pw.println(s);
        }
        assert pw != null;
        pw.close();

        PrintWriter pw2 = null;
        try {
            int begID = 1;
            while (file2.exists()) {
                begID++;
                file2 = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\EXTutIsland\\Script" + begID + ".json");
            }
            file2.createNewFile();
            IDs[1] = begID;

            pw2 = new PrintWriter(file2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String s : arr2) {
            assert pw2 != null;
            pw2.println(s);
        }
        assert pw2 != null;
        pw2.close();

        return IDs;
    }

    private String[] editAccount(File file, String account) {
        String data = "";
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                data = sb.toString();
            }
        } catch (IOException e) {
            Log.info("File not found");
        }

        String[] arr1 = data.split(System.lineSeparator());
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i].contains("\"RsUsername\":")) {
                arr1[i] = "\t\t\"RsUsername\": " + "\"" + account + "\"" + ",";
            }
            if (arr1[i].contains("\"World\":")) {
                arr1[i] = "\t\t\"World\": " + main.popWorldsArr[Script.randInt(0, 2)] + ",";
            }
        }
        return arr1;
    }
}
