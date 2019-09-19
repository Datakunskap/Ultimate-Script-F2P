package script.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class BegLauncher {

    private String beggar;

    public BegLauncher(int beggarID, String path) {

        File beggarJSON = new File(path + beggarID + ".json");
        beggar = "\"C:\\Program Files\\Java\\jdk1.8.0_201\\bin\\java.exe\" -jar C:\\Users\\bllit\\OneDrive\\Desktop\\RSPeer\\RSPeer-Launcher " + beggarJSON;

        //System.exit(0);
    }

    public void launch() throws Exception {
        if (!launcher(beggar, 10)) {
            throw new Exception("FAILURE");
        }
    }

    private boolean launcher(String Beggar, int retries) {
        if (retries > 0) {
            //Process p1;
            try {
                //p1 = launchSimscape(TutIsland);
                //Thread.sleep(sleep);
                //processKill(exit, p1);
                //Thread.sleep(5000);
                launchBeggar(Beggar);
            } catch (Throwable t) {
                t.printStackTrace();
                launcher(Beggar, retries - 1);
                return false;
            } finally {
                if (errFileExists()) {
                    launcher(Beggar, retries - 1);
                }
                if (replayFileExists()) {
                    launcher(Beggar, retries - 1);
                }
            }
            return true;
        }
        return false;
    }

    private Process launchSimscape(String TutIsland) throws Exception {
        int tries = 0;
        Process p;
        do {
            p = Runtime.getRuntime().exec(
                    "cmd /c start cmd.exe /K \"" + TutIsland + "\"");
            p.waitFor(15000, TimeUnit.MILLISECONDS);
            Thread.sleep(20000);
            tries++;
        } while ((/*errCheck(p1) || */p.exitValue() != 0) && tries < 10);
        return p;
    }

    private void processKill(String exit, Process p1) throws Exception {
        Process pk = Runtime.getRuntime().exec(
                "cmd /c start cmd.exe /K \"" + "wmic Path win32_process Where \"CommandLine Like '%" + exit + "%'\" Call Terminate" + "\"");
        pk.waitFor(15000, TimeUnit.MILLISECONDS);
        pk.destroy();
        p1.destroy();
    }

    private void launchBeggar(String Beggar) throws Exception {
        int tries = 0;
        Process p2;
        do {
            p2 = Runtime.getRuntime().exec(
                    "cmd /c start cmd.exe /K \"" + Beggar + " && exit" + "\"");
            p2.waitFor(15000, TimeUnit.MILLISECONDS);
            Thread.sleep(15000);
            tries++;
        } while ((/*errCheck(p2) || */p2.exitValue() != 0) && tries < 10);
    }

    private boolean errFileExists() {
        String regex = "hs_err_pid[0-9]+.log";
        final File root = new File("C:\\Users\\bllit\\OneDrive\\Desktop");
        File[] errFiles = listFilesMatching(root, regex);

        if (errFiles != null && errFiles.length > 0) {
            for (File file : errFiles) {
                if (file != null && file.delete()) {
                    System.out.println("ERROR FILE FOUND & DELETED  |  RETRYING");
                }
            }
            return true;
        }
        return false;
    }

    private boolean replayFileExists() {
        String regex = "replay_pid[0-9]+.log";
        final File root = new File("C:\\Users\\bllit\\OneDrive\\Desktop");
        File[] replayFiles = listFilesMatching(root, regex);

        if (replayFiles != null && replayFiles.length > 0) {
            for (File file : replayFiles) {
                if (file != null && file.delete()) {
                    System.out.println("REPLAY FILE FOUND & DELETED  |  RETRYING");
                }
            }
            return true;
        }
        return false;
    }

    private boolean errCheck(Process p) throws IOException {
        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));

        // read the output from the command
        // System.out.println("Here is the standard output of the command:\n");
        if (stdInput.lines().anyMatch(x -> x.contains("ILLEGAL_INSTRUCTION")))
            return true;

        // read any errors from the attempted command
        // System.out.println("Here is the standard error of the command (if any):\n");
        if (stdError.lines().anyMatch(x -> x.contains("ILLEGAL_INSTRUCTION")))
            return true;

        return false;
    }

    private File[] listFilesMatching(File root, String regex) {
        if (!root.isDirectory()) {
            throw new IllegalArgumentException(root + " is no directory.");
        }
        final Pattern p = Pattern.compile(regex); // careful: could also throw an exception!
        return root.listFiles(file -> p.matcher(file.getName()).matches());
    }
}
