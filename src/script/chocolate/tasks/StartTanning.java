package script.chocolate.tasks;

import script.Beggar;
import script.chocolate.Main;
import script.tasks.Mule;

public final class StartTanning {

    public static boolean validate(int made, int buyQ) {
        return made + buyQ > Main.BAR_GE_LIMIT;
    }

    public static void execute(Main chocolate, Beggar beggar) {
        script.tanner.Main tanner = script.tanner.Main.getInstance(beggar);
        beggar.tanner = tanner;
        beggar.isChoc = false;
        beggar.isTanning = true;

        if (chocolate.isMuling) {
            Mule.logoutMule();
        }

        tanner.start();
    }
}
