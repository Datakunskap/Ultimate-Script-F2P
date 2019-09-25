package script.tasks;

import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import script.Beggar;
import script.data.CheckInstances;
import script.data.CheckTutIsland;

public class StartupChecks extends Task {

    private Beggar main;

    public StartupChecks(Beggar main){
        this.main = main;
    }

    @Override
    public boolean validate() {
        return !main.startupChecks && Game.isLoggedIn() && Worlds.getCurrent() > 0;
    }

    @Override
    public int execute() {
        if (!Game.isLoggedIn() || Players.getLocal() == null)
            return 2000;

        tutIslandCheck();
        fighterCheck();
        instanceCheck();
        addWorldToFile();
        main.startupChecks = true;
        return 1000;
    }

    private void tutIslandCheck() {
        CheckTutIsland checkT = new CheckTutIsland(main);

        if (checkT.onTutIsland()) {
            checkT.execute();
        }
    }

    private void fighterCheck() {
        if (Players.getLocal().getCombatLevel() <= 3)
            main.startFighter();
    }

    public void instanceCheck() {
        CheckInstances checkI = new CheckInstances(main);
        while (checkI.validate()) {
            checkI.execute(main.readAccount(true));
        }

        main.runningClients = checkI.getRunningClients();
        //main.checkBadInstances(main.cList, main.badInstances, 10);
    }

    private void addWorldToFile() {
        main.currWorld = Worlds.getCurrent();
        main.writeWorldToFile(main.currWorld);
    }
}
