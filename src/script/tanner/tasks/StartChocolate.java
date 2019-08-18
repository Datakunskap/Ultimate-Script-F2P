package script.tanner.tasks;

import org.rspeer.runetek.api.component.Bank;
import org.rspeer.ui.Log;
import script.Beggar;
import script.tanner.Main;
import script.tasks.Mule;

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

    public boolean execute() {
        if (tanner.isMuling)
            return false;

        if (    (tanner.getPPH() < 45000 && tanner.timeRan.exceeds(Duration.ofHours(8))) ||
                (tanner.getPPH() < 40000 && tanner.timeRan.exceeds(Duration.ofHours(6))) ||
                (tanner.getPPH() < 35000 && tanner.timeRan.exceeds(Duration.ofHours(4)))) {

            Log.fine("Starting Chocolate");
            banking.openAndDepositAll();
            Bank.close();

            //beggar.resetRender();
            beggar.isTanning = false;
            beggar.isChoc = true;
            beggar.timesTanned ++;

            script.chocolate.Main choc = script.chocolate.Main.getInstance(beggar);
            beggar.choc = choc;
            if (tanner.isMuling) {
                Mule.logoutMule();
            }

            choc.start();
            return true;
        }
        return false;
    }
}
