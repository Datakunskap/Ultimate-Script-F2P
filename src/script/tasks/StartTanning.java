package script.tasks;

import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.script.task.Task;
import script.Beggar;
import script.tanner.Main;

import java.time.Duration;

public class StartTanning extends Task {

    private Beggar beggar;
    private Main tanner;

    private final int startGP = 65000;

    public StartTanning(Beggar beggar){
        this.beggar = beggar;
    }

    @Override
    public boolean validate() {
        return (beggar.runtime.exceeds(Duration.ofHours(8)) && Inventory.getCount(true, 995) >= startGP) ||
                (Inventory.getCount(true, 995) >= startGP && beggar.runtime.getHourlyRate(Beggar.gainedC) < 45000) || // try to check last trade time instead
                checkBeggarsOnTop3Worlds();
    }

    private boolean checkBeggarsOnTop3Worlds() {
        return Beggar.OTHER_BEG_WORLDS != null && Beggar.OTHER_BEG_WORLDS.contains(301) && Beggar.OTHER_BEG_WORLDS.contains(308) && Beggar.OTHER_BEG_WORLDS.contains(393) &&
                Beggar.currWorld != 301 && Beggar.currWorld != 308 && Beggar.currWorld != 393 &&
                Worlds.get(Worlds.getCurrent()).getPopulation() < 600 &&
                Inventory.getCount(true, 995) >= startGP;

    }

    @Override
    public int execute() {
        tanner = Main.getInstance(beggar);

        beggar.tanner = tanner;
        beggar.isTanning = true;
        WorldHop.removeCurrBegWorld();
        tanner.start();
        return 1000;
    }
}
