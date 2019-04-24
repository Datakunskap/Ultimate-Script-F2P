import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.Trade;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.script.ScriptMeta;
import org.rspeer.script.task.TaskScript;
import org.rspeer.ui.Log;
import tasks.Banking;
import tasks.TradePlayer;

import java.awt.*;

@ScriptMeta(name = "Begging bot", desc = "Begs for gold", developer = "DrScatman")
public class Beggar extends TaskScript implements RenderListener {

    private int startC;
    private StopWatch runtime;

    @Override
    public void onStart() {
        Log.fine("Script started.");
        runtime = StopWatch.start();
        startC = Inventory.getCount(true, "Coins");

        submit(new Banking(), new TradePlayer());
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
        g.drawString(switcher.getName(), 20, 80);
    }
}
