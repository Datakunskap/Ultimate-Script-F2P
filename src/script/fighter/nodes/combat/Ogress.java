package script.fighter.nodes.combat;

import org.rspeer.runetek.adapter.component.Item;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.Pickable;
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
import script.fighter.CombatStore;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.debug.Logger;
import script.fighter.framework.BackgroundTaskExecutor;
import script.fighter.framework.Node;
import script.fighter.services.PriceCheckService;
import script.fighter.wrappers.*;

import java.util.Comparator;

//@ScriptMeta(version = 2.11, name = "F2P Ogress Safespot killer", category = ScriptCategory.COMBAT, developer = "Larrysm", desc = "Best F2P money maker - Uses fire strike & alch items")
public class Ogress extends Node {

    private Fighter main;
    private boolean looting = false;
    private final Position LEFT_SAFESPOT = new Position(2011, 9002, 1);
    private final Area SEARCH_AREA = Area.rectangular(1998, 9000, 2025, 8993, 1);
    private final Area[] RIGHT_AREA = new Area[] {
            Area.rectangular(2015, 9000, 2016, 9002, 1),
            Area.rectangular(2016, 9002, 2014, 9000, 1),
            Area.rectangular(2016, 9002, 2013, 9000, 1) };
    private final Position RIGHT_SAFESPOT = new Position(2014, 9003, 1);
    private String action = "Idle";
    private boolean running;
    private String[] lootNames;
    private Position STUCK_POSITION = new Position(2014, 8999, 1);
    private int numOtherPlayersSafespotting;
    private int numOtherPlayersDungeon;

    public class IntegerComparator implements Comparator<Integer> {

        @Override
        public int compare(Integer o1, Integer o2) {
            return o2.compareTo(o1);
        }
    }

    @Override
    public boolean validate() {
        return OgressWrapper.CORSAIR_COVE_DUNGEON.contains(Players.getLocal());
    }

    @Override
    public int execute() {
        main.invalidateTask(this);
        running = true;
        if (lootNames == null) {
            lootNames = Config.getProgressive().getLoot().toArray(new String[0]);
            WorldhopWrapper.resetChecker();
        }

        if (!LEFT_SAFESPOT.equals(Players.getLocal().getPosition()) && !RIGHT_SAFESPOT.equals(Players.getLocal().getPosition()) && !shouldLoot()) {
            returnSafeSpot();
        }

        Spell spell = Config.getProgressive().getSpell();

        if (Random.nextInt(0, 750) == 1) {
            OgressWrapper.openRandomTab();
        }

        if (!Magic.Autocast.isEnabled() || !Magic.Autocast.isSpellSelected(spell)) {
            Log.fine("Setting Auto-cast");
            Magic.Autocast.select(Magic.Autocast.Mode.OFFENSIVE, spell);
        }

        if (!Movement.isRunEnabled() && Movement.getRunEnergy() > Random.nextInt(5, 15))
            Movement.toggleRun(true);

        if (Dialog.canContinue())
            Dialog.processContinue();

        Item invBones = Inventory.getFirst("Bones");
        if (invBones != null) {
            invBones.interact("Bury");
        }
        if (!GEWrapper.hasRunes(Config.getProgressive().getSpell())) {
            Logger.debug("Out of runes");
            Movement.walkTo(LEFT_SAFESPOT);
            return 5000;
        }

        Pickable loot = getLoot();
        if (loot != null && shouldLoot()) {
            if (loot.interact("Take")) {
                Log.fine("Looting: " + loot.getName());
                looting = true;
            }
        }

        BankWrapper.updateInventoryValue();

        if (loot == null) {
            looting = false;
        }

        if (Skills.getCurrentLevel(Skill.MAGIC) > 54 && Inventory.contains("Nature rune") && Inventory.contains("Fire rune")) {
            if (Inventory.contains("Rune med helm")) {
                Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY, Inventory.getFirst("Rune med helm"));
                Time.sleep(400, 3000);
                Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY, Inventory.getFirst("Rune med helm"));
                Time.sleep(500, 3000);
                BankWrapper.updateInventoryValue();
                OgressWrapper.itemsAlched++;
            }
            if (Inventory.contains("Rune full helm")) {
                Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY, Inventory.getFirst("Rune full helm"));
                Time.sleep(400, 3000);
                Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY, Inventory.getFirst("Rune full helm"));
                Time.sleep(500, 3000);
                BankWrapper.updateInventoryValue();
                OgressWrapper.itemsAlched++;
            }
            if (Inventory.contains("Rune battleaxe")) {
                Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY, Inventory.getFirst("Rune battleaxe"));
                Time.sleep(400, 3000);
                Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY, Inventory.getFirst("Rune battleaxe"));
                Time.sleep(500, 3000);
                BankWrapper.updateInventoryValue();
                OgressWrapper.itemsAlched++;
            }
            if (Inventory.contains("Mithril kiteshield")) {
                Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY, Inventory.getFirst("Mithril kiteshield"));
                Time.sleep(400, 3000);
                Magic.cast(Spell.Modern.HIGH_LEVEL_ALCHEMY, Inventory.getFirst("Mithril kiteshield"));
                Time.sleep(500, 3000);
                BankWrapper.updateInventoryValue();
                OgressWrapper.itemsAlched++;
            }

        }

        Npc Tzhaar2 = Npcs.newQuery()
                .names("Ogress Warrior")
                .filter(n -> n.getTargetIndex() == -1 || n.getTarget().equals(Players.getLocal()))
                .within(SEARCH_AREA).results().nearest();
        Npc Tzhaar3 = Npcs.newQuery()
                .names("Ogress Warrior")
                .filter(n -> n.getTargetIndex() == -1 || n.getTarget().equals(Players.getLocal()))
                .within(RIGHT_AREA[0]).results().nearest();

        if (RIGHT_AREA[0].contains(Tzhaar3) && !looting) {

            if (!RIGHT_SAFESPOT.equals(Players.getLocal().getPosition())) {
                Movement.walkTo(RIGHT_SAFESPOT);
                Time.sleepUntil(() -> Players.getLocal().getPosition().equals(RIGHT_SAFESPOT) || !Players.getLocal().isMoving(), 4050);
                if (getTargetingMe().length == 0 && (getLoot() == null || !shouldLoot())) {
                    Time.sleep(600, 4050);
                }
            }

            loot = getLoot();
            if (loot != null && shouldLoot()) {
                if (loot.interact("Take")) {
                    Log.fine("Looting: " + loot.getName());
                    looting = true;
                    return Fighter.getLoopReturn();
                }
            }

            if (Players.getLocal().getTargetIndex() == -1) {
                if (Tzhaar3 != null && !isDead(Tzhaar3)) {
                    action = "Attacking";
                    if (!Players.getLocal().isMoving() && RIGHT_AREA[0].contains(Tzhaar3)) {
                        if (Health.getCurrent() > 8  || (!Tzhaar3.isMoving() && Tzhaar3.distance() <= 3)) {
                            Log.info("Casting: " + spell.getName());
                            if (Tzhaar3.interact("Attack")) {
                                Time.sleepUntil(Players.getLocal()::isAnimating, Random.nextInt(4000, 5000));
                            }
                        } else {
                            action = "Healing";
                        }
                    }
                } else {
                    if (Players.getLocal().getPosition().distance(LEFT_SAFESPOT) > 5) {
                        action = "Walking";
                        Log.fine("Walking");
                        Movement.walkToRandomized(LEFT_SAFESPOT);
                    } else {
                        action = "Waiting for spawn";
                    }
                }
            } else {
                action = "Waiting/In combat";
                if (Prayers.getPoints() >= 3 && Prayers.isUnlocked(Prayer.MYSTIC_WILL) && !Prayers.isActive(Prayer.MYSTIC_WILL)) {
                    Prayers.toggle(true, Prayer.MYSTIC_WILL);
                }
            }

        } else if (!RIGHT_AREA[0].contains(Tzhaar2) && !looting) {

            if (!LEFT_SAFESPOT.equals(Players.getLocal().getPosition())) {
                Movement.walkTo(LEFT_SAFESPOT);
                Time.sleepUntil(() -> Players.getLocal().getPosition().equals(LEFT_SAFESPOT) || !Players.getLocal().isMoving(), 4050);
                if (getTargetingMe().length == 0 && (getLoot() == null || !shouldLoot())) {
                    Time.sleep(600, 4050);
                }
            }

            loot = getLoot();
            if (loot != null && shouldLoot()) {
                if (loot.interact("Take")) {
                    Log.fine("Looting: " + loot.getName());
                    looting = true;
                    return Fighter.getLoopReturn();
                }
            }

            if (Players.getLocal().getTargetIndex() == -1) {
                if (Tzhaar2 != null && !isDead(Tzhaar2)) {
                    action = "Attacking";
                    if (!Players.getLocal().isMoving() && SEARCH_AREA.contains(Tzhaar2)) {
                        if (Health.getCurrent() > 8 || (!Tzhaar2.isMoving() && Tzhaar2.distance() <= 3)) {
                            Log.info("Casting: " + spell.getName());
                            if (Tzhaar2.interact("Attack")) {
                                Time.sleepUntil(Players.getLocal()::isAnimating, Random.nextInt(4000, 6000));
                            }
                        } else {
                            action = "Healing";
                        }
                    }
                } else {
                    if (Players.getLocal().getPosition().distance(LEFT_SAFESPOT) > 5) {
                        action = "Walking";
                        Log.fine("Walking");
                        Movement.walkToRandomized(LEFT_SAFESPOT);
                    } else {
                        action = "Waiting for spawn";
                    }
                }
            } else {
                action = "Waiting/In combat";
                if (Prayers.getPoints() >= 3 && Prayers.isUnlocked(Prayer.MYSTIC_WILL) && !Prayers.isActive(Prayer.MYSTIC_WILL)) {
                    Prayers.toggle(true, Prayer.MYSTIC_WILL);
                }
            }
        }

        if (!LEFT_SAFESPOT.equals(Players.getLocal().getPosition()) && !RIGHT_SAFESPOT.equals(Players.getLocal().getPosition()) && !shouldLoot()) {
            returnSafeSpot();
        }
        else if (!looting && Health.getCurrent() > 8) {
            WorldhopWrapper.checkWorldhop(true);
        }

        return Fighter.getLoopReturn();
    }

    private void returnSafeSpot() {Log.severe("Targeted / Low HP  |  Returning to safespot");
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
        Time.sleepUntil(() -> Players.getLocal().isMoving(), 1500);
        Time.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);

    }

    private Pickable getLoot() {
        return Pickables.newQuery()
                .names(lootNames)
                .within(LEFT_SAFESPOT, 20)
                .results()
                .sortByDistance()
                .sort(Comparator.comparingInt(l -> (PriceCheckService.getPrice(l.getId()).getSellAverage() * l.getStackSize())))
                .last();
    }

    private boolean shouldLoot() {
        int health = Health.getCurrent();
        int numTargetingMe = getTargetingMe().length;

        return health > 8 && (numTargetingMe <= 0 || (health > 16 && numTargetingMe <= 1));
    }

    public static boolean isDead(Npc npc) {
        return npc == null || Npcs.getAt(npc.getIndex()) == null || npc.getHealthPercent() == 0;
    }

    private boolean isTargetStuck(Npc target) {
        return STUCK_POSITION.equals(target.getPosition()) && !target.isMoving();
    }

    private Npc[] getTargetingMe() {
        return Npcs.getLoaded(n -> n.getTarget() != null && n.getTarget().equals(Players.getLocal()));
        //return CombatStore.getTargetingMe().size() > 0;
    }

    @Override
    public String status() {
        return action;
    }

    @Override
    public void onInvalid() {
        running = false;
        looting = false;
        lootNames = null;
        PriceCheckService.purgeFailedPriceCache();
        WorldhopWrapper.resetChecker();
        CombatStore.resetTargetingValues();
        super.onInvalid();
    }

    private void fightNodeTargetChecking() {
       /* if(result != null && !CombatStore.hasTarget()) {
            doAttack(result.getNpc());
            return Fighter.getLoopReturn();
        }
        NpcResult target = CombatStore.getCurrentTarget();
        if(target == null || CombatWrapper.isDead(target.getNpc())) {
            status = "Target has died.";
            CombatStore.setCurrentTarget(null);
            return Fighter.getLoopReturn();
        }
        if(!CombatWrapper.isTargetingMe(target.getNpc())) {
            Logger.debug("Our current target is not targeting me.");
            if(CombatStore.getTargetingMe().size() > 0) {
                status = "Switching to target that is targeting me.";
                Npc first = CombatStore.getTargetingMe().stream().filter(n -> {
                    PathingEntity npcsTarget = n.getTarget();
                    return npcsTarget != null && npcsTarget.equals(Players.getLocal()) && n.getIndex() != target.getNpc().getIndex();
                }).findFirst().orElse(null);
                if(first == null) {
                    Logger.debug("Targeting me first is null, grabbing next.");
                    Npc next = CombatStore.getTargetingMe().iterator().next();
                    CombatStore.setCurrentTarget(new NpcResult(next, true));
                    doAttack(next);
                } else {
                    Logger.debug("Changing target to: " + first.getIndex());
                    CombatStore.setCurrentTarget(new NpcResult(first, true));
                    doAttack(first);
                }
                return Fighter.getLoopReturn();
            }
            doAttack(target.getNpc());
        }
        else if(spell != null) {
            doAttack(target.getNpc());
        }*/
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
