package script.fighter.nodes.ogress;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Pickable;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Pickables;
import org.rspeer.runetek.api.scene.Players;
import script.fighter.Fighter;
import script.fighter.debug.Logger;
import script.fighter.framework.Node;
import script.fighter.wrappers.OgressWrapper;

//@ScriptMeta(version = 2.11, name = "F2P Ogress Safespot killer", category = ScriptCategory.COMBAT, developer = "Larrysm", desc = "Best F2P money maker - Uses fire strike & alch items")
public class Ogress extends Node {

    private Fighter main;
    private boolean looting = false;
    private final Position docks = new Position(2011, 9002 , 1);
    private final Area docks2 = Area.rectangular(1998, 9000, 2025, 8993, 1);
    private final Area docks3 = Area.rectangular(2015, 9000, 2016, 9002, 1);
    private final Position docks4 = new Position(2014, 9003, 1);
    private String action = "Idle";

    @Override
    public boolean validate() {
        return OgressWrapper.CORSAIR_COVE_DUNGEON.contains(Players.getLocal());
    }

    @Override
    public int execute() {
        main.invalidateTask(this);
        Player me = Players.getLocal();

        Npc Tzhaar = Npcs.getNearest(x -> x.getName().equals("Ogress Warrior") && (x.getTargetIndex() == -1 || x.getTarget().equals(me)) && x.getHealthPercent() > 0 && docks2.contains(x));
        Pickable loot = Pickables.newQuery().names("Iron arrow","Steel arrow","Mithril arrow","Rune med helm","Rune full helm","Rune battleaxe","Shaman mask","Air rune",
                "Mind rune","Water rune","Earth rune","Fire rune","Chaos rune","Cosmic rune","Nature rune","Law rune","Death rune","Ranarr seed","Snapdragon seed","Torstol seed",
                "Cadantine seed","Snape grass seed", "Mithril kiteshield").filter(p -> p.distance() < 20 && p.distance(docks) < 20).results().nearest();
        Item invBones = Inventory.getFirst("Bones");
        Npc Tzhaar2 = Npcs.newQuery().names("Ogress Warrior").within(docks2).results().nearest();
        Npc Tzhaar3 = Npcs.newQuery().names("Ogress Warrior").within(docks3).results().nearest();
        if (!Movement.isRunEnabled() && Movement.getRunEnergy() > Random.nextInt(5, 15))
            Movement.toggleRun(true);

        if (Dialog.canContinue())
            Dialog.processContinue();

        if (invBones != null)
            invBones.interact("Bury");
        if(Inventory.getCount(true, "Air rune") < 2 ||
                Inventory.getCount(true, "Mind rune") < 1 ){
            Logger.debug("Out of runes");
            Movement.walkTo(docks);
            return 5000;
        }

        if (loot != null && !Inventory.isFull()) {
            looting = true;
            loot.interact("Take");
        }
        if(loot == null){
            looting = false;
        }
        if(Skills.getCurrentLevel(Skill.MAGIC) > 54 && Inventory.contains("Nature rune") && Inventory.contains("Fire rune")) {
            if (Inventory.contains("Rune med helm")) {
                Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY, Inventory.getFirst("Rune med helm"));
                Time.sleep(400, 3000);
                Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY, Inventory.getFirst("Rune med helm"));
                Time.sleep(500, 3000);
            }
            if (Inventory.contains("Rune full helm")) {
                Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY, Inventory.getFirst("Rune full helm"));
                Time.sleep(400, 3000);
                Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY, Inventory.getFirst("Rune full helm"));
                Time.sleep(500, 3000);
            }
            if (Inventory.contains("Rune battleaxe")) {
                Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY, Inventory.getFirst("Rune battleaxe"));
                Time.sleep(400, 3000);
                Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY, Inventory.getFirst("Rune battleaxe"));
                Time.sleep(500, 3000);
            }
            if (Inventory.contains("Mithril kiteshield")) {
                Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY, Inventory.getFirst("Mithril kiteshield"));
                Time.sleep(400, 3000);
                Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY, Inventory.getFirst("Mithril kiteshield"));
                Time.sleep(500, 3000);
            }

        }
        if (docks3.contains(Tzhaar3)  && !looting){
            if (!docks4.equals(me.getPosition())) {
                Movement.walkTo(docks4);
                Time.sleep(800, 4050);
            }

            if (me.getTargetIndex() == -1) {
                if (Tzhaar3 != null) {
                    action = "Attacking";
                    if (!me.isMoving() && docks3.contains(Tzhaar3)){
                        Tzhaar3.interact("Attack");
                        Time.sleepUntil(me::isAnimating, Random.nextInt(4000, 5000));
                    }
                }

                else {
                    if (me.getPosition().distance(docks) > 5) {
                        action = "Walking";
                        Movement.walkToRandomized(docks);
                    } else {
                        action = "Waiting for spawn";
                    }
                }
            } else {
                action = "Waiting/In combat";
            }
        }

        else if(!docks3.contains(Tzhaar2) && looting == false){
            if (!docks.equals(me.getPosition())) {
                Movement.walkTo(docks);
                Time.sleep(1020, 4050);
            }

            if (me.getTargetIndex() == -1) {
                if (Tzhaar2 != null) {
                    action = "Attacking";
                    if (!me.isMoving() && docks2.contains(Tzhaar2)){
                        Tzhaar2.interact("Attack");
                        Time.sleepUntil(me::isAnimating, Random.nextInt(4000, 6000));
                    }
                }

                else {
                    if (me.getPosition().distance(docks) > 5) {
                        action = "Walking";
                        Movement.walkToRandomized(docks);
                    } else {
                        action = "Waiting for spawn";
                    }
                }
            } else {
                action = "Waiting/In combat";
            }
        }
        return Random.nextInt(500, 1000);
    }

    @Override
    public String status() {
        return action;
    }

    public Ogress(Fighter main){
        this.main = main;
    }
}
