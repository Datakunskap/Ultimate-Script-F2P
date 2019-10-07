package script.tutorial_island;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.runetek.api.input.menu.ActionOpcodes;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;

import java.util.Arrays;
import java.util.List;

public final class SurvivalSection extends TutorialSection {

    private final List<Position> PATH_TO_GATE = Arrays.asList(
            new Position(3098, 3092, 0),
            new Position(3092, 3091, 0)
    );

    public SurvivalSection() {
        super("Survival Expert");
    }

    @Override
    public boolean validate() {
        return getTutorialSection() >= 2 && getTutorialSection() <= 3;
    }

    @Override
    public int execute () {
        if (pendingContinue()) {
            selectContinue();
            return TutorialIsland.getRandSleep();
        }
        switch (getProgress()) {
            case 20:
                talkToInstructor();
                break;
            case 30:
                Tabs.open(Tab.INVENTORY);
                break;
            case 40:
                fish();
                break;
            case 50:
                Tabs.open(Tab.SKILLS);
                break;
            case 60:
                talkToInstructor();
                break;
            case 70:
                chopTree();
                break;
            case 80:
            case 90:
            case 100:
            case 110:
                if (!Tabs.isOpen(Tab.INVENTORY)) {
                    Tabs.open(Tab.INVENTORY);
                } else if (!Inventory.contains("Raw shrimps")) {
                    fish();
                } else if (SceneObjects.getNearest("Fire") == null || Interfaces.getComponent(263, 1, 0) != null &&
                        Interfaces.getComponent(263, 1, 0).getText().contains("time to light a fire")) {
                    if (!Inventory.contains("Logs")) {
                        chopTree();
                    } else {
                        //Log.info("Lighting fire");
                        lightFire();
                    }
                } else {
                    cook();
                }
                break;
            case 120:
                SceneObject gate = SceneObjects.getNearest("Gate");
                if (gate != null) {
                    if (gate.interact("Open")) {
                        randWalker(gate.getPosition());
                        Time.sleepUntil(() -> getProgress() == 130, 5000);
                    }
                } else {
                    Movement.walkToRandomized(PATH_TO_GATE.get(1));
                }
                break;
        }
        return TutorialIsland.getRandSleep();
    }

    private void chopTree() {
        SceneObject tree = SceneObjects.getNearest("Tree");
        if (tree != null && tree.interact("Chop down")) {
            Time.sleepUntil(() -> Inventory.contains("Logs") || tree.isTransformed(), 2000, 10_000);
        }
    }

    private void fish() {
        Npc fishingSpot = Npcs.getNearest("Fishing spot");
        if (fishingSpot != null && fishingSpot.interact("Net")) {
            long rawShrimpCount = Inventory.getCount(true, "Raw shrimps");
            Time.sleepUntil(() -> Inventory.getCount(true, "Raw shrimps") > rawShrimpCount, 2000, 10_000);
        }
    }

    private void lightFire() {
        if (standingOnFire()) {
            //Log.info("Standing on fire");
            getEmptyPosition(true, 10, true).ifPresent(position -> {
                if (Movement.getDestinationDistance() > 0)
                    Movement.walkTo(position);
            });
        } else if (Inventory.getSelectedItem() == null || !"Tinderbox".equals(Inventory.getSelectedItem().getName())) {
            Inventory.getFirst("Tinderbox").interact("Use");
        } else if (Inventory.getFirst("Logs").interact(ActionOpcodes.ITEM_ON_ITEM)) {
            Position playerPos = Players.getLocal().getPosition();
            Time.sleepUntil(() -> !Players.getLocal().getPosition().equals(playerPos), 2000, 10_000);
        }

        InterfaceComponent noFireHere = Interfaces.getFirst(229, t -> t.getText().contains("light a fire here"));
        if (noFireHere != null && noFireHere.isVisible()) {
            Movement.walkToRandomized(Players.getLocal().getPosition().randomize(3));
        }
    }

    private boolean standingOnFire() {
        return SceneObjects.getNearest(obj -> obj.getPosition().distance(Players.getLocal()) <= 0 && obj.getName().equals("Fire")) != null;
    }

    private void cook() {
        if (Inventory.getSelectedItem() == null || !"Raw shrimps".equals(Inventory.getSelectedItem().getName())) {
            Inventory.getFirst("Raw shrimps").interact("Use");
        } else {
            SceneObject fire = SceneObjects.getNearest("Fire");
            if (fire != null && fire.interact("Use")) {
                long rawShrimpCount = Inventory.getCount(true,"Raw shrimps");
                Time.sleepUntil(() -> Inventory.getCount(true,"Raw shrimps") < rawShrimpCount, 2000, 5000);
            }
        }
    }
}
