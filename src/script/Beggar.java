package script;

import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.event.listeners.ChatMessageListener;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.ChatMessageType;
import script.data.Location;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.script.ScriptMeta;
import org.rspeer.script.task.TaskScript;
import org.rspeer.ui.Log;
import script.tasks.Banking;
import script.tasks.ToggleRun;
import script.tasks.TradePlayer;
import script.tasks.Traverse;

import java.awt.*;

@ScriptMeta(name = "Begging bot", desc = "Begs for gold", developer = "DrScatman")
public class Beggar extends TaskScript implements RenderListener {

    private int startC;
    private StopWatch runtime;

    public static Location location;

    @Override
    public void onStart() {
        Log.fine("Script started.");
        runtime = StopWatch.start();
        startC = Inventory.getCount(true, "Coins");
        location = Location.GE_AREA;

        submit(new Banking(),
                new TradePlayer(),
                new Traverse(),
                new ToggleRun());
    }

    @Override
    public void onStop() {
        Log.severe("Script stopped.");
    }

    @Override
    public void notify(RenderEvent e) {
        Graphics g = e.getSource();

        int gainedC  = Inventory.getCount(true, "Coins") - startC;
        InterfaceComponent switcher = Interfaces.getComponent(182, 3);

        g.drawString("Runtime: " + runtime.toElapsedString(), 20, 20);
        g.drawString("Gp gained: " + gainedC, 20, 40);
        g.drawString("Gp /h: " + runtime.getHourlyRate(gainedC), 20, 60);
    }
}
