package script.tutorial_island;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Bank;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.input.menu.ActionOpcodes;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import script.Beggar;

import java.util.Arrays;
import java.util.List;

public final class BankSection extends TutorialSection {

    private static final Area BANK_AREA = Area.rectangular(3119, 3125, 3124, 3119);

    private static final List<Position> PATH_TO_BANK = Arrays.asList(
            new Position(3111, 3123, 0),
            new Position(3114, 3119, 0),
            new Position(3118, 3116, 0),
            new Position(3121, 3118, 0)
    );

    public BankSection() {
        super("Account Guide");
    }

    @Override
    public boolean validate() {
        return getTutorialSection() >= 14 && getTutorialSection() <= 15;
    }

    @Override
    public int execute () {
        if (pendingContinue()) {
            selectContinue();
            return TutorialIsland.getRandSleep();
        }

        switch (getProgress()) {
            case 510:
                if (!BANK_AREA.contains(Players.getLocal())) {
                    daxWalker(BANK_AREA.getTiles().get(Beggar.randInt(0, BANK_AREA.getTiles().size() -1)), BANK_AREA);
                } else if (Dialog.isOpen() && Dialog.isViewingChatOptions()) {
                    Dialog.process("Yes.");
                } else if (SceneObjects.getNearest("Bank booth").interact("Use")) {
                    Time.sleepUntil(this::pendingContinue, 5000);
                }
                break;
            case 520:
                if (Bank.isOpen()) {
                    Bank.close();
                } else if (!SceneObjects.getNearest("Poll booth").isPositionInteractable()) {
                    Movement.walkToRandomized(BANK_AREA.getCenter());
                    //getCamera().toEntity(getObjects().closest("Poll booth"));
                } else if (SceneObjects.getNearest("Poll booth").interact("Use")) {
                    Time.sleepUntil(this::pendingContinue, 5000);
                }
                break;
            case 525:
                if (Interfaces.closeAll() && openDoorAtPosition(new Position(3125, 3124, 0))) {
                    Time.sleepUntil(() -> getProgress() != 525, 5000);
                }
                break;
            case 530:
                talkToInstructor();
                break;
            case 531:
                openAccountManagementTab();
                break;
            case 532:
                talkToInstructor();
                break;
            case 540:
                if (openDoorAtPosition(new Position(3130, 3124, 0))) {
                    randWalker(new Position(3130, 3124, 0));
                    Time.sleepUntil(() -> getProgress() != 540, 2000, 5000);
                }
                break;
        }
        return TutorialIsland.getRandSleep();
    }

    private boolean openDoorAtPosition(final Position position) {
        SceneObject door = SceneObjects.getNearest(obj -> obj.getName().equals("Door") && obj.getPosition().equals(position));
        return door != null && door.interact("Open");
    }

    private void openAccountManagementTab() {
        InterfaceComponent accountManagementWidget = Interfaces.getComponent(548, 32);

        if (accountManagementWidget.isVisible() && accountManagementWidget.interact(ActionOpcodes.INTERFACE_ACTION)) {
            Time.sleepUntil(() -> getProgress() == 532,2000, 5000);
        }
    }

}
