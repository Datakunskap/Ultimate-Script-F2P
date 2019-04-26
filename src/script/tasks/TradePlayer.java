package script.tasks;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class TradePlayer extends Task implements ChatMessageListener{

    private boolean tradePending = false;
    private boolean tradingP1 = false;
    private boolean tradingP2 = false;
    private String traderName;


    @Override
    public void notify(ChatMessageEvent msg) {
        // If not in a trade and a player trades you...
        if (!Trade.isOpen() && msg.getType().equals(ChatMessageType.TRADE)) {
            traderName = msg.getSource();
            tradePending = true;
        }
    }

    @Override
    public boolean validate() {
        return tradePending || tradingP1 || tradingP2;
    }

    @Override
    public int execute() {
        //Log.info("Trading");
        Beggar.beg = false;
        Beggar.walk = false;

        if (Trade.isOpen(false) && tradingP1) {
            // handle first trade window...
            Log.info("Entering trade offer");
            Trade.offer("Coins", x -> x.contains("X"));
            Time.sleep(3000, 3500);
            Keyboard.sendText(Beggar.gp.getSgp());
            Keyboard.pressEnter();
            tradingP1 = false;
        }

        else if(!tradingP1 && Trade.isOpen(false)){
            for (Item item : Trade.getTheirItems(x -> x.getName().equals("Coins"))){
                if(item.getStackSize() > Beggar.gp.getGp()){
                    if(Trade.accept()) {
                        Log.info("Accepted trade");
                        tradingP2 = true;
                        return 1000;
                    }
                }
            }
        }

        else if (Trade.isOpen(true) && tradingP2){
            if(Trade.accept()) {
                Log.fine("Trade completed");
                tradingP2 = false;
                //Beggar.walk = true;
                //Beggar.beg = true;
                return 1000;
            }
        }

        // If someone is requesting to trade you & you're not in trade, accept trade...
        else if (!Trade.isOpen(true) && !Trade.isOpen(false) && tradePending) {
            Player trader = Players.getNearest(traderName);
            if (tradePending && trader != null) {
                Players.getNearest(traderName).interact("Trade with");
                tradePending = false;
                tradingP1 = true;
                return 500;
            }
        }
        else {
            return 500;
        }
        return 1200;
    }
}
