package script.tutorial_island;

import com.dax.walker.DaxWalker;
import com.dax.walker.Server;
import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Varps;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.runetek.providers.RSTileDecor;
import org.rspeer.script.Script;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

import java.awt.event.KeyEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public abstract class TutorialSection extends Task {

    private final String INSTRUCTOR_NAME;
    private final DaxWalker daxWalker;

    public TutorialSection(final String INSTRUCTOR_NAME) {
        this.INSTRUCTOR_NAME = INSTRUCTOR_NAME;
        daxWalker = new DaxWalker(new Server("sub_DPjXXzL5DeSiPf", "PUBLIC-KEY"));
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
        } else if (i != null) {
            Log.info("Walking to instructor");
            daxWalker.walkTo(i.getPosition().randomize(3));
        } else {
            daxWalker.walkTo(Players.getLocal().getPosition().randomize(6));
            Log.severe("Cant Find Instructor: Section " + getTutorialSection() + " Progress " + getProgress());
            return false;
        }
        return true;
    }

    Npc getInstructor() {
        return Npcs.getNearest(INSTRUCTOR_NAME);
    }

    protected boolean pendingContinue() {
        InterfaceComponent wierdContinue = Interfaces.getComponent(162, 44);
        if (wierdContinue != null && wierdContinue.isVisible()) {
            String msg = wierdContinue.getText().toLowerCase();
            if (msg.contains("someone") || msg.contains("reach") || msg.contains("already")){
                Game.getClient().fireScriptEvent(299, 1, 1);
                return true;
            }
        }

        return Dialog.isOpen() && Dialog.canContinue();
    }

    protected boolean selectContinue() {
        Keyboard.pressEventKey(KeyEvent.VK_SPACE);
        return true;
    }

    Optional<Position> getEmptyPosition() {
        List<Position> allPositions = Area.surrounding(Players.getLocal().getPosition(), 10).getTiles();
        //List<Position> allPositions = Players.getLocal()..getArea(10).getPositions();

        // Remove any position with an object (except ground decorations, as they can be walked on)
        for (SceneObject object : SceneObjects.getLoaded()) {
            if (object.getProvider() instanceof RSTileDecor) {
                continue;
            }
            allPositions.removeIf(position -> object.getPosition().equals(position));
        }

        allPositions.removeIf(position -> !position.isPositionInteractable());

        return allPositions.stream().min(Comparator.comparingInt(p -> (int) Players.getLocal().getPosition().distance(p)));
    }

    void randWalker(Position posRequired) {
        Log.info("Walking to next section");
        while (!Script.interrupted() && !Players.getLocal().getPosition().equals(posRequired)) {
            Time.sleep(800, 1800);
            daxWalker.walkTo(posRequired);
        }
        if (posRequired.distance(Players.getLocal()) < 4) {
            getEmptyPosition().ifPresent(position -> {
                if (Movement.getDestinationDistance() > 0)
                    daxWalker.walkTo(position);
            });
            //daxWalker.walkTo(Players.getLocal().getPosition().randomize(8));
            Time.sleep(1000);
            Time.sleepUntil(() -> !Players.getLocal().isMoving(), 2000, Beggar.randInt(2000, 6000));
        }
    }

    void daxWalker(Position position , Area stopArea) {
        daxWalker.walkTo(position, () -> {
            if (stopArea.contains(Players.getLocal()) || (Dialog.isOpen() && Dialog.canContinue())) {
                Time.sleep(1000, 5000);
                return true;
            }
            return false; // false to continue walking after check. true to exit out of walker.
        });
    }

    void daxWalker(Position position) {
        daxWalker.walkTo(position, () -> {
            if (Dialog.isOpen() && Dialog.canContinue()) {
                Time.sleep(1000, 5000);
                return true;
            }
            return false; // false to continue walking after check. true to exit out of walker.
        });
    }
}
