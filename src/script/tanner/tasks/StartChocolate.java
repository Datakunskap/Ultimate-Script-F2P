package script.tanner.tasks;

import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.ui.Log;
import script.Beggar;
import script.fighter.wrappers.WorldhopWrapper;
import script.tanner.Main;
import script.beg.Mule;
import script.beg.StartOther;

import java.time.Duration;

public class StartChocolate {

    private Main tanner;
    private Beggar beggar;
    private Banking banking;

    StartChocolate(Main tanner, Beggar beggar, Banking banking) {
        this.tanner = tanner;
        this.beggar = beggar;
        this.banking = banking;
    }

    public boolean validate() {
        if (tanner.isMuling)
            return false;

        return tanner.getPPH() < beggar.getChocolatePPH(StartOther.CHOC_PER_HR, false) && tanner.timeRan.exceeds(Duration.ofMinutes(45));
        /*return (tanner.getPPH() < 45000 && tanner.timeRan.exceeds(Duration.ofHours(8))) ||
                (tanner.getPPH() < 40000 && tanner.timeRan.exceeds(Duration.ofHours(6))) ||
                (tanner.getPPH() < 35000 && tanner.timeRan.exceeds(Duration.ofHours(4)));*/
    }

    public void execute() {
        Log.fine("Starting Chocolate");
        banking.openAndDepositAll(0);
        Bank.close();

        int currWorld = Worlds.getCurrent();
        if (Worlds.get(currWorld).getPopulation() > 400) {
            WorldhopWrapper.hopToLowPopWorld(400, currWorld);
        }


        if (tanner.isMuling) {
            Mule.logoutMule();
        }
        WorldhopWrapper.removeWorld(beggar.currWorld, Beggar.CURR_WORLD_PATH);
        beggar.isChoc = true;
        beggar.isTanning = false;
        beggar.timesChocolate++;
        beggar.chocolate = new script.chocolate.Main(beggar);
        beggar.chocolate.amntMuled += tanner.amntMuled;
        beggar.chocolate.start();
    }
}
