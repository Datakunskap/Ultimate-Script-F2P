package script.fighter.nodes.restock;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.ui.Log;
import script.Beggar;
import script.data.Location;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.debug.Logger;
import script.fighter.framework.Node;
import script.fighter.models.Progressive;
import script.fighter.wrappers.*;
import script.tanner.ExGrandExchange;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class BuyGE extends Node {

    private Fighter main;
    private String status;
    private Iterator<String> runesIterator;
    private HashSet<String> items;
    private String itemToBuy;
    private Spell spell;
    private boolean checkedBank;
    private boolean triedTeleport;
    private int coinsToSpend;
    private int quantity;

    public BuyGE(Fighter main) {
        this.main = main;
    }

    @Override
    public boolean validate() {
        if (runesIterator != null || GEWrapper.itemsStillActive(RSGrandExchangeOffer.Type.BUY))
            return true;

        Progressive p = Config.getProgressive();
        items = new HashSet<>();
        spell = p.getSpell();

        if ((p.isSplash() || p.isOgress()) && runesIterator == null &&
                (!GEWrapper.hasRunes(spell) || !GEWrapper.hasEquipment())) {

            if (main.getActive() == null || !main.getActive().equals(main.supplier.SELL_GE)) {
                Log.fine("Selling Loot");
                GEWrapper.setSellItems(true);
                return false;
            }

            if (!GEWrapper.hasRunes(spell)) {
                Log.fine("Restocking: Runes");
                items.addAll(p.getRunes());
            }
            if (!GEWrapper.hasEquipment()) {
                Log.fine("Restocking: Equipment");
                items.addAll(GEWrapper.getEquipmentNeeded());
            }

            runesIterator = items.iterator();
            itemToBuy = runesIterator.next();
            quantity = getQuantity(itemToBuy);
            return true;
        }

        return false;
    }

    @Override
    public int execute() {
        main.invalidateTask(this);

        if (!Location.GE_AREA_LARGE.getBegArea().contains(Players.getLocal())) {
            if (!triedTeleport && (OgressWrapper.CORSAIR_COVE[0].contains(Players.getLocal())
                    || SplashWrapper.getSplashArea().contains(Players.getLocal()))) {

                TeleportWrapper.tryTeleport(false);
                triedTeleport = true;
            }
            status = "Walking to GE";
            GEWrapper.walkToGE();
            return Fighter.getLoopReturn();
        }

        coinsToSpend = Inventory.getCount(true, "Coins");

        // check GP
        if (runesIterator != null && !GEWrapper.itemsStillActive(RSGrandExchangeOffer.Type.BUY)) {
            if (coinsToSpend < (getPrice(itemToBuy) * quantity)) {
                if (!checkedBank) {
                    Log.info(itemToBuy + " : "  + (getPrice(itemToBuy) * quantity) + " --> Checking Bank");
                    BankWrapper.openAndDepositAll(true, true, true);
                    runesIterator = null;
                    checkedBank = true;
                } else {
                    Log.fine(itemToBuy + " : "  + (getPrice(itemToBuy) * quantity) + " --> Begging");
                    main.onStop(true, true, 10);
                    runesIterator = null;
                }
                return Fighter.getLoopReturn();
            }
        }

        if (!GrandExchange.isOpen()) {
            status = "Restocking";
            Bank.close();
            BankWrapper.updateInventoryValue();
            GEWrapper.openGE();
            return Fighter.getLoopReturn();
        }

        if (runesIterator != null && !GEWrapper.itemsStillActive(RSGrandExchangeOffer.Type.BUY)) {
            if ((!Inventory.contains(itemToBuy) || Inventory.getCount(true, itemToBuy) < quantity) && !Equipment.contains(itemToBuy)) {
                if (ExGrandExchange.buy(itemToBuy, quantity, getPrice(itemToBuy), false)) {
                    Log.info("Buying: " + quantity + " " + itemToBuy);
                    if (Time.sleepUntil(() -> GrandExchange.getFirst(x -> x.getItemName().toLowerCase().equals(itemToBuy)) != null, 8000)) {
                        if (runesIterator.hasNext()) {
                            itemToBuy = runesIterator.next();
                            quantity = getQuantity(itemToBuy);
                        } else {
                            runesIterator = null;
                        }
                    }
                }
            } else {
                Log.info("Already has: " + quantity + " " + itemToBuy);
                if (runesIterator.hasNext()) {
                    itemToBuy = runesIterator.next();
                    quantity = getQuantity(itemToBuy);
                } else {
                    runesIterator = null;
                }
            }
        }

        if (!GrandExchange.getView().equals(GrandExchange.View.OVERVIEW)) {
            GrandExchange.open(GrandExchange.View.OVERVIEW);
        }

        if (GEWrapper.itemsStillActive(RSGrandExchangeOffer.Type.BUY)) {
            status = "Waiting for completion";
            GrandExchange.collectAll();
            Keyboard.pressEnter();
        }

        if (!GEWrapper.itemsStillActive(RSGrandExchangeOffer.Type.BUY) && runesIterator == null) {
            Log.fine("Done restocking 1");
            doneRestockingHelper();
        }

        return Random.low(800, 1500);
    }

    private void doneRestockingHelper() {
        status = "Completed";
        Interfaces.closeAll();
        GEWrapper.closeGE();

        BankWrapper.openAndDepositAll(Config.getProgressive().isSplash() ? 45 : 0, true, true);
        Bank.close();
        Time.sleepUntil(Bank::isClosed, 1000, 5000);
        BankWrapper.updateInventoryValue();
    }

    private int getQuantity(String item) {
        Progressive p = Config.getProgressive();
        int amnt;

        if (p.isSplash() && p.getRunes().contains(item)) {
            return SplashWrapper.getSplashStartAmnt(false) / (getPrice(item) * p.getRunes().size());
        }
        if (spell.equals(Spell.Modern.WIND_STRIKE) && p.getRunes().contains(item)) {

            amnt = coinsToSpend / (getPrice(item) * p.getRunes().size());
            if (amnt <= 50) {
                return amnt;
            }
            return Random.mid(35, 50);
        }
        if (spell.equals(Spell.Modern.FIRE_STRIKE) && p.getRunes().contains(item)) {

            if (!GEWrapper.hasEquipment()) {
                Collection<String> equipment = p.getEquipmentMap().values();
                for (String e : equipment) {
                    if (items.contains(e)) {
                        coinsToSpend -= (getPrice(e) / p.getRunes().size());
                    }
                }
            }

            if (item.equalsIgnoreCase("air rune")) {
                amnt = coinsToSpend / (getPrice(item) * p.getRunes().size());
                amnt = amnt * 2;

                if (amnt <= (20 * Beggar.OGRESS_MAX_MINUTES_WORTH_OF_RUNES * 2)) {
                    return amnt;
                }
            } else {
                amnt = coinsToSpend / (getPrice(item) * p.getRunes().size());

                if (amnt <= (20 * Beggar.OGRESS_MAX_MINUTES_WORTH_OF_RUNES)) {
                    return amnt;
                }
            }

            return item.equalsIgnoreCase("air rune")
                    ? Random.mid(1200, (20 * Beggar.OGRESS_MAX_MINUTES_WORTH_OF_RUNES)) * 2
                    : Random.mid(1200, (20 * Beggar.OGRESS_MAX_MINUTES_WORTH_OF_RUNES));
        }

        return 1;
    }

    private int getPrice(String item) {
        Progressive p = Config.getProgressive();
        if (item.equalsIgnoreCase("air rune") || item.equalsIgnoreCase("mind rune")) {
            return 6;
        }
        if (item.equalsIgnoreCase("staff of fire")) {
            return 2000;
        }
        if (p.getEquipmentMap().containsValue(item.toLowerCase())) {
            if (p.isSplash()) {
                return 500;
            }
            if (item.equalsIgnoreCase("blue wizard robe")) {
                return 2000;
            }
            if (item.equalsIgnoreCase("blue wizard hat")) {
                return 500;
            }
            if (item.equalsIgnoreCase("zamorak monk bottom")) {
                return 3000;
            }
        }

        return coinsToSpend / items.size();
    }

    @Override
    public void onInvalid() {
        if (!GEWrapper.isSellItems()) {
            Log.fine("Done Restocking");
            doneRestockingHelper();
        }

        checkedBank = false;
        runesIterator = null;
        triedTeleport = false;
        items = null;
        super.onInvalid();
    }

    public void invalidateTask(Node active) {
        if (active != null && !this.equals(active)) {
            Logger.debug("Node has changed.");
            active.onInvalid();
        }
        main.setActive(this);
    }

    @Override
    public String status() {
        return status;
    }
}
