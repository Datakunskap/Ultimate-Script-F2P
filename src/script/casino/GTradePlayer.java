/*
package script.casino;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.EnterInput;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class GTradePlayer extends Task {


    private int sameTraderCount = 0;
    private final String[] completeLines = new String[]{Beggar.traderName + " is gambling " + Beggar.gambleAmnt};

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

                if (Beggar.giveGambler && Inventory.contains(995)) {
                    int attempts = 0;
                    while (true) {
                        attempts++;
                        Log.info("Entering trade offer");
                        Trade.offer("Coins", x -> x.contains("X"));
                        Time.sleep(1000);
                        if (EnterInput.isOpen()) {
                            if(Beggar.sameTraderCount < 2) {
                                EnterInput.initiate(Beggar.gambleAmnt * 2);
                            } else {
                                EnterInput.initiate(Beggar.gambleAmnt);
                            }
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

            // Checks if they an offer
            if (Trade.getTheirItems().length > 0) {
                for (Item item : Trade.getTheirItems(x -> x.getName().equals("Coins"))) {
                    Time.sleep(300);
                        if (Time.sleepUntilForDuration(() -> Trade.contains(false, 995), 2000, 10000)) {
                            if (Trade.accept()) {
                                Log.info("Accepted trade");
                                Beggar.gambleAmnt = item.getStackSize();
                                Time.sleepUntil(() -> Trade.isOpen(true), 300, 3000);
                            }
                        }
                }
            }
        } else if (Trade.isOpen(true)) {
            // handle second trade window...
            Time.sleep(500, 1500);
            if (Trade.accept()) {
                Time.sleepUntil(() -> Trade.hasOtherAccepted() || !Trade.isOpen(true), 180000);
                Log.fine("Trade completed");
                Time.sleep(3000, 3500);
                Keyboard.sendText(completeLines[Beggar.randInt(0, completeLines.length - 1)]);
                Keyboard.pressEnter();
                Beggar.walk = false;
                Beggar.beg = false;
//                Beggar.sendTrade = false;
                Beggar.trading = false;
                Beggar.roll = true;
                Beggar.startTime = System.currentTimeMillis();
                Beggar.gamblerName = Beggar.traderName;
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
*/
