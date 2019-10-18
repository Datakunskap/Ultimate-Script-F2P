package script.fighter.ogress;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
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
        return !OgressWrapper.CORSAIR_COVE.contains(Players.getLocal()) &&
                !OgressWrapper.CORSAIR_COVE_DUNGEON.contains(Players.getLocal());
    }

    @Override
    public int execute() {
        main.invalidateTask(this);
        if (Dialog.canContinue())
            Dialog.processContinue();

        if (!hasQuest) {
            if (!Dialog.isOpen()) {
                if (!talkToTock(OgressWrapper.TOCK_QUEST_POSITION)) {
                    Logger.fine("Quest Obtained");
                    hasQuest = true;
                }
            } else if (Dialog.isViewingChatOptions()){
                Dialog.process("What kind of help do you need?",
                                 "Sure, I'll try to help with your curse.");
            }
        }
        else {
            if (!Dialog.isOpen()) {
                talkToTock(OgressWrapper.TOCK_BOAT_TO_COVE_POSITION);

            } else if (Dialog.isViewingChatOptions()) {
                Dialog.process("Okay, I'm ready go to Corsair Cove.");
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

    @Override
    public String status() {
        return status;
    }

    public GoToCove(Fighter main) {
        this.main = main;
    }
}
