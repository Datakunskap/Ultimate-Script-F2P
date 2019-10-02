package script.beg;

import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class SendTrade extends Task {

    private Beggar main;

    public SendTrade(Beggar beggar){
        main = beggar;
    }

    @Override
    public boolean validate() {
        return main.setSendTrades && !main.walk && !main.beg && main.sendTrade && !main.trading;
    }

    @Override
    public int execute() {
        sendTradeNearest();
        return 1000;
    }

    private void sendTradeNearest() {
        String name = "";
        while (!main.tradeSent && main.sendTryCount < 10) {
            if(main.walk || main.trading) {
                return;
            }
            Player p = Players.getNearest(x -> x != null && x.getCombatLevel() > 3 && x.isPositionInteractable() && x.isPositionWalkable());
            if (p != null) {
                p.interact("Trade with");
                name = p.getName();
                Time.sleepUntil(() -> main.tradeSent, 1200);
            }
            main.sendTryCount++;
        }

        if (main.sendTryCount < 10){
            Log.info("Trade sent to " + name);
        } else {
            Log.severe("Unable to find player");
        }
        main.tradeSent = false;
        main.sendTrade = false;
        main.sendTryCount = 0;
    }

    public void sendTradeLoaded() {
        while (!main.tradeSent && main.sendTryCount < 10) {
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
            main.sendTryCount++;
        }

        if (main.sendTryCount <= 10){
            Log.info("Trade sent");
        } else {
            Log.severe("Unable to find player");
        }
        main.tradeSent = false;
        main.sendTrade = false;
        main.sendTryCount = 0;
    }
}
