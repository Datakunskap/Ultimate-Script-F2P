package script.tutorial_island;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import script.Beggar;

import java.util.Arrays;
import java.util.List;

public final class QuestSection extends TutorialSection {

    private static final Area QUEST_BUILDING = Area.rectangular(3083, 3119, 3089, 3125);

    private static final List<Position> PATH_TO_QUEST_BUILDING = Arrays.asList(
            new Position(3071, 3090, 0),
            new Position(3071, 3094, 0),
            new Position(3071, 3099, 0),
            new Position(3072, 3103, 0),
            new Position(3074, 3108, 0),
            new Position(3076, 3111, 0),
            new Position(3077, 3115, 0),
            new Position(3076, 3118, 0),
            new Position(3076, 3122, 0),
            new Position(3079, 3125, 0),
            new Position(3083, 3127, 0),
            new Position(3086, 3126, 0)
    );

    public QuestSection() {
        super("Quest Guide");
    }

    @Override
    public boolean validate() {
        return getTutorialSection() >= 6 && getTutorialSection() <= 7;
    }

    @Override
    public int execute () {
        if (pendingContinue()) {
            selectContinue();
            return TutorialIsland.getRandSleep();
        }
        switch (getProgress()) {
            case 200:
                boolean isRunning = Movement.isRunEnabled();
                if (Movement.toggleRun(!isRunning)) {
                    Time.sleepUntil(() -> Movement.isRunEnabled() == !isRunning, 2000, 1200);
                }
                break;
            case 210:
                if (!Movement.isRunEnabled()) {
                    if (Movement.toggleRun(true)) {
                        Time.sleepUntil(Movement::isRunEnabled, 2000, 1200);
                    }
                } else {
                    if (Movement.walkToRandomized(QUEST_BUILDING.getCenter())) {
                        Time.sleepUntil(() -> getProgress() != 210, 2000,5000);
                    }
                }
                break;
            case 220:
                talkToInstructor();
                break;
            case 230:
                Tabs.open(Tab.QUEST_LIST);
                break;
            case 240:
                talkToInstructor();
                break;
            case 250:
                if (SceneObjects.getNearest("Ladder").interact("Climb-down")) {
                    Time.sleepUntil(() -> getProgress() != 250, 2000,5000);
                    Movement.walkToRandomized(Players.getLocal().getPosition().randomize(8));
                    Time.sleepUntil(() -> !Players.getLocal().isMoving(), Beggar.randInt(3500, 6500));
                }
                break;
        }
        return TutorialIsland.getRandSleep();
    }
}
