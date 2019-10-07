package script.data;

import api.bot_management.BotManagement;
import api.bot_management.data.LaunchedClient;
import api.bot_management.data.QuickLaunch;
import org.rspeer.runetek.api.commons.Time;
import org.rspeer.ui.Log;
import script.Beggar;

import java.io.IOException;
import java.util.List;

public class CheckInstances {

    private Beggar main;
    private List<LaunchedClient> runningClients;
    private boolean generatedAccounts = false;

    public CheckInstances(Beggar beggar) {
        main = beggar;
    }

    public boolean validate() {
        try {
            runningClients = BotManagement.getRunningClients();
            Log.fine(runningClients.size() + " Clients Running");

            for (int t = 0; t < 2; t++) {
                if (runningClients.size() < Beggar.ALLOWED_INSTANCES) {
                    Time.sleep(10000);
                    runningClients = BotManagement.getRunningClients();
                    Log.info(runningClients.size() + " Clients Running");
                }
            }

            return !main.isStopping() && runningClients.size() < Beggar.ALLOWED_INSTANCES;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<LaunchedClient> getRunningClients() {
        return runningClients;
    }

    public int execute(String[] accountInfo) {
        Log.fine("Launching another instance");

        if (!generatedAccounts) {
            main.accountGeneratorDriver(Beggar.NUM_BACKLOG_ACCOUNTS);
            generatedAccounts = true;
        }

        QuickLaunch quickLaunch = main.setupQuickLauncher(accountInfo);

        try {

            BotManagement.startClient(0, quickLaunch.get().toString(), 0, null, 1, 10);

        } catch (Exception e) {
            main.writeToErrorFile("CheckInstances.execute():  " + e.getMessage());
            Log.severe(e);
            e.printStackTrace();
            System.exit(1);
        }

        /*main.generateAccount(Beggar.NUM_BACKLOG_ACCOUNTS);
        int[] IDs = main.writeJson(main.readAccount(false));
        String path = "C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Beggar";

        try {
            new BegLauncher(IDs[0], path).launch();

            for (LaunchedClient client : Management.getRunningClients(main.API_KEY)) {
                if (client.getRunescapeEmail().equals(RSPeer.getGameAccount().getUsername())) {
                    client.kill(main.API_KEY);
                }
            }
        } catch (Exception e) {
            main.writeToErrorFile("Failed Launching Client");
            e.printStackTrace();
        }*/

        return 30000;
    }
}
