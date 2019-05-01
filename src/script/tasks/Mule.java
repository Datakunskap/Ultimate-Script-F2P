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
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.script.events.LoginScreen;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;
import script.gui.GUI;

import java.awt.*;
import java.io.*;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Mule extends Task {

    public static final org.rspeer.runetek.api.movement.position.Position Mulepos = new Position(3179, 3512);
    public String name;
    public int Gold;
    public int Gold2;
    public int gold3;
    public String status1 = "mule";
    String user;
    public String status = "needgold";
    public GUI gui;
    private boolean startScript = false;
    public static String Username;
    public static String Password;
    public int muleAmount;

    public void setupGui(){
        gui = new GUI(this);
        gui.setVisible(true);

        muleAmount = gui.muleAmount;
        Username = gui.Username;
        Password = gui.Password;
    }

    private void setupMule(){
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

            while (((status = br.readLine())) != null) {
                Log.info(status);
            }

            br.close();
        } catch (IOException e) {
            Log.info("File not found");
        }
    }

    @Override
    public boolean validate() {
        if(Inventory.getCount(true, 995) >= muleAmount){
            setupMule();
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
                    Trade.accept();
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
                            // handle first trade window...
                            Trade.offer("Coins", x -> x.contains("Offer-X"));
                            Time.sleep(1000);
                            if (EnterInput.isOpen()) {
                                EnterInput.initiate(4000000);
                                Time.sleep(1000);
                            }
                            if (Trade.contains(true, 995)) {
                                Trade.accept();
                                Time.sleep(700);
                            }
                        } else if (Trade.isOpen(true)) {
                            // handle second trade window...
                            Trade.accept();
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
        ChatMessageType type = Chatevent.getType();

        if (type.equals(ChatMessageType.TRADE)) {
            user = Chatevent.getSource();
            // Do stuff
            Log.info(user + " is Trading");

        }
    }
}

