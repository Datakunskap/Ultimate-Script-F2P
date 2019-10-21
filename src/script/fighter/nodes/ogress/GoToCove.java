package script.fighter.nodes.ogress;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import script.fighter.CombatStore;
import script.fighter.Fighter;
import script.fighter.debug.Logger;
import script.fighter.framework.Node;
import script.fighter.wrappers.OgressWrapper;

public class GoToCove extends Node {

    private Fighter main;
    private boolean hasQuest;
    private String status;

    @Override
    public boolean validate() {
        return !OgressWrapper.CORSAIR_COVE_DUNGEON.contains(Players.getLocal());
    }

    @Override
    public int execute() {
        main.invalidateTask(this);
        if (Dialog.canContinue())
            Dialog.processContinue();

        if (shouldEnableRun())
            enableRun();

        if (!OgressWrapper.CORSAIR_COVE[1].contains(Players.getLocal()) &&
                !OgressWrapper.CORSAIR_COVE[0].contains(Players.getLocal())) {
            if (!hasQuest) {
                status = "Getting quest";
                if (!Dialog.isOpen()) {
                    if (!talkToTock(OgressWrapper.TOCK_QUEST_POSITION)) {
                        Logger.fine("Quest Obtained");
                        hasQuest = true;
                    }
                } else if (Dialog.isViewingChatOptions()) {
                    Dialog.process("What kind of help do you need?",
                            "Sure, I'll try to help with your curse.");
                }
            } else {
                status = "Walking to boat";
                if (!Dialog.isOpen()) {
                    talkToTock(OgressWrapper.TOCK_BOAT_TO_COVE_POSITION);

                } else if (Dialog.isViewingChatOptions()) {
                    Dialog.process("Okay, I'm ready go to Corsair Cove.",
                            "Let's go.");
                    //if (Dialog.isViewingChatOptions() && Dialog.getChatOption(o -> o.contains("back to Rimmington.")) != null)
                }
            }
        }
        else if(OgressWrapper.CORSAIR_COVE[1].contains(Players.getLocal())) {
            status = "Get off boat";
            SceneObject plank = SceneObjects.getNearest("Gangplank");
            if (plank != null) {
                plank.interact("Cross");
            }
        }
        else if(OgressWrapper.CORSAIR_COVE[0].contains(Players.getLocal())) {
            status = "Walking to dungeon";
            SceneObject hole = SceneObjects.getNearest("Hole");
            if (hole != null && Movement.isInteractable(hole, true)) {
                hole.interact("Enter");
                CombatStore.resetTargetingValues();
            } else {
                Movement.walkTo(OgressWrapper.DUNGEON_ENTRANCE);
            }
        }
        return 1000;
    }

    private boolean talkToTock(Position position) {
        Npc tock = Npcs.getNearest("Captain Tock");
        if (tock == null && position.distance() > 3) {
            status = "Walking to Captain Tock";
            Movement.walkTo(position);

        }else if (tock == null && position.distance() <= 1) {
            Logger.debug("Captain Tock no longer at position");
            return false;

        } else if (tock != null) {
            tock.interact("Talk-to");
        }
        return true;
    }

    public static boolean shouldEnableRun() {
        if (Movement.isRunEnabled()) {
            return false;
        }
        if (Random.nextInt(1, 100) == 1) {
            // sometimes I like to random enable run
            return true;
        }
        if (Players.getLocal().getHealthPercent() < 20) {
            return true;
        }
        return Movement.getRunEnergy() > Random.nextInt(12, 30);
    }

    public static void enableRun() {
        Movement.toggleRun(true);
        Time.sleepUntil(Movement::isRunEnabled, 500);
    }

    @Override
    public String status() {
        return status;
    }

    public GoToCove(Fighter main) {
        this.main = main;
    }
}
