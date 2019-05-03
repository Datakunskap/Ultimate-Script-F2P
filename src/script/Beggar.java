package script;

import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import script.data.Coins;
import script.data.Lines;
import script.data.Location;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.script.ScriptMeta;
import org.rspeer.script.task.TaskScript;
import org.rspeer.ui.Log;
import script.data.MuleArea;
import script.tasks.*;
import script.ui.Gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.awt.*;

@ScriptMeta(name = "Ultimate Beggar", desc = "Begs for gp", developer = "DrScatman")
public class Beggar extends TaskScript implements RenderListener, ChatMessageListener {

    private int startC;
    private StopWatch runtime;

    public static List<Coins> gpArr;
    public static Coins gp;
    public static Location location;
    public static Lines lines;
    public static MuleArea muleArea;

    public static boolean walk = true;
    public static boolean beg = true;
    public static boolean tradePending = false;
    public static boolean trading = false;
    public static String traderName;
    public static boolean changeAmount = false;
    public static boolean iterAmount;
    public static boolean atGE = false;
    public static String muleName;
    public static int muleAmnt = Integer.MAX_VALUE;
    public static int minWait;
    public static int maxWait;
    public static int muleKeep;
    public static int amountChance;
    public static boolean isMuling = false;
    public static String[] linesArr;
    public static boolean defaultLines = false;
    public static String inputLines;

    @Override
    public void onStart() {
        Log.fine("Script started.");
        runtime = StopWatch.start();
        startC = Inventory.getCount(true, 995);
        location = Location.GE_AREA;

        submit(new Gui(),
                new Mule(),
                new TradePlayer(),
                new WaitTrade(),
                new ChangeAmount(),
                new ToggleRun(),
                new Banking(),
                new Traverse(),
                new Beg()
        );
    }

    @Override
    public void onStop() {
        Log.severe("Script stopped.");
        removeAll();
    }

    @Override
    public void notify(ChatMessageEvent msg) {
        // If not in a trade and a player trades you...
        if (!Trade.isOpen() && msg.getType().equals(ChatMessageType.TRADE) && !isMuling) {
            traderName = msg.getSource();
            tradePending = true;
            trading = true;
            walk = false;
            beg = false;
            //new TradePlayer().execute();
        }
    }

    @Override
    public void notify(RenderEvent e) {
        Graphics g = e.getSource();

        int gainedC = Inventory.getCount(true, 995) - startC;

        g.drawString("Runtime: " + runtime.toElapsedString(), 20, 40);
        //g.drawString("Gp in bank: " + Bank.getCount(995), 20, 60);
        g.drawString("Gp gained: " + gainedC, 20, 80);
        g.drawString("Gp /h: " + runtime.getHourlyRate(gainedC), 20, 100);
    }

    public static int randInt(int min, int max) {
        java.util.Random rand = new java.util.Random();
        int randomNum = rand.nextInt(max - min + 1) + min;
        return randomNum;
    }

    public static void reloadLines(){
        if(defaultLines){
            defaultLines();
        } else {
            convertInputLines(inputLines);
        }
    }

    public static void defaultLines() {
        linesArr = new String[]{"Can someone pls double my " + Beggar.gp.getSgp() + " coins?",
                "I only have " + Beggar.gp.getSgp() + " can anyone double it pls?",
                "I have " + Beggar.gp.getSgp() + " Can someone double it so I buy pick",
                "Can Any1 Double My " + Beggar.gp.getSgp() + " gp pls??",
                "Can someone Doble my coins pls :)",
                "Any1 willing to double " + Beggar.gp.getSgp() + "?",
                "Can someone help a noob out and double my " + Beggar.gp.getSgp() + "? :)",
                "Please someone double me so I can buy a pick and diggy diggy hole!",
                "Im newb can some1 help me and double " + Beggar.gp.getSgp() + "! :)"};

        lines = new Lines(linesArr);
    }

    public static void convertInputLines(String inputLines) {
        // Trims leading/trailing whitespace
        String inLines = inputLines.trim();

        // ENTER delimiter
        List<String> arr = Arrays.asList(inLines.split(System.lineSeparator()));
        linesArr = new String[arr.size()];
        int index = 0;

        // Checks for no gp amount in line
        for (String s : arr) {
            if (!s.contains("$")) {
                linesArr[index] = s;
                index++;
            }
        }

        List<String> arr2 = new ArrayList<>();
        for (String s2 : arr) {
            if (s2.contains("$")) {
                String[] temp = s2.split("\\$", -1);
                arr2.add(temp[0]);
                arr2.add(temp[1]);
            }
        }

        for (int i = 0; i < arr2.size(); i += 2) {

            // Checks for gp amount at start of line
            if (arr2.get(i) == null || arr2.get(i).equals("") || arr2.get(i).equals(" ")) {
                linesArr[index] = gp.getSgp() + arr2.get(i + 1);
                index++;
            }

            // Checks for gp amount at end of line
            else if (arr2.get(i + 1) == null || arr2.get(i + 1).equals("") || arr2.get(i + 1).equals(" ")) {
                linesArr[index] = arr2.get(i) + gp.getSgp();
                index++;
            }

            // Otherwise somewhere in middle
            else {
                linesArr[index] = arr2.get(i) + gp.getSgp() + arr2.get(i + 1);
                index++;
            }
        }

        lines = new Lines(linesArr);
    }
}
