package script.tasks;

import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.script.task.Task;

public class BuyGE extends Task {

    @Override
    public boolean validate() {
        return false;
    }

    @Override
    public int execute() {
//        if (!GrandExchange.isOpen()) {
//            GrandExchange.open();
//            return 1000;
//        }
//        RSGrandExchangeOffer.Type
//        GrandExchange.createOffer()
         return 1000;
    }
}
