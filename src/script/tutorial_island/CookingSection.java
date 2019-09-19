package script.tutorial_island;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.input.menu.ActionOpcodes;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.SceneObjects;

import java.util.Arrays;
import java.util.List;

public class CookingSection extends TutorialSection {

    private static final Area COOK_BUILDING = Area.rectangular(3073, 3083, 3078, 3086);
    private static final List<Position> PATH_TO_COOK_BUILDING = Arrays.asList(
            new Position(3087, 3091, 0),
            new Position(3083, 3086, 0),
            new Position(3080, 3083, 0)
    );

    public CookingSection() {
        super("Master Chef");
    }

    @Override
    public boolean validate() {
        return getTutorialSection() >= 4 && getTutorialSection() <= 5;
    }

    @Override
    public int execute () {
        if (pendingContinue()) {
            selectContinue();
            return TutorialIsland.getRandSleep();
        }
        switch (getProgress()) {
            case 130:
                if (Movement.walkToRandomized(COOK_BUILDING.getCenter())) {
                    Time.sleepUntil(() -> getProgress() == 140, 2000, 5000);
                }
                break;
            case 140:
                talkToInstructor();
                break;
            case 150:
                makeDough();
                break;
            case 160:
                bakeDough();
                break;
            case 170:
                if (Movement.walkTo(new Position(3071, 3090, 0))) {
                    randWalker(new Position(3071, 3090, 0));
                    Time.sleepUntil(() -> getProgress() != 170, 2000, 5000);
                }
                break;
        }
        return TutorialIsland.getRandSleep();
    }

    private void makeDough() {
        if (Inventory.getSelectedItem() == null || !"Pot of flour".equals(Inventory.getSelectedItem().getName())) {
            Inventory.getFirst("Pot of flour").interact("Use");
        } else if (Inventory.getFirst("Bucket of water").interact(ActionOpcodes.ITEM_ON_ITEM)) {
            Time.sleepUntil(() -> Inventory.contains("Bread dough"), 2000, 3000);
        }
    }

    private void bakeDough() {
        if (Inventory.getSelectedItem() == null || !"Bread dough".equals(Inventory.getSelectedItem().getName())) {
            Inventory.getFirst("Bread dough").interact("Use");
        } else if (SceneObjects.getNearest("Range").interact(ActionOpcodes.OBJECT_ACTION_0)) {
            Time.sleepUntil(() -> Inventory.contains("Bread"), 2000,5000);
        }
    }
}
