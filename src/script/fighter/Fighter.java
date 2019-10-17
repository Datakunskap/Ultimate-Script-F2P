package script.fighter;

import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.commons.StopWatch;
import org.rspeer.runetek.api.commons.Time;
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
import script.Beggar;
import script.fighter.backgroundTasks.TargetChecker;
import script.fighter.config.Config;
import script.fighter.config.ProgressiveSet;
import script.fighter.debug.LogLevel;
import script.fighter.framework.BackgroundTaskExecutor;
import script.fighter.framework.Node;
import script.fighter.framework.NodeManager;
import script.fighter.models.Progressive;
import script.fighter.nodes.combat.CombatListener;
import script.fighter.paint.CombatPaintRenderer;
import script.fighter.paint.ScriptPaint;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

//@ScriptMeta(name = "Pro Fighter TaskScript", desc = "RSPeer Official AIO Fighter", developer = "MadDev", category = ScriptCategory.COMBAT)
public class Fighter {

    private NodeManager manager;
    private ScriptPaint paint;
    private StopWatch runtime;
    //private Progressive progressive;

    private long stopTimeMs;
    public long startTimeMs;
    public Beggar beggar;

    public Fighter(Beggar script, long stopTimeMs) {
        beggar = script;
        this.stopTimeMs = stopTimeMs;
    }

    public static int getLoopReturn() {
        return Random.high(200, 1000);
    }

    public StopWatch getRuntime() {
        return runtime;
    }

    public NodeManager getManager() {
        return manager;
    }

    public NodeSupplier supplier;

    public NodeSupplier getSupplier() {
        return supplier;
    }

    public void onStart() {
        try {
            beggar.isFighterRunning = true;
            if (Beggar.OGRESS) {
                setupMagicProgressive();
                //setupLesserDemonProgressive();
            } else {
                setupDefaultProgressive("chicken");
            }

            Config.setLogLevel(LogLevel.Debug);
            supplier = new NodeSupplier(this);
            manager = new NodeManager();

            runtime = StopWatch.start();
            startTimeMs = System.currentTimeMillis();
            paint = new ScriptPaint(this);
            setBackgroundTasks();
            active = null;
            setupNodes();

            if(!GameCanvas.isInputEnabled()) {
                GameCanvas.setInputEnabled(true);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
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
        String[] runeLoot = new String[] {"air rune", "mind rune","water rune","earth rune","fire rune",
                "chaos rune", "cosmic rune","nature rune","law rune","death rune", "body rune"} ;
        loot.addAll(Arrays.asList(runeLoot));
        progressive.setLoot(loot);
        progressive.setPrioritizeLooting(false);
        progressive.setBuryBones(false);
        switch (Beggar.randInt(0, 3)) {
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
        progressive.setRandomIdleBuffer(Beggar.randInt(20, 30));
        progressive.setMinimumLevel(1);
        int switchLvl = Beggar.randInt(5, 8);
        progressive.setMaximumLevel(switchLvl);
        ProgressiveSet.add(progressive);

        Progressive progressive2 = new Progressive();
        progressive2.copy(progressive);
        enemies.add("goblin");
        progressive2.setEnemies(enemies);
        if (Beggar.randInt(0, 1) == 0) {
            progressive.setPosition(new Position(3248, 3237)); //Lumbridge east river lum
        } else {
            progressive.setPosition(new Position(3188, 3277)); //Lumbridge chickens (small)
        }
        progressive2.setMinimumLevel(switchLvl);
        progressive2.setMaximumLevel(13);
        ProgressiveSet.add(progressive2);
    }

    private void setupLesserDemonProgressive() {
        Progressive progressive = new Progressive();
        progressive.setName("Train Magic: Lesser Demon");
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
        progressive.setRandomIdleBuffer(Beggar.randInt(20, 30));

        ProgressiveSet.add(progressive);
    }

    private void setupDefaultProgressive(String... enemiesToFight) {
        Progressive progressive = new Progressive();
        progressive.setName("Default");
        HashMap<EquipmentSlot, String> map = new HashMap<>();
        switch (Beggar.randInt(0, 2)) {
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

        switch (Beggar.randInt(0, 2)) {
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
        String[] runes = new String[] {"air rune", "mind rune","water rune","earth rune","fire rune",
                "chaos rune", "cosmic rune","nature rune","law rune","death rune", "body rune"} ;
        loot.addAll(Arrays.asList(runes));
        if (Beggar.randInt(0, 1) == 0) {
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
            switch (Beggar.randInt(0, 3)) {
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
            switch (Beggar.randInt(0, 1)) {
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
        progressive.setRandomIdleBuffer(Beggar.randInt(20, 30));


        CombatStore.resetTargetingValues();
        if (!ProgressiveSet.isEmpty()) {
            ProgressiveSet.removeAll();
        }
        ProgressiveSet.add(progressive);
    }

    private void setupNodes() {

        beggar.submit(
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
                supplier.BACK_TO_FIGHT
        );
    }

    private void setBackgroundTasks() {
        new TargetChecker();
    }

    public void onStop(boolean scriptStopping, int bShutdownRetries) {
        beggar.isFighterRunning = false;
        beggar.removeAll();
        manager.onScriptStop();

        try {
            BackgroundTaskExecutor.shutdown();
            Time.sleep(2000);

        } catch (Exception e) {
            beggar.writeToErrorFile("Fighter onStop() -> Failed to shutdown");
            if (bShutdownRetries > 0) {
                onStop(scriptStopping, bShutdownRetries - 1);
            }
            e.printStackTrace();
        }

        if (!scriptStopping) {
            beggar.restartBeggar();
        }
        //super.onStop();
    }

    public void notify(RenderEvent e) {
        Graphics g = e.getSource();
        try {
            if(manager != null) {
                paint.notify(e);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        g.setColor(Color.GREEN);

        Position p = Config.getStartingTile();
        if(p != null && Game.isLoggedIn()) {
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
        checkStopTime();
    }

    private void checkStopTime() {
        if(!Beggar.OGRESS && (System.currentTimeMillis() - startTimeMs) > stopTimeMs) {
            if (Players.getLocal().getCombatLevel() > 3) {
                Log.fine("Stopping Fighter");
                onStop(false, 10);
            } else {
                Log.severe("Low Combat LVL Continuing");
                startTimeMs = System.currentTimeMillis() - (stopTimeMs / 2);
                invalidateNodes();
                CombatStore.resetTargetingValues();
            }
        }
    }

    private void invalidateNodes() {
        Node[] nodes = supplier.getTasks();
        for (Node node : nodes) {
            node.onInvalid();
        }
    }

}
