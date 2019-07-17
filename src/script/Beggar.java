package script;

import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.listeners.ItemTableListener;
import org.rspeer.runetek.event.listeners.LoginResponseListener;
import org.rspeer.runetek.event.types.*;
import org.rspeer.script.Script;
import org.rspeer.script.events.LoginScreen;
import script.data.Coins;
import script.data.Lines;
import script.data.Location;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.script.ScriptMeta;
import org.rspeer.script.task.TaskScript;
import org.rspeer.ui.Log;
import script.data.MuleArea;
import script.tanner.ExPriceChecker;
import script.tanner.Main;
import script.tasks.*;
import script.ui.Gui;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.util.List;

import static org.rspeer.runetek.event.types.LoginResponseEvent.Response.INVALID_CREDENTIALS;
import static org.rspeer.runetek.event.types.LoginResponseEvent.Response.RUNESCAPE_UPDATE_2;

@ScriptMeta(name = "Ultimate Beggar", desc = "Begs for gp", developer = "DrScatman")
public class Beggar extends TaskScript implements RenderListener, ChatMessageListener, LoginResponseListener {

    public int startC = -1;
    public StopWatch runtime;

    public List<Coins> gpArr;
    public Coins gp;
    public Location location;
    public Lines lines;
    public MuleArea muleArea;

    public int preTradeGP = 0;
    public boolean walk = true;
    public boolean beg = true;
    public boolean tradePending = false;
    public boolean trading = false;
    public String traderName = "";
    public boolean changeAmount = false;
    public boolean iterAmount;
    public String muleName;
    public int muleAmnt = Integer.MAX_VALUE;
    public int minWait;
    public int maxWait;
    public int muleKeep;
    public int amountChance;
    public boolean isMuling = false;
    private String[] linesArr;
    public boolean defaultLines = false;
    public String inputLines;
    public int muleWorld;
    public boolean buildGEPath = true;
    public int worldPop;
    public boolean worldHop;
    public int hopTime;
    public boolean hopTimeExpired = false;
    public int sameTraderCount = 0;
    public int walkChance;
    public boolean worldHopf2p;
    public boolean equipped = false;
    public int currWorld = -1;
    public Player trader = null;
    public int hopTryCount = 0;
    public int amntIndex = 0;
    public boolean processSentTrade = false;
    public boolean tradeSent = false;
    public boolean sendTrade = true;
    public int sendTryCount = 0;
    public boolean sentTradeInit = false;
    public int gainedC = 0;
    public int amntMuled = 0;
    public boolean banked = false;
    public boolean bought = false;
    public boolean disableChain = true;
    public int randBuyGP = randInt(1500, 3500);
    public boolean atMinPop = false;
    // ADD TO GUI
    public boolean setSendTrades = false;
    private int genTries = 0;
    public final int[] items = new int[]{1117, 1115, 1139, 1155, 1153, 1137, 1067};
    public int item = items[randInt(0, items.length - 1)];
    public StopWatch lastTradeTime;
    public ArrayList<Integer> two301;
    public ArrayList<Integer> two308;
    public ArrayList<Integer> two393;
    public boolean refreshPrices = false;

    public long startTime = 0;
    public static final String CURR_WORLD_PATH = Script.getDataDirectory() + "\\CurrBegWorld.txt";

    public boolean tradeGambler = false;
    public boolean roll = false;
    public int gambleAmnt = 0;
    public String gamblerName = "";
    public boolean giveGambler = false;

    private static final String PYTHON_3_EXE = "C:\\Users\\bllit\\AppData\\Local\\Programs\\Python\\Python37\\python.exe";
    private static final String ACC_GEN_PY = "C:\\Users\\bllit\\IdeaProjects\\OSRS-Account-Generator\\create_rs_account.py";
    private static final String PASSWORD_ARG = "-p plmmlp";

    public ArrayList<Integer> OTHER_BEG_WORLDS;

    private final boolean GAMBLER = false;

    public Main tanner;
    public boolean isTanning = false;
    public final Area TUTORIAL_ISLAND_AREA = Area.rectangular(3049, 3139, 3161, 3057);
    public boolean restartBeggar = false;
    public boolean checkedTutIsland = false;

    @Override
    public void onStart() {
        Log.fine("Script Started");
        if (!restartBeggar) {
            LoginScreen ctx = new LoginScreen(this);
            ctx.setDelayOnLoginLimit(true);
            ctx.setStopScriptOn(LoginResponseEvent.Response.ACCOUNT_DISABLED, true);
            ctx.setStopScriptOn(LoginResponseEvent.Response.ACCOUNT_LOCKED, true);
            ctx.setStopScriptOn(LoginResponseEvent.Response.RUNESCAPE_UPDATE, true);
            ctx.setStopScriptOn(RUNESCAPE_UPDATE_2, true);

            runtime = StopWatch.start();
            startC = Inventory.getCount(true, 995);
            location = Location.GE_AREA;

            two301 = new ArrayList<>();
            two301.add(301);
            two301.add(301);
            two308 = new ArrayList<>();
            two308.add(308);
            two308.add(308);
            two393 = new ArrayList<>();
            two393.add(393);
            two393.add(393);
        }
        submit(new Gui(this));
        restartBeggar = false;

        if (worldHop || worldHopf2p) {
            startTime = System.currentTimeMillis();
        }

        if (GAMBLER) {
            /*submit(new GTradePlayer(),
                    new GWaitTrade(),
                    //new Mule(),
                    new GSendTrade(),
                    new GRoll(),
                    new WorldHop(this),
                    new Banking(this),
                    new GTraverse(),
                    new Gambler()
            );*/
        } else {
            submit( new CheckTutIsland(this),
                    //new CheckInstances(this),
                    new TradePlayer(this),
                    new WaitTrade(this),
                    new StartTanning(this),
                    new Mule(this),
                    new WorldHop(this),
                    new ChangeAmount(this),
                    new ToggleRun(this),
                    new Banking(this),
                    new Traverse(this),
                    new BuyGE(this),
                    new Beg(this),
                    new SendTrade(this)
            );
        }
    }

    @Override
    public void onPause() {
        if (!disableChain) {
            Log.fine("Chain Disabled");
            disableChain = true;
            lastTradeTime = null;
        } else {
            Log.fine("Chain Enabled");
            disableChain = false;
            lastTradeTime = null;
        }
    }

    @Override
    public void notify(LoginResponseEvent loginResponseEvent) {
        if (    loginResponseEvent.getResponse().equals(LoginResponseEvent.Response.ACCOUNT_DISABLED) ||
                loginResponseEvent.getResponse().equals(LoginResponseEvent.Response.ACCOUNT_STOLEN) ||
                loginResponseEvent.getResponse().equals(LoginResponseEvent.Response.ACCOUNT_LOCKED) ||
                loginResponseEvent.getResponse().equals(LoginResponseEvent.Response.RUNESCAPE_UPDATE) ||
                loginResponseEvent.getResponse().equals(RUNESCAPE_UPDATE_2) ||
                loginResponseEvent.getResponse().equals(INVALID_CREDENTIALS)
            ) {
            disableChain = false;
            setStopping(true);
        }
    }

    @Override
    public void onStop() {
        Log.severe("Script Stopped");
        removeAll();

        if (isMuling) {
            Mule.logoutMule();
            currWorld = muleWorld;
        }

        if (currWorld != -1) {
            Log.info("World Removed");
            removeCurrBegWorld();
        }

        if (!disableChain && !GAMBLER) {
            Log.fine("Chaining");
            try {
                Thread.sleep(randInt(0, 600000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int[] IDs = writeJson(readAccount());

            generateAccount(7);

            String path1 = "C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Simscape";
            String path2 = "C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Beggar";
            int sleep = randInt(900000, 1200000);
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

    private void executeGenerator() {
        String randEmailArg = "-e " + getRandString() + "@gmail.com";

        try {
            Runtime.getRuntime().exec(
                    "cmd /c start cmd.exe /K \"" + PYTHON_3_EXE + " " + ACC_GEN_PY + " " + randEmailArg + " " + PASSWORD_ARG + " && exit" + "\"");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String getRandString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        int strLen = randInt(12, 18);

        while (salt.length() < strLen) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }

        return salt.toString();
    }

    public void generateAccount(int setNumBacklogged) {
        while (genTries < setNumBacklogged / 2 && getNumAccsBacklogged() < setNumBacklogged) {
            executeGenerator();
            genTries++;

            try {
                Thread.sleep(20000);
                if (getNumAccsBacklogged() >= setNumBacklogged)
                    break;
                Thread.sleep(20000);
                if (getNumAccsBacklogged() >= setNumBacklogged)
                    break;
                Thread.sleep(20000);
                if (getNumAccsBacklogged() >= setNumBacklogged)
                    break;
                Thread.sleep(20000);
                if (getNumAccsBacklogged() >= setNumBacklogged)
                    break;
                Thread.sleep(20000);
                if (getNumAccsBacklogged() >= setNumBacklogged)
                    break;
                Thread.sleep(20000);
                if (getNumAccsBacklogged() >= setNumBacklogged)
                    break;
                Thread.sleep(20000);
                if (getNumAccsBacklogged() >= setNumBacklogged)
                    break;
                Thread.sleep(20000);
                if (getNumAccsBacklogged() >= setNumBacklogged)
                    break;
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                Time.sleep(180000);
                e.printStackTrace();
            }
        }
    }

    public int[] writeJson(String account) {
        File file1 = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Beggar1.json");
        File file2 = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Simscape1.json");
        String data1 = "";
        String data2 = "";
        final int[] worlds = new int[]{301, 308, 393};

        try {
            try (BufferedReader br = new BufferedReader(new FileReader(file1))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                data1 = sb.toString();
            }
        } catch (IOException e) {
            Log.info("File not found");
        }
        //data = data.trim();
        String[] arr1 = data1.split(System.lineSeparator());
        for (int i = 0; i < arr1.length; i++) {
            if (arr1[i].contains("\"RsUsername\":")) {
                arr1[i] = "\t\t\"RsUsername\": " + "\"" + account + "\"" + ",";
            }
            if (arr1[i].contains("\"World\":")) {
                arr1[i] = "\t\t\"World\": " + worlds[randInt(0, 2)] + ",";
            }
        }

        try {
            try (BufferedReader br = new BufferedReader(new FileReader(file2))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                data2 = sb.toString();
            }
        } catch (IOException e) {
            Log.info("File not found");
        }
        //data = data.trim();
        String[] arr2 = data2.split(System.lineSeparator());
        for (int i = 0; i < arr2.length; i++) {
            if (arr2[i].contains("\"RsUsername\":")) {
                arr2[i] = "\t\t\"RsUsername\": " + "\"" + account + "\"" + ",";
            }
            if (arr1[i].contains("\"World\":")) {
                arr1[i] = "\t\t\"World\": " + worlds[randInt(0, 2)] + ",";
            }
        }

        int[] IDs = new int[2];
        PrintWriter pw = null;
        try {
            int beggarID = 1;
            while (file1.exists()) {
                beggarID++;
                file1 = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Beggar" + beggarID + ".json");
            }
            file1.createNewFile();
            IDs[0] = beggarID;

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
            int simscapeID = 1;
            while (file2.exists()) {
                simscapeID++;
                file2 = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Simscape" + simscapeID + ".json");
            }
            file2.createNewFile();
            IDs[1] = simscapeID;

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

    private static final String ACCOUNTS_FILE_PATH = "C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\f2pAccounts.txt";
    private List<String> accountsList;

    public String readAccount() {
        String accounts = "";
        File file = new File(ACCOUNTS_FILE_PATH);

        if (!file.exists()) {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                readAccount();
            }
        }

        try {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                accounts = sb.toString();
            }
        } catch (IOException e) {
            Log.info("File not found");
            readAccount();
        }

        //file.delete();

        accounts = accounts.trim();
        accountsList = Arrays.asList(accounts.split(System.lineSeparator()));
        String account = accountsList.get(0);
        account = account.trim();

        writeAccounts(file);

        return account;
    }

    private void writeAccounts(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        PrintWriter pw = null;
        try {
            pw = new PrintWriter(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (int i = 1; i < accountsList.size(); i++) {
            if (pw != null) {
                pw.println(accountsList.get(i));
            }
        }
        if (pw != null) {
            pw.close();
        }
    }

    private int getNumAccsBacklogged() {
        int numAccsBacklogged = 0;
        File file = new File("C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\f2pAccounts.txt");

        try {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line = br.readLine();

                while (line != null) {
                    line = br.readLine();
                    numAccsBacklogged++;
                }
            }
        } catch (IOException e) {
            Log.info("File not found");
        }

        return numAccsBacklogged;
    }

    @Override
    public void notify(ChatMessageEvent msg) {
        // If not in a trade and a player trades you...
        if (!Trade.isOpen() && msg.getType().equals(ChatMessageType.TRADE) && !isMuling) {
            if (msg.getSource().equals(traderName)) {
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
            tradeSent = true;
        }

        if (msg.getMessage().contains("player is busy at the moment") && !isMuling && !Trade.isOpen() && !trading) {
            //walk = true;
            //beg = true;
        }
    }

    @Override
    public void notify(RenderEvent e) {

        Graphics g = e.getSource();
        gainedC = Inventory.getCount(true, 995) + amntMuled;
        gainedC -= startC;
        g.drawString("Runtime: " + runtime.toElapsedString(), 20, 40);
        g.drawString(lastTradeTime == null ? "Last trade completed: " + "00:00:00" : "Last trade completed: " + lastTradeTime.toElapsedString(), 20, 60);
        g.drawString("Gp gained: " + format(gainedC), 20, 80);
        g.drawString("Gp /h: " + format((long) runtime.getHourlyRate(gainedC)), 20, 100);
        g.drawString("Times tanned: " + timesTanned, 20, 120);

        if (isTanning) {
            tanner.render(e);
        }
    }

    private int timesTanned = 0;

    public void resetRender() {
        runtime.reset();
        runtime = StopWatch.start();
        startC = gainedC;
        startTime = System.currentTimeMillis();
        lastTradeTime = null;
        timesTanned++;
    }

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();

    {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "B");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    private String format(long value) {
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
        return rand.nextInt(max - min + 1) + min;
    }

    public void reloadLines() {
        if (defaultLines) {
            defaultLines();
        } else {
            convertInputLines(inputLines);
        }
    }

    public void defaultLines() {
        linesArr = new String[]{"Can someone pls double my " + gp.getSgp() + " coins?",
                "I only have " + gp.getSgp() + " can anyone double it pls?",
                "I have " + gp.getSgp() + " Can someone double it so I buy pick",
                "Can Any1 Double My " + gp.getSgp() + " gp pls??",
                "Can someone Doble my coins pls :)",
                "Any1 willing to double " + gp.getSgp() + "?",
                "Can someone help a noob out and double my " + gp.getSgp() + "? :)",
                "Please someone double me so I can buy a pick and diggy diggy hole!",
                "Im newb can some1 help me and double " + gp.getSgp() + "! :)"};

        lines = new Lines(linesArr);
    }

    public void convertInputLines(String inputLines) {
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

    public void checkWorldHopTime() {
        long currTime = System.currentTimeMillis();
        int elapsedSeconds = (int) ((currTime - startTime) / 1000);
        if (elapsedSeconds > (hopTime * 60) && !isMuling) {
            currWorld = Worlds.getCurrent();
            hopTimeExpired = true;
            startTime = System.currentTimeMillis();
        }
    }

    public void removeCurrBegWorld(){
        BufferedReader reader;
        try {
            File inputFile = new File(CURR_WORLD_PATH);
            File tempFile = new File(Script.getDataDirectory() + "\\TEMPCurrBegWorld.txt");

            if (!inputFile.exists())
                return;

            reader = new BufferedReader(new FileReader(inputFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

            String lineToRemove = Integer.toString(currWorld);
            String currentLine;

            while((currentLine = reader.readLine()) != null) {
                // trim newline when comparing with lineToRemove
                String trimmedLine = currentLine.trim();
                if(trimmedLine.equals(lineToRemove)) continue;
                writer.write(currentLine + System.getProperty("line.separator"));
            }
            writer.close();
            reader.close();

            if (inputFile.exists() && !inputFile.delete()) {
                Log.severe("Could not delete file | Retrying...");
                Thread.sleep(5000);
                removeCurrBegWorld();
            }

            if (tempFile.exists() && !tempFile.renameTo(inputFile)) {
                Log.severe("Could not rename file | Retrying...");
                Thread.sleep(5000);
                removeCurrBegWorld();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int leatherPrice = 0;
    private int cowhidePrice = 0;

    public int getTannerPPH(boolean refreshPrices) {
        setTannerPrices(refreshPrices);
        setHighestProfitLeather();

        return 1053 * (leatherPrice - cowhidePrice);
    }

    private void setTannerPrices(boolean refresh) {
        {
            int COWHIDE = 1739;
            try {
                leatherPrice = ExPriceChecker.getOSBuddySellPrice(LEATHER, refresh);
                cowhidePrice = ExPriceChecker.getOSBuddyBuyPrice(COWHIDE, refresh);
                Log.fine("Using OSBuddy prices");
            } catch (IOException e) {
                Log.severe("Failed getting OSBuddy price");
                e.printStackTrace();
            } finally {
                try {
                    if (leatherPrice < 70) {
                        leatherPrice = ExPriceChecker.getRSBuddySellPrice(LEATHER, refresh);
                    }
                    if (cowhidePrice < 50) {
                        cowhidePrice = ExPriceChecker.getRSBuddyBuyPrice(COWHIDE, refresh);
                        Log.fine("Using RSBuddy prices");
                    }
                } catch (IOException e) {
                    Log.severe("Failed getting RSBuddy price");
                    e.printStackTrace();
                } finally {
                    //Fall-back prices
                    if (leatherPrice < 70) {
                        Log.severe("Using fall-back leather price");
                        leatherPrice = 70;
                    }
                    if (cowhidePrice < 50) {
                        Log.severe("Using fall-back cowhide price");
                        cowhidePrice = 170;
                    }
                }
            }
        }
    }

    private int LEATHER = 1741;

    public void setHighestProfitLeather() {
        int currLeather = LEATHER;
        int currProfit = (LEATHER == 1741) ? leatherPrice - cowhidePrice : leatherPrice - cowhidePrice - 2;

        // switch to other leather
        if (LEATHER == 1741) {
            LEATHER = 1743;
        } else {
            LEATHER = 1741;
        }

        setTannerPrices(false);
        if ((LEATHER == 1741 && (leatherPrice - cowhidePrice) < currProfit) ||
                (LEATHER == 1743 && (leatherPrice - cowhidePrice) - 2 < currProfit)) {
            LEATHER = currLeather;
            setTannerPrices(false);
        }
    }
}
