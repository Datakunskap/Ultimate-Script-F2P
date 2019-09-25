package script.tutorial_island;

import org.rspeer.RSPeer;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Login;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.script.GameAccount;
import org.rspeer.ui.Log;
import script.Beggar;
import script.data.CheckTutIsland;
import script.fighter.Fighter;

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

    public void logoutAndSwitchAcc() {
        String currAcc = RSPeer.getGameAccount().getUsername();
        beggar.writeAccount(currAcc);

        if (Game.logout()) {
            RSPeer.setGameAccount(new GameAccount(beggar.readAccount(true), "plmmlp"));
            if (Time.sleepUntil(() -> !RSPeer.getGameAccount().getUsername().equals(currAcc), 20000))
                Log.fine("Account Switched");

            while(!Game.isLoggedIn() && !Login.getResponseLines()[0].toLowerCase().contains("disabled")) {
                Login.enterCredentials(RSPeer.getGameAccount().getUsername(), RSPeer.getGameAccount().getPassword());
                Time.sleep(200);
                Keyboard.pressEnter();
                Time.sleepUntil(() -> Game.isLoggedIn() || Login.getResponseLines()[0].toLowerCase().contains("disabled"), 2000, 10000);
            }
        }
    }

    public void startFighter() {
        //logoutAndSwitchAcc();
        beggar.resetRender();
        beggar.removeAll();
        beggar.fighter = new Fighter(beggar, Beggar.randInt(720_000, 1_200_000)); // 12 - 20
        beggar.fighter.onStart();
    }

    public void startFighter(Beggar script) {
        //logoutAndSwitchAcc();
        script.resetRender();
        script.removeAll();
        beggar.fighter = new Fighter(beggar, Beggar.randInt(720_000, 1_200_000)); // 12 - 20
        beggar.fighter.onStart();
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
