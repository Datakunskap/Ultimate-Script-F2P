package script.tanner.tasks;

import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.ui.Log;
import script.Script;
import script.fighter.wrappers.WorldhopWrapper;
import script.tanner.Main;
import script.beg.Mule;
import script.beg.StartOther;

import java.time.Duration;

public class StartChocolate {

    private Main tanner;
    private Script script;
    private Banking banking;

    StartChocolate(Main tanner, Script script, Banking banking) {
        this.tanner = tanner;
        this.script = script;
        this.banking = banking;
    }

    public boolean validate() {
        if (tanner.isMuling)
            return false;

        return tanner.getPPH() < script.getChocolatePPH(StartOther.CHOC_PER_HR, false) && tanner.timeRan.exceeds(Duration.ofMinutes(45));
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
            Mule.logoutMule(Script.MULE_IP);
        }
        WorldhopWrapper.removeWorld(script.currWorld, Script.CURR_WORLD_PATH);
        script.isChoc = true;
        script.isTanning = false;
        script.timesChocolate++;
        script.chocolate = new script.chocolate.Main(script);
        script.chocolate.amntMuled += tanner.amntMuled;
        script.chocolate.start();
    }
}
