package script.tasks;

import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Login;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.EnterInput;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

import java.awt.*;
import java.io.*;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Mule extends Task implements ChatMessageListener {

    public static final org.rspeer.runetek.api.movement.position.Position Mulepos = new Position(3181, 3511);
    public String name;
    public int Gold;
    public int Gold2;
    public int gold3;
    public String status1;
    String user;
    public String status = "needgold";
    private boolean startScript = true;
    public static String Username;
    public static String Password;
    private boolean muleing = false;

    private void loginMule(){
        try {
            File file = new File("mule.txt");

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

    public void logoutMule(){
        try {
            File file = new File("mule.txt");

            if (!file.exists()) {
                file.createNewFile();
            }
            PrintWriter pw = new PrintWriter(file);
            pw.println("done");
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

    @Override
    public boolean validate() {
        if(Inventory.getCount(true, 995) >= Beggar.muleAmnt || muleing){
            loginMule();
            return true;
        }
        return false;
    }

    @Override
    public int execute() {
        if(startScript) {
            //inRead();
            if (status != null) {
                status = status.trim();
            }
            if (org.rspeer.runetek.api.component.Dialog.canContinue()) {
                Dialog.processContinue();
                Time.sleep(1000);
            }
            if (Mulepos.distance() > 2) {
                Movement.setWalkFlag(Mulepos);
            }

            if (Inventory.getFirst(995) != null) {
                Gold = Inventory.getFirst(995).getStackSize();
            }

            gold3 = Gold2 - Gold;


            if (status.contains("mule")) {
                if (!Game.isLoggedIn() && Username != null && Password != null) {
                    Login.enterCredentials(Username, Password);
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
                if (Trade.hasOtherAccepted()) {
                    Time.sleepUntil(() -> Trade.accept(), 5000);
                    Log.info("Trade accepted");
                }
            }
            if (status.contains("done")) {
                Game.logout();
                setStartScript(false);
            }
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
                                        EnterInput.initiate(Beggar.muleAmnt - Beggar.muleKeep);
                                        Time.sleep(1000);
                                    }
                                    if (Time.sleepUntil(() -> Trade.contains(true, 995), 500, 3500)) {
                                        Log.info("Trade entered & accepted");
                                        Trade.accept();
                                        Time.sleepUntil(() -> Trade.isOpen(true), 5000);
                                        break;
                                    }
                                    if(attempts > 10){
                                        break;
                                    }
                                }
                        } if (Trade.isOpen(true)) {
                            // handle second trade window...
                            Time.sleep(500, 1500);
                            if(Trade.accept()) {
                                if(!Trade.isOpen(true) && !Trade.isOpen(false)
                                        && !Inventory.contains(995)) {
                                    Log.fine("Trade completed shutting down mule");
                                    logoutMule();
                                    muleing = false;
                                }
                            }
                            Time.sleep(700);
                        }

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

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 1000) return Long.toString(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

    private void inRead() {
        try {
            File file = new File("mule.txt");

            if(!file.exists()) {
                file.createNewFile();
            }

            BufferedReader br = new BufferedReader(new FileReader(file));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                status = sb.toString();
                Log.info(status);
            } finally {
                br.close();
            }
            br.close();
        } catch (IOException e) {
            Log.info("File not found");
        }
    }

    public void notify(RenderEvent renderEvent) {
        Graphics g = renderEvent.getSource();
        g.drawString("status =  " + status, 300, 330);
        g.drawString("Gold Received " + format(gold3), 300, 350);
    }

    public void setStartScript(boolean startScript) {
        this.startScript = startScript;
    }

    public void notify(ChatMessageEvent Chatevent) {

        if (Chatevent.getMessage().contains("Accepted Trade")) {
            if(Inventory.getFirst(995) != null){
                Gold2 = Inventory.getFirst(995).getStackSize();
            }
        }
//        ChatMessageType type = Chatevent.getType();
//
//        if (type.equals(ChatMessageType.TRADE) && Mulepos.distance() <= 2) {
//            user = Chatevent.getSource();
//            // Do stuff
//            Log.info(user + " is Trading");
//
//        }
    }
}

