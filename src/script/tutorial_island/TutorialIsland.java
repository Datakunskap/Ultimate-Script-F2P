package script.tutorial_island;

import org.rspeer.ui.Log;
import script.Script;
import script.data.CheckTutIsland;

public final class TutorialIsland {

    private static TutorialIsland main;
    public Script script;
    final int idleTutSection = Script.randInt(0, 20);
    boolean hasIdled = !Script.TUTORIAL_IDLE;
    boolean isIdling;

    private TutorialIsland(Script script) {
        this.script = script;
    }

    //method to return instance of class
    public static TutorialIsland getInstance(Script script) {
        if (main == null) {
            // if instance is null, initialize
            main = new TutorialIsland(script);
        }
        return main;
    }

    public boolean onTutorialIsland() {
        if (new CheckTutIsland(script).onTutIsland()) {
            return true;
        }
        Log.fine("Tutorial Island Complete");
        return false;
    }

    public static int getRandSleep(){
        return Script.randInt(1000, 2500);
    }

    public static int randomSectionRun;

    public void start() {
        script.removeAll();

        randomSectionRun = Script.randInt(2, 5);
        Log.fine("Starting Tutorial Island");

        if (!Script.EXPLV_TUTORIAL) {
            script.submit(new dqw4w9wgxcq(main));
        } else {
            script.submit(
                    new Idle(this),
                    new RuneScapeGuideSection(),
                    new EnableRun(),
                    new SurvivalSection(),
                    new CookingSection(),
                    new QuestSection(),
                    new MiningSection(),
                    new FightingSection(),
                    new BankSection(),
                    new PriestSection(),
                    new WizardSection(this)
            );
        }
    }

    public boolean isStopping(){
        return script.isStopping();
    }

    public TutorialIsland copy() {
        return new TutorialIsland(script);
    }
}
