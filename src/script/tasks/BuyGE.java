package script.tasks;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.GrandExchangeSetup;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class BuyGE extends Task {

    private boolean bought = false;
    private final String[] items = new String[] {"Bronze platebody", "Iron platebody", "Bronze med helm",
                                "Bronze full helm", "Iron full helm", "Iron med helm", "Iron platelegs"};
    private final String X = items[Beggar.randInt(0, items.length-1)];

    @Override
    public boolean validate() {
        return Inventory.getCount(true, 995) > 1000 && !Beggar.equipped;
    }

    @Override
    public int execute() {
        Log.info("Buying from GE & Equipping");

        if (!GrandExchange.isOpen() && !bought) {
            Npcs.getNearest("Grand Exchange Clerk").interact("Exchange");
            Time.sleep(Beggar.randInt(700, 1300));
            return 1000;
        }

        if (!Inventory.contains(X) && !bought) {
            GrandExchange.createOffer(RSGrandExchangeOffer.Type.BUY);
            Time.sleep(800);
            GrandExchangeSetup.setItem(X);
            Time.sleep(600);
            GrandExchangeSetup.increasePrice(Random.nextInt(30,35));
            Time.sleep(600);
            GrandExchangeSetup.setQuantity(1);
            Time.sleep(600);
            GrandExchangeSetup.confirm();
            Time.sleep(600);
            GrandExchange.collectAll();
            Time.sleep(Random.mid(300, 600));
            GrandExchange.collectAll();
            Time.sleep(Random.mid(300, 600));
            return 1000;
        }

        if (Inventory.contains(X)) {
            bought = true;
            if (Inventory.getFirst(X).interact("Wear")) {
                //Beggar.equipped = true;
                if (Time.sleepUntil(() -> Equipment.contains(X), Random.mid(2300, 2850))) {
                    Beggar.equipped = true;
                } else {
                    Beggar.walk = true;
                }
            }
        }
        if (Equipment.contains(X)) {
            Beggar.equipped = true;
        }
        return 1000;
    }

    public void geOffer(RSGrandExchangeOffer.Type type, String itemToBuy, int quantityNeeded, int amtTimesToChangePrice) {
        int price = 0;
        if (type.equals(RSGrandExchangeOffer.Type.BUY)) {
            if (GrandExchange.createOffer(RSGrandExchangeOffer.Type.BUY)) {
                Time.sleepUntil(() -> GrandExchangeSetup.getSetupType().equals(RSGrandExchangeOffer.Type.BUY), 1050, 1350);
            }
        }
        if (type.equals(RSGrandExchangeOffer.Type.SELL)) {
            if (GrandExchange.createOffer(RSGrandExchangeOffer.Type.SELL)) {
                Time.sleepUntil(() -> GrandExchangeSetup.getSetupType().equals(RSGrandExchangeOffer.Type.SELL), 1050, 1530);
            }
        }

        if (GrandExchangeSetup.setItem(itemToBuy)) {
            Time.sleepUntil(() -> GrandExchangeSetup.getItem() != null, 1100, 1850);
            Time.sleep(Beggar.randInt(500, 1000));
            price = GrandExchangeSetup.getPricePerItem();
        }

        if (type.equals(RSGrandExchangeOffer.Type.BUY)) {
            if (GrandExchangeSetup.increasePrice(amtTimesToChangePrice)) {
                final int fprice = price;
                Time.sleepUntil(() -> GrandExchangeSetup.getPricePerItem() > fprice, 1400, 1800);
            }
        }

        if (type.equals(RSGrandExchangeOffer.Type.SELL)) {
            if (GrandExchangeSetup.decreasePrice(amtTimesToChangePrice)) {
                final int fprice = price;
                Time.sleepUntil(() -> GrandExchangeSetup.getPricePerItem() > fprice, 1600, 1800);
            }
        }
        if (GrandExchangeSetup.setQuantity(quantityNeeded)) {
            Time.sleepUntil(() -> GrandExchangeSetup.getQuantity() >= quantityNeeded, 1600, 1800);
        }
        if (GrandExchangeSetup.confirm()) {
            Time.sleepUntil(() -> this.isFinished(), 5200, 5800);
        }

        for (int i = 0; i < 4; i++) {
            if (this.isFinished()) {
                if (GrandExchange.collectAll()) {
                    Time.sleepUntil(() -> this.isEmpty(), 5000, 5900);
                }
            }
        }
    }

    boolean isFinished() {
        RSGrandExchangeOffer[] offers = GrandExchange.getOffers();
        if (offers != null) {
            for (int i = 0; i < offers.length; i++) {
                if (offers[i].getProgress().equals(RSGrandExchangeOffer.Progress.FINISHED)) {
                    Log.info("Offer finished");
                    return true;
                }
            }
        }
        return false;
    }

    boolean isEmpty() {
        RSGrandExchangeOffer[] offers = GrandExchange.getOffers();
        if (offers != null) {
            for (int i = 0; i < offers.length; i++) {
                if (offers[i].getProgress().equals(RSGrandExchangeOffer.Progress.EMPTY)) {
                    Log.info("Offer empty");
                    return true;
                }
            }
        }
        return false;
    }
}
