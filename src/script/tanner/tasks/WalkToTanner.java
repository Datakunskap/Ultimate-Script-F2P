package script.tanner.tasks;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.input.menu.ActionOpcodes;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.script.task.Task;
import script.tanner.Main;
import script.tanner.data.Location;

public class WalkToTanner extends Task {

    private Main main;
    private CommonConditions cc;

    public WalkToTanner (Main main) {
        this.main = main;
        cc = new CommonConditions(main);
    }

    @Override
    public boolean validate() {
        // True if player is far away from the tanner
        return (!cc.nearTanner() && cc.gotCowhide() && cc.gotEnoughCoins()) &&
                !main.restock && !main.isMuling;
    }

    @Override
    public int execute() {
        //Log.info("Walking to tanner");
        if (WalkingHelper.shouldEnableRun()) {
            WalkingHelper.enableRun();
        }

        // walk to toll-gate
        if (!main.paidToll && !Location.TOLL_GATE.getTollArea().contains(Players.getLocal())) {
            Movement.walkToRandomized(Location.TOLL_GATE.getTollArea().getCenter());
        }
        else if (!main.paidToll) {
            SceneObject gate = SceneObjects.getNearest(2883, 2882);
            if (gate != null)
                gate.interact(ActionOpcodes.OBJECT_ACTION_0);
                Time.sleepUntil(() -> Dialog.isOpen() && Dialog.canContinue(), 5000);
                while (Dialog.isOpen() && Dialog.canContinue()) {
                    Dialog.processContinue();
                    Time.sleep(1000, 2000);
                }
                Time.sleepUntil(() -> !Dialog.isProcessing(), 5000);
                Dialog.process(x -> x.toLowerCase().contains("yes"));
                Time.sleepUntil(Dialog::canContinue, 5000);

                while (Dialog.isOpen() && Dialog.canContinue()) {
                    InterfaceComponent fail = Interfaces.getComponent(217, 4);
                    if (fail != null && fail.isVisible() && fail.getText().toLowerCase().contains("oh dear")) {
                        main.paidToll = true;
                    }
                    Dialog.processContinue();
                    Time.sleep(1000, 2000);
                }
                if (Time.sleepUntil(() -> !Dialog.isOpen() && !Location.TOLL_GATE.getTollArea().contains(Players.getLocal()), 8000))
                    main.paidToll = true;
        }
        else {
            if (WalkingHelper.shouldSetDestination()) {
                if (Movement.walkToRandomized(main.TANNER_AREA.getCenter())) {
                    Time.sleepUntil(cc::nearTanner, Random.mid(1800, 2400));
                }
            }
        }
        return 600;
    }
}
