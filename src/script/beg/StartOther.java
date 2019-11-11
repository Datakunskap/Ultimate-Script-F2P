package script.beg;

import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Script;
import script.chocolate.Main;
import script.fighter.wrappers.OgressWrapper;
import script.fighter.wrappers.WorldhopWrapper;

import java.time.Duration;

public class StartOther extends Task {

    private final int HIGH_OTHER_PPH_AMNT = 55_000;
    private final int SUM_TOP_3_WORLDS_POP_LIMIT = 3000;
    private final int RUNTIME_HOURS_LIMIT = 8;
    private final int LAST_TRADE_MINUTES = 30;
    private final int LAST_TRADE_MINUTES_MUTED = 35;
    public static final int START_GP = 85_000;
    private final int MIN_START_GP = 55_000;

    public static final int TANS_PER_HR = 1400;
    public static final int CHOC_PER_HR = 3000;

    private Script main;

    public StartOther(Script script) {
        main = script;
    }

    @Override
    public boolean validate() {
        if (main.isMuling)
            return false;

        checkMuted();

        if (Script.OGRESS && hasEnoughGP(Script.OGRESS_START_GP)) {
            return true;
        }

        if ((hasEnoughGP(START_GP) && hasLongRuntime(RUNTIME_HOURS_LIMIT, 0))
                || (hasEnoughGP(START_GP) && hasTopBegWorldsCovered())
                || (hasEnoughGP(MIN_START_GP) && hasLongLastTradeTime(LAST_TRADE_MINUTES))
                || (hasEnoughGP(MIN_START_GP) && (hasLowPPH() || hasHighOtherPPH()
                || (hasLowTopWorldsPopulation() && hasTopBegWorldsCovered())))
        ) {
            startComparePPH(true);
            return true;
        }
        return false;
    }

    private void startComparePPH(boolean refresh) {
        if (main.getTannerPPH(TANS_PER_HR, refresh) > main.getChocolatePPH(CHOC_PER_HR, false)) {
            main.isTanning = true;
        } else {
            main.isChoc = true;
        }
    }

    private boolean hasHighOtherPPH() {
        return main.getTannerPPH(TANS_PER_HR, false) >= HIGH_OTHER_PPH_AMNT || main.getChocolatePPH(CHOC_PER_HR, false) >= HIGH_OTHER_PPH_AMNT;
    }

    private boolean hasLowTopWorldsPopulation() {
        return main.sumTopPops < SUM_TOP_3_WORLDS_POP_LIMIT;
    }

    private boolean hasLongRuntime(int hours, int minutes) {
        return (hours > 0) ? main.runtime.exceeds(Duration.ofHours(hours))
                : (Script.RESET_RUNTIME ? main.runtime.exceeds(Duration.ofMinutes(minutes))
                : main.runtime.exceeds(Duration.ofMinutes(minutes + 30)));
    }

    private boolean hasEnoughGP(int amount) {
        return Inventory.getCount(true, 995) >= amount;
    }

    private boolean hasLowPPH() {
        boolean refresh = false;
        if (main.refreshPrices) {
            refresh = true;
            main.refreshPrices = false;
        }
        return main.runtime.exceeds(Duration.ofMinutes(LAST_TRADE_MINUTES)) && main.runtime.getHourlyRate(main.gainedC) < main.getTannerPPH(TANS_PER_HR, refresh) ||
                main.runtime.exceeds(Duration.ofMinutes(LAST_TRADE_MINUTES)) && main.runtime.getHourlyRate(main.gainedC) < main.getChocolatePPH(CHOC_PER_HR, refresh);
    }

    private boolean hasTopBegWorldsCovered() {
        return WorldhopWrapper.OTHER_BEG_WORLDS != null && WorldhopWrapper.OTHER_BEG_WORLDS.contains(main.popWorldsArr[0]) && WorldhopWrapper.OTHER_BEG_WORLDS.contains(main.popWorldsArr[1]) && WorldhopWrapper.OTHER_BEG_WORLDS.contains(main.popWorldsArr[2]) &&
                main.currWorld != main.popWorldsArr[0] && main.currWorld != main.popWorldsArr[1] && main.currWorld != main.popWorldsArr[2];
    }

    private boolean hasLongLastTradeTime(int minutes) {
        return main.lastTradeTime != null && main.lastTradeTime.exceeds(Duration.ofMinutes(minutes));
    }

    private void checkMuted() {
        if (hasLongLastTradeTime(LAST_TRADE_MINUTES_MUTED) || (main.lastTradeTime == null && hasLongRuntime(0, LAST_TRADE_MINUTES_MUTED))) {
            Log.severe("Muted");
            main.muted = true;
        }
    }

    @Override
    public int execute() {
        if (main.isTanning && !main.isChoc) {
            script.tanner.Main tanner = new script.tanner.Main(main);

            main.tanner = tanner;
            main.timesTanned++;
            main.tanner.amntMuled += main.amntMuled;
            WorldhopWrapper.removeWorld(main.currWorld, Script.CURR_WORLD_PATH);
            if (main.isMuling) {
                Mule.logoutMule(Script.MULE_IP);
            }
            OgressWrapper.unequipAll(true);
            tanner.start();
            return 5000;
        }

        if (main.isChoc && !main.isTanning) {
            if (Worlds.get(Worlds.getCurrent()).getPopulation() > 400) {
                WorldhopWrapper.hopToLowPopWorld(400, Worlds.getCurrent());
            }
            if (main.isMuling) {
                Mule.logoutMule(Script.MULE_IP);
            }
            WorldhopWrapper.removeWorld(main.currWorld, Script.CURR_WORLD_PATH);
            main.timesChocolate++;
            main.chocolate = new Main(main);
            main.chocolate.amntMuled += main.amntMuled;
            main.chocolate.start();
            return 5000;
        }

        if (hasEnoughGP(Script.OGRESS_START_GP)) {
            if (main.isMuling) {
                Mule.logoutMule(Script.MULE_IP);
            }
            if (Worlds.get(Worlds.getCurrent()).getPopulation() > 400) {
                WorldhopWrapper.hopToLowPopWorld(400, Worlds.getCurrent(), WorldhopWrapper.getWorldsFromFile(Script.OGRESS_WORLD_PATH));
            }

            OgressWrapper.unequipAll(false);
            WorldhopWrapper.removeWorld(main.currWorld, Script.CURR_WORLD_PATH);
            main.startOgress();
            return 5000;
        }
        return 1000;
    }

}
