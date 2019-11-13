package script.chocolate.tasks;

import script.Script;
import script.chocolate.Main;
import script.beg.Mule;
import script.beg.StartOther;

import java.time.Duration;

public final class StartTanning {

    public static boolean validate(Main main, Script script) {
        return main.atGELimit || (main.getPPH() < script.getTannerPPH(StartOther.TANS_PER_HR, false) && main.timeRan.exceeds(Duration.ofMinutes(30)));
    }

    public static void execute(Main chocolate, Script script) {
        script.tanner.Main tanner = new script.tanner.Main(script);
        script.tanner = tanner;
        script.isChoc = false;
        script.isTanning = true;
        script.timesTanned ++;

        if (chocolate.isMuling) {
            Mule.logoutMule();
        }
        tanner.amntMuled += chocolate.amntMuled;
        tanner.start();
    }
}
