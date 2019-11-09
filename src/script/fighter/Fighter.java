package script.fighter;

import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.*;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.Projection;
import org.rspeer.runetek.event.types.ChatMessageEvent;
import org.rspeer.runetek.event.types.DeathEvent;
import org.rspeer.runetek.event.types.RenderEvent;
import org.rspeer.runetek.event.types.TargetEvent;
import org.rspeer.runetek.providers.subclass.GameCanvas;
import org.rspeer.ui.Log;
import script.Script;
import script.fighter.backgroundTasks.TargetChecker;
import script.fighter.config.Config;
import script.fighter.config.ProgressiveSet;
import script.fighter.debug.LogLevel;
import script.fighter.debug.Logger;
import script.fighter.framework.BackgroundTaskExecutor;
import script.fighter.framework.Node;
import script.fighter.framework.NodeManager;
import script.fighter.models.Progressive;
import script.fighter.nodes.combat.CombatListener;
import script.fighter.paint.CombatPaintRenderer;
import script.fighter.paint.ScriptPaint;
import script.fighter.wrappers.BankWrapper;
import script.fighter.wrappers.OgressWrapper;
import script.fighter.wrappers.SplashWrapper;
import script.fighter.wrappers.WorldhopWrapper;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

//@ScriptMeta(name = "Pro Fighter TaskScript", desc = "RSPeer Official AIO Fighter", developer = "MadDev", category = ScriptCategory.COMBAT)
public class Fighter {

    private NodeManager manager;
    private static StopWatch runtime;
    private long stopTimeMs;
    private long startTimeMs;
    private Script script;
    private ScriptPaint paint;

    public Fighter(Script script, long stopTimeMs) {
        this.script = script;
        this.stopTimeMs = stopTimeMs;
    }

    public Fighter(Script script) {
        stopTimeMs = Long.MAX_VALUE;
        this.script = script;
    }

    public Script getScript() {
        return script;
    }

    public static int getLoopReturn() {
        return Random.high(200, 1000);
    }

    public static StopWatch getRuntime() {
        return runtime;
    }

    public NodeManager getManager() {
        return manager;
    }

    public NodeSupplier supplier;

    public NodeSupplier getSupplier() {
        return supplier;
    }

    public void onStart(boolean isOgress, int retries) {
        script.isFighterRunning = true;
        if (isOgress) {
            setupSplashProgressive();
            setupOgressProgressive();

            BankWrapper.updateInventoryValue();
        } else {
            setupDefaultProgressive("chicken");
            //setupMagicProgressive();
        }

        Config.setLogLevel(LogLevel.Debug);
        supplier = new NodeSupplier(this, isOgress);
        manager = new NodeManager();

        runtime = StopWatch.start();
        startTimeMs = System.currentTimeMillis();
        paint = new ScriptPaint(this);
        if (!isOgress)
            setBackgroundTasks();
        active = null;
        setupNodes(isOgress);

        if (!GameCanvas.isInputEnabled()) {
            GameCanvas.setInputEnabled(true);
        }

    }

    private void setupNodes(boolean isOgress) {
        if (isOgress) {
            script.submit(
                    supplier.MULE,
                    supplier.SELL_GE,
                    supplier.BUY_GE,
                    supplier.DEPOSIT_LOOT,
                    supplier.OGRESS,
                    supplier.SPLASH,
                    supplier.GO_TO_COVE);
        } else {
            script.submit(
                    supplier.EAT,
                    supplier.GET_FOOD,
                    supplier.DEPOSIT_LOOT,
                    supplier.LOOT,
                    supplier.PROGRESSION_CHECKER,
                    supplier.BURY_BONES,
                    supplier.IDLE,
                    supplier.SELL_GE,
                    supplier.BUY_GE,
                    supplier.FIGHT,
                    supplier.BACK_TO_FIGHT,
                    supplier.OGRESS,
                    supplier.SPLASH
            );
        }
    }

    private void setupOgressProgressive() {
        Progressive p = new Progressive();
        p.setName("Ogress Killer");
        p.setStyle(Combat.AttackStyle.CASTING);
        p.setSkill(Skill.MAGIC);
        HashMap<EquipmentSlot, String> map = new HashMap<>();
        map.put(EquipmentSlot.HEAD, "blue wizard hat");
        map.put(EquipmentSlot.NECK, "amulet of magic");
        map.put(EquipmentSlot.CHEST, "blue wizard robe");
        map.put(EquipmentSlot.LEGS, "zamorak monk bottom");
        map.put(EquipmentSlot.MAINHAND, "staff of fire");
        p.setEquipmentMap(map);
        HashSet<String> runes = new HashSet<>();
        runes.add("air rune");
        runes.add("mind rune");
        p.setRunes(runes);
        p.setSpell(Spell.Modern.FIRE_STRIKE);
        HashSet<String> enemies = new HashSet<>();
        p.setEnemies(enemies);
        String[] primaryLoot = new String[]{
                "Iron arrow", "Steel arrow", "Adamant arrow", "Mithril arrow", "Rune med helm", "Rune full helm", "Rune battleaxe", "Shaman mask", "Air rune",
                "Mind rune", "Water rune", "Earth rune", "Fire rune", "Chaos rune", "Cosmic rune", "Nature rune", "Law rune", "Death rune", "Mithril kiteshield", "Bones", "Shaman mask"
        };
        String[] secondaryLoot = new String[]{
                "Uncut diamond", "Uncut ruby", "Uncut emerald", "Uncut sapphire", "Limpwurt root", "Bones", "Shaman mask", "Big bones"
        };
        HashSet<String> lootSet = new HashSet<>(Arrays.asList(primaryLoot));
        lootSet.addAll(map.values());
        if (Script.OGRESS_LOOT_ALL) {
            lootSet.addAll(Arrays.asList(secondaryLoot));
        }
        p.setLoot(lootSet);
        p.setPrioritizeLooting(false);
        p.setBuryBones(false);
        p.setPosition(OgressWrapper.TOCK_QUEST_POSITION);
        p.setRadius(Random.low(1, 3));
        p.setRandomIdle(false);
        p.setMinimumLevel(13);
        p.setOgress(true);
        ProgressiveSet.add(p);
    }

    private void setupSplashProgressive() {
        Progressive progressive = new Progressive();
        progressive.setName("Train Magic: Splash");
        progressive.setSplash(true);
        progressive.setStyle(Combat.AttackStyle.CASTING);
        progressive.setSkill(Skill.MAGIC);
        HashSet<String> runes = new HashSet<>();
        runes.add("air rune");
        runes.add("mind rune");
        progressive.setRunes(runes);
        progressive.setSpell(Spell.Modern.WIND_STRIKE);
        progressive.setMinimumLevel(1);
        progressive.setMaximumLevel(13);
        progressive.setPrioritizeLooting(false);
        progressive.setPosition(SplashWrapper.getSplashArea().getCenter());
        progressive.setRadius(3);
        HashSet<String> enemies = new HashSet<>();
        enemies.add("mugger");
        enemies.add("thief");
        progressive.setEnemies(enemies);
        progressive.setEquipmentMap(new HashMap<>());
        progressive.setRandomIdle(false);
        progressive.setRandomIdleBuffer(Script.randInt(20, 30));
        HashMap<EquipmentSlot, String> map = new HashMap<>();
        progressive.setUseSplashGear(Script.SPLASH_USE_EQUIPMENT);
        if (Script.SPLASH_USE_EQUIPMENT) {
            map.put(EquipmentSlot.HEAD, "bronze full helm");
            map.put(EquipmentSlot.CHEST, "bronze platebody");
            map.put(EquipmentSlot.LEGS, "bronze platelegs");
            map.put(EquipmentSlot.OFFHAND, "bronze kiteshield");
        }
        progressive.setEquipmentMap(map);

        ProgressiveSet.add(progressive);
    }

    private void setupMagicProgressive() {
        Progressive progressive = new Progressive();
        progressive.setName("Train Magic");
        progressive.setStyle(Combat.AttackStyle.CASTING);
        progressive.setSkill(Skill.MAGIC);
        HashMap<EquipmentSlot, String> map = new HashMap<>();

        progressive.setEquipmentMap(map);
        HashSet<String> runes = new HashSet<>();
        runes.add("air rune");
        runes.add("mind rune");
        progressive.setRunes(runes);
        progressive.setSpell(Spell.Modern.WIND_STRIKE);
        HashSet<String> enemies = new HashSet<>();
        enemies.add("chicken");
        progressive.setEnemies(enemies);
        HashSet<String> loot = new HashSet<>();
        //loot.add("raw chicken");
        loot.add("bones");
        String[] runeLoot = new String[]{"air rune", "mind rune", "water rune", "earth rune", "fire rune",
                "chaos rune", "cosmic rune", "nature rune", "law rune", "death rune", "body rune"};
        loot.addAll(Arrays.asList(runeLoot));
        progressive.setLoot(loot);
        progressive.setPrioritizeLooting(false);
        progressive.setBuryBones(false);
        switch (Script.randInt(0, 3)) {
            case 0:
                progressive.setPosition(new Position(3017, 3290)); //Sarim chickens
                break;
            case 1:
                progressive.setPosition(new Position(3031, 3286)); //Sarim chickens (small)
                break;
            case 2:
                progressive.setPosition(new Position(3231, 3295)); //Lumbridge chickens
                break;
            case 3:
                progressive.setPosition(new Position(3188, 3277)); //Lumbridge chickens (small)
                break;
        }
        progressive.setRadius(Random.low(10, 15));
        progressive.setRandomIdle(true);
        progressive.setRandomIdleBuffer(Script.randInt(20, 30));
        progressive.setMinimumLevel(1);
        int switchLvl = Script.randInt(5, 8);
        progressive.setMaximumLevel(switchLvl);
        ProgressiveSet.add(progressive);

        Progressive progressive2 = new Progressive();
        progressive2.copy(progressive);
        progressive2.setName("Train Magic 2");
        HashSet<String> e2 = new HashSet<>(enemies);
        e2.add("goblin");
        progressive2.setEnemies(e2);
        if (Script.randInt(0, 1) == 0) {
            progressive2.setPosition(new Position(3248, 3237)); //Lumbridge east river lum
        } else {
            progressive2.setPosition(new Position(3188, 3277)); //Lumbridge chickens (small)
        }
        progressive2.setMinimumLevel(switchLvl);
        progressive2.setMaximumLevel(13);
        ProgressiveSet.add(progressive2);
    }

    private void setupLesserDemonProgressive() {
        Progressive progressive = new Progressive();
        progressive.setName("Train Magic: Lesser Demon");
        progressive.setSplash(true);
        progressive.setStyle(Combat.AttackStyle.CASTING);
        progressive.setSkill(Skill.ATTACK);
        HashSet<String> runes = new HashSet<>();
        runes.add("air rune");
        runes.add("mind rune");
        progressive.setRunes(runes);
        progressive.setSpell(Spell.Modern.WIND_STRIKE);
        progressive.setMinimumLevel(1);
        HashSet<String> enemies = new HashSet<>();
        progressive.setPrioritizeLooting(false);
        progressive.setPosition(new Position(3110, 3159, 2));
        progressive.setRadius(3);
        enemies.add("lesser demon");
        progressive.setEnemies(enemies);
        progressive.setEquipmentMap(new HashMap<>());
        progressive.setRandomIdle(true);
        progressive.setRandomIdleBuffer(Script.randInt(20, 30));

        ProgressiveSet.add(progressive);
    }

    private void setupDefaultProgressive(String... enemiesToFight) {
        Progressive progressive = new Progressive();
        progressive.setName("Default");
        HashMap<EquipmentSlot, String> map = new HashMap<>();
        switch (Script.randInt(0, 2)) {
            case 0:
                map.put(EquipmentSlot.MAINHAND, "Bronze sword");
                map.put(EquipmentSlot.OFFHAND, "Wooden shield");
                break;
            case 1:
                if (Inventory.contains("Bronze arrow")) {
                    map.put(EquipmentSlot.MAINHAND, "Shortbow");
                    map.put(EquipmentSlot.QUIVER, "Bronze arrow");
                }
                break;
            case 2:
                map.put(EquipmentSlot.MAINHAND, "Bronze dagger");
                map.put(EquipmentSlot.OFFHAND, "Wooden shield");
                break;
        }

        progressive.setEquipmentMap(map);

        switch (Script.FIGHTER_TRAIN_DEFENCE ? 2 : Script.randInt(0, 2)) {
            case 0:
                if (map.containsKey(EquipmentSlot.QUIVER)) {
                    progressive.setStyle(Combat.AttackStyle.ACCURATE);
                    //progressive.setSkill(Skill.RANGED);
                } else {
                    progressive.setStyle(Combat.AttackStyle.ACCURATE);
                    //progressive.setSkill(Skill.ATTACK);
                }
                break;
            case 1:
                if (map.containsKey(EquipmentSlot.QUIVER)) {
                    progressive.setStyle(Combat.AttackStyle.RAPID);
                    //progressive.setSkill(Skill.RANGED);
                } else {
                    progressive.setStyle(Combat.AttackStyle.AGGRESSIVE);
                    //progressive.setSkill(Skill.STRENGTH);
                }
                break;
            case 2:
                if (map.containsKey(EquipmentSlot.QUIVER)) {
                    progressive.setStyle(Combat.AttackStyle.LONGRANGE);
                    //progressive.setSkill(Skill.DEFENCE);
                } else {
                    progressive.setStyle(Combat.AttackStyle.DEFENSIVE);
                    //progressive.setSkill(Skill.DEFENCE);
                }
                break;
        }
        progressive.setSkill(Skill.ATTACK);

        progressive.setMinimumLevel(1);
        HashSet<String> enemies = new HashSet<>();
        progressive.setEnemies(enemies);
        HashSet<String> loot = new HashSet<>();
        loot.add("bronze scimitar");
        String[] runes = new String[]{"air rune", "mind rune", "water rune", "earth rune", "fire rune",
                "chaos rune", "cosmic rune", "nature rune", "law rune", "death rune", "body rune"};
        loot.addAll(Arrays.asList(runes));
        if (Script.randInt(0, 1) == 0) {
            loot.add("bones");
        }
        progressive.setLoot(loot);
        progressive.setRadius(Random.low(10, 15));
        progressive.setBuryBones(true);
        progressive.setPrioritizeLooting(false);

        for (String enemy : enemiesToFight) {
            enemies.add(enemy.toLowerCase());
        }
        progressive.setEnemies(enemies);

        if (progressive.getEnemies().contains("chicken")) {
            switch (Script.randInt(0, 3)) {
                case 0:
                    progressive.setPosition(new Position(3017, 3290)); //Sarim chickens
                    break;
                case 1:
                    progressive.setPosition(new Position(3031, 3286)); //Sarim chickens (small)
                    break;
                case 2:
                    progressive.setPosition(new Position(3231, 3295)); //Lumbridge chickens
                    break;
                case 3:
                    progressive.setPosition(new Position(3188, 3277)); //Lumbridge chickens (small)
                    break;
            }
        } else if (progressive.getEnemies().contains("cow")) {
            switch (Script.randInt(0, 1)) {
                case 0:
                    progressive.setPosition(new Position(3032, 3305)); //Sarim cows
                    break;
                case 1:
                    progressive.setPosition(new Position(3254, 3283)); //Lumbridge cows
                    break;
            }
        } else if (progressive.getEnemies().contains("goblin")) {
            switch (1) {
                case 0:
                    progressive.setPosition(new Position(3183, 3220)); //Behind lumbridge castle
                    break;
                case 1:
                    progressive.setPosition(new Position(3248, 3237)); //Lumbridge east river lum
                    break;
            }
        }

        progressive.setRandomIdle(true);
        progressive.setRandomIdleBuffer(Script.randInt(20, 30));


        CombatStore.resetTargetingValues();
        if (!ProgressiveSet.isEmpty()) {
            ProgressiveSet.removeAll();
        }
        ProgressiveSet.add(progressive);
    }

    private void setBackgroundTasks() {
        if (!BackgroundTaskExecutor.isStarted()) {
            new TargetChecker();
        }
    }

    public void onStop(boolean startBeggar, int bShutdownRetries) {
        OgressWrapper.deaths = 0;
        onStop(startBeggar, false, bShutdownRetries);
    }

    public void onStop(boolean startBeggar, boolean ogressBeg, int bShutdownRetries) {
        if (WorldhopWrapper.currentWorld > 0) {
            WorldhopWrapper.removeWorld(WorldhopWrapper.currentWorld, Script.OGRESS_WORLD_PATH);
        }
        if (manager != null) {
            manager.onScriptStop();
        }
        script.removeAll();
        ProgressiveSet.removeAll();
        CombatStore.resetTargetingValues();

        if (startBeggar) {
            if (!ogressBeg) {
                script.isFighterRunning = false;
            }
            script.startBeggar(ogressBeg);
        }
        //super.onStop();
    }

    public void notify(RenderEvent e) {
        Graphics g = e.getSource();
        try {
            if (manager != null) {
                paint.notify(e);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        g.setColor(Color.GREEN);

        Position p = Config.getStartingTile();
        if (p != null && Game.isLoggedIn()) {
            Point start = Projection.toMinimap(p);
            if (start != null) {
                int size = Config.getRadius() * 4;
                g.drawOval(start.x - (size / 2), start.y - (size / 2), size, size);
            }
            CombatPaintRenderer.onRenderEvent(g);
        }
    }

    public void notify(TargetEvent e) {
        CombatListener.onTargetEvent(e);
    }

    public void notify(DeathEvent e) {
        CombatListener.onDeathEvent(e, supplier);
    }

    public void notify(ChatMessageEvent e) {
        CombatListener.onChatMessage(e);
    }

    private Node active = null;

    public Node getActive() {
        return active;
    }

    public void setActive(Node task) {
        active = task;
        if (stopTimeMs > 0) {
            checkStopTime();
        }
    }

    private void checkStopTime() {
        if ((System.currentTimeMillis() - startTimeMs) > stopTimeMs) {
            if (Players.getLocal().getCombatLevel() > 3) {
                Log.fine("Stopping Fighter");
                onStop(true, 10);
            } else {
                Log.severe("Low Combat LVL Continuing");
                startTimeMs = System.currentTimeMillis() - (stopTimeMs / 2);
                //invalidateNodes();
                CombatStore.resetTargetingValues();
            }
        }
    }

    public void invalidateTask(Node thisNode) {
        if (active != null && !thisNode.equals(active)) {
            Logger.debug("Node has changed.");
            active.onInvalid();
        }
        setActive(thisNode);
    }

    private void invalidateNodes() {
        Node[] nodes = supplier.getTasks();
        for (Node node : nodes) {
            node.onInvalid();
        }
    }

}
