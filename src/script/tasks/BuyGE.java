package script.tasks;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;

import static org.rspeer.runetek.providers.RSGrandExchangeOffer.Type.BUY;

public class BuyGE extends Task {

    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public int execute() {
        if (!GrandExchange.isOpen()) {
            GrandExchange.open();
            return 1000;
        }
        GrandExchange.open(GrandExchange.View.BUY_OFFER);
        if (Time.sleepUntil(() -> GrandExchange.getView().compareTo(GrandExchange.View.BUY_OFFER) == 0, 10000)) {
            GrandExchange.createOffer(BUY);
            GrandExchangeSetup.setItem(1117);
            GrandExchangeSetup.increasePrice(5);
            if(GrandExchangeSetup.confirm()){
                Log.fine("Succesfully purchased");
                GrandExchange.collectAll();
            }
        }
         return 1000;
    }

    private void equip(int id){
        Inventory.getFirst(1117).interact("Equip");
    }
}
