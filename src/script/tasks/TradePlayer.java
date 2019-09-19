package script.tasks;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.EnterInput;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class TradePlayer extends Task {

    private Beggar main;

    public TradePlayer(Beggar beggar){
        main = beggar;
    }

    private int sameTraderCount = 0;
    private final String[] completeLines = new String[]{"Holy shit!!!!", "Wowzzaaa!!! thnk u :))",
            "Holy Shit!! Ty!!! :))))", "Woah ur rich! TY mann!!!", "Wow ur rich!! thanks man :))",
            "Dang mannn!!!! thanks :)",
            "Holy Fucking Shit!!", "Omfggg!!", "OMG!!!! thnk youu!!!!",
            "Yes!!! Im 11k from rune legs!!!! Ty :)", "Yess!!!! Holly thnk u!!",
            "Yes man!!! Thanks man!!!", "Yesss!!!!!", "Holly mannn y is everyone giving me stuff lol thanks",
            "Shyt ur rich! Thnk u!!!!", "U guys make me rich in 3 minutes thnks :)))", "ure 2 nice!!!",
            "omfggq1!!!!!", "Yessssssssssssss :)", "Yess!! man you pimping me out lmao",
            "Wat is happening lmao!!! Y is evry1 giving so much coin lol",
            "yesssssssssssssssss!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!",
            "Thnk u man!!!!!!! Im half way to 100k now!!!!!!!!",
            "Thnk u man!!!!!!! Im half way to 10k now!!!!!!!!",
            "U guys are seriously making my year hahahha :)", "Omgg im almost half a millionire!!! Thnks!!!",
            "Omggg Im almost half 100k!!!!!! Thank you man!! you the man!!!!",
            "Holy man Tyyy!!! 10ks alot to give a random person!! Thnks :)))))",
            "Holy man Tyyy!!! 20k is alot to give a random person!! Thnks :)))))",
            "Hollllyy 5k is a lot to give a random person Tyy!!!!! :)"};

    @Override
    public boolean validate() {
        return main.trading;
    }

    @Override
    public int execute() {

        //Log.info("Trading");
        if (Trade.isOpen(false)) {
            // handle first trade window...
            if (main.tradePending || main.processSentTrade) {
                main.tradePending = false;
                main.processSentTrade = false;

                if (Inventory.contains(995)) {
                    int attempts = 0;
                    while (true) {
                        attempts++;
                        Log.info("Entering trade offer");
                        Trade.offer("Coins", x -> x.contains("X"));

                        Time.sleep(1000);
                        if (EnterInput.isOpen()) {
                            EnterInput.initiate(main.gp.getGp());
                            Time.sleep(1000);
                        }
                        if (Time.sleepUntil(() -> Trade.contains(true, 995), 500, 3500)) {
                            Log.info("Trade entered");
                            break;
                        }
                        if (attempts > 3) {
                            break;
                        }
                    }
                }
            }

            // Checks if they entered more gp than you
            if (Trade.getTheirItems().length > 0) {
                for (Item item : Trade.getTheirItems(x -> x.getName().equals("Coins"))) {
                    Time.sleep(300);
                    if (item.getStackSize() > main.gp.getGp()) {
                        if (Time.sleepUntilForDuration(() -> Trade.contains(false, 995), 2000, 10000)) {
                            if (Trade.accept()) {
                                Log.info("Accepted trade");
                                Time.sleepUntil(() -> Trade.isOpen(true), 300, 3000);
                            }
                        }
                    }
                    // Checks if they are just trying to donate
                    if (item.getStackSize() <= main.gp.getGp()) {
                        Time.sleep(4500);
                        if (item.getStackSize() <= main.gp.getGp()) {
                            for (Item my : Trade.getMyItems(x -> x.getName().equals("Coins"))) {
                                if (item.getStackSize() <= main.gp.getGp()) {
                                    my.interact(x -> x.contains("All"));
                                }
                            }
                            Time.sleepUntil(() -> !Trade.contains(true, 995), 500, 5000);
                            if (Trade.contains(false, 995)) {
                                if (Trade.accept()) {
                                    Log.info("Accepted trade");
                                    Time.sleepUntil(() -> Trade.isOpen(true), 300, 3000);
                                }
                            }
                        }
                    }
                }
            }
        } else if (Trade.isOpen(true)) {
            // handle second trade window...
            Time.sleep(500, 1500);
            if (Trade.accept()) {
                Time.sleepUntil(() -> Trade.hasOtherAccepted() || !Trade.isOpen(true), 150000);
                Time.sleepUntil(() -> !Trade.isOpen(true), 5000);
                if (Inventory.contains(995) && Inventory.getCount(true, 995) > main.preTradeGP) {
                    Log.fine("Trade completed");
                    Time.sleep(3000, 3500);
                    Keyboard.sendText(completeLines[Beggar.randInt(0, completeLines.length - 1)]);
                    Keyboard.pressEnter();

                    if(main.lastTradeTime != null) {
                        main.lastTradeTime.reset();
                    }
                    main.lastTradeTime = StopWatch.start();

                    int coins = Inventory.getCount(true, 995);

                    if (coins > main.randBuyGP) {
                        main.equipped = false;
                        main.bought = false;
                    }

                    if (coins < main.muleAmnt || StartOther.START_GP >= main.muleAmnt)
                        main.refreshPrices = true;

                    if (coins > Beggar.START_CB_AMNT)
                        main.startChocBeg = true;
                }

                main.walk = false;
                main.beg = false;
                main.sendTrade = false;
                main.trading = false;
                main.changeAmount = true;
                main.startTime = System.currentTimeMillis();
                //Time.sleep(9000, 14000);
            }
        }
        // If someone is requesting to trade you & you're not in trade, accept trade...
        else {
            if (main.tradePending && !Trade.isOpen()) {
                if (main.trader != null) {
                    if (main.traderName.equals(main.trader.getName())) {
                        sameTraderCount++;
                    } else {
                        sameTraderCount = 0;
                    }
                }
                if (sameTraderCount >= 5) {
                    Log.severe("Trade Blocked");
                    main.tradePending = false;
                }

                main.trader = Players.getNearest(main.traderName);

                if (main.trader != null && sameTraderCount < 5) {
                    if(Inventory.contains(995))
                        main.preTradeGP = Inventory.getCount(true, 995);
                    Players.getNearest(main.traderName).interact("Trade with");
                    main.beg = false;
                    main.sendTrade = false;
                    main.walk = false;
                    Time.sleep(2500);
                }
            }
            if (!Trade.isOpen() && !main.tradePending) {
                Time.sleepUntil(Trade::isOpen, 500, 5000);
                if (!Trade.isOpen() && !main.tradePending) {
                    main.walk = true;
                    main.beg = true;
                    main.sendTrade = true;
                    main.trading = false;
                }
            }
        }
        return 500;
    }
}
