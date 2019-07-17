package script.tasks;

import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;
import script.tanner.Main;

import java.time.Duration;

public class StartTanning extends Task {

    private final int RUNTIME_HOURS_LIMIT = 8;
    private final int START_GP = 100000;
    private final int MIN_START_GP = 60000;
    private final int PPH_LIMIT = 40000;
    private final int LAST_TRADE_MINUTES = 40;

    private final int LAST_TRADE_MINUTES_MUTED = 60;

    private Beggar main;

    public StartTanning(Beggar beggar){
        main = beggar;
    }

    @Override
    public boolean validate() {
        checkMuted();

        return ( (hasEnoughGP(START_GP) && hasLongRuntime(RUNTIME_HOURS_LIMIT)) ||
                (hasEnoughGP(START_GP) && hasLowPPH()) ||
                (hasEnoughGP(START_GP) && hasTopBegWorldsCovered()) ||
                (hasEnoughGP(MIN_START_GP) && hasLongLastTradeTime(LAST_TRADE_MINUTES)) );
    }

    private boolean hasLongRuntime(int hours) {
        return main.runtime.exceeds(Duration.ofHours(hours));
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
        return main.runtime.getHourlyRate(main.gainedC) < main.getTannerPPH(refresh) && main.runtime.exceeds(Duration.ofMinutes(30));
    }

    private boolean hasTopBegWorldsCovered() {
        return main.OTHER_BEG_WORLDS != null && main.OTHER_BEG_WORLDS.contains(301) && main.OTHER_BEG_WORLDS.contains(308) && main.OTHER_BEG_WORLDS.contains(393) &&
                main.currWorld != 301 && main.currWorld != 308 && main.currWorld != 393 &&
                Worlds.get(Worlds.getCurrent()).getPopulation() < 700;
    }

    private boolean hasLongLastTradeTime(int minutes) {
        return main.lastTradeTime != null && main.lastTradeTime.exceeds(Duration.ofMinutes(minutes));
    }

    private void checkMuted() {
        if (hasLongLastTradeTime(LAST_TRADE_MINUTES_MUTED) || (main.lastTradeTime == null && hasLongRuntime(LAST_TRADE_MINUTES_MUTED / 60))) {
            Log.severe("Muted");
            main.disableChain = false;
            main.setStopping(true);
        }
    }

    @Override
    public int execute() {
        Main tanner = Main.getInstance(main);

        main.tanner = tanner;
        main.isTanning = true;
        main.removeCurrBegWorld(Worlds.getCurrent());
        tanner.start();
        return 5000;
    }
}
