package script.tasks;

import org.rspeer.runetek.adapter.Interactable;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;

public class TradePlayer extends Task implements ChatMessageListener {

    private Player toTrade;
    private InterfaceComponent acceptBtn;
    private InterfaceComponent tradeBtn;

    @Override
    public void notify(ChatMessageEvent event) {
        if (event.getType().equals(ChatMessageType.TRADE)) {
            String name = event.getMessage().replaceAll(" wishes to trade with you.", "");
            toTrade = Players.getNearest(name);
            if (toTrade != null) {
                //toTrade.interact("Trade with");
                Time.sleep(3000, 6000);
                Log.info("Clicking trade");
                tradeBtn = Dialog.getChatOption(x -> x.contains("wishes to trade with you."));
                tradeBtn.click();
            }
        }
    }

    @Override
    public boolean validate() {
        acceptBtn = Interfaces.getComponent(335, 11);
        return toTrade != null && acceptBtn.isVisible();
    }

    @Override
    public int execute() {
        Log.info("Trading");
        acceptBtn.click();
        return 1000;
    }
}
