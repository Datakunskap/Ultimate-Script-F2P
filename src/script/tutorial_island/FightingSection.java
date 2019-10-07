package script.tutorial_island;

import org.rspeer.runetek.adapter.component.InterfaceComponent;
import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.adapter.scene.SceneObject;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Interfaces;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.runetek.api.input.menu.ActionOpcodes;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.runetek.api.scene.SceneObjects;
import org.rspeer.ui.Log;

public final class FightingSection extends TutorialSection {

    private static final Area LADDER_AREA = Area.rectangular(3108, 9523, 3114, 9529);
    private static final Area INSIDE_RAT_CAGE_GATE_AREA = Area.rectangular(3107, 9517, 3110, 9520);
    private static final Area OUTSIDE_RAT_CAGE_GATE_AREA = Area.rectangular(3111, 9516, 3113, 9521);

    public FightingSection() {
        super("Combat Instructor");
    }

    @Override
    public boolean validate() {
        return getTutorialSection() >= 10 && getTutorialSection() <= 12;
    }

    @Override
    public int execute () {
        if (pendingContinue()) {
            selectContinue();
            return TutorialIsland.getRandSleep();
        }

        InterfaceComponent VIEW_EQUIPMENT_STATS_WIDGET = Interfaces.getComponent(387, 17);

        SceneObject gate = SceneObjects.getNearest("Gate");
        if (getInstructor() == null && gate != null && getProgress() < 390) {
            Movement.walkTo(gate.getPosition());
            if (gate.isPositionInteractable() && Players.getLocal().getPosition().distance(gate) < 3) {
                Log.info("Opening gate");
                gate.interact("Open");
            }
            Time.sleepUntil(() -> getInstructor() != null, 2000, 8000);
        }

        switch (getProgress()) {
            case 370:
                talkToInstructor();
                break;
            case 390:
                Tabs.open(Tab.EQUIPMENT);
                break;
            case 400:
                if (VIEW_EQUIPMENT_STATS_WIDGET.interact(ActionOpcodes.INTERFACE_ACTION)) {
                    Time.sleepUntil(() -> getProgress() != 400, 2000, 3000);
                }
                break;
            case 405:
                wieldItem("Bronze dagger");
                break;
            case 410:
                talkToInstructor();
                break;
            case 420:
                if (!Equipment.contains("Bronze sword")) {
                    wieldItem("Bronze sword");
                } else if (!Equipment.contains("Wooden shield")) {
                    wieldItem("Wooden shield");
                }
                break;
            case 430:
                Tabs.open(Tab.COMBAT);
                break;
            case 440:
                enterRatCage();
                break;
            case 450:
            case 460:
                if (!inRatCage()) {
                    enterRatCage();
                } else if (!isAttackingRat()) {
                    attackRat();
                }
                break;
            case 470:
                if (inRatCage()) {
                    leaveRatCage();
                } else {
                    talkToInstructor();
                }
                break;
            case 480:
            case 490:
                if (!Equipment.contains("Shortbow")) {
                    wieldItem("Shortbow");
                } else if (!Equipment.contains("Bronze arrow")) {
                    wieldItem("Bronze arrow");
                } else if (!isAttackingRat()) {
                    attackRat();
                }
                break;
            case 500:
                if (!LADDER_AREA.contains(Players.getLocal())) {
                    Movement.walkToRandomized(LADDER_AREA.getCenter());
                } else if (SceneObjects.getNearest("Ladder").interact("Climb-up")) {
                    if(Time.sleepUntil(() -> !LADDER_AREA.contains(Players.getLocal()), 2000, 6000)) {
                        randWalker(Players.getLocal().getPosition());
                    }
                }
                break;
        }
        return TutorialIsland.getRandSleep();
    }

    private boolean inRatCage() {
        return Npcs.getNearest("Combat Instructor") != null && !Npcs.getNearest("Combat Instructor").isPositionInteractable();//!Movement.getReachableMap().isReachable(Npcs.getNearest("Combat Instructor"), true);//!getMap().canReach(getNpcs().closest("Combat Instructor"));
    }

    private void enterRatCage() {
        if (!OUTSIDE_RAT_CAGE_GATE_AREA.contains(Players.getLocal())) {
            Movement.walkToRandomized(OUTSIDE_RAT_CAGE_GATE_AREA.getCenter());
        } else if (SceneObjects.getNearest("Gate").interact("Open")) {
            if (Time.sleepUntil(this::inRatCage, 2000,5000)) {
                randWalker(Players.getLocal().getPosition());
            }
        }
    }

    private void leaveRatCage() {
        if (!INSIDE_RAT_CAGE_GATE_AREA.contains(Players.getLocal())) {
            Movement.walkToRandomized(INSIDE_RAT_CAGE_GATE_AREA.getCenter());
        } else if (SceneObjects.getNearest("Gate").interact("Open")) {
            if (Time.sleepUntil(() -> !inRatCage(), 2000, 5000)) {
                randWalker(Players.getLocal().getPosition());
            }
        }
    }

    private boolean isAttackingRat() {
        return Players.getLocal().isAnimating() && Players.getLocal().isFacing(Npcs.getNearest("Giant rat"));
    }

    private void attackRat() {
        //noinspection unchecked
        Npc giantRat = Npcs.getNearest(npc -> npc.getName().equals("Giant rat"));

        if (giantRat != null && giantRat.interact("Attack")) {
            Time.sleepUntil(() -> Players.getLocal().isAnimating(), 2000, 5000);
        }
    }

    private void wieldItem(String name) {
        if (Inventory.getFirst(name).interact("Wield") || Inventory.getFirst(name).interact("Equip")) {
            Time.sleepUntil(() -> Equipment.contains(name), 2000, 1500);
        }
    }
}
