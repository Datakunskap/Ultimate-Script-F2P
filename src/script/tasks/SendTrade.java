package script.tasks;

import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class SendTrade extends Task {

    @Override
    public boolean validate() {
        return Beggar.setSendTrades && !Beggar.walk && !Beggar.beg && Beggar.sendTrade && !Beggar.trading;
    }

    @Override
    public int execute() {
        sendTradeNearest();
        return 1000;
    }

    public void sendTradeNearest() {
        String name = "";
        while (!Beggar.tradeSent && Beggar.sendTryCount < 10) {
            if(Beggar.walk || Beggar.trading) {
                return;
            }
            Player p = Players.getNearest(x -> x.getCombatLevel() > 3 && x != null && x.isPositionInteractable() && x.isPositionWalkable());
            if (p != null) {
                p.interact("Trade with");
                name = p.getName();
                Time.sleepUntil(() -> Beggar.tradeSent, 1200);
            }
            Beggar.sendTryCount++;
        }

        if (Beggar.sendTryCount < 10 && Beggar.tradeSent){
            Log.info("Trade sent to " + name);
        } else {
            Log.severe("Unable to find player");
        }
        Beggar.tradeSent = false;
        Beggar.sendTrade = false;
        Beggar.sendTryCount = 0;
    }

    public void sendTradeLoaded() {
        while (!Beggar.tradeSent && Beggar.sendTryCount < 10) {
            Player[] players = Players.getLoaded(x -> x != null && x.isPositionInteractable() && x.isPositionWalkable());
            Player currNearest = null;
            for (Player p : players) {
                if (p != null) {
                    if (currNearest == null || p.getPosition().distance(Players.getLocal()) < currNearest.getPosition().distance(Players.getLocal())) {
                        currNearest = p;
                    }
                }
            }
            if (currNearest != null) {
                currNearest.interact("Trade with");
            }
            Beggar.sendTryCount++;
        }

        if (Beggar.sendTryCount <= 10){
            Log.info("Trade sent");
        } else {
            Log.severe("Unable to find player");
        }
        Beggar.tradeSent = false;
        Beggar.sendTrade = false;
        Beggar.sendTryCount = 0;
    }
}
