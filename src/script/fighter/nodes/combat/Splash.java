package script.fighter.nodes.combat;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Shop;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.debug.Logger;
import script.fighter.framework.Node;
import script.fighter.wrappers.GEWrapper;
import script.fighter.wrappers.OgressWrapper;
import script.fighter.wrappers.SplashWrapper;

public class Splash extends Node {

    private Fighter main;
    private static final int STOP_LVL = 13;
    private String status;
    private Spell spell;
    private static boolean shiftPosition;
    private int lvl;

    public Splash(Fighter main) {
        this.main = main;
    }

    @Override
    public boolean validate() {
        spell = Config.getProgressive().getSpell();

        return Config.getProgressive().isSplash() && GEWrapper.hasRunes(spell);
    }

    @Override
    public int execute() {
        invalidateTask(main.getActive());

        if (!Equipment.contains(SplashWrapper.getSplashGear(true)[0])) {
            status = "Getting staff";
            getStaff();
            return Random.high(600, 1600);
        }

        if (!SplashWrapper.getSplashArea().contains(Players.getLocal())) {
            status = "Walking to splash area";
            if (BackToFightZone.shouldEnableRun()) {
                BackToFightZone.enableRun();
            }
            Movement.walkTo(SplashWrapper.getSplashArea().getCenter());
            return Random.high(600, 1600);
        }

        if (Dialog.canContinue()) {
            Dialog.processContinue();
            if (lvl != Skills.getLevel(Skill.MAGIC)) {
                lvl = Skills.getLevel(Skill.MAGIC);
                Log.fine("Magic LVL: " + lvl);
            }
            if (lvl >= STOP_LVL) {
                SplashWrapper.setSplash(false);
            }
        }

        if (!Magic.Autocast.isEnabled() || !Magic.Autocast.isSpellSelected(spell)) {
            Log.info("Setting autocast");
            if (SplashWrapper.hasSplashGear(true)) {
                Magic.Autocast.select(Magic.Autocast.Mode.OFFENSIVE, spell);
            } else {
                Magic.Autocast.select(Magic.Autocast.Mode.DEFENSIVE, spell);
            }
        }

        Npc npc = Npcs.getNearest(n -> Config.getProgressive().getEnemies().contains(n.getName().toLowerCase()));
        if (npc != null && Players.getLocal().getTargetIndex() == -1 && !Players.getLocal().isAnimating()) {
            Log.info("Manual cast");
            status = "Splashing: " + npc.getName();
            if (!npc.interact("Attack") || shiftPosition) {
                Log.info("Shifting position");
                Movement.walkTo(Players.getLocal().getPosition().translate(Random.nextInt(-1, 1), Random.nextInt(-1, 1)));
                shiftPosition = false;
            }
        } else if (npc == null){
            Log.severe("Cant Find Npc");
        }

        return Random.high(2000, 5000);
    }

    private void getStaff() {
        Area market = SplashWrapper.DRAYNOR_MARKET_AREA;
        Npc django = Npcs.getNearest("Diango");
        String staff = SplashWrapper.getSplashGear(true)[0];

        if (Inventory.contains(staff)) {
            if (Shop.isOpen()) {
                Shop.close();
            } else {
                Inventory.getFirst(staff).interact(a -> true);
            }
        }
        else if (Shop.isOpen()) {
            Shop.buyOne(staff);
        }
        else if (django != null && django.isPositionInteractable()) {
            django.interact("Trade");
        }
        else if (django != null && django.isPositionWalkable()) {
            Movement.walkTo(django);
        }
        else if (!market.contains(Players.getLocal())) {
            Movement.walkTo(market.getCenter());
        }
    }

    @Override
    public void onInvalid() {
        if (SplashWrapper.hasSplashGear(false)) {
            Tabs.open(Tab.EQUIPMENT);
            Time.sleepUntil(() -> Tabs.getOpen() == Tab.EQUIPMENT, 1000, 5000);
            OgressWrapper.unequipAll(false);
        }
        super.onInvalid();
    }

    @Override
    public String status() {
        return status;
    }

    public void invalidateTask(Node active) {
        if (active != null && !this.equals(active)) {
            Logger.debug("Node has changed.");
            active.onInvalid();
        }
        main.setActive(this);
    }

    public static void setShiftPosition(boolean shift) {
        shiftPosition = shift;
    }

}
