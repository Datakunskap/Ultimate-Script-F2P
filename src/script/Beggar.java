package script;

import script.data.Coins;
import script.data.Location;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.event.listeners.RenderListener;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.script.ScriptMeta;
import org.rspeer.script.task.TaskScript;
import org.rspeer.ui.Log;
import script.tasks.*;

import java.awt.*;

@ScriptMeta(name = "Begging bot", desc = "Begs for gold", developer = "DrScatman")
public class Beggar extends TaskScript implements RenderListener {

    private int startC;
    private StopWatch runtime;

    public static Coins gp;
    public static Location location;

    public static boolean walk = false;
    public static boolean beg = true;

    @Override
    public void onStart() {
        Log.fine("Script started.");
        runtime = StopWatch.start();
        startC = Inventory.getCount(true, "Coins");
        location = Location.GE_AREA;
        gp = Coins.GP_1000;

        submit(new Banking(),
                new TradePlayer(),
                new Traverse(),
                new ToggleRun(),
                new Beg());
    }

    @Override
    public void onStop() {
        Log.severe("Script stopped.");
    }

    @Override
    public void notify(RenderEvent e) {
        Graphics g = e.getSource();

        int gainedC  = Inventory.getCount(true, "Coins") - startC;

        g.drawString("Runtime: " + runtime.toElapsedString(), 20, 20);
        g.drawString("Gp gained: " + gainedC, 20, 40);
        g.drawString("Gp /h: " + runtime.getHourlyRate(gainedC), 20, 60);
    }
}
