package script.tanner;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.input.menu.ActionOpcodes;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.ui.Log;
import script.Beggar;
import script.data.MuleArea;
import script.tanner.data.Location;
import script.tanner.tasks.*;
import script.tanner.ui.Gui;
import script.tasks.StartOther;

import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;

public class Main {
    ////////////////////////////////////////////////////////////////////////////////////

    // Increase buying GP per hide
    public int addHidePrice = 5;
    // Decrease selling GP per leather
    public int subLeatherPrice = 5;
    // Time(min) to increase/decrease price
    public int resetGeTime = 5;
    // Amount to increase/decrease each interval
    public int intervalAmnt = 5;
    // Kill cows restock option
    public boolean killCows = false;
    // Loot hides restock option
    public boolean lootCows = false;
    // Food choice
    public String food = "Trout";
    // Food Amount
    public int foodAmnt = 0;
    // Amount of hide to loot each restock
    public int lootAmount = 0;
    // Amount to mule at
    public int muleAmnt = 1000000;
    // Amount to keep from mule
    public int muleKeep = 500000;
    // Mules in-game name
    public String muleName = "";
    // GE area to mule
    public MuleArea muleArea = MuleArea.COOKS_GUILD;
    // Mules World
    public int muleWorld = 301;

    ////////////////////////////////////////////////////////////////////////////////////
/*
    DO NOT CHANGE
*/
    public final int COWHIDE = 1739;
    public int LEATHER = 1741;
    public int LEATHER_NOTE = LEATHER + 1;
    public boolean restock = true;
    public final Location GE_LOCATION = Location.GE_AREA;
    public final Location COW_LOCATION = Location.COW_AREA;
    public boolean sold = false;
    public boolean checkedBank = false;
    public boolean isMuling = false;
    public boolean geSet = false;
    public int gp = 0;
    public int amntMuled = 0;
    public boolean checkRestock = true;
    public long startTime = 0;
    public int elapsedSeconds = 0;
    public boolean buyPriceChng = false;
    public int decSellPrice = 0;
    public int incBuyPrice = 0;
    public int timesPriceChanged = 0;
    public int cowHideCount = 0;
    public int totalTanned = 0;
    public final Area TANNER_AREA = Area.rectangular(3271, 3191, 3277, 3193);
    public final int TANNER_ID = 3231;
    public int leatherPrice = 0;
    public int cowhidePrice = 0;
    private int[] lastPrices = new int[2];
    public boolean paidToll = true;
    public int idleTanNum = Beggar.randInt((StartOther.TANS_PER_HR / 2  - 200), (StartOther.TANS_PER_HR / 2  + 200));

    //private Main tanner;
    public Beggar beggar;

    public Main(Beggar script) {
        beggar = script;
    }

    /*//method to return instance of class
    public static Main getInstance(Beggar script) {
        if (tanner == null) {
            // if instance is null, initialize
            tanner = new Main(script);
        }
        return tanner;
    }*/

    public void setHighestProfitLeather (boolean refreshPrice) {
        Log.fine("Setting Most Profitable Leather");
        int currLeather = LEATHER;
        int currProfit = (LEATHER == 1741) ? leatherPrice - cowhidePrice : leatherPrice - cowhidePrice - 2;

        // switch to other leather
        if (LEATHER == 1741) {
            LEATHER = 1743;
        } else {
            LEATHER = 1741;
        }

        setPrices(refreshPrice);
        if ((LEATHER == 1741 && (leatherPrice - cowhidePrice) < currProfit) ||
                (LEATHER == 1743 && (leatherPrice - cowhidePrice) - 2 < currProfit)) {
            LEATHER = currLeather;
            setPrices(false);
        }

        LEATHER_NOTE = LEATHER + 1;
        printLeather();
    }

    public void setPrices(boolean refresh) {
        {
            try {
                Log.info("Setting prices");
                leatherPrice = ExPriceChecker.getOSBuddySellPrice(LEATHER, refresh) - subLeatherPrice;
                cowhidePrice = ExPriceChecker.getOSBuddyBuyPrice(COWHIDE, refresh) + addHidePrice;
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

    public StopWatch timeRan = null; // stopwatch is started by GUI

    public void start() {
        beggar.removeAll();
        setPrices(true);

        javax.swing.SwingUtilities.invokeLater(() ->
                new Gui(this)
        );

        beggar.submit(new Mule(this),
                new CheckRestock(this),
                new Eat(this),
                new WalkToCows(this),
                new LootHide(this),
                new AttackCow(this),
                new WalkToGE(this),
                new SellGE(this, beggar),
                new BuyGE(this),
                new WalkToBank(this),
                new BankAK(this),
                new Idle(this),
                new WalkToTanner(this),
                new TanHide(this));

        Combat.toggleAutoRetaliate(true);
    }

    public void setRandMuleKeep(int min, int max){
        muleKeep = randInt(min, max);
        muleAmnt = (muleKeep + 100000);
    }

    private int getHourlyRate(Duration sw) {
        double hours = sw.getSeconds() / 3600.0;
        double tannedPerHour = totalTanned / hours;
        return (int) tannedPerHour;
    }

    public void logStats() {
        int[] stats = getStats();
        String statsString = "Tanned: "
                + stats[0]
                + "  |  Total profit: " + stats[1]
                + "  |  Hourly profit: " + stats[2]
                + "  |  Amount muled: " + stats[3];
        Log.info(statsString);
    }

    private final DecimalFormat formatNumber = new DecimalFormat("#,###");

    public void render(RenderEvent renderEvent) {

        Graphics g = renderEvent.getSource();

        // render time running
        g.setFont(new Font("TimesRoman", Font.BOLD, 20));

        drawStringWithShadow(
                g,
                timeRan == null ? "00:00:00" : timeRan.toElapsedString(),
                242,
                21,
                Color.YELLOW.darker()
        );


        // render tanned and profit
        int[] stats = getStats();
        int totalCowhideTanned = stats[0];
        int totalProfit = stats[1];
        int hourlyProfit = stats[2];
        int amountMuled = stats[3];

        g.setFont(new Font("TimesRoman", Font.BOLD, 15));

        int adjustY = -5;

        drawStringWithShadow(g, "Total Tanned: " + formatNumber.format(totalCowhideTanned), 8, 269 + adjustY, Color.WHITE);
        drawStringWithShadow(g, "Total Profit: " + formatNumber.format(totalProfit), 8, 294 + adjustY, Color.WHITE);
        drawStringWithShadow(g, "Profit/Hr: " + formatNumber.format(hourlyProfit), 8, 319 + adjustY, Color.WHITE);
        drawStringWithShadow(g, "Amount Muled:  " + formatNumber.format(amountMuled), 8, 344 + adjustY, Color.WHITE);
    }

    private void drawStringWithShadow(Graphics g, String str, int x, int y, Color color) {
        g.setColor(Color.BLACK);
        g.drawString(str, x + 2, y + 2); // draw shadow
        g.setColor(color);
        g.drawString(str, x, y); // draw string
    }

    public int[] getStats() {
        final Duration durationRunning = timeRan == null ? Duration.ofSeconds(0) : timeRan.getElapsed();

        int totalLeatherValue = totalTanned * leatherPrice;
        int totalProfit = (totalLeatherValue - totalTanned * cowhidePrice);
        int hourlyProfit = getHourlyRate(durationRunning) * (leatherPrice - cowhidePrice);
        return new int[]{
                totalTanned,
                totalProfit,
                hourlyProfit,
                amntMuled
        };
    }

    public int getPPH() {
        setPrices(true);
        final Duration durationRunning = timeRan == null ? Duration.ofSeconds(0) : timeRan.getElapsed();
        return getHourlyRate(durationRunning) * (leatherPrice - cowhidePrice);
    }

    public int randInt(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        java.util.Random rand = new java.util.Random();
        return rand.nextInt(max - min + 1) + min;
    }

    public void printHide() {
        // Cow
        Log.fine("Cowhide");
    }

    public void printLeather() {
        if (LEATHER == 1741) {
            Log.fine("Leather");
        }
        if (LEATHER == 1743) {
            Log.fine("Hard Leather");
        }
    }

    public void checkTime() {
        long currTime = System.currentTimeMillis();
        elapsedSeconds = (int) ((currTime - startTime) / 1000);
    }

    public void closeGE() {
        while (GrandExchange.isOpen() || GrandExchangeSetup.isOpen()) {
            InterfaceComponent X = Interfaces.getComponent(465, 2, 11);
            X.interact(ActionOpcodes.INTERFACE_ACTION);
            Time.sleepUntil(() -> !GrandExchange.isOpen() && !GrandExchangeSetup.isOpen(), 5000);
        }
    }

    public void teleportHome() {
        if (Tabs.open(Tab.MAGIC)) {
            Time.sleep(1500);
            Log.fine("Teleporting Home");
            Magic.cast(Spell.Modern.HOME_TELEPORT);
            paidToll = false;
            Time.sleep(18000);

            if (!Inventory.contains(995) && Inventory.getCount(true, 995) < 10) {
                new Banking(this).openAndDepositAll(10);
            }
        }
    }
}