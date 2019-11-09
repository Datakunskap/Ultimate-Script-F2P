package script.beg;

import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.runetek.api.component.tab.Equipment;
import org.rspeer.runetek.api.component.tab.Inventory;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Script;
import script.data.CheckTutIsland;
import script.data.ClientQuickLauncher;
import script.data.QuestingDriver;
import script.fighter.wrappers.*;

import java.io.IOException;

public class StartupChecks extends Task {

    private Script main;

    public StartupChecks(Script main) {
        this.main = main;
    }

    @Override
    public boolean validate() {
        return !main.startupChecks && Worlds.getCurrent() > 0;
    }

    @Override
    public int execute() {
        if (!Game.isLoggedIn() || Players.getLocal() == null || Game.getState() != Game.STATE_IN_GAME ||
                Game.isLoadingRegion() || Game.getState() == Game.STATE_LOADING_REGION)
            return 5000;

        Log.fine("-Startup Checks-");
        main.startupChecks = true;
        CheckTutIsland checkT = new CheckTutIsland(main);

        if (checkT.onTutIsland()) {
            checkT.execute();
        }
        else if (Players.getLocal().getCombatLevel() <= 3) {
            instanceCheck();
            main.startFighter();
        }
        else if (isQuesting()) {
            Log.fine("Starting Quests");
            TeleportWrapper.tryTeleport(true);
            instanceCheck();
            new QuestingDriver(main).startSPXQuesting(Script.randInt(15, 20));
        }
        else if (isOgress() && !main.ogressBeg) {
            instanceCheck();
            main.startOgress();
        }
        else {
            Log.fine("Starting Beggar");
            instanceCheck();
            //addWorldToFile();
        }

        return 1000;
    }

    private boolean isQuesting() {
        if (isOgress()) {
            return !OgressWrapper.has7QuestPoints() && Skills.getLevel(Skill.MAGIC) >= 13;
        }
        return false;
    }

    private boolean isOgress() {
        return Script.OGRESS && (Skills.getLevel(Skill.MAGIC) >= 13
                || Equipment.contains("Cursed goblin staff")
                || (Script.OGRESS && Inventory.getCount(true, "Coins") >= Script.OGRESS_START_GP)
                || (Inventory.contains(r -> r.getName().equals("Air rune") && r.getStackSize() > 300)
                && Inventory.contains(r -> r.getName().equals("Mind rune") && r.getStackSize() > 300)));
    }


    public void instanceCheck() {
        ClientQuickLauncher launcher = new ClientQuickLauncher(
                "Ultimate Script", false, main.getNextWorld());

            while (!launcher.isInstanceLimit() && Game.isLoggedIn() && !main.isStopping()) {
                try {
                    main.accountGeneratorDriver(Script.NUM_BACKLOG_ACCOUNTS);
                    launcher.launchClient(main.readAccount(true));
                    if (!Script.MULTI_CLIENT_LAUNCH) {
                        break;
                    } else {
                        Time.sleep(60_000);
                    }
                } catch (IOException e) {
                    Log.severe(e);
                    e.printStackTrace();
                }
            }

        main.runningClients = launcher.getRunningClients();
    }

    private void addWorldToFile() {
        main.currWorld = Worlds.getCurrent();
        WorldhopWrapper.writeWorldToFile(main.currWorld, Script.CURR_WORLD_PATH);
    }
}
