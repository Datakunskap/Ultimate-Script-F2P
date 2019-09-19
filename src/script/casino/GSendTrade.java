/*
package script.casino;

import org.fighter.runetek.adapter.scene.Player;
import org.fighter.runetek.api.commons.Time;
import org.fighter.runetek.api.scene.Players;
import org.fighter.script.task.Task;
import org.fighter.ui.Log;
import script.Beggar;

public class GSendTrade extends Task {

    @Override
    public boolean validate() {
        return Beggar.tradeGambler && !Beggar.trading;
    }

    @Override
    public int execute() {
        sendTradeWinner();
        return 1000;
    }

    public void sendTradeWinner() {
        while (!Beggar.tradeSent && Beggar.sendTryCount < 10) {
            Player p = Players.getNearest(Beggar.gamblerName);
            if (p != null) {
                p.interact("Trade with");
                Time.sleepUntil(() -> Beggar.tradeSent, 1200);
            }
            Beggar.sendTryCount++;
        }

        if (Beggar.sendTryCount < 10 && Beggar.tradeSent) {
            Log.info("Trade sent to " + Beggar.gamblerName);
        } else {
            Log.severe("Unable to find player");
        }
        Beggar.tradeSent = false;
        Beggar.tradeGambler = false;
        Beggar.giveGambler = true;
        Beggar.sendTryCount = 0;
    }
}*/
