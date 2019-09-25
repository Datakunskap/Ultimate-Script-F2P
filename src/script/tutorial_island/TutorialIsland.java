package script.tutorial_island;

import org.rspeer.ui.Log;
import script.Beggar;
import script.data.CheckTutIsland;

public final class TutorialIsland {

    private static TutorialIsland main;
    public Beggar beggar;
    final int idleTutSection = Beggar.randInt(0, 20);
    boolean hasIdled = (Beggar.randInt(0, 10) == 0);

    private TutorialIsland(Beggar script) {
        beggar = script;
    }

    //method to return instance of class
    public static TutorialIsland getInstance(Beggar script) {
        if (main == null) {
            // if instance is null, initialize
            main = new TutorialIsland(script);
        }
        return main;
    }

    public boolean onTutorialIsland() {
        if (new CheckTutIsland(beggar).onTutIsland()) {
            return true;
        }
        Log.fine("Tutorial Island Complete");
        return false;
    }

    public static int getRandSleep(){
        return Beggar.randInt(1000, 2500);
    }

    public static int randomSectionRun;

    public void start() {
        beggar.removeAll();

        randomSectionRun = Beggar.randInt(2, 5);
        Log.fine("Starting Tutorial Island");

        beggar.submit(
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

    public TutorialIsland copy() {
        return new TutorialIsland(beggar);
    }
}
