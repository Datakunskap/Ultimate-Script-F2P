package script.chocolate;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.input.menu.ActionOpcodes;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.script.ScriptMeta;
import org.rspeer.ui.Log;
import script.Beggar;
import script.chocolate.tasks.*;
import script.data.MuleArea;
import script.tanner.ExPriceChecker;
import script.tasks.StartOther;

import java.awt.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.Duration;

@ScriptMeta(name = "Ultimate Chocolate", desc = "Crushes Chocolate Bars", developer = "DrScatman")
public class Main {

    // Time(min) to increase/decrease price
    public int resetGeTime = 5;
    // Amount to increase/decrease each interval
    public int intervalAmnt = 5;
    // Increase buying GP per item
    private int addBuyPrice = 0;
    // Decrease selling GP per item
    private int subSellPrice = 0;

    // Amount of GP gained to mule at
    public int muleGainedGP = 100000;
    // Max amount to keep from mule
    public int maxKeep = 100000;
    // Min amount to keep from mule
    public int minKeep = 85000;

    public String muleName;
    public MuleArea muleArea = MuleArea.COOKS_GUILD;
    public int muleWorld = 301;
    public static final int BAR_GE_LIMIT = 13000;
    public static final int BAR = 1973;
    public static final int BAR_NOTE = 1974;
    public static final int DUST = 1975;
    public static final int DUST_NOTE = 1976;
    public static final int KNIFE = 946;
    public boolean atGELimit = false;
    public int knifePrice = 100;
    public StopWatch timeRan = null;
    public boolean isMuling = false;
    public boolean restock = true;
    public int buyPrice = 0;
    public int sellPrice = 0;
    public int incBuyPrice = 0;
    public int decSellPrice = 0;
    public long startTime = 0;
    public int gp = 0;
    public int barCount = 0;
    public boolean sold = false;
    public boolean checkedBank = false;
    public boolean buyPriceChng = false;
    public int timesPriceChanged = 0;
    public int elapsedSeconds;
    public int totalMade = 0;
    public int amntMuled = 0;
    public boolean geSet = false;
    public boolean hasKnife = false;
    public boolean justBoughtKnife = false;
    public int muleAmnt = 100000;
    public int muleKeep = 0;
    public int[] lastPrices = new int[2];
    public boolean usingBuyFallback = false;
    public boolean usingSellFallback = false;
    public int idleChocNum = Beggar.randInt((StartOther.CHOC_PER_HR / 2 - 500), (StartOther.CHOC_PER_HR / 2 + 500));

    private static script.chocolate.Main chocolate;
    private static Beggar beggar;

    private Main(Beggar script) {
        beggar = script;
    }

    //method to return instance of class
    public static Main getInstance(Beggar script) {
        if (chocolate == null) {
            // if instance is null, initialize
            chocolate = new Main(script);
        }
        return chocolate;
    }

    public void start() {
        beggar.removeAll();

        Log.fine("Chocolate Started");
        muleName = Beggar.MULE_NAME;
        muleArea = Beggar.MULE_AREA;
        muleWorld = Beggar.MULE_WORLD;

        setPrices(true);
        setRandMuleKeep(minKeep, maxKeep);
        timeRan = StopWatch.start();

        beggar.submit(new Mule(this),
                new Traverse(this),
                new SellGE(this),
                new BuyGE(this, beggar),
                new Grind(this));
    }

    public void setPrices(boolean refresh) {
        try {
            Log.info("Setting prices");
            sellPrice = ExPriceChecker.getOSBuddySellPrice(DUST, refresh);
            buyPrice = ExPriceChecker.getOSBuddyBuyPrice(BAR, refresh);
        } catch (IOException e) {
            Log.severe("Failed getting OSBuddy price");
            e.printStackTrace();
        } finally {
            try {
                if (sellPrice < SELL_PL) {
                    sellPrice = ExPriceChecker.getRSBuddySellPrice(DUST, refresh);
                    Log.fine("Using RSBuddy sell price");
                }
                if (buyPrice < BUY_PL) {
                    buyPrice = ExPriceChecker.getRSBuddyBuyPrice(BAR, refresh);
                    Log.fine("Using RSBuddy buy price");
                }
            } catch (IOException e) {
                Log.severe("Failed getting RSBuddy price");
                e.printStackTrace();
            } finally {
                fallbackPriceHelper();
            }
        }
        sellPrice -= subSellPrice;
        buyPrice += addBuyPrice;
    }

    private static final int SELL_PL = 50;
    private static final int SET_SELL_PL = 50;
    private static final int BUY_PL = 30;
    private static final int SET_BUY_PL = 70;

    private void fallbackPriceHelper() {
        //Fall-back prices
        if (sellPrice < SELL_PL) {
            if (lastPrices != null && lastPrices[0] >= SELL_PL) {
                Log.fine("Using previous sell price");
                sellPrice = lastPrices[0];
                usingSellFallback = false;
            } else {
                Log.severe("Using fall-back sell price");
                sellPrice = SET_SELL_PL;
                usingSellFallback = true;
            }
        } else {
            if (sellPrice != SET_SELL_PL) {
                Log.info("Sell price set to: " + sellPrice);
                lastPrices[0] = sellPrice;
                usingSellFallback = false;
            }
        }

        if (buyPrice < BUY_PL) {
            if (lastPrices != null && lastPrices[1] >= BUY_PL) {
                Log.fine("Using previous buy price");
                buyPrice = lastPrices[1];
                usingBuyFallback = false;
            } else {
                Log.severe("Using fall-back buy price");
                buyPrice = SET_BUY_PL;
                usingBuyFallback = true;
            }
        } else {
            if (buyPrice != SET_BUY_PL) {
                Log.info("Buy price set to: " + buyPrice);
                lastPrices[1] = buyPrice;
                usingBuyFallback = false;
            }
        }
    }

    public void closeGE() {
        while (GrandExchange.isOpen() || GrandExchangeSetup.isOpen()) {
            InterfaceComponent X = Interfaces.getComponent(465, 2, 11);
            X.interact(ActionOpcodes.INTERFACE_ACTION);
            Time.sleepUntil(() -> !GrandExchange.isOpen() && !GrandExchangeSetup.isOpen(), 5000);
        }
    }

    public void checkTime() {
        long currTime = System.currentTimeMillis();
        elapsedSeconds = (int) ((currTime - startTime) / 1000);
    }

    private int getHourlyRate(Duration sw) {
        double hours = sw.getSeconds() / 3600.0;
        double tannedPerHour = totalMade / hours;
        return (int) tannedPerHour;
    }

    public int getPPH() {
        setPrices(true);
        final Duration durationRunning = timeRan == null ? Duration.ofSeconds(0) : timeRan.getElapsed();
        return getHourlyRate(durationRunning) * (sellPrice - buyPrice);
    }

    public int randInt(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        java.util.Random rand = new java.util.Random();
        return rand.nextInt(max - min + 1) + min;
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
                Color.RED.darker()
        );


        // render tanned and profit
        int[] stats = getStats();
        int totalCowhideTanned = stats[0];
        int totalProfit = stats[1];
        int hourlyProfit = stats[2];
        int amountMuled = stats[3];

        g.setFont(new Font("TimesRoman", Font.BOLD, 15));

        int adjustY = -5;

        drawStringWithShadow(g, "Total: " + formatNumber.format(totalCowhideTanned), 8, 269 + adjustY, Color.WHITE);
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

        int totalLeatherValue = totalMade * sellPrice;
        int totalProfit = (totalLeatherValue - totalMade * buyPrice);
        int hourlyProfit = getHourlyRate(durationRunning) * (sellPrice - buyPrice);
        return new int[]{
                totalMade,
                totalProfit,
                hourlyProfit,
                amntMuled
        };
    }

    public void setRandMuleKeep(int min, int max) {
        muleKeep = randInt(min, max);
        muleAmnt = (muleKeep + muleGainedGP);
    }
}
