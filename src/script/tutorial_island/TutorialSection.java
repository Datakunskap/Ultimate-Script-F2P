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
    public final char[] vowels = "aeiouyAEIOUY".toCharArray();
    public final char[] nonVowels = "bcdfghjklmnpqrstvwxzBCDFGHJKLMNPQRSTVWXZ".toCharArray();

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

    protected final void talkToInstructor() {
        Npc i = getInstructor();
        if (i != null && i.isPositionInteractable() && i.interact("Talk-to")) {
            Time.sleepUntil(this::pendingContinue, 2000, 5000);
        } else if (i != null && i.isPositionWalkable()) {
            Movement.walkToRandomized(i.getPosition());
        } else {
            Movement.walkToRandomized(Players.getLocal().getPosition().randomize(3));
        }
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

    protected void randWalker(Position posRequired) {
        Log.info("Walking to next section");
        while (!Players.getLocal().getPosition().equals(posRequired)) {
            Time.sleep(800, 1800);
            Movement.walkToRandomized(posRequired);
        }
        if (posRequired.distance(Players.getLocal()) < 4) {
            Movement.walkToRandomized(Players.getLocal().getPosition().randomize(8));
            Time.sleepUntil(() -> !Players.getLocal().isMoving(), 2000, Beggar.randInt(3000, 6000));
        }
    }
}
