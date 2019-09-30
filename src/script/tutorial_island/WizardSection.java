package script.tutorial_island;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.BankLocation;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.component.tab.Tab;
import org.rspeer.runetek.api.component.tab.Tabs;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import script.Beggar;
import script.fighter.Fighter;

import java.util.Arrays;
import java.util.List;

public final class WizardSection extends TutorialSection {

    private static final Area WIZARD_BUILDING = Area.polygonal(
            new Position(3142, 3091, 0),
            new Position(3143, 3090, 0),
            new Position(3143, 3083, 0),
            new Position(3138, 3082, 0),
            new Position(3141, 3087, 0),
            new Position(3138, 3091, 0)
    );

    private static final List<Position> PATH_TO_WIZARD_BUILDING = Arrays.asList(
            new Position(3122, 3101, 0),
            new Position(3125, 3097, 0),
            new Position(3127, 3093, 0),
            new Position(3129, 3088, 0),
            new Position(3135, 3087, 0),
            new Position(3141, 3086, 0)
    );

    private static final Area CHICKEN_AREA = Area.polygonal(
            new Position(3140, 3088),
            new Position(3140, 3089),
            new Position(3137, 3092),
            new Position(3141, 3092),
            new Position(3144, 3089),
            new Position(3144, 3088)
    );

    private TutorialIsland main;

    public WizardSection(TutorialIsland main) {
        super("Magic Instructor");
        this.main = main;
    }

    @Override
    public boolean validate() {
        return getTutorialSection() >= 18 && getTutorialSection() <= 20;
    }

    @Override
    public int execute() {
        if (pendingContinue()) {
            selectContinue();
            return TutorialIsland.getRandSleep();
        }

        if (getInstructor() == null && main.onTutorialIsland()) {
            //Time.sleepUntil(() -> Players.getLocal().isAnimating(), 2000, 5000);
            daxWalker(WIZARD_BUILDING.getCenter(), WIZARD_BUILDING);
            return Fighter.getLoopReturn();
        }

        switch (getProgress()) {
            case 620:
                talkToInstructor();
                break;
            case 630:
                Tabs.open(Tab.MAGIC);
                break;
            case 640:
                talkToInstructor();
                break;
            case 650:
                if (!CHICKEN_AREA.contains(Players.getLocal())) {
                    walkToChickenArea();
                } else {
                    attackChicken();
                }
                break;
            case 670:
                if (Dialog.isViewingChatOptions()) {
                    Dialog.process("No, I'm not planning to do that.", "Yes.", "I'm fine, thanks.");
                } else if (Magic.isSpellSelected()) {
                    Magic.interact(Magic.Autocast.getSelectedSpell(), "Cancel");
                } else {
                    talkToInstructor();
                }
                break;
        }

        if (!main.onTutorialIsland()) {
            switch (Beggar.randInt(0, 2)) {
                case 0:
                    getEmptyPosition(false, Beggar.TUTORIAL_COMPLETED_WALK_DIST).ifPresent(this::randWalker);
                    break;
                case 1:
                    randWalker(BankLocation.LUMBRIDGE_CASTLE.getPosition());
                    break;
                case 2:
                    randWalker(BankLocation.DRAYNOR.getPosition());
                    break;
            }
            main.beggar.startFighter(true);
        }

        return TutorialIsland.getRandSleep();
    }

    private boolean walkToChickenArea() {
        Movement.walkToRandomized(CHICKEN_AREA.getCenter());

        return CHICKEN_AREA.contains(Players.getLocal());
    }

    private boolean attackChicken() {
        Npc chicken = Npcs.getNearest("Chicken");
        if (chicken != null && Magic.cast(Spell.Modern.WIND_STRIKE, chicken)) {
            Time.sleepUntil(() -> getProgress() != 650, 2000, 3000);
            return true;
        }
        return false;
    }
}
