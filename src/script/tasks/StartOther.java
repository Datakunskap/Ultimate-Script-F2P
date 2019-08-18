package script.tasks;

import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

import java.time.Duration;

public class StartOther extends Task {

    private final int RUNTIME_HOURS_LIMIT = 7;
    private final int LAST_TRADE_MINUTES = 30;
    private final int LAST_TRADE_MINUTES_MUTED = 35;

    public static final int TAN_START_GP = 85000;
    private final int TAN_MIN_START_GP = 55000;
    private final int TANS_PER_HR = 1053;

    public static final int CHOC_START_GP = 85000;
    private final int CHOC_MIN_START_GP = 55000;
    private final int CHOC_PER_HR = 3500;

    private Beggar main;

    public StartOther(Beggar beggar){
        main = beggar;
    }

    @Override
    public boolean validate() {
        if(main.isMuling)
            return false;

        checkMuted();

        if ((hasEnoughGP(CHOC_START_GP) && hasLongRuntime(RUNTIME_HOURS_LIMIT, 0)) ||
                (hasEnoughGP(CHOC_START_GP) && hasLowPPH(false)) ||
                (hasEnoughGP(CHOC_START_GP) && hasTopBegWorldsCovered()) ||
                (hasEnoughGP(CHOC_MIN_START_GP) && hasLongLastTradeTime(LAST_TRADE_MINUTES)) ) {

            compareOtherPPH(true);
            return true;
        }

        if ((hasEnoughGP(TAN_START_GP) && hasLongRuntime(RUNTIME_HOURS_LIMIT, 0)) ||
                (hasEnoughGP(TAN_START_GP) && hasLowPPH(true)) ||
                (hasEnoughGP(TAN_START_GP) && hasTopBegWorldsCovered()) ||
                (hasEnoughGP(TAN_MIN_START_GP) && hasLongLastTradeTime(LAST_TRADE_MINUTES)) ) {

            compareOtherPPH(true);
            return true;
        }
        return false;
    }

    private void compareOtherPPH(boolean refresh) {
        if (main.getTannerPPH(TANS_PER_HR, refresh) > main.getChocolatePPH(CHOC_PER_HR, false)) {
            main.isTanning = true;
        } else {
            main.isChoc = true;
        }
    }

    private boolean hasLongRuntime(int hours, int minutes) {
        return (hours > 0) ? main.runtime.exceeds(Duration.ofHours(hours)) : main.runtime.exceeds(Duration.ofMinutes(minutes));
    }

    private boolean hasEnoughGP(int amount) {
        return Inventory.getCount(true, 995) >= amount;
    }

    private boolean hasLowPPH(boolean tanPPH) {
        boolean refresh = false;
        if (main.refreshPrices) {
            refresh = true;
            main.refreshPrices = false;
        }
        if (tanPPH) {
            return main.runtime.exceeds(Duration.ofMinutes(30)) && main.runtime.getHourlyRate(main.gainedC) < main.getTannerPPH(TANS_PER_HR, refresh);
        } else {
            return main.runtime.exceeds(Duration.ofMinutes(30)) && main.runtime.getHourlyRate(main.gainedC) < main.getChocolatePPH(CHOC_PER_HR, refresh);
        }
    }

    private boolean hasTopBegWorldsCovered() {
        return main.OTHER_BEG_WORLDS != null && main.OTHER_BEG_WORLDS.contains(main.popWorldsArr[0]) && main.OTHER_BEG_WORLDS.contains(main.popWorldsArr[1]) && main.OTHER_BEG_WORLDS.contains(main.popWorldsArr[2]) &&
                main.currWorld != main.popWorldsArr[0] && main.currWorld != main.popWorldsArr[1] && main.currWorld != main.popWorldsArr[2] &&
                Worlds.get(Worlds.getCurrent()).getPopulation() < 700;
    }

    private boolean hasLongLastTradeTime(int minutes) {
        return main.lastTradeTime != null && main.lastTradeTime.exceeds(Duration.ofMinutes(minutes));
    }

    private void checkMuted() {
        if (hasLongLastTradeTime(LAST_TRADE_MINUTES_MUTED) || (main.lastTradeTime == null && hasLongRuntime(0,LAST_TRADE_MINUTES_MUTED))) {
            Log.severe("Muted");
            main.muted = true;
        }
    }

    @Override
    public int execute() {
        if (main.isTanning && !main.isChoc) {
            script.tanner.Main tanner = script.tanner.Main.getInstance(main);

            main.tanner = tanner;
            main.removeCurrBegWorld(main.currWorld);
            if (main.isMuling) {
                Mule.logoutMule();
            }
            tanner.start();
            return 5000;
        }

        if (main.isChoc && !main.isTanning) {
            script.chocolate.Main choc = script.chocolate.Main.getInstance(main);

            main.choc = choc;
            main.removeCurrBegWorld(main.currWorld);
            if (main.isMuling) {
                Mule.logoutMule();
            }
            choc.start();
            return 5000;
        }
        return 1000;
    }
}
