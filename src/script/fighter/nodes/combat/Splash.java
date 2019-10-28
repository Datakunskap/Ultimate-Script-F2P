package script.fighter.nodes.combat;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.Game;
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
import script.Script;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.framework.Node;
import script.fighter.models.Progressive;
import script.fighter.wrappers.*;

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
        if (Script.SPLASH_USE_EQUIPMENT && !GEWrapper.hasEquipment()) {
            return false;
        }

        return Config.getProgressive().isSplash() && GEWrapper.hasRunes(spell);
    }

    @Override
    public int execute() {
        main.invalidateTask(this);

        if (!Game.isLoggedIn() || Players.getLocal() == null) {
            return 1000;
        }

        Progressive p = Config.getProgressive();
        if (p.isUseSplashGear()) {
            if (!Equipment.containsAll(p.getEquipmentMap().values().toArray(new String[0]))) {
                SplashWrapper.equipEquipment();
            }
        }

        if (!Equipment.contains(SplashWrapper.getStaff())) {
            status = "Getting staff";
            getStaff();
            TeleportWrapper.tryTeleport(true);
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
        }

        if (lvl != Skills.getLevel(Skill.MAGIC)) {
            lvl = Skills.getLevel(Skill.MAGIC);
            Log.fine("Magic LVL: " + lvl);
        }

        if (lvl >= STOP_LVL) {
            p.setSplash(false);
            TeleportWrapper.tryTeleport(false);
        }

        if (!Magic.Autocast.isEnabled() || !Magic.Autocast.isSpellSelected(spell)) {
            Log.info("Setting autocast");
            if (GEWrapper.hasEquipment()) {
                Magic.Autocast.select(Magic.Autocast.Mode.OFFENSIVE, spell);
            } else {
                Magic.Autocast.select(Magic.Autocast.Mode.DEFENSIVE, spell);
            }
        }

        Npc npc = Npcs.getNearest(n -> Config.getProgressive().getEnemies().contains(n.getName().toLowerCase())
                && (n.getTargetIndex() == -1 || (n.getTarget() != null && n.getTarget().equals(Players.getLocal()))));

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
            WorldhopWrapper.checkWorldhop(false);
        }

        return Random.high(2000, 5000);
    }

    private void getStaff() {
        Area market = SplashWrapper.DRAYNOR_MARKET_AREA;
        Npc django = Npcs.getNearest("Diango");
        String staff = SplashWrapper.getStaff();

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
        if (!Config.getProgressive().isSplash() && GEWrapper.hasEquipment()) {
            Tabs.open(Tab.EQUIPMENT);
            Time.sleepUntil(() -> Tabs.getOpen() == Tab.EQUIPMENT, 1000, 5000);
            OgressWrapper.unequipAll(false);
        }

        WorldhopWrapper.resetChecker();
        super.onInvalid();
    }

    @Override
    public String status() {
        return status;
    }

    public static void setShiftPosition(boolean shift) {
        shiftPosition = shift;
    }

}
