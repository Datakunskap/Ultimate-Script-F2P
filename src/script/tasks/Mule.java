package script.tasks;

import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Login;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.EnterInput;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.api.component.WorldHopper;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.Script;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

import java.io.*;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Mule extends Task {

    public int Gold;
    public int Gold2;
    public int gold3;
    public String status1;
    String user;
    public String status = "needgold";
    public static String Username;
    public static String Password;
    private boolean muleing = false;
    private int begWorld = -1;
    private static final String MULE_FILE_PATH = Script.getDataDirectory() + "\\mule.txt";

    private void loginMule() {
        try {
            File file = new File(MULE_FILE_PATH);

            if (!file.exists()) {
                file.createNewFile();
            }
            PrintWriter pw = new PrintWriter(file);
            pw.println("mule");
            pw.close();

            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            while (((status1 = br.readLine())) != null) {
                Log.info(status1);
            }

            br.close();
        } catch (IOException e) {
            Log.info("File not found");
        }

        user = Beggar.muleName;
    }

    public static void logoutMule() {
        try {
            File file = new File(MULE_FILE_PATH);

            if (!file.exists()) {
                Log.info("Logout file not found");
            }
            PrintWriter pw = new PrintWriter(file);
            pw.println("done");
            pw.close();

            Log.info("done");

        } catch (IOException e) {
            Log.info("File not found");
        }
    }

    @Override
    public boolean validate() { return Inventory.getCount(true, 995) >= Beggar.muleAmnt || muleing; }

    @Override
    public int execute() {
        Beggar.isMuling = true;
        loginMule();

        if(Worlds.getCurrent() != Beggar.muleWorld){
            begWorld = Worlds.getCurrent();
            WorldHopper.hopTo(Beggar.muleWorld);
            Time.sleepUntil(() -> Worlds.getCurrent() == Beggar.muleWorld, 10000);
        }

        if (status != null) {
            status = status.trim();
        }
        if (org.rspeer.runetek.api.component.Dialog.canContinue()) {
            Dialog.processContinue();
            Time.sleep(1000);
        }
        if (!Beggar.muleArea.getMuleArea().contains(Players.getLocal())) {
            Movement.setWalkFlag(Beggar.muleArea.getMuleArea().getTiles().get(Beggar.randInt(0, Beggar.muleArea.getMuleArea().getTiles().size()-1)));
        }

        if (Inventory.getFirst(995) != null) {
            Gold = Inventory.getFirst(995).getStackSize();
        }

        gold3 = Gold2 - Gold;

        if (status.contains("needgold")) {
            if (!Game.isLoggedIn() && Username != null && Password != null) {
                Login.enterCredentials(Username, Password);
                Keyboard.pressEnter();
                Time.sleep(200);
                Keyboard.pressEnter();
                Time.sleep(200);
                Keyboard.pressEnter();
                Time.sleep(200);
                Keyboard.pressEnter();
            }
            if (Players.getNearest(user) != null && !Trade.isOpen()) {
                Players.getNearest(user).interact("Trade with");
                Time.sleep(3000);
            }
            if (Inventory.getFirst(995) != null) {
                if (!Trade.contains(true, 995)) {
                    int Coins = Inventory.getFirst(995).getStackSize();
                    if (Trade.isOpen(false)) {
                        muleing = true;
                        // handle first trade window...
                        int attempts = 0;
                        while (true) {
                            attempts++;
                            Log.info("Entering trade offer");
                            Trade.offer("Coins", x -> x.contains("X"));
                            Time.sleep(1000);
                            if (EnterInput.isOpen()) {
                                EnterInput.initiate(Coins - Beggar.muleKeep);
                                Time.sleep(1000);
                            }
                            if (Time.sleepUntil(() -> Trade.contains(true, 995), 500, 3500)) {
                                Log.info("Trade entered & accepted");
                                Trade.accept();
                                Time.sleepUntil(() -> Trade.isOpen(true), 5000);
                                break;
                            }
                            if (attempts > 6) {
                                break;
                            }
                        }
                    }
                    if (Trade.isOpen(true)) {
                        // handle second trade window...
                        Time.sleep(500, 1500);
                        if (Trade.accept()) {
                            Time.sleep(3000);
                            Log.fine("Complete Shutting Down Mule");
                            muleing = false;
                            logoutMule();
                            Beggar.changeAmount = true;
                            Beggar.walk = true;
                            Beggar.sendTrade = true;
                            Beggar.beg = true;
                            //Beggar.atGE = false;
                            Beggar.buildGEPath = true;
                            Beggar.trading = false;
                            Beggar.amntMuled += (Coins - Beggar.muleKeep);
                            Beggar.equipped = false;
                            if(begWorld != -1) {
                                WorldHopper.hopTo(begWorld);
                                Time.sleepUntil(() -> Worlds.getCurrent() == begWorld, 10000);
                            }
                            Time.sleep(8000, 10000);
                            Beggar.isMuling = false;
                        }
                        Time.sleep(700);
                    }

                }
            }
        }
        return 500;
    }

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "B");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }
}

