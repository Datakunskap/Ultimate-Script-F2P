package script.fighter.nodes.restock;

import api.component.ExPriceCheck;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Definitions;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.GrandExchange;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.movement.Movement;
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
import script.fighter.wrappers.BankWrapper;
import script.fighter.wrappers.GEWrapper;
import script.tanner.ExGrandExchange;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class BuyGE extends Node {

    private Fighter main;
    private String status;
    private Iterator<String> runesIterator;
    private int quantity;
    private String itemToBuy;
    private Spell spell;
    private boolean checkedBank;

    public BuyGE(Fighter main) {
        this.main = main;
    }

    @Override
    public boolean validate() {
        if (runesIterator != null || GEWrapper.itemsStillActive(RSGrandExchangeOffer.Type.BUY))
            return true;

        Progressive p = Config.getProgressive();
        HashSet<String> items = p.getRunes();
        spell = p.getSpell();

        if (spell != null && Inventory.contains(i -> i.getName().equals("Coins") &&
                i.getStackSize() >= Config.getSplashStartAmnt(true)) && Skills.getLevel(Skill.MAGIC) < 13) {
            Log.fine("Splash Time!");
            p.setSplash(true);
        }

        if (items != null && items.size() > 0 && spell != null &&
                (!Config.hasRunes() || !Config.hasEquipment() ||
                        (p.isSplash() && !Config.hasSplashGear(false)))) {

            Log.fine("Restocking");
            if (p.isSplash() && !Config.hasSplashGear(false)) {
                items.addAll(Arrays.asList(Config.getSplashGear(false)));
            } else {
                items.addAll(p.getEquipmentMap().values());
            }
            runesIterator = items.iterator();
            itemToBuy = runesIterator.next();
            quantity = getQuantity(p, itemToBuy);
            return true;
        }

        return false;
    }

    @Override
    public int execute() {
        invalidateTask(main.getActive());

        if (!Location.GE_AREA.getBegArea().contains(Players.getLocal())) {
            status = "Walking to GE";
            if (!GEWrapper.isSellItems()) {
                Log.fine("Selling Loot");
                GEWrapper.setSellItems(true);
            }
            if (!Movement.walkToRandomized(BankLocation.GRAND_EXCHANGE.getPosition())) {
                handleObstacles();
            }
            return Fighter.getLoopReturn();
        }

        // check GP
        if (runesIterator != null && !GEWrapper.itemsStillActive(RSGrandExchangeOffer.Type.BUY)) {
            if (!Inventory.contains(i -> i.getName().equals("Coins") && i.getStackSize() >= (getPrice() * quantity))) {
                if (!checkedBank) {
                    Log.info(itemToBuy + " :Need "  + (getPrice() * quantity) + " --> Checking bank");
                    BankWrapper.openAndDepositAll(true, Config.getProgressive().getRunes().toArray(new String[0]));
                    checkedBank = true;
                } else {
                    Log.info(itemToBuy + " :Need "  + (getPrice() * quantity) + " --> Selling Items");
                    GEWrapper.setSellItems(true);
                }
                return Fighter.getLoopReturn();
            }
        }

        if (!GrandExchange.isOpen()) {
            status = "Restocking";
            GEWrapper.openGE();
            return Fighter.getLoopReturn();
        }

        if (runesIterator != null && !GEWrapper.itemsStillActive(RSGrandExchangeOffer.Type.BUY)) {
            if (!Inventory.contains(itemToBuy) || Inventory.getCount(true, itemToBuy) < quantity) {
                if (ExGrandExchange.buy(itemToBuy, quantity, getPrice(), false)) {
                    if (Time.sleepUntil(() -> GrandExchange.getFirst(x -> x.getItemName().toLowerCase().equals(itemToBuy)) != null, 1000, 8000)) {
                        Logger.debug("Buying: " + itemToBuy);
                        if (runesIterator.hasNext()) {
                            itemToBuy = runesIterator.next();
                        } else {
                            runesIterator = null;
                        }
                    }
                }
            } else {
                Log.info("Already has " + itemToBuy);
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
            Time.sleep(Random.mid(300, 600));
            Keyboard.pressEnter();
        }

        if (!GEWrapper.itemsStillActive(RSGrandExchangeOffer.Type.BUY) && runesIterator == null) {
            GEWrapper.closeGE();
            Progressive p = Config.getProgressive();
            equipEquipment(p);
            BankWrapper.openAndDepositAll(true, Config.getProgressive().getRunes().toArray(new String[0]));
            Log.fine("Done restocking");
            if (p.getEnemies().contains("chicken") || p.getEnemies().contains("lesser demon")) {
                GEWrapper.teleportHome();
            }
        }

        return Fighter.getLoopReturn();
    }

    private int getQuantity(Progressive p, String item) {
        if (p.isSplash() && p.getRunes().contains(item)) {
            return Config.getSplashStartAmnt(false) / (getPrice() * 2);
        }
        if (p.isSplash() && Arrays.asList(Config.getSplashGear(false)).contains(item)) {
            return 1;
        }
        if (spell.equals(Spell.Modern.WIND_STRIKE) && p.getRunes().contains(item)) {
            int amnt = Inventory.getCount(true, "Coins") / (getPrice() * 2);
            if (amnt <= 50) {
                return amnt;
            }
            return Random.mid(35, 50);
        }
        if (spell.equals(Spell.Modern.FIRE_STRIKE) && p.getRunes().contains(item)) {
            return Random.low(300, 400);
        }
        return 1;
    }

    /*private void equipSet(Progressive p) {
        Item set = Inventory.getFirst(x -> x.getName().toLowerCase().contains("set"));
        Npc clerk = Npcs.getNearest(x -> x.getName().contains("Grand Exchange Clerk"));
        if (set != null && clerk != null) {
            Time.sleepUntil(() -> clerk.interact("Sets"), 1000, 10000);
            Time.sleep(700, 1300);

        }
    }*/

    private void equipEquipment(Progressive p) {
        Collection<String> equip = p.getEquipmentMap().values();
        if (p.isSplash()) {
            equip.addAll(Arrays.asList(Config.getSplashGear(false)));
        }
        for (String e : equip) {
            if (Inventory.contains(e)) {
                Inventory.getFirst(e).interact(a -> true);
                Time.sleepUntil(() -> Equipment.contains(e), 1000, 8000);
            }
        }
    }

    private int getPrice() {
        if (itemToBuy.equalsIgnoreCase("air rune") || itemToBuy.equalsIgnoreCase("mind rune")) {
            return 6;
        }
        if (Arrays.asList(Config.getSplashGear(false)).contains(itemToBuy.toLowerCase())) {
            return 500;
        }

        //String upperName = itemToBuy.toUpperCase().charAt(0) + itemToBuy.substring(1);
        RSItemDefinition item = Definitions.getItem(itemToBuy, x -> !x.isNoted());
        try {
            int price = ExPriceCheck.getOSBuddyBuyPrice(item.getId(), true);
            if (price < 1) {
                price = ExPriceCheck.getRSBuddyBuyPrice(item.getId(), true);
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
        SceneObject stairs = SceneObjects.getNearest("Staircase");
        if (stairs != null) {
            if (stairs.interact("Climb-down")) {
                Logger.debug("Climbing stairs");
            } else if (stairs.getPosition().randomize(3).isPositionWalkable()) {
                Movement.walkTo(stairs);
            } else {
                SceneObject door = SceneObjects.getNearest("Door");
                if (door != null && door.isPositionInteractable()) {
                    door.interact("Open");
                }
            }
        }
    }

    @Override
    public void onInvalid() {
        runesIterator = null;
        checkedBank = false;
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
