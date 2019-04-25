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
import script.Beggar;

public class TradePlayer extends Task {

    private InterfaceComponent acceptBtn;

    @Override
    public boolean validate() {
        acceptBtn = Interfaces.getComponent(335, 11);
        return acceptBtn != null && Beggar.trading;
    }

    @Override
    public int execute() {
        Log.info("Trading");
        //Beggar.trading = false;
        //acceptBtn.click();
        return 1000;
    }
}
