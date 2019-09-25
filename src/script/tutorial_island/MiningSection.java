package script.tutorial_island;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.runetek.api.input.menu.ActionOpcodes;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

enum Rock {

    COPPER((short) 4645, (short) 4510),
    TIN((short) 53);

    private final short[] COLOURS;

    Rock(final short... COLOURS) {
        this.COLOURS = COLOURS;
    }

    public SceneObject getClosestWithOre() {
        //noinspection unchecked
        return SceneObjects.getNearest(obj -> {
            short[] colours = obj.getDefinition().getNewColors();
            if (colours != null) {
                for (short c : colours) {
                    for (short col : COLOURS) {
                        if (c == col) return true;
                    }
                }
            }
            return false;
        });
    }
}

public final class MiningSection extends TutorialSection {

    private static final Area SMITH_AREA = Area.rectangular(3076, 9497, 3082, 9504);

    private static final List<Position> PATH_TO_SMITH_AREA = Arrays.asList(
            new Position(3080, 9518, 0),
            new Position(3080, 9511, 0),
            new Position(3080, 9505, 0)
    );

    private static final List<Position> PATH_TO_GATE = Arrays.asList(
            new Position(3086, 9505, 0),
            new Position(3091, 9503, 0)
    );

    public MiningSection() {
        super("Mining Instructor");
    }

    @Override
    public boolean validate() {
        return getTutorialSection() >= 8 && getTutorialSection() <= 9;
    }

    @Override
    public int execute () throws NullPointerException {
        if (pendingContinue()) {
            selectContinue();
            return TutorialIsland.getRandSleep();
        }

        switch (getProgress()) {
            case 260:
                if (getInstructor() == null) {
                    Movement.walkToRandomized(SMITH_AREA.getCenter());
                } else {
                    talkToInstructor();
                }
                break;
            case 270:
                prospect(Rock.TIN);
                break;
            case 280:
                prospect(Rock.COPPER);
                break;
            case 290:
                talkToInstructor();
                break;
            case 300:
                mine(Rock.TIN);
                break;
            case 310:
                mine(Rock.COPPER);
                break;
            case 320:
                if (Tabs.open(Tab.INVENTORY)) {
                    smelt();
                }
                break;
            case 330:
                talkToInstructor();
                break;
            case 340:
                if (Tabs.open(Tab.INVENTORY)) {
                    smith();
                }
                break;
            case 350:
                Optional<InterfaceComponent> daggerWidgetOpt = getDaggerWidget();
                if (daggerWidgetOpt.isPresent()) {
                    if (daggerWidgetOpt.get().interact(ActionOpcodes.INTERFACE_ACTION)) {
                        Time.sleepUntil(() -> Inventory.contains("Bronze dagger"), 2000, 6000);
                    }
                } else {
                    smith();
                }
                break;
            case 360:
                if (Movement.walkToRandomized(new Position(3096, 9503, 0))) {
                    randWalker(new Position(3096, 9503, 0));
                            Time.sleepUntil(() -> getProgress() != 360, 2000, 5000);
                }
                break;
        }
        return TutorialIsland.getRandSleep();
    }

    private void smith() {
        if (!SMITH_AREA.contains(Players.getLocal())) {
            Movement.walkTo(SMITH_AREA.getCenter());
        } else if (Inventory.getSelectedItem() == null || !"Bronze bar".equals(Inventory.getSelectedItem().getName())) {
            Inventory.getFirst("Bronze bar").interact("Use");
        } else if (SceneObjects.getNearest("Anvil").interact("Use")) {
            Time.sleepUntil(() -> getDaggerWidget().isPresent(), 2000, 5000);
        }
    }

    private Optional<InterfaceComponent> getDaggerWidget() {
        InterfaceComponent daggerTextWidget = Interfaces.getComponent(312, 9, 0);
        if (daggerTextWidget != null) {
            return Optional.ofNullable(Interfaces.getComponent(daggerTextWidget.getRootIndex(), daggerTextWidget.getParentIndex()));
        }
        return Optional.empty();
    }

    private void smelt() {
        if (Inventory.getSelectedItem() == null || !"Tin ore".equals(Inventory.getSelectedItem().getName())) {
            Inventory.getFirst("Tin ore").interact("Use");
        } else if (SceneObjects.getNearest("Furnace").interact("Use")) {
            Time.sleepUntil(() -> Inventory.contains("Bronze bar"), 2000,5000);
        }
    }

    private void prospect(Rock rock) {
        SceneObject closestRock = rock.getClosestWithOre();
        if (closestRock != null && closestRock.interact("Prospect")) {
            Time.sleepUntil(this::pendingContinue, 2000,6000);
        }
    }

    private void mine(Rock rock) {
        SceneObject closestRock = rock.getClosestWithOre();
        if (closestRock != null && closestRock.interact("Mine")) {
            Time.sleepUntil(this::pendingContinue, 2000,6000);
        }
    }
}
