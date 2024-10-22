package script.chocolate.tasks;

import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Login;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.*;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.Script;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.chocolate.Main;

import java.io.*;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Mule extends Task {

    private int Gold;
    private int Gold2 = 0;
    private String status = "needgold";
    private static String Username = null;
    private static String Password = null;
    private boolean muleing = false;
    private int begWorld = -1;
    private static final String MULE_FILE_PATH = Script.getDataDirectory() + "\\mule.txt";

    private Main main;

    public Mule(Main main) {
        this.main = main;
    }

    private void loginMule() {
        String status1;
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
        return main.sold && (Inventory.getCount(true, 995) >= main.muleAmnt || Bank.getCount(995) >= main.muleAmnt || muleing); }

    @Override
    public int execute() {
        main.isMuling = true;
		if (Bank.isOpen() && Bank.getCount(995) >= main.muleAmnt) {
			Bank.withdrawAll(995);
			Time.sleepUntil(() -> !Bank.contains(995), 5000);
		}
		
        loginMule();

        if(Worlds.getCurrent() != main.muleWorld){
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
        }

        if (status != null) {
            status = status.trim();
        }
        if (Dialog.canContinue()) {
            Dialog.processContinue();
            Time.sleep(1000);
        }
        if (!main.muleArea.getMuleArea().contains(Players.getLocal())) {
            if (script.chocolate.tasks.WalkingHelper.shouldEnableRun()) {
                script.chocolate.tasks.WalkingHelper.enableRun();
            }
            Movement.setWalkFlag(main.muleArea.getMuleArea().getTiles().get(main.randInt(0, main.muleArea.getMuleArea().getTiles().size()-1)));
        }

        if (Inventory.getFirst(995) != null) {
            Gold = Inventory.getFirst(995).getStackSize();
        }

        int gold3 = Gold2 - Gold;

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
            if (Players.getNearest(main.muleName) != null && !Trade.isOpen()) {
                Players.getNearest(main.muleName).interact("Trade with");
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
                            Log.fine("Trade completed shutting down mule");
                            logoutMule();
                            muleing = false;
                            main.amntMuled += (Coins - main.muleKeep);
                            main.setRandMuleKeep(main.minKeep, main.maxKeep);
                            if(begWorld != -1) {
                                WorldHopper.hopTo(begWorld);
                                Time.sleepUntil(() -> Worlds.getCurrent() == begWorld, 10000);
                            }
                            Time.sleep(8000, 10000);
                            main.isMuling = false;
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

