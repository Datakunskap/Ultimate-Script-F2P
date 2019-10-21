package script.fighter.nodes.restock;

import api.component.ExPriceCheck;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Definitions;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.EquipmentSlot;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.providers.RSGrandExchangeOffer;
import org.rspeer.runetek.providers.RSItemDefinition;
import org.rspeer.ui.Log;
import script.data.Location;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.debug.Logger;
import script.fighter.framework.Node;
import script.fighter.models.Progressive;
import script.fighter.nodes.ogress.GoToCove;
import script.fighter.wrappers.*;
import script.tanner.ExGrandExchange;

import java.util.Arrays;
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

    public BuyGE(Fighter main) {
        this.main = main;
    }

    @Override
    public boolean validate() {
        if (runesIterator != null || GEWrapper.itemsStillActive(RSGrandExchangeOffer.Type.BUY))
            return true;

        Progressive p = Config.getProgressive();
        items = p.getRunes();
        spell = p.getSpell();

        if (items != null && items.size() > 0 && spell != null &&
                (!GEWrapper.hasRunes(spell) || !GEWrapper.hasEquipment() ||
                        (p.isSplash() && p.isUseSplashGear() && !SplashWrapper.hasSplashGear(false)))) {

            Log.info(items != null);
            Log.info(items.size() > 0);
            Log.info(spell != null);
            Log.info(!GEWrapper.hasRunes(spell));
            Log.info(!GEWrapper.hasEquipment());
            Log.info(p.isSplash());
            Log.info(p.isUseSplashGear());
            Log.info(!SplashWrapper.hasSplashGear(false));

            if (p.isSplash() && p.isUseSplashGear() && !SplashWrapper.hasSplashGear(false)) {
                Log.fine("Restocking: Runes & Splash Gear");
                items.addAll(Arrays.asList(SplashWrapper.getSplashGear(false)));
            } else if (!GEWrapper.hasEquipment()) {
                Log.fine("Restocking: Runes & Equipment");
                items.addAll(p.getEquipmentMap().values());
            } else {
                Log.fine("Restocking: Runes");
            }

            if (main.getActive() != null && !main.getActive().equals(main.supplier.SELL_GE) && !GEWrapper.isSellItems()) {
                Log.info("Selling loot");
                GEWrapper.setSellItems(true);
            }

            runesIterator = items.iterator();
            itemToBuy = runesIterator.next();
            return true;
        }

        return false;
    }

    @Override
    public int execute() {
        invalidateTask(main.getActive());

        if (!Location.GE_AREA.getBegArea().contains(Players.getLocal())) {
            if (!triedTeleport && (OgressWrapper.CORSAIR_COVE[0].contains(Players.getLocal())
                    || SplashWrapper.getSplashArea().contains(Players.getLocal()))) {

                TeleportWrapper.teleportHome();
                triedTeleport = true;
            }
            status = "Walking to GE";
            walkToGE();
            return Fighter.getLoopReturn();
        }

        if (!GrandExchange.isOpen()) {
            status = "Restocking";
            coinsToSpend = Inventory.getCount(true, "Coins");
            GEWrapper.openGE();
            return Fighter.getLoopReturn();
        }

        if (runesIterator != null && !GEWrapper.itemsStillActive(RSGrandExchangeOffer.Type.BUY)) {
            if ((!Inventory.contains(itemToBuy) || Inventory.getCount(true, itemToBuy) < getQuantity(itemToBuy)) && !Equipment.contains(itemToBuy)) {
                if (ExGrandExchange.buy(itemToBuy, getQuantity(itemToBuy), getPrice(itemToBuy), false)) {
                    if (Time.sleepUntil(() -> GrandExchange.getFirst(x -> x.getItemName().toLowerCase().equals(itemToBuy)) != null, 1000, 8000)) {
                        Logger.debug("Buying: " + getQuantity(itemToBuy) + " " + itemToBuy);
                        if (runesIterator.hasNext()) {
                            itemToBuy = runesIterator.next();
                        } else {
                            runesIterator = null;
                        }
                    }
                }
            } else {
                Log.info("Already has: " + getQuantity(itemToBuy) + " " + itemToBuy);
                if (runesIterator.hasNext()) {
                    itemToBuy = runesIterator.next();
                } else {
                    runesIterator = null;
                }
            }
        }

        if (!GrandExchange.getView().equals(GrandExchange.View.OVERVIEW)) {
            GrandExchange.open(GrandExchange.View.OVERVIEW);
        }

        if (GEWrapper.itemsStillActive(RSGrandExchangeOffer.Type.BUY)) {
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
        Progressive p = Config.getProgressive();
        GEWrapper.closeGE();
        BankWrapper.openAndDepositAll(p.isSplash() ? 45 : 0, items.toArray(new String[0]));
        Bank.close();
        Time.sleepUntil(Bank::isClosed, 1000, 5000);
        equipEquipment(p);
        if (p.isSplash() || p.getEnemies().contains("chicken")) {
            TeleportWrapper.teleportHome();
        }
    }

    private void walkToGE() {
        if (GoToCove.shouldEnableRun())
            Movement.toggleRun(true);

        if (OgressWrapper.CORSAIR_COVE_DUNGEON.contains(Players.getLocal()) ||
                OgressWrapper.CORSAIR_COVE[0].contains(Players.getLocal())) {

            Movement.walkTo(OgressWrapper.TOCK_BOAT_FROM_COVE_POSITION);
            handleObstacles();

        } else if (OgressWrapper.CORSAIR_COVE[1].contains(Players.getLocal())) {
            Npc tock = Npcs.getNearest("Captain Tock");
            if (tock != null) {
                if (!Dialog.isOpen()) {
                    tock.interact("Talk-to");
                } else {
                    Dialog.process(d -> d.contains("Rimmington"));
                    Dialog.processContinue();
                }
            }
        } else if (!Movement.walkToRandomized(BankLocation.GRAND_EXCHANGE.getPosition())) {
            handleObstacles();
        }
    }

    private int getQuantity(String item) {
        Progressive p = Config.getProgressive();
        int spending = coinsToSpend;

        if (p.isSplash() && p.isUseSplashGear() && Arrays.asList(SplashWrapper.getSplashGear(false)).contains(item)) {
            return 1;
        }
        if (p.isSplash() && p.getRunes().contains(item)) {
            return SplashWrapper.getSplashStartAmnt(p.isUseSplashGear()) / (getPrice(item) * p.getRunes().size());
        }
        if (spell.equals(Spell.Modern.WIND_STRIKE) && p.getRunes().contains(item)) {

            int amnt = spending / (getPrice(item) * p.getRunes().size());
            if (amnt <= 50) {
                return amnt;
            }
            return Random.mid(35, 50);
        }
        if (spell.equals(Spell.Modern.FIRE_STRIKE) && p.getRunes().contains(item) && !item.contains("staff")) {

            String staff = p.getEquipmentMap().get(EquipmentSlot.MAINHAND);

            if (items.contains(staff) && !GEWrapper.hasEquipment()) {
                spending -= (getPrice(staff) / p.getRunes().size());
            }

            int amnt = spending / (getPrice(item) * p.getRunes().size());

            if (amnt <= 2400) {
                return amnt;
            }
            return Random.mid(1600, 2400);
        }
        return 1;
    }

    private void equipEquipment(Progressive p) {
        HashSet<String> equip = new HashSet<>(p.getEquipmentMap().values());
        if (p.isSplash()) {
            equip.addAll(Arrays.asList(SplashWrapper.getSplashGear(false)));
        }
        for (String e : equip) {
            if (Inventory.contains(e)) {
                Inventory.getFirst(e).interact(a -> true);
                Time.sleepUntil(() -> Equipment.contains(e), 1000, 8000);
            }
        }
    }

    private int getPrice(String item) {
        if (item.equalsIgnoreCase("air rune") || item.equalsIgnoreCase("mind rune")) {
            return 6;
        }
        if (Arrays.asList(SplashWrapper.getSplashGear(false)).contains(item.toLowerCase())) {
            return 500;
        }
        if (item.contains("staff")) {
            return 2000;
        }

        //String upperName = itemToBuy.toUpperCase().charAt(0) + itemToBuy.substring(1);
        RSItemDefinition def = Definitions.getItem(item, x -> !x.isNoted() && x.isTradable());
        try {
            int price = ExPriceCheck.getOSBuddyBuyPrice(def.getId(), true);
            if (price < 1) {
                price = ExPriceCheck.getRSBuddyBuyPrice(def.getId(), true);
            }
            if (price < 1) {
                price = Inventory.getCount(true, 995) / 2;
            }
            return price + (int) (price * .15);
        } catch (Exception e) {
            Log.severe(e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    private void handleObstacles() {
        status = "Handling obstacles";
        SceneObject ladder = (OgressWrapper.CORSAIR_COVE_DUNGEON.contains(Players.getLocal())) ?
                SceneObjects.getNearest("Vine ladder") : null;
        SceneObject plank1 = SceneObjects.getFirstAt(new Position(2578, 2839, 0));
        SceneObject plank2 = SceneObjects.getFirstAt(new Position(2909, 3228, 1));

        if (ladder != null) {
            if (ladder.interact("Climb")) {
                Time.sleep(1500, 2000);
            } else {
                Movement.walkTo(ladder);
            }
        } else if (plank1 != null) {
            if (plank1.interact("Cross")) {
                Time.sleep(3000, 5000);
            } else {
                Movement.walkTo(plank1);
            }
        } else if (plank2 != null) {
            if (plank2.interact("Cross")) {
                Time.sleep(3000, 5000);
            } else {
                Movement.walkTo(plank2);
            }
        }
    }

    @Override
    public void onInvalid() {
        if (!GEWrapper.isSellItems()) {
            Log.fine("Done Restocking");
            doneRestockingHelper();
        }

        runesIterator = null;
        checkedBank = false;
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
