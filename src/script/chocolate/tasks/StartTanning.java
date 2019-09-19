package script.chocolate.tasks;

import script.Beggar;
import script.chocolate.Main;
import script.tasks.Mule;
import script.tasks.StartOther;

import java.time.Duration;

public final class StartTanning {

    public static boolean validate(Main main, Beggar beggar) {
        return main.atGELimit || (main.getPPH() < beggar.getTannerPPH(StartOther.TANS_PER_HR, false) && main.timeRan.exceeds(Duration.ofMinutes(30)));
    }

    public static void execute(Main chocolate, Beggar beggar) {
        script.tanner.Main tanner = script.tanner.Main.getInstance(beggar);
        beggar.tanner = tanner;
        beggar.isChoc = false;
        beggar.isTanning = true;
        beggar.timesTanned ++;

        if (chocolate.isMuling) {
            Mule.logoutMule();
        }
        tanner.amntMuled += chocolate.amntMuled;
        tanner.start();
    }
}
