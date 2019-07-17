package script.tasks;

import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;
import script.automation.Management;

import java.io.IOException;

public class CheckInstances extends Task {

    private Beggar beggar;

    public CheckInstances(Beggar beggar) {
        this.beggar = beggar;
    }

    @Override
    public boolean validate() {
        try {
            return Management.getRunningClients().size() < 8;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int execute() {
        Log.fine("Launching another instance");

        int[] IDs = beggar.writeJson(beggar.readAccount());

        beggar.generateAccount(7);

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
        return 30000;
    }
}
