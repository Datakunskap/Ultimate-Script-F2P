package script.beg;

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
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Script;

import java.io.*;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Mule extends Task {

    private int Gold;
    private int Gold2;
    private int gold3;
    private String status1;
    String user;
    private String status = "needgold";
    private String Username;
    private String Password;
    private boolean muleing = false;
    private int begWorld = -1;
    private static final String MULE_FILE_PATH = org.rspeer.script.Script.getDataDirectory() + "\\mule.txt";

    private Script main;

    public Mule(Script script) {
        main = script;
    }

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

        user = main.muleName;
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
    public boolean validate() {
        if (main.muted && !atMuleAmnt(Script.MUTED_MULE_AMNT)) {
            main.disableChain = false;
            main.setStopping(true);
        }

        return atMuleAmnt(main.muleAmnt) || (main.muted && atMuleAmnt(Script.MUTED_MULE_AMNT)) || muleing;
    }

    private boolean atMuleAmnt(int amnt) {
        return Inventory.contains(995) && Inventory.getCount(true, 995) >= amnt;
    }

    @Override
    public int execute() {
        main.isMuling = true;
        loginMule();

        if (Worlds.getCurrent() != main.muleWorld) {
            begWorld = Worlds.getCurrent();
            WorldHopper.hopTo(main.muleWorld);

            if (Dialog.isOpen()) {
                if (Dialog.canContinue()) {
                    Dialog.processContinue();
                }
                Dialog.process(x -> x != null && x.toLowerCase().contains("future"));
                Dialog.process(x -> x != null && (x.toLowerCase().contains("switch") || x.toLowerCase().contains("yes")));
                Time.sleepUntil(() -> !Dialog.isProcessing(), 10000);
            }

            Time.sleepUntil(() -> Worlds.getCurrent() == main.muleWorld, 10000);
            main.currWorld = main.muleWorld;
        }

        if (status != null) {
            status = status.trim();
        }
        if (org.rspeer.runetek.api.component.Dialog.canContinue()) {
            Dialog.processContinue();
            Time.sleep(1000);
        }
        if (!main.muleArea.getMuleArea().contains(Players.getLocal())) {
            Movement.setWalkFlag(main.muleArea.getMuleArea().getTiles().get(Script.randInt(0, main.muleArea.getMuleArea().getTiles().size() - 1)));
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
                                EnterInput.initiate(Coins - main.muleKeep);
                                Time.sleep(1000);
                            }
                            if (Time.sleepUntil(() -> Trade.contains(true, 995), 500, 3500)) {
                                if (Script.MULE_ITEMS) {
                                    offerItems();
                                }

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
                            main.changeAmount = true;
                            main.walk = true;
                            main.sendTrade = true;
                            main.beg = true;
                            //main.atGE = false;
                            main.buildGEPath = true;
                            main.trading = false;
                            main.amntMuled += (Coins - main.muleKeep);
                            main.setRandMuleKeep(2500, 10000);

                            main.equipped = false;
                            main.bought = false;

                            if (begWorld != -1) {
                                WorldHopper.hopTo(begWorld);
                                Time.sleepUntil(() -> Worlds.getCurrent() == begWorld, 20000);
                                main.currWorld = Worlds.getCurrent();
                            }
                            Time.sleep(8000, 10000);
                            main.randBuyGP = Script.randInt(1500, 5000);
                            main.isMuling = false;

                            if (main.muted) {
                                main.disableChain = false;
                                main.setStopping(true);
                            }
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

    private boolean isDefaultItem(int id) {
        for (int i : main.items) {
            if (i == id)
                return true;
        }
        return false;
    }

    private void offerItems() {
        while (!Inventory.containsOnly(x -> x != null && x.getId() == 995 &&
                !x.isExchangeable() && isDefaultItem(x.getId())) && !Inventory.isEmpty()) {
            Trade.offerAll(x -> x.getId() != 995 && x.isExchangeable() && !isDefaultItem(x.getId()));
            Time.sleep(500);
        }
    }
}

