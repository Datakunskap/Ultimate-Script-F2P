package script.fighter.backgroundTasks;

import script.fighter.Fighter;
import script.fighter.framework.BackgroundTaskExecutor;

public class StopTimeChecker {

    private final Fighter fighter;
    private final long START_TIME;
    private final long STOP_TIME;

    public StopTimeChecker(Fighter fighter, long stopTime) {
        this.fighter = fighter;
        this.STOP_TIME = stopTime;
        START_TIME = System.currentTimeMillis();

        Runnable checkStopTime = () -> {
            if ((System.currentTimeMillis() - START_TIME) > STOP_TIME) {
                fighter.onStop(true, 10);
            }
        };
        BackgroundTaskExecutor.submit(checkStopTime, 100);
    }
}
