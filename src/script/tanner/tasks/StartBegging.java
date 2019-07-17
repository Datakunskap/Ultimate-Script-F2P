package script.tanner.tasks;

import org.rspeer.ui.Log;
import script.Beggar;
import script.tanner.Main;

import java.time.Duration;

public class StartBegging {

    private Main tanner;
    private Beggar beggar;
    private Banking banking;

    StartBegging(Main tanner, Beggar beggar, Banking banking) {
        this.tanner = tanner;
        this.beggar = beggar;
        this.banking = banking;
    }

    public boolean execute() {
        if (    (tanner.getPPH() < 45000 && tanner.timeRan.exceeds(Duration.ofHours(8))) ||
                (tanner.getPPH() < 40000 && tanner.timeRan.exceeds(Duration.ofHours(6))) ||
                (tanner.getPPH() < 35000 && tanner.timeRan.exceeds(Duration.ofHours(4)))) {

            Log.fine("Starting Beggar");
            banking.openAndDepositAll();

            beggar.removeAll();
            beggar.resetRender();
            beggar.isTanning = false;
            beggar.restartBeggar = true;
            beggar.onStart();
            return true;
        }
        return false;
    }
}
