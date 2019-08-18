package script.data;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.ui.Log;
import script.Beggar;
import script.automation.Management;
import script.automation.data.LaunchedClient;

import java.io.IOException;
import java.util.List;

public class CheckInstances {

    private Beggar main;
    private List<LaunchedClient> runningClients;

    public CheckInstances(Beggar beggar) {
        main = beggar;
    }

    public boolean validate() {
        try {
            runningClients = Management.getRunningClients(main.API_KEY);
            Log.fine(runningClients.size() + " Clients Running");

            if (runningClients.size() < Beggar.ALLOWED_INSTANCES) {
                Time.sleep(10000);
                runningClients = Management.getRunningClients(main.API_KEY);
                if (runningClients.size() < Beggar.ALLOWED_INSTANCES) {
                    Time.sleep(10000);
                    runningClients = Management.getRunningClients(main.API_KEY);
                    if (runningClients.size() < Beggar.ALLOWED_INSTANCES) {
                        Time.sleep(10000);
                        runningClients = Management.getRunningClients(main.API_KEY);
                        if (runningClients.size() < Beggar.ALLOWED_INSTANCES) {
                            Time.sleep(10000);
                            runningClients = Management.getRunningClients(main.API_KEY);
                            if (runningClients.size() < Beggar.ALLOWED_INSTANCES) {
                                Time.sleep(10000);
                                runningClients = Management.getRunningClients(main.API_KEY);
                                if (runningClients.size() < Beggar.ALLOWED_INSTANCES) {
                                    Time.sleep(10000);
                                    runningClients = Management.getRunningClients(main.API_KEY);
                                    return !main.isStopping() && runningClients.size() < Beggar.ALLOWED_INSTANCES;
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<LaunchedClient> getRunningClients() {
        return runningClients;
    }

    public int execute() {
        Log.fine("Launching another instance");

        int[] IDs = main.writeJson(main.readAccount());

        main.generateAccount(Beggar.NUM_BACKLOG_ACCOUNTS);

        String path1 = "C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Simscape";
        String path2 = "C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\Beggar";
        int sleep = Beggar.randInt(900000, 1200000);
        String javaVersion = "java";//"\"C:\\Program Files\\Java\\jdk1.8.0_201\\bin\\java.exe\"";
        String launcher = javaVersion + " -jar C:\\Users\\bllit\\OneDrive\\Desktop\\BegLauncher.jar "
                + IDs[0] + " " + IDs[1] + " " + path1 + " " + path2 + " " + sleep + " && exit";

        try {
            Runtime.getRuntime().exec(
                    "cmd /c start cmd.exe /K \"" + launcher + "\"");

        } catch (Exception e) {
            System.out.println("HEY Buddy ! U r Doing Something Wrong ");
            e.printStackTrace();
        }

        try {
            int retries = 10;
            do {
                runningClients = Management.getRunningClients(main.API_KEY);
                Time.sleep(5000);
                retries--;
            } while (runningClients.size() <= 0 && retries > 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 30000;
    }
}
