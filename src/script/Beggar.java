package script;

import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.Login;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.listeners.LoginResponseListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.runetek.event.types.LoginResponseEvent;
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
import script.data.MuleArea;
import script.tasks.*;
import script.ui.Gui;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.awt.*;
import java.util.List;

import static org.rspeer.runetek.event.types.LoginResponseEvent.Response.*;

@ScriptMeta(name = "Ultimate Beggar", desc = "Begs for gp", developer = "DrScatman")
public class Beggar extends TaskScript implements RenderListener, ChatMessageListener {

    public static int startC;
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
    public static String traderName = "";
    public static boolean changeAmount = false;
    public static boolean iterAmount;
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
    public static int muleWorld;
    public static boolean buildGEPath = true;
    public static int worldPop;
    public static boolean worldHop;
    public static int hopTime;
    public static boolean hopTimeExpired = false;
    public static int sameTraderCount = 0;
    public static int walkChance;
    public static boolean worldHopf2p;
    public static boolean equipped = false;
    public static int currWorld = -1;
    public static Player trader = null;
    public static int hopTryCount = 0;
    public static int amntIndex = 0;
    public static boolean processSentTrade = false;
    public static boolean tradeSent = false;
    public static boolean sendTrade = true;
    public static int sendTryCount = 0;
    public static boolean sentTradeInit = false;
    public static int gainedC = 0;
    public static int amntMuled = 0;
    public static boolean banked = false;

    public static long startTime = 0;
    public static long currTime = 0;

    private static boolean firstHopCall = false;

    @Override
    public void onStart() {
        Log.fine("Script started.");
        LoginScreen ctx = new LoginScreen(this);
        ctx.setStopScriptOn(INVALID_CREDENTIALS);


        runtime = StopWatch.start();
        startC = Inventory.getCount(true, 995);
        location = Location.GE_AREA;

        submit(new Gui());

        if (Beggar.worldHop) {
            startTime = System.currentTimeMillis();
        }

        submit(new TradePlayer(),
                new WaitTrade(),
                new Mule(),
                new WorldHop(),
                new WorldHopF2P(),
                new ChangeAmount(),
                new ToggleRun(),
                new Banking(),
                new Traverse(),
                new BuyGE(),
                new SendTrade(),
                new Beg()
        );
    }

    @Override
    public void onStop() {
        Log.severe("Script stopped.");
        if (isMuling) {
            Mule.logoutMule();
        }
        writeJson(readAccount());
        removeAll();
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        String[] arr = pid.split("@");
        String id = arr[0];
        String pKill = "taskkill -f /PID " + id;
        String launcher = "java -jar C:\\Users\\bllit\\OneDrive\\Desktop\\BeggarLauncher.jar";

        try {
            Runtime.getRuntime().exec(
                    "cmd /c start cmd.exe /K \"" + launcher + "\"");

            Runtime.getRuntime().exec(
                    "cmd /c start cmd.exe /K \"" + pKill + "\"");
        } catch (Exception e) {
            System.out.println("HEY Buddy ! U r Doing Something Wrong ");
            e.printStackTrace();
        }
    }

    public static void writeJson(String account) {
        File file1 = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Beggar1.json");
        File file2 = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Simscape1.json");
        String data1 = "";
        String data2 = "";

        try {
            BufferedReader br = new BufferedReader(new FileReader(file1));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                data1 = sb.toString();
            } finally {
                br.close();
            }
            br.close();
        } catch (IOException e) {
            Log.info("File not found");
        }
        //data = data.trim();
        String[] arr1 = data1.split(System.lineSeparator());
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i].contains("\"RsUsername\":")){
                arr1[i] = "\t\t\"RsUsername\": " + "\"" + account + "\"" + ",";
            }
        }

        try {
            BufferedReader br = new BufferedReader(new FileReader(file2));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                data2 = sb.toString();
            } finally {
                br.close();
            }
            br.close();
        } catch (IOException e) {
            Log.info("File not found");
        }
        //data = data.trim();
        String[] arr2 = data2.split(System.lineSeparator());
        for (int i = 0; i < arr2.length; i++) {
            if (arr2[i].contains("\"RsUsername\":")){
                arr2[i] = "\t\t\"RsUsername\": " + "\"" + account + "\"" + ",";
            }
        }

        PrintWriter pw = null;
        try {
            int id = 1;
            while(file1.exists()) {
                id++;
                file1 = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Beggar" + id + ".json");
            }
            file1.createNewFile();

            pw = new PrintWriter(file1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String s : arr1) {
            pw.println(s);
        }
        pw.close();

        PrintWriter pw2 = null;
        try {
            int id = 1;
            while(file2.exists()) {
                id++;
                file2 = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Simscape" + id + ".json");
            }
            file2.createNewFile();

            pw2 = new PrintWriter(file2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String s : arr2) {
            pw2.println(s);
        }
        pw2.close();
    }

    public static String readAccount() {
        String accounts = "";
        File file = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\f2pAccounts.txt");

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                accounts = sb.toString();
            } finally {
                br.close();
            }
            br.close();
        } catch (IOException e) {
            Log.info("File not found");
        }

        accounts = accounts.trim();
        List<String> arr = Arrays.asList(accounts.split(System.lineSeparator()));
        String account = arr.get(0);
        account = account.trim();

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (int i = 1; i < arr.size(); i++) {
            pw.println(arr.get(i));
        }
        pw.close();

        return account;
}

    @Override
    public void notify(ChatMessageEvent msg) {
        // If not in a trade and a player trades you...
        if (!Trade.isOpen() && msg.getType().equals(ChatMessageType.TRADE) && !isMuling) {
            if(msg.getSource().equals(traderName)) {
                sameTraderCount++;
            } else {
                sameTraderCount = 0;
            }
            if (sameTraderCount < 5) {
                traderName = msg.getSource();
                tradePending = true;
                trading = true;
                walk = false;
                beg = false;
            }
        }

        if ((msg.getMessage().contains("Sending trade offer") || msg.getType().equals(ChatMessageType.TRADE_SENT)) &&
                !isMuling && !Trade.isOpen() && !trading) {
            //Log.info("Trade sent");
            Beggar.tradeSent = true;
        }

        if (msg.getMessage().contains("player is busy at the moment") && !isMuling && !Trade.isOpen() && !trading) {
            Beggar.walk = true;
        }
    }

    @Override
    public void notify(RenderEvent e) {
        Graphics g = e.getSource();

        gainedC = Inventory.getCount(true, 995) + amntMuled;
        gainedC -= startC;
        g.drawString("Runtime: " + runtime.toElapsedString(), 20, 40);
        g.drawString("Gp gained: " + format(gainedC), 20, 60);
        g.drawString("Gp /h: " + format((long) runtime.getHourlyRate(gainedC)), 20, 80);
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

    public static int randInt(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
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

    public static void checkWorldHopTime(){
        currTime = System.currentTimeMillis();
        int elapsedSeconds = (int)((currTime - startTime) / 1000);
        if (elapsedSeconds > (hopTime * 60)) {
            if (firstHopCall) {
                hopTimeExpired = true;
            } else {
                firstHopCall = true;
                startTime = System.currentTimeMillis();
            }
        }
    }
}
