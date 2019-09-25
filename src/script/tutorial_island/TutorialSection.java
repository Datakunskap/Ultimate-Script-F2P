package script.tutorial_island;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public abstract class TutorialSection extends Task {

    private final String INSTRUCTOR_NAME;

    public TutorialSection(final String INSTRUCTOR_NAME) {
        this.INSTRUCTOR_NAME = INSTRUCTOR_NAME;
    }

    //public abstract void onLoop() throws InterruptedException;

    protected final int getProgress() {
        return Varps.get(281);
    }

    public int getTutorialSection() {
        return Varps.get(406);
    }

    protected final boolean talkToInstructor() {
        Npc i = getInstructor();
        if (i != null && i.isPositionInteractable() && i.interact("Talk-to")) {
            Log.info("Talking to instructor");
            Time.sleepUntil(this::pendingContinue, 2000, 6000);
        } else if (i != null && i.isPositionWalkable()) {
            Log.info("Walking to instructor");
            Movement.walkToRandomized(i.getPosition().randomize(3));
        } else {
            Movement.walkToRandomized(Players.getLocal().getPosition().randomize(6));
            Log.severe("Cant Find Instructor: Section " + getTutorialSection() + " Progress " + getProgress());
            return false;
        }
        return true;
    }

    protected Npc getInstructor() {
        return Npcs.getNearest(INSTRUCTOR_NAME);
    }

    protected boolean pendingContinue() {
        InterfaceComponent wierdContinue = Interfaces.getComponent(162, 44);
        if (wierdContinue != null && wierdContinue.isVisible() &&
                (wierdContinue.getText().toLowerCase().contains("someone") ||
                        wierdContinue.getText().toLowerCase().contains("reach") ||
                        wierdContinue.getText().toLowerCase().contains("already"))) {
            return true;
        }

        return Dialog.isOpen() && Dialog.canContinue();
    }

    protected boolean selectContinue() {
        if (pendingContinue()) {
            Game.getClient().fireScriptEvent(299, 1, 1);
            return Dialog.processContinue();
        }
        return false;
    }

    void randWalker(Position posRequired) {
        Time.sleep(1000, 1500);
        Time.sleepUntil(() -> Movement.walkToRandomized(posRequired) && Players.getLocal().getPosition().equals(posRequired), 1000, 8000);
        if (posRequired.distance(Players.getLocal()) < 4) {
            Log.info("Random walk");
            Movement.walkToRandomized(Players.getLocal().getPosition().randomize(6));
            Time.sleep(2000);
            Time.sleepUntil(() -> !Players.getLocal().isMoving(), 2000, Beggar.randInt(2000, 6000));
        }
    }
}
