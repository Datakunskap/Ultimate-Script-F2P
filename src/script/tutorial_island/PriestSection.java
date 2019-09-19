package script.tutorial_island;


import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.runetek.api.input.menu.ActionOpcodes;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;

import java.util.Arrays;
import java.util.List;

public final class PriestSection extends TutorialSection {

    private static final Area CHURCH_AREA = Area.rectangular(3120, 3103, 3128, 3110);

    private static final List<Position> PATH_TO_CHURCH = Arrays.asList(
            new Position(3131, 3124, 0),
            new Position(3134, 3121, 0),
            new Position(3134, 3117, 0),
            new Position(3132, 3114, 0),
            new Position(3130, 3111, 0),
            new Position(3130, 3108, 0),
            new Position(3129, 3106, 0)
    );

    public PriestSection() {
        super("Brother Brace");
    }

    @Override
    public boolean validate() {
        return getTutorialSection() >= 16 && getTutorialSection() <= 17;
    }

    @Override
    public int execute () {
        if (pendingContinue()) {
            selectContinue();
            return TutorialIsland.getRandSleep();
        }

        InterfaceComponent IGNORE_LIST = Interfaces.getComponent(429, 1);

        switch (getProgress()) {
            case 550:
                if (getInstructor() == null) {
                    Movement.walkToRandomized(CHURCH_AREA.getCenter());
                } else if (Players.getLocal().getPosition().distance(getInstructor()) > 4) {
                    Movement.walkToRandomized(CHURCH_AREA.getCenter());
                } else {
                    talkToInstructor();
                }
                break;
            case 560:
                Tabs.open(Tab.PRAYER);
                break;
            case 570:
                talkToInstructor();
                break;
            case 580:
                Tabs.open(Tab.FRIENDS_LIST);
                break;
            case 590:
                IGNORE_LIST.interact(ActionOpcodes.INTERFACE_ACTION);
                break;
            case 600:
                talkToInstructor();
                break;
            case 610:
                if (Movement.walkToRandomized(new Position(3122, 3101, 0))) {
                    randWalker(new Position(3122, 3101, 0));
                    Time.sleepUntil(() -> getProgress() != 610, 5000);
                }
                break;
        }
        return TutorialIsland.getRandSleep();
    }
}
