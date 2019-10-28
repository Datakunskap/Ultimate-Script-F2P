package tasks;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.*;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;

public class doBonding extends Task {

    private static final String BOND = "Old school bond";
    private static final String BOND_UNTRADEABLE = "Old school bond (untradeable)";
    private static final String COINS = "Coins";
    private static final String MULE_NAME = "2147 Emblems";
    private static final String TRADE_ACTION = "Trade with";
    private static final int OFFERPRICE_BOND = 6000000;
    private static final Area GE_AREA = Area.rectangular(3157, 3489, 3171, 3477);

    @Override
    public boolean validate() {
        return Game.isLoggedIn() &&
                Game.getRemainingMembershipDays() <= 1;
    }

    @Override
    public int execute() {

        Player local = Players.getLocal();
        if (GE_AREA.contains(local)) {
            if (!Inventory.contains(BOND_UNTRADEABLE)) {
                if (Inventory.contains(COINS)) {
                    if (Inventory.getCount(true, COINS) >= OFFERPRICE_BOND) {
                        if (!Inventory.contains(BOND)) {
                            buyItem(BOND, 6000000, 1);
                        }
                    }
                    if (Inventory.getCount(true, COINS) < OFFERPRICE_BOND) {
                        Player mule = Players.getNearest(MULE_NAME);
                        if (Trade.isOpen(true)) {
                            Log.info("Second trade screen is open");
                            if (Trade.hasOtherAccepted()) {
                                Log.info("Accepting second trade screen");
                                if (Trade.accept()) {
                                    Time.sleepUntil(() -> Inventory.contains(COINS), 5000);
                                }
                            }
                        }
                        if (Trade.isOpen(false)) {
                            Log.info("First trade screen is open");
                            if (Trade.hasOtherAccepted()) {
                                Log.info("Accepting first trade screen");
                                if (Trade.accept()) {
                                    Time.sleepUntil(() -> Trade.isOpen(), 5000);
                                }
                            }
                        }
                        if (!Trade.isOpen()) {
                            Log.info("Trading the mule");
                            if (mule.interact(TRADE_ACTION)) {
                                Time.sleepUntil(() -> Trade.isOpen(), 10000);
                            }
                        }
                    }
                }
                if (!Inventory.contains(COINS)) {
                    Player mule = Players.getNearest(MULE_NAME);
                    if (Trade.isOpen(true)) {
                        Log.info("Second trade screen is open");
                        if (Trade.hasOtherAccepted()) {
                            Log.info("Accepting second trade screen");
                            if (Trade.accept()) {
                                Time.sleepUntil(() -> Inventory.contains(COINS), 5000);
                            }
                        }
                    }
                    if (Trade.isOpen(false)) {
                        Log.info("First trade screen is open");
                        if (Trade.hasOtherAccepted()) {
                            Log.info("Accepting first trade screen");
                            if (Trade.accept()) {
                                Time.sleepUntil(() -> Trade.isOpen(), 5000);
                            }
                        }
                    }
                    if (!Trade.isOpen()) {
                        Log.info("Trading the mule");
                        if (mule.interact(TRADE_ACTION)) {
                            Time.sleepUntil(() -> Trade.isOpen(), 10000);
                        }
                    }
                }
            }
            if (Inventory.contains(BOND_UNTRADEABLE)) {
                Item untradeableBond = Inventory.getFirst(BOND_UNTRADEABLE);
                if (untradeableBond != null) {
                    InterfaceComponent bondingScreen = Interfaces.getComponent(66, 7);
                    if (bondingScreen == null) {
                        Log.info("Redeeming bond");
                        if (untradeableBond.interact("Redeem")) {
                            Time.sleepUntil(() -> bondingScreen.isVisible(), 5000);
                        }
                    }
                    if (bondingScreen != null) {
                        InterfaceComponent confirmButton = Interfaces.getComponent(66, 24);
                        InterfaceComponent exchangeForMembershipButton = Interfaces.getComponent(66, 7);
                        if (confirmButton != null) {
                            Log.info("Confirming using bond");
                            if (confirmButton.interact("Confirm")) {
                                Time.sleepUntil(() -> confirmButton == null, 5000);
                            }
                            if (confirmButton == null) {
                                Log.info("Selecting 14 day option");
                                if (exchangeForMembershipButton.interact("1 Bond")) {
                                    Time.sleepUntil(() -> confirmButton != null, 5000);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (!GE_AREA.contains(local)) {
            Log.info("Walking to the GE");
            Movement.walkTo(GE_AREA.getCenter());
        }

        return lowRandom();
    }

    private int lowRandom() {
        return Random.mid(300, 450);
    }

    public void buyItem(String itemName, int price, int amount) {
        Area GE = Area.rectangular(3148, 3506, 3181, 3473);
        Player local = Players.getLocal();
        if (GE.contains(local)) {
            if (GrandExchange.isOpen()) {
                Log.info("I am buying" + " " + itemName);
                if (!Inventory.contains(itemName)) {
                    if (GrandExchange.createOffer(RSGrandExchangeOffer.Type.BUY)) {
                        Time.sleepUntil(() -> GrandExchangeSetup.getSetupType() == RSGrandExchangeOffer.Type.BUY, 1555, 2111);
                        if (GrandExchangeSetup.getSetupType() == RSGrandExchangeOffer.Type.BUY) {
                            if (GrandExchangeSetup.setItem(itemName)) {
                                Time.sleepUntil(() -> GrandExchangeSetup.getItem() != null, 1555, 2111);
                                if (GrandExchangeSetup.setPrice(price)) {
                                    Time.sleepUntil(() -> GrandExchangeSetup.getPricePerItem() == price, 1555, 2111);
                                    if (GrandExchangeSetup.setQuantity(amount)) {
                                        Time.sleepUntil(() -> GrandExchangeSetup.getQuantity() == amount, 1555, 2111);
                                        if (GrandExchangeSetup.confirm()) {
                                            Time.sleepUntil(() -> GrandExchange.getFirstActive() != null, 1555, 2111);
                                            if (GrandExchange.collectAll()) {
                                                Time.sleepUntil(() -> GrandExchange.getFirstActive() == null, 1555, 2111);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!GrandExchange.isOpen()) {
                Npc clerk = Npcs.getNearest("Grand Exchange Clerk");
                clerk.interact("Exchange");
                Time.sleepUntil(() -> GrandExchange.isOpen(), 2222, 3333);
            }
        }

        if (!GE.contains(local)) {
            Log.info("I'm traveling to GE to buy items");
            Movement.walkTo(new Position(3164, 3483, 0));
        }
    }
}
