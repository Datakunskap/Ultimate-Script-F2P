package script.tanner.tasks;

import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.WorldHopper;
import org.rspeer.ui.Log;
import script.Beggar;
import script.tanner.Main;
import script.tasks.Mule;
import script.tasks.StartOther;

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

        return tanner.getPPH() < beggar.getChocolatePPH(StartOther.CHOC_PER_HR, false) && tanner.timeRan.exceeds(Duration.ofMinutes(60));
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
            WorldHopper.randomHop(x -> x != null && x.getPopulation() <= 400 &&
                    !x.isMembers() && !x.isBounty() && !x.isSkillTotal());
            Time.sleepUntil(() -> Worlds.getCurrent() != currWorld, 12000);
        }
        if (tanner.isMuling) {
            Mule.logoutMule();
        }
        beggar.removeCurrBegWorld(beggar.currWorld);
        beggar.isChoc = true;
        beggar.timesChocolate ++;
        beggar.chocolate = script.chocolate.Main.getInstance(beggar);
        beggar.chocolate.amntMuled += tanner.amntMuled;
        beggar.chocolate.start();
    }
}
