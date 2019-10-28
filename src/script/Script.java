package script;

import api.bot_management.BotManagement;
import api.bot_management.RsPeerDownloader;
import api.bot_management.data.LaunchedClient;
import api.component.ExPriceCheck;
import org.rspeer.RSPeer;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Login;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.*;
import org.rspeer.runetek.event.types.*;
import org.rspeer.runetek.providers.RSWorld;
import org.rspeer.runetek.providers.subclass.GameCanvas;
import org.rspeer.script.GameAccount;
import org.rspeer.script.ScriptMeta;
import org.rspeer.script.events.LoginScreen;
import org.rspeer.script.task.TaskScript;
import org.rspeer.ui.Log;
import script.beg.*;
import script.data.*;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.framework.BackgroundTaskExecutor;
import script.fighter.services.PriceCheckService;
import script.fighter.wrappers.BankWrapper;
import script.fighter.wrappers.WorldhopWrapper;
import script.tanner.Main;
import script.ui.Gui;

import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.rspeer.runetek.event.types.LoginResponseEvent.Response.INVALID_CREDENTIALS;
import static org.rspeer.runetek.event.types.LoginResponseEvent.Response.RUNESCAPE_UPDATE_2;

@ScriptMeta(name = "Ultimate Script", desc = "My Script", developer = "DrScatman")
public class Script extends TaskScript implements RenderListener, ChatMessageListener, LoginResponseListener, DeathListener, TargetListener {

    public int startC = -1;
    public StopWatch runtime;
    public List<Coins> gpArr;
    public Coins gp;
    public Location location;
    public Lines lines;
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
    public final int[] items = new int[]{1117, 1115, 1139, 1155, 1153, 1137, 1067, 1061, 1203, 1349, 1323, 1267};
    public int item = items[randInt(0, items.length - 1)];
    public StopWatch lastTradeTime;
    public boolean refreshPrices = false;
    public long startTime = 0;
    private final boolean GAMBLER = false;
    public Main tanner;
    public boolean isTanning = false;
    public final Area TUTORIAL_ISLAND_AREA = Area.rectangular(3049, 3139, 3161, 3057);
    public boolean startupChecks = false;
    public List<LaunchedClient> runningClients;
    public int[] popWorldsArr = new int[]{301, 308, 393};
    public int minPop = 250;
    public boolean muted = false;
    public MuleArea muleArea;
    private int[] lastPrices = new int[4];
    public boolean muleChocBeg = false;
    public static final int SAVE_BEG_GP = 10000;
    public script.chocolate.Main chocolate;
    public boolean isChoc = false;
    public int sumTopPops = 0;
    public int numBegs = 0;
    public int idleBegNum = randInt(50, 70);
    public Fighter fighter;
    public boolean ogressBeg;
    public static final boolean BUY_GEAR = true;
    private static final boolean SELENIUM_VERIFY_GEN = false;
    public static final boolean RESET_RUNTIME = true;


    public static final String[] PROXY_IP = new String[]{}; // { "209.163.117.212", "167.160.71.140", "172.96.82.150", "172.96.95.99", "DEFAULT" };//"108.187.189.123";
    public static final String PROXY_USER = "";
    public static final String PROXY_PASS = "";
    public static final String PROXY_PORT = "1080";
    private static final String PYTHON_3_EXE = System.getProperty("user.home") + "\\AppData\\Local\\Programs\\Python\\Python37\\python.exe";
    private static final String ACC_GEN_PY = System.getProperty("user.home") + "\\IdeaProjects\\UltimateScript\\create_rs_account.py";
    public static final String CURR_WORLD_PATH = org.rspeer.script.Script.getDataDirectory() + "\\CurrBegWorld.txt";
    public static final String OGRESS_WORLD_PATH = org.rspeer.script.Script.getDataDirectory() + "\\OgressWorlds.txt";
    private static final String ERROR_FILE_PATH = System.getProperty("user.home") + "\\OneDrive\\Desktop\\RSPeerErrors.txt";
    private static final String ACCOUNTS_FILE_PATH = System.getProperty("user.home") + "\\OneDrive\\Desktop\\RSPeer\\f2pAccounts.txt";
    public static final String BEG_LINES_PATH = System.getProperty("user.home") + "\\IdeaProjects\\UltimateScript\\BegLines.txt";
    private static final String SELENIUM_GEN_PATH = System.getProperty("user.home") + "\\IdeaProjects\\UltimateScript\\Runescape-Account-Generator-2.0.jar";
    public static final String RESTART_SCRIPT_PATH = System.getProperty("user.home") + "\\IdeaProjects\\UltimateScript\\RestartScript.jar";

    public static final String MULE_NAME = "Madman38snur";
    public static final MuleArea MULE_AREA = MuleArea.GE_NEW;
    public static final int MULE_WORLD = 393;
    public static final boolean MULE_ITEMS = false;
    public static final int MUTED_MULE_AMNT = 30000;
    public static final int ALLOWED_INSTANCES = 8;
    public static final String API_KEY = "JV5ML4DE4M9W8Z5KBE00322RDVNDGGMTMU1EH9226YCVGFUBE6J6OY1Q2NJ0RA8YAPKO70";
    public static final int NUM_BACKLOG_ACCOUNTS = 15;
    private static final boolean TUTORIAL_COMPLETED_SLEEP = false;
    public static final boolean TUTORIAL_IDLE = false;
    public static final boolean IDLE_LOGOUT = false;
    public static final int TUTORIAL_COMPLETED_WALK_DIST = randInt(10, 40);
    public static final boolean EXPLV_TUTORIAL = false;
    public static final boolean FIGHTER_TRAIN_DEFENCE = false;

    public static final boolean OGRESS = true;
    public static final int OGRESS_START_GP = 25_000;
    public static final boolean SPLASH_USE_EQUIPMENT = true;
    public static final int OGRESS_WORLD_HOP_MINS = 5;
    public static final int OGRESS_MAX_MINUTES_WORTH_OF_RUNES = 150;
    public static final int OGRESS_MULE_AMOUNT = 115_115;

    @Override
    public void onStart() {
        Log.fine("Script Started");
        //updateRSPeer();

        LoginScreen ctx = new LoginScreen(this);
        ctx.setDelayOnLoginLimit(true);
        ctx.setStopScriptOn(LoginResponseEvent.Response.ACCOUNT_DISABLED, true);

        runtime = StopWatch.start();
        startC = Inventory.getCount(true, 995);
        location = Location.GE_AREA;

        submit(new Gui(this));
        muleName = MULE_NAME;
        muleWorld = MULE_WORLD;
        muleArea = MULE_AREA;

        startTime = (worldHop || worldHopf2p) ? System.currentTimeMillis() : 0;
        setRandMuleKeep(2500, 10000);

        submitTasks();

        if (!GameCanvas.isInputEnabled()) {
            GameCanvas.setInputEnabled(true);
        }
    }

    private void submitTasks() {
        submit(new StartupChecks(this),
                new TradePlayer(this),
                new WaitTrade(this),
                new SellGE(this),
                new StartOther(this),
                new Mule(this),
                new WorldHop(this),
                new ChangeAmount(this),
                new ToggleRun(this),
                new Banking(this),
                new Traverse(this),
                new BuyEquip(this),
                new Idle(this),
                new Beg(this),
                new SendTrade(this)
        );
    }

    public void startOgress() {
        Log.fine("Starting Ogress");

        int world = Worlds.getCurrent();
        if (world > 0) {
            WorldhopWrapper.writeWorldToFile(world, OGRESS_WORLD_PATH);
            WorldhopWrapper.currentWorld = world;
        }
        resetRender(RESET_RUNTIME);
        removeAll();
        fighter = new Fighter(this);
        fighter.onStart(true, 5);
    }

    public void startFighter() {
        //logoutAndSwitchAcc();
        Log.fine("Starting Fighter");
        if (TUTORIAL_COMPLETED_SLEEP) {
            int ms = randInt(300_000, 600_000);
            Log.info("Sleeping for " + TimeUnit.MILLISECONDS.toMinutes(ms) + " min(s)");
            Time.sleep(ms);
        }
        resetRender(RESET_RUNTIME);
        removeAll();
        fighter = new Fighter(this, randInt(600_000, 1_200_000)); // 10 - 20
        fighter.onStart(false, 5);
    }

    public void startBeggar(boolean ogressBeg) {
        removeAll();

        banked = false;
        changeAmount = false;
        walk = true;
        beg = true;
        buildGEPath = true;
        trading = false;
        equipped = false;
        bought = false;
        randBuyGP = Script.randInt(1500, 5000);
        isMuling = false;
        startTime = (worldHop || worldHopf2p) ? System.currentTimeMillis() : 0;
        startupChecks = false;
        this.ogressBeg = ogressBeg;

        resetRender(RESET_RUNTIME);

        Log.fine("Starting Script");
        submitTasks();
    }

    @Override
    public void notify(LoginResponseEvent loginResponseEvent) {
        if (loginResponseEvent.getResponse().equals(LoginResponseEvent.Response.ACCOUNT_DISABLED) ||
                loginResponseEvent.getResponse().equals(INVALID_CREDENTIALS)
        ) {

            disableChain = false;
            setStopping(true);

        } else if (loginResponseEvent.getResponse().equals(LoginResponseEvent.Response.RUNESCAPE_UPDATE) ||
                loginResponseEvent.getResponse().equals(RUNESCAPE_UPDATE_2)) {

            String[] info = new String[]{RSPeer.getGameAccount().getUsername(), RSPeer.getGameAccount().getPassword()};
            int world = getNextWorld();

            try {
                new ClientQuickLauncher("Ultimate Script", false, world).launchClient(info);
                killClient();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private int stopRetries = 5;

    @Override
    public void onStop() {
        Log.severe("Script Stopped");
        removeAll();
        disposeBackgroundTasks(10);

        if (isFighterRunning) {
            if (!disableChain) {
                writeToErrorFile("Ogress  |  Runtime: "
                        + Fighter.getRuntime().toElapsedString() + "  |  Value Gained: " + BankWrapper.getTotalValueGained()
                        + "  |  Value / H: " + format((long) Fighter.getRuntime().getHourlyRate(BankWrapper.getTotalValueGained())));
            }
            fighter.onStop(false, 10);
        }

        if (isMuling || (isTanning && tanner.isMuling) || (isChoc && chocolate.isMuling) || Config.isMuleing) {
            Mule.logoutMule();
        }

        if (currWorld != -1 && !isTanning && !isChoc) {
            Log.info("World Removed");
            WorldhopWrapper.removeWorld(currWorld, Script.CURR_WORLD_PATH);
        }

        if (!disableChain && !GAMBLER) {
            Log.fine("Chaining");
            try {
                Thread.sleep(randInt(5000, 300000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (Game.isLoggedIn()) {
                Game.logout();
            }

            accountGeneratorDriver(NUM_BACKLOG_ACCOUNTS);
            int world = getNextWorld();

            try {
                new ClientQuickLauncher("Ultimate Script", false, world).launchClient(readAccount(true));
                killClient();

            } catch (Exception e) {
                Log.severe(e);
                if (stopRetries > 0) {
                    stopRetries--;
                    onStop();
                }
                writeToErrorFile("onStop():  " + e.toString());
            }
        }
    }

    private void disposeBackgroundTasks(int retries) {
        try {
            BackgroundTaskExecutor.dispose();
            PriceCheckService.dispose();

        } catch (Exception e) {
            if (retries > 0) {
                disposeBackgroundTasks(retries - 1);
            }
            writeToErrorFile("FAILED TO DISPOSE BACKGROUND TASKS");
        }
    }

    public void killClient() throws IOException {
        RSPeer.shutdown();
        for (LaunchedClient client : BotManagement.getRunningClients()) {
            if (client.getRunescapeEmail().equals(RSPeer.getGameAccount().getUsername())) {
                client.kill();
            }
        }
        System.exit(0);
    }

    public int getNextWorld() {
        int nextBotWorld = getNextBotWorld(randInt(0, 500));

        if (nextBotWorld > 0) {
            return nextBotWorld;
        }
        if (WorldhopWrapper.currentWorld > 0) {
            return currWorld;
        }
        if (currWorld > 0) {
            return currWorld;
        }

        return popWorldsArr[randInt(0, 2)];
    }

    private int getNextBotWorld(int pop) {
        RSWorld newWorld = Worlds.get(x -> x.getPopulation() <= pop &&
                !x.isMembers() && !x.isBounty() && !x.isSkillTotal());

        if (newWorld != null && newWorld.getId() > 0) {
            return newWorld.getId();
        }
        if (pop <= 1000) {
            getNextBotWorld(pop + 50);
        }
        return 0;
    }

    private void seleniumGenerator(int numToGen) {
        int firstExt = randInt(0, 9994);
        int lastExt = firstExt + numToGen;
        String emailBase = getRandString(false, 12, 18);
        String password = getRandString(true, 6, 20);

        try {
            if (PROXY_IP != null && PROXY_IP.length < 1) {
                //selenium.performTask(emailBase, password, firstExt, lastExt,
                //       ACCOUNTS_FILE_PATH, PROXY_IP, PROXY_PORT, PROXY_USER, PROXY_PASS);
                Runtime.getRuntime().exec(
                        "cmd /c start cmd.exe /K \"" + "java -jar" + " " + SELENIUM_GEN_PATH + " " + emailBase
                                + " " + password + " " + firstExt + " " + lastExt + " " + ACCOUNTS_FILE_PATH + " " +
                                PROXY_IP + " " + PROXY_PORT + PROXY_USER + " " + PROXY_PASS + " && exit" + "\"");
            } else {
                //selenium.performTask(emailBase, password, firstExt, lastExt, ACCOUNTS_FILE_PATH);
                Runtime.getRuntime().exec(
                        "cmd /c start cmd.exe /K \"" + "java -jar" + " " + SELENIUM_GEN_PATH + " " + emailBase
                                + " " + password + " " + firstExt + " " + lastExt + " " + ACCOUNTS_FILE_PATH +
                                " && exit" + "\"");
            }
        } catch (Exception e) {
            writeToErrorFile(e.getMessage());
            e.printStackTrace();
        }
    }

    private void pythonGenerator(int retries) {
        final String EMAIL_ARG = "-e " + getVerificationEmailAlias();
        final String PASSWORD_ARG = "-p " + "qazzaq";//getRandString(true, 6, 20);
        final String PROXY_IP_ARG = "-i " + (PROXY_IP.length > 0 ? PROXY_IP[randInt(0, PROXY_IP.length - 1)] : "DEFAULT");
        final String PROXY_USER_ARG = "-u " + PROXY_USER;
        final String PROXY_PASS_ARG = "-x " + PROXY_PASS;
        final String PROXY_PORT_ARG = "-o " + PROXY_PORT;

        try {
            if (PROXY_IP == null || PROXY_IP.length < 1 || PROXY_IP_ARG.equals("-i DEFAULT")) {
                final String EMAIL_ARG_2 = "-e2 " + getVerificationEmailAlias();
                final String PASSWORD_ARG_2 = "-p2 " + "qazzaq";//getRandString(true, 6, 20);
                Runtime.getRuntime().exec(
                        "cmd /c start cmd.exe /K \"" + PYTHON_3_EXE + " " + ACC_GEN_PY + " " + EMAIL_ARG_2 + " " + PASSWORD_ARG_2 + " && exit" + "\"");
            } else {
                Runtime.getRuntime().exec(
                        "cmd /c start cmd.exe /K \"" + PYTHON_3_EXE + " " + ACC_GEN_PY + " " + EMAIL_ARG + " " + PASSWORD_ARG +
                                " " + PROXY_IP_ARG + " " + PROXY_USER_ARG + " " + PROXY_PASS_ARG + " " + PROXY_PORT_ARG + " && exit" + "\"");
            }
        } catch (Exception e) {
            writeToErrorFile("executeGenerator()  |  " + e.getMessage());
            Log.severe("executeGenerator()  |  " + e.getMessage());
            e.printStackTrace();
            if (retries > 0) {
                pythonGenerator(retries - 1);
            }
        }
    }

    private String getVerificationEmailAlias() {
        final String DOMAIN = "@gmail.com";
        String base = "milleja115";

        String extension = "+" + getRandString(false, 3, 35);
        String newBase = base + extension;

        int numDots = randInt(1, newBase.length());
        for (int i = 0; i < numDots; i++) {
            int randomSplitIndex = randInt(1, newBase.length() - 2);

            String dotAttempt = newBase.substring(0, randomSplitIndex) + "." +
                    newBase.substring(randomSplitIndex);

            if (!dotAttempt.contains("..")) {
                newBase = dotAttempt;
            }
        }
        return newBase + DOMAIN;
    }

    private String getRandString(boolean caseSensitive, int min, int max) {
        String SALTCHARS = !caseSensitive ? "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890" :
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder salt = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        int strLen = randInt(min, max);

        while (salt.length() < strLen) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }

        return salt.toString();
    }

    public void accountGeneratorDriver(int setNumBacklogged) {
        int numToGen = setNumBacklogged - getNumAccsBacklogged();
        if (numToGen <= 0)
            return;

        numToGen = numToGen <= 5 ? numToGen : 5;

        if (!SELENIUM_VERIFY_GEN) {
            Log.fine("Generating " + numToGen + " Accounts");
            for (int g = 0; g < numToGen; g++) {
                pythonGenerator(10);
                Time.sleep(300);
            }
        } else {
            Log.fine("Generating & Verifying " + numToGen + " Accounts");
            seleniumGenerator(numToGen);
        }
    }

    public void logoutAndSwitchAcc() {
        String[] info = new String[]{RSPeer.getGameAccount().getUsername(), RSPeer.getGameAccount().getPassword()};
        writeAccount(info);

        if (Game.logout()) {
            String[] newInfo = readAccount(true);
            RSPeer.setGameAccount(new GameAccount(info[0], info[1]));
            if (Time.sleepUntil(() -> !RSPeer.getGameAccount().getUsername().equals(info[0]), 20000))
                Log.fine("Account Switched");

            while (!Game.isLoggedIn() && !Login.getResponseLines()[0].toLowerCase().contains("disabled")) {
                Login.enterCredentials(RSPeer.getGameAccount().getUsername(), RSPeer.getGameAccount().getPassword());
                Time.sleep(200);
                Keyboard.pressEnter();
                Time.sleepUntil(() -> Game.isLoggedIn() || Login.getResponseLines()[0].toLowerCase().contains("disabled"), 2000, 10000);
            }
        }
    }

    private List<String> accountsList;

    public String[] readAccount(boolean readFirst) {
        String accounts = "";
        File file = new File(ACCOUNTS_FILE_PATH);

        if (!file.exists()) {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                readAccount(readFirst);
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
            readAccount(readFirst);
        }

        //file.delete();

        accounts = accounts.trim();
        accountsList = Arrays.asList(accounts.split(System.lineSeparator()));
        String account = readFirst ? accountsList.get(0) : accountsList.get(accountsList.size() - 1);
        String[] info = account.trim().split(":");
        if (info.length == 1) {
            writeToErrorFile("Using Default Password");
            info = new String[]{info[0], "plmmlp"};
        }

        writeAccounts(file);

        return info;
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

    public void writeAccount(String[] info) {
        try (FileWriter fw = new FileWriter(ACCOUNTS_FILE_PATH, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            if (info.length == 6) {
                out.println(info[0] + ":" + info[1] + ":" + info[2] + ":" + info[3] + ":" + info[4] + ":" + info[5]);
            } else if (info.length == 4) {
                out.println(info[0] + ":" + info[1] + ":" + info[2] + ":" + info[3]);
            } else {
                out.println(info[0] + ":" + info[1]);
            }
        } catch (IOException e) {
            writeToErrorFile("Failed writing account");
            e.printStackTrace();
        }
    }

    private int getNumAccsBacklogged() {
        int numAccsBacklogged = 0;
        File file = new File(ACCOUNTS_FILE_PATH);

        try {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line = br.readLine();

                while (line != null && line.contains("@")) {
                    numAccsBacklogged++;
                    line = br.readLine();
                }
            }
        } catch (IOException e) {
            Log.info("File not found");
        }

        return numAccsBacklogged;
    }

    @Override
    public void notify(ChatMessageEvent msg) {
        if (msg.getType() == ChatMessageType.PUBLIC || msg.getType() == ChatMessageType.PRIVATE_RECEIVED)
            return;

        if (!isFighterRunning && !isChoc && !isTanning) {
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

        } else if (isChoc || isTanning) {
            if (msg.getMessage().toLowerCase().contains("that offer costs")) {
                if (isTanning) {
                    tanner.sold = false;
                    tanner.checkedBank = false;
                    Movement.setWalkFlag(Players.getLocal());
                    //tanner.closeGE();
                }
                if (isChoc) {
                    chocolate.sold = false;
                    chocolate.checkedBank = false;
                    Movement.setWalkFlag(Players.getLocal());
                    //chocolate.closeGE();
                }
            }
        } else {
            fighter.notify(msg);
        }
    }

    public static int timesIdled = 0;
    public static long itemsSoldProfitAmount = 0;

    @Override
    public void notify(RenderEvent e) {
        if (isStopping())
            return;
        if (!isFighterRunning) {
            Graphics g = e.getSource();
            if (!isTanning && !isChoc) {
                gainedC = Inventory.getCount(true, 995) + amntMuled;
                gainedC -= startC;
            }
            g.drawString("Runtime: " + runtime.toElapsedString(), 20, 40);
            g.drawString(lastTradeTime == null ? "Last Trade Completed: " + "00:00:00" : "Last Trade Completed: " + lastTradeTime.toElapsedString(), 20, 60);
            g.drawString("Begging Profit: " + format(gainedC), 20, 80);
            g.drawString("GP / H: " + format((long) runtime.getHourlyRate(gainedC)), 20, 100);
            g.drawString("Sold Items Profit: " + format(itemsSoldProfitAmount), 20, 120);
            g.drawString("Times Tanned: " + timesTanned, 20, 140);
            g.drawString("Times Chocolate: " + timesChocolate, 20, 160);
            g.drawString("Times Idled: " + timesIdled, 20, 180);
            g.drawString("Sum Top Worlds: " + sumTopPops, 20, 200);

            if (isTanning) {
                tanner.render(e);
            }
            if (isChoc) {
                chocolate.render(e);
            }
        } else {
            fighter.notify(e);
        }
    }

    public int timesTanned = 0;
    public int timesChocolate = 0;

    public void resetRender(boolean resetRuntime) {
        if (resetRuntime) {
            runtime.reset();
            runtime = StopWatch.start();
        }
        startC = gainedC;
        startTime = System.currentTimeMillis();
        lastTradeTime = null;
        if (isTanning)
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

    public String[] randSpecialLines(String[] arr) {
        for (int i = 0; i < arr.length; i++) {
            if (randInt(1, 5) == 1) { // 20%
                switch (randInt(0, 4)) {
                    case 0:
                        arr[i] = "Flash2:" + arr[i];
                        break;
                    case 1:
                        arr[i] = "Flash3:" + arr[i];
                        break;
                    case 2:
                        arr[i] = "Glow1:" + arr[i];
                        break;
                    case 3:
                        arr[i] = "Glow2:" + arr[i];
                        break;
                    case 4:
                        arr[i] = "Glow3:" + arr[i];
                        break;
                }
            }
        }
        return arr;
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

        linesArr = randSpecialLines(linesArr);
        lines = new Lines(linesArr);
    }

    public void checkWorldHopTime() {
        long currTime = System.currentTimeMillis();
        int elapsedSeconds = (int) ((currTime - startTime) / 1000);
        if (elapsedSeconds > (hopTime * 60) && !isMuling) {
            currWorld = Worlds.getCurrent();

            loadPopWorldsArr(5);
            sumTopPops = Worlds.get(popWorldsArr[0]).getPopulation() + Worlds.get(popWorldsArr[1]).getPopulation() + Worlds.get(popWorldsArr[2]).getPopulation();

            hopTimeExpired = true;
            startTime = System.currentTimeMillis();
        }
    }

    public void writeToErrorFile(String errMsg) {
        try (FileWriter fw = new FileWriter(new File(ERROR_FILE_PATH), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.println(errMsg);
        } catch (IOException e) {
            Log.severe("Error file not found");
        }
    }

    private int leatherPrice = 0;
    private int cowhidePrice = 0;

    public int getTannerPPH(int tannedPH, boolean refreshPrices) {
        setTannerPrices(refreshPrices);
        setHighestProfitLeather();

        return tannedPH * (leatherPrice - cowhidePrice);
    }

    private void setTannerPrices(boolean refresh) {
        {
            int COWHIDE = 1739;
            try {
                leatherPrice = ExPriceCheck.getOSBuddySellPrice(LEATHER, refresh);
                cowhidePrice = ExPriceCheck.getOSBuddyBuyPrice(COWHIDE, refresh);
            } catch (Exception e) {
                Log.severe("Exception getting OSBuddy price");
                e.printStackTrace();
            } finally {
                try {
                    for (int i = 0; leatherPrice < 70 && i < 3; i++) {
                        if (i == 0) {
                            try {
                                leatherPrice = ExPriceCheck.getAccurateRSPrice(LEATHER);
                            } catch (Exception ignored) {
                            }
                        }
                        if (i == 1)
                            leatherPrice = ExPriceCheck.getRSBuddySellPrice(LEATHER, refresh);
                        if (i == 2)
                            leatherPrice = ExPriceCheck.getRSPrice(LEATHER);
                    }
                } catch (Exception e) {
                    writeToErrorFile("Failed getting sell price");
                }

                try {
                    for (int i = 0; cowhidePrice < 50 && i < 3; i++) {
                        if (i == 0) {
                            try {
                                cowhidePrice = ExPriceCheck.getAccurateRSPrice(COWHIDE);
                            } catch (Exception ignored) {
                            }
                        }
                        if (i == 1)
                            cowhidePrice = ExPriceCheck.getRSBuddyBuyPrice(COWHIDE, refresh);
                        if (i == 2)
                            cowhidePrice = ExPriceCheck.getRSPrice(COWHIDE);
                    }

                } catch (Exception e) {
                    writeToErrorFile("Failed getting buy price");

                } finally {
                    //Fall-back prices
                    if (leatherPrice < 70) {
                        if (lastPrices != null && lastPrices[0] >= 70) {
                            Log.fine("Using previous leather price");
                            leatherPrice = lastPrices[0];
                        } else {
                            Log.severe("Using fall-back leather price");
                            leatherPrice = 70;
                        }
                    } else {
                        if (leatherPrice != 70)
                            lastPrices[0] = leatherPrice;
                    }
                    if (cowhidePrice < 50) {
                        if (lastPrices != null && lastPrices[1] >= 50) {
                            Log.fine("Using previous cowhide price");
                            cowhidePrice = lastPrices[1];
                        } else {
                            Log.severe("Using fall-back cowhide price");
                            cowhidePrice = 170;
                        }
                    } else {
                        if (cowhidePrice != 170)
                            lastPrices[1] = cowhidePrice;
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

    private int buyPrice = 0;
    private int sellPrice = 0;
    private static final int SELL_PL = 50;
    private static final int SET_SELL_PL = 70;
    private static final int BUY_PL = 30;
    private static final int SET_BUY_PL = 60;

    private void setChocolatePrices(boolean refresh) {
        try {
            sellPrice = ExPriceCheck.getOSBuddySellPrice(script.chocolate.Main.DUST, refresh);
            buyPrice = ExPriceCheck.getOSBuddyBuyPrice(script.chocolate.Main.BAR, refresh);
        } catch (Exception e) {
            Log.severe("Exception getting accurate OSBuddy price");
            e.printStackTrace();
        } finally {
            try {
                for (int i = 0; sellPrice < SELL_PL && i < 3; i++) {
                    if (i == 0) {
                        try {
                            sellPrice = ExPriceCheck.getAccurateRSPrice(script.chocolate.Main.DUST);
                        } catch (Exception ignored) {
                        }
                    }
                    if (i == 1)
                        sellPrice = ExPriceCheck.getRSBuddySellPrice(script.chocolate.Main.DUST, refresh);
                    if (i == 2)
                        sellPrice = ExPriceCheck.getRSPrice(script.chocolate.Main.DUST);
                }
            } catch (Exception e) {
                writeToErrorFile("Failed getting sell price");
            }

            try {
                for (int i = 0; buyPrice < BUY_PL && i < 3; i++) {
                    if (i == 0)
                        try {
                            buyPrice = ExPriceCheck.getAccurateRSPrice(script.chocolate.Main.BAR);
                        } catch (Exception ignored) {
                        }
                    if (i == 1)
                        buyPrice = ExPriceCheck.getRSBuddyBuyPrice(script.chocolate.Main.BAR, refresh);
                    if (i == 2)
                        buyPrice = ExPriceCheck.getRSPrice(script.chocolate.Main.BAR);
                }
            } catch (Exception e) {
                writeToErrorFile("Failed getting buy price");

            } finally {
                fallbackPriceHelper();
            }
        }
        /*sellPrice -= subSellPrice;
        buyPrice += addBuyPrice;*/
    }

    private void fallbackPriceHelper() {
        //Fall-back prices
        if (sellPrice < SELL_PL) {
            if (lastPrices != null && lastPrices[2] >= SELL_PL) {
                Log.fine("Using previous sell price");
                sellPrice = lastPrices[2];
            } else {
                Log.severe("Using fall-back sell price");
                sellPrice = SET_SELL_PL;
            }
        } else {
            if (sellPrice != SET_SELL_PL) {
                ///Log.info("Sell price set to: " + sellPrice);
                lastPrices[2] = sellPrice;
            }
        }

        if (buyPrice < BUY_PL) {
            if (lastPrices != null && lastPrices[3] >= BUY_PL) {
                Log.fine("Using previous buy price");
                buyPrice = lastPrices[3];
            } else {
                Log.severe("Using fall-back buy price");
                buyPrice = SET_BUY_PL;
            }
        } else {
            if (buyPrice != SET_BUY_PL) {
                //Log.info("Buy price set to: " + buyPrice);
                lastPrices[3] = buyPrice;
            }
        }
    }

    public int getChocolatePPH(int grindPH, boolean refreshPrices) {
        setChocolatePrices(refreshPrices);
        return grindPH * (sellPrice - buyPrice);
    }

    public void loadPopWorldsArr(int loadTries) {
        int[] popWorldsArr = new int[3];
        RSWorld[] worlds = null;
        int maxW1 = 0;
        int maxW2 = 0;
        int maxW3 = 0;
        int maxP = 0;

        while (loadTries > 0 && (worlds == null || worlds.length < 3)) {
            worlds = Worlds.getLoaded(x -> x != null && x.getPopulation() >= minPop &&
                    !x.isMembers() && !x.isBounty() && !x.isSkillTotal());
            loadTries--;
            Time.sleep(500);
        }

        if (worlds == null || worlds.length < 3) {
            Log.severe("Using Default Worlds");
            writeToErrorFile("Using Default Worlds");
            return;
        }

        for (RSWorld w : worlds) {
            if (w.getPopulation() > maxP) {
                maxW1 = w.getId();
                maxP = w.getPopulation();
            }
        }
        popWorldsArr[0] = maxW1;
        maxP = 0;

        for (RSWorld w : worlds) {
            if (w.getPopulation() > maxP && w.getId() != maxW1) {
                maxW2 = w.getId();
                maxP = w.getPopulation();
            }
        }
        popWorldsArr[1] = maxW2;
        maxP = 0;

        for (RSWorld w : worlds) {
            if (w.getPopulation() > maxP && w.getId() != maxW1 && w.getId() != maxW2) {
                maxW3 = w.getId();
                maxP = w.getPopulation();
            }
        }
        popWorldsArr[2] = maxW3;

        this.popWorldsArr = popWorldsArr;
    }

    public void setRandMuleKeep(int min, int max) {
        muleKeep = randInt(min, max);
        muleAmnt = (muleKeep + 100000);
    }

    public boolean isFighterRunning = false;

    @Override
    public void notify(DeathEvent deathEvent) {
        if (isFighterRunning) {
            fighter.notify(deathEvent);
        }
    }

    @Override
    public void notify(TargetEvent targetEvent) {
        if (isFighterRunning) {
            fighter.notify(targetEvent);
        }
    }

    public String getProxyIp(String email) {
        try {
            List<LaunchedClient> clients = BotManagement.getRunningClients();
            for (LaunchedClient client : clients) {
                if (client.getRunescapeEmail().equals(email)) {
                    if (client.getProxyIp() != null && !client.getProxyIp().isEmpty()) {
                        return client.getProxyIp();
                    }

                }
            }
        } catch (IOException e) {
            Log.severe(e);
        }
        return null;
    }

    private void updateRSPeer() {
        try {
            if (RsPeerDownloader.shouldDownload()) {
                writeToErrorFile("DOWNLOAD NEW JAR");
                //RsPeerDownloader.downloadNewJar();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
