package script;

import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.script.events.LoginScreen;
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
//import script.gui.muleGUI;
import script.gui.GUI;
import script.tasks.*;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;

@ScriptMeta(name = "Begging bot (Rand)", desc = "Begs for gold", developer = "DrScatman")
public class Beggar extends TaskScript implements RenderListener, ChatMessageListener {

    private int startC;
    private StopWatch runtime;

    public static ArrayList<Coins> gpArr;
    public static Coins gp;
    public static Location location;
    public static Lines lines;

    public static boolean walk = true;
    public static boolean beg = true;
    public static boolean tradePending = false;
    public static boolean trading = false;
    public static String traderName;
    public static boolean changeAmount = false;
    public static boolean iterAmount;
    public static boolean atGE = false;

    @Override
    public void onStart() {
        Log.fine("Script started.");
        runtime = StopWatch.start();
        startC = Inventory.getCount(true, 995);
        location = Location.GE_AREA;

        gpArr = new ArrayList<>();
        gpArr.add(Coins.GP_25);
        //gpArr.add(Coins.GP_50);
        //gpArr.add(Coins.GP_100);
        gpArr.add(Coins.GP_500);
        gpArr.add(Coins.GP_1000);
        gpArr.add(Coins.GP_2500);
        gpArr.add(Coins.GP_5000);

        // Set start amount
        gp = gpArr.get(2);
        // Set increment or random
        iterAmount = false;

        loadLines();

        // Mule
        removeBlockingEvent(LoginScreen.class);
        Mule mule = new Mule();
        //mule.setupMule();
        //submit(mule);

        submit(new TradePlayer(),
                new ToggleRun(),
                new Traverse(),
                new Beg(),
                new Amount(),
                new Banking(),
                new WaitTrade()
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
        if (!Trade.isOpen() && msg.getType().equals(ChatMessageType.TRADE)) {
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

        int gainedC  = Inventory.getCount(true, 995) - startC;

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

    public static void loadLines(){
        lines = new Lines("Can someone pls double my " + Beggar.gp.getSgp() + " coins?",
                "I only have " + Beggar.gp.getSgp() + " can anyone double it pls?",
                "I have " + Beggar.gp.getSgp() + " Can someone double it so I buy pick",
                "Can Any1 Double My " + Beggar.gp.getSgp() + " gp pls??",
                "Can someone Doble my coins pls :)",
                "Any1 willing to double " + Beggar.gp.getSgp() + "?",
                "Can someone help a noob out and double my " + Beggar.gp.getSgp() + "? :)",
                "Please someone double me so I can buy a pick and diggy diggy hole!",
                "Im newb can some1 help me and double " + Beggar.gp.getSgp() + "! :)");

    }
}
