package script.fighter.nodes.ogress;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Pickable;
import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.local.Health;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Pickables;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;
import script.beg.StartOther;
import script.fighter.CombatStore;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.debug.Logger;
import script.fighter.framework.BackgroundTaskExecutor;
import script.fighter.framework.Node;
import script.fighter.models.NpcResult;
import script.fighter.wrappers.CombatWrapper;
import script.fighter.wrappers.GEWrapper;
import script.fighter.wrappers.OgressWrapper;

import java.time.Duration;

//@ScriptMeta(version = 2.11, name = "F2P Ogress Safespot killer", category = ScriptCategory.COMBAT, developer = "Larrysm", desc = "Best F2P money maker - Uses fire strike & alch items")
public class Ogress extends Node {

    private Fighter main;
    private boolean looting = false;
    private final Position LEFT_SAFESPOT = new Position(2011, 9002, 1);
    private final Area SEARCH_AREA = Area.rectangular(1998, 9000, 2025, 8993, 1);
    private final Area RIGHT_AREA = Area.rectangular(2015, 9000, 2016, 9002, 1);
    private final Position RIGHT_SAFESPOT = new Position(2014, 9003, 1);
    private String action = "Idle";
    private boolean running;
    private StopWatch worldHopTimer;

    @Override
    public boolean validate() {
        return OgressWrapper.CORSAIR_COVE_DUNGEON.contains(Players.getLocal());
    }

    @Override
    public int execute() {
        main.invalidateTask(this);
        running = true;
        Player me = Players.getLocal();
        Spell spell = Config.getProgressive().getSpell();
        if (worldHopTimer == null) {
            worldHopTimer = StopWatch.start();
        }

        if (!Magic.Autocast.isEnabled() || !Magic.Autocast.isSpellSelected(spell)) {
            Log.fine("Setting Auto-cast");
            Magic.Autocast.select(Magic.Autocast.Mode.DEFENSIVE, spell);
        }

        Item invBones = Inventory.getFirst("Bones");
        Npc ogreOnMe = Npcs.newQuery()
                .names("Ogress Warrior")
                .filter(n -> n.getTarget() != null && n.getTarget().equals(Players.getLocal()))
                .within(SEARCH_AREA).results().nearest();
        Npc ogreOnMe2 = Npcs.newQuery()
                .names("Ogress Warrior")
                .filter(n -> n.getTarget() != null && n.getTarget().equals(Players.getLocal()) && !n.equals(ogreOnMe))
                .within(SEARCH_AREA).results().nearest();
        Pickable loot = Pickables.newQuery()
                .names(Config.getLoot().toArray(new String[0]))
                .filter(p -> p.distance() < 20 && p.distance(LEFT_SAFESPOT) < 20 &&
                        (!p.getName().equalsIgnoreCase("coins") || p.getStackSize() > 100)).results().nearest();
        Npc Tzhaar2 = Npcs.newQuery()
                .names("Ogress Warrior")
                .filter(n -> n.getTargetIndex() == -1 || n.getTarget().equals(Players.getLocal()))
                .within(SEARCH_AREA).results()
                .sort((n1, n2) -> (int) (n1.distance() - n2.distance()))
                .sort((n1, n2) -> (n1.getTarget().equals(me) ? -1 : n2.getTarget().equals(me) ? 1 : 0))
                .first();
        Npc Tzhaar3 = Npcs.newQuery()
                .names("Ogress Warrior")
                .filter(n -> n.getTargetIndex() == -1 || n.getTarget().equals(Players.getLocal()))
                .within(RIGHT_AREA).results()
                .sort((n1, n2) -> (int) (n1.distance() - n2.distance()))
                .sort((n1, n2) -> (n1.getTarget().equals(me) ? -1 : n2.getTarget().equals(me) ? 1 : 0))
                .first();
                /*.names("Ogress Warrior")
                .filter(n -> n.getTargetIndex() == -1 || n.getTarget().equals(Players.getLocal()))
                .within(RIGHT_AREA).results().nearest();*/
        int numOgreOnMe = Npcs.newQuery()
                .names("Ogress Warrior")
                .filter(n -> n.getTargetIndex() != -1 && n.getTarget().equals(Players.getLocal()))
                .within(OgressWrapper.CORSAIR_COVE_DUNGEON).results().size();
        int numOtherPlayersSafespotting = Players.newQuery()
                .filter(p -> !p.equals(Players.getLocal()) && (RIGHT_AREA.contains(p.getPosition()) ||
                        RIGHT_SAFESPOT.equals(p.getPosition()) || LEFT_SAFESPOT.equals(p.getPosition())))
                .within(SEARCH_AREA).targeting().results().size();
        int numOtherPlayersSearchArea = Players.newQuery()
                .filter(p -> !p.equals(Players.getLocal()) &&
                        (RIGHT_AREA.contains(p.getPosition()) || RIGHT_SAFESPOT.equals(p.getPosition()) ||
                                LEFT_SAFESPOT.equals(p.getPosition()) || SEARCH_AREA.contains(p.getPosition())))
                .within(SEARCH_AREA).targeting().results().size();

        if (!Movement.isRunEnabled() && Movement.getRunEnergy() > Random.nextInt(5, 15))
            Movement.toggleRun(true);

        if (Dialog.canContinue())
            Dialog.processContinue();

        if (invBones != null)
            invBones.interact("Bury");
        if (!GEWrapper.hasRunes(Spell.Modern.FIRE_STRIKE)) {
            Logger.debug("Out of runes");
            Movement.walkTo(LEFT_SAFESPOT);
            return 5000;
        }

        if (loot != null && Health.getCurrent() > 8 && numOgreOnMe == 0 && !isPlayerTargeted(ogreOnMe, ogreOnMe2)) {

                looting = true;
                Log.fine("Looting: " + loot.getName());
                loot.interact("Take");
        }

        if (loot == null) {
            looting = false;
        }

        if (Skills.getCurrentLevel(Skill.MAGIC) > 54 && Inventory.contains("Nature rune") && Inventory.contains("Fire rune")) {
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
        if (RIGHT_AREA.contains(Tzhaar3) && !looting) {

            if (!RIGHT_SAFESPOT.equals(Players.getLocal().getPosition()) && Players.getLocal().getTargetIndex() == -1) {

                Movement.walkTo(RIGHT_SAFESPOT);
                Log.info("Walking to RIGHT Safespot");
                Time.sleep(800, 4050);
            }

            if (me.getTargetIndex() == -1) {
                if (Tzhaar3 != null) {
                    action = "Attacking";
                    if (!Players.getLocal().isMoving() && RIGHT_AREA.contains(Tzhaar3)) {
                        if (Tzhaar3.interact("Attack")) {
                            Log.info("Casting: " + spell.getName());
                            CombatStore.setCurrentTarget(new NpcResult(Tzhaar3, true));
                            Time.sleepUntil(Players.getLocal()::isAnimating, Random.nextInt(4000, 5000));
                        }
                    }
                } else {
                    if (me.getPosition().distance(LEFT_SAFESPOT) > 5) {
                        action = "Walking";
                        Log.info("Walking");
                        Movement.walkToRandomized(LEFT_SAFESPOT);
                    } else {
                        action = "Waiting for spawn";
                    }
                }
            } else {
                action = "Waiting/In combat";
            }

        } else if (!RIGHT_AREA.contains(Tzhaar2) && !looting) {

            if (!LEFT_SAFESPOT.equals(Players.getLocal().getPosition()) && Players.getLocal().getTargetIndex() == -1) {

                Movement.walkTo(LEFT_SAFESPOT);
                Log.info("Walking to LEFT Safespot");
                Time.sleep(1020, 4050);
            }

            if (me.getTargetIndex() == -1) {
                if (Tzhaar2 != null) {
                    action = "Attacking";
                    if (!Players.getLocal().isMoving() && SEARCH_AREA.contains(Tzhaar2)) {
                        if (Tzhaar2.interact("Attack")) {
                            Log.info("Casting: " + spell.getName());
                            CombatStore.setCurrentTarget(new NpcResult(Tzhaar2, true));
                            Time.sleepUntil(Players.getLocal()::isAnimating, Random.nextInt(4000, 6000));
                        }
                    }
                } else {
                    if (me.getPosition().distance(LEFT_SAFESPOT) > 5) {
                        action = "Walking";
                        Log.info("Walking");
                        Movement.walkToRandomized(LEFT_SAFESPOT);
                    } else {
                        action = "Waiting for spawn";
                    }
                }
            } else {
                action = "Waiting/In combat";
            }
        }

        if (!LEFT_SAFESPOT.equals(me.getPosition()) && !RIGHT_SAFESPOT.equals(me.getPosition()) &&
                (Health.getCurrent() <= 8 || isPlayerTargeted(ogreOnMe, ogreOnMe2))) {

            Log.info("Targeted / Low HP  |  Returning to safespot");
            looting = false;
            double dist1 = LEFT_SAFESPOT.distance();
            double dist2 = RIGHT_SAFESPOT.distance();
            if (dist1 <= dist2) {
                Movement.walkTo(LEFT_SAFESPOT);
            } else {
                Movement.walkTo(RIGHT_SAFESPOT);
            }
            if (!Movement.isRunEnabled()) {
                Movement.toggleRun(true);
            }
        }
        else {
            if (Players.getLocal().getTargetIndex() == -1 && worldHopTimer.exceeds(Duration.ofMinutes(5)) &&
                    (numOtherPlayersSafespotting >= 2 || numOtherPlayersSearchArea >= 4)) {

                Log.fine("World-Hopping");
                StartOther.hopToLowPopWorld(0, Worlds.getCurrent());
                worldHopTimer = StopWatch.start();
            }
        }

        return Random.nextInt(200, 800);
    }

    private boolean isPlayerTargeted(Npc potential1, Npc potential2) {
        return CombatWrapper.isTargetingMe(potential1) || CombatWrapper.isTargetingMe(potential2);
    }

    @Override
    public String status() {
        return action;
    }

    @Override
    public void onInvalid() {
        running = false;
        looting = false;
        worldHopTimer = null;
        CombatStore.resetTargetingValues();
        super.onInvalid();
    }

    private void findNextTarget() {
        if (!running)
            return;
        CombatStore.setNextTarget(CombatWrapper.findTarget(true));
    }

    public Ogress(Fighter main) {
        this.main = main;
        BackgroundTaskExecutor.submit(this::findNextTarget, 1000);
    }
}
