package script.tasks;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.EnterInput;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.api.component.chatter.Chat;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class TradePlayer extends Task {

    private boolean modified = false;
    private int sameTraderCount = 0;
    private int meCount = 0;
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
        return Beggar.trading;
    }

    @Override
    public int execute() {

        //Log.info("Trading");
        if (Trade.isOpen(false)) {
            // handle first trade window...
            if (Beggar.tradePending || Beggar.processSentTrade) {
                Beggar.tradePending = false;
                Beggar.processSentTrade = false;

                if (Inventory.contains(995)) {
                    int attempts = 0;
                    while (true) {
                        attempts++;
                        Log.info("Entering trade offer");
                        Trade.offer("Coins", x -> x.contains("X"));
                        Time.sleep(1000);
                        if (EnterInput.isOpen()) {
                            EnterInput.initiate(Beggar.gp.getGp());
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
                    if (item.getStackSize() > Beggar.gp.getGp()) {
                        if (Time.sleepUntilForDuration(() -> Trade.contains(false, 995), 2000, 10000)) {
                            if (Trade.accept()) {
                                Log.info("Accepted trade");
                                Time.sleepUntil(() -> Trade.isOpen(true), 300, 3000);
                            }
                        }
                    }
                    // Checks if they are just trying to donate
                    if (item.getStackSize() <= Beggar.gp.getGp()) {
                        Time.sleep(4500);
                        if (item.getStackSize() <= Beggar.gp.getGp()) {
                            for (Item my : Trade.getMyItems(x -> x.getName().equals("Coins"))) {
                                if (item.getStackSize() <= Beggar.gp.getGp()) {
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
                Time.sleepUntil(() -> Trade.hasOtherAccepted() || !Trade.isOpen(true), 120000);
                Log.fine("Trade completed");
                Time.sleep(3000, 3500);
                Keyboard.sendText(completeLines[Beggar.randInt(0, completeLines.length - 1)]);
                Keyboard.pressEnter();
                Beggar.walk = false;
                Beggar.beg = false;
                Beggar.sendTrade = false;
                Beggar.trading = false;
                Beggar.changeAmount = true;
                Beggar.startTime = System.currentTimeMillis();
                //Time.sleep(9000, 14000);
            }
        }
        // If someone is requesting to trade you & you're not in trade, accept trade...
        else {
            if (Beggar.tradePending && !Trade.isOpen()) {
                if (Beggar.trader != null) {
                    if (Beggar.traderName.equals(Beggar.trader.getName())) {
                        sameTraderCount++;
                    } else {
                        sameTraderCount = 0;
                    }
                }
                if (sameTraderCount >= 5) {
                    Log.severe("Trade Blocked");
                    Beggar.tradePending = false;
                }

                Beggar.trader = Players.getNearest(Beggar.traderName);

                if (Beggar.trader != null && sameTraderCount < 5) {
                    Players.getNearest(Beggar.traderName).interact("Trade with");
                    Beggar.beg = false;
                    Beggar.sendTrade = false;
                    Beggar.walk = false;
                    Time.sleep(2500);
                }
            }
            if (!Trade.isOpen() && !Beggar.tradePending) {
                Time.sleepUntil(() -> Trade.isOpen(), 500, 5000);
                if (!Trade.isOpen() && !Beggar.tradePending) {
                    Beggar.walk = true;
                    Beggar.beg = true;
                    Beggar.sendTrade = true;
                    Beggar.trading = false;
                }
            }
        }
        return 500;
    }
}
