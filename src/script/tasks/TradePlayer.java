package script.tasks;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.EnterInput;
import org.rspeer.runetek.api.component.Trade;
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

    @Override
    public boolean validate() {
        return Beggar.trading;
    }

    @Override
    public int execute() {

        //Log.info("Trading");
        if (Trade.isOpen(false)) {
            // handle first trade window...
            if (Beggar.tradePending) {
                Beggar.tradePending = false;

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
                    if(attempts > 10){
                        break;
                    }
                }
            }

            // Checks if they entered more gp than you
            if (Trade.getTheirItems().length > 0) {
                for (Item item : Trade.getTheirItems(x -> x.getName().equals("Coins"))) {
                    Time.sleep(300);
                    if (item.getStackSize() > Beggar.gp.getGp()) {
                        if (Time.sleepUntilForDuration(() -> Trade.contains(false, 995), 2000, 10000)) {
                            if(Trade.accept()) {
                                Log.info("Accepted trade");
                                Time.sleepUntil(() -> Trade.isOpen(true), 300, 3000);
                            }
                        } else {
                            Trade.decline();
                        }
                    }
                    // Checks if they are just trying to pay the half
                    if(item.getStackSize() == Beggar.gp.getGp()) {
                        Time.sleep(4500);
                        if(item.getStackSize() == Beggar.gp.getGp()) {
                            for (Item my : Trade.getMyItems(x -> x.getName().equals("Coins"))) {
                                my.interact(x -> x.contains("All"));
                            }
                            Time.sleepUntil(() -> !Trade.contains(true, 995), 500, 5000);
                            if (!Trade.contains(false, 995)) {
                                Trade.decline();
                            }
                            else {
                                if(Trade.accept()) {
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
            if(Trade.accept()) {
                Log.fine("Trade completed");
                Keyboard.sendText("Wowza thank you!");
                Keyboard.pressEnter();
                Time.sleep(10000, 15000);
                Beggar.walk = true;
                Beggar.beg = true;
                Beggar.trading = false;
                Beggar.changeAmount = true;
            }
        }
        // If someone is requesting to trade you & you're not in trade, accept trade...
        else {
            if (Beggar.tradePending && !Trade.isOpen()) {
                Player trader = Players.getNearest(Beggar.traderName);
                if (trader != null) {
                    Players.getNearest(Beggar.traderName).interact("Trade with");
                    Beggar.beg = false;
                    Beggar.walk = false;
                    Time.sleep(2500);
                }
            }
            if (!Trade.isOpen(false) && !Beggar.tradePending) {
                Time.sleepUntil(() -> Trade.isOpen(false), 500, 5000);
                if (!Trade.isOpen(false) && !Beggar.tradePending) {
                    Beggar.walk = true;
                    Beggar.beg = true;
                    Beggar.trading = false;
                }
            }
        }
        return 500;
    }
}
