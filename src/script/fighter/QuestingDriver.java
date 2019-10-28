package script.fighter;

import org.rspeer.RSPeer;
import org.rspeer.runetek.api.Game;
import org.rspeer.runetek.api.Worlds;
import org.rspeer.script.GameAccount;
import org.rspeer.ui.Log;
import script.Script;
import script.data.ClientQuickLauncher;

import java.io.IOException;

public class QuestingDriver {

    private Script script;
    private String killKey;

    public QuestingDriver(Script script) {
        this.script = script;
    }

    public QuestingDriver(Script script, String killKey) {
        this.script = script;
        this.killKey = killKey;
    }

    public void startSPXQuesting(int sleepMinutesUntilScriptRestart) {
        String[] scriptArgs = new String[]{
                /*"-quests SHEEP_SHEARER,RUNE_MYSTERIES,ROMEO_AND_JULIET -randomizeOrder true",
                "-quests SHEEP_SHEARER,THE_RESTLESS_GHOST,ROMEO_AND_JULIET -randomizeOrder true",*/
                "-quests RUNE_MYSTERIES,THE_RESTLESS_GHOST,ROMEO_AND_JULIET -randomizeOrder true"
        };

        ClientQuickLauncher launcher = new ClientQuickLauncher(
                "[PREMIUM] [SPX] AIO Questing", true, Worlds.getCurrent(),
                scriptArgs[0/*Script.randInt(0, scriptArgs.length - 1)*/], getSetKillKey());

        GameAccount account = RSPeer.getGameAccount();
        String[] accountInfo = new String[] { account.getUsername(), account.getPassword() };
        try {
            Game.logout();
            launcher.launchClient(accountInfo);
            startRestartScript(accountInfo, sleepMinutesUntilScriptRestart);
            script.killClient();

        } catch (IOException e) {
            Log.severe(e);
        }
    }

    private void startRestartScript(String[] accountInfo, int sleepMinutesUntilScriptRestart) {
        String email = accountInfo[0];
        String password = accountInfo[1];
        String command = "java -jar " + Script.RESTART_SCRIPT_PATH + " ";
        String args;

        String proxyIp = null;
        if (Script.PROXY_IP.length > 0) {
            proxyIp = script.getProxyIp(email);
        }

        if (proxyIp != null) {
            args = "-sleep " + sleepMinutesUntilScriptRestart + " " +
                    "-killEmail " + "true" + " " +
                    "-email " + email + " " +
                    "-password " + password + " " +
                    "-world " + Worlds.getCurrent() + " " +
                    "-apiKey " + Script.API_KEY + " " +
                    "-proxyIp " + proxyIp + " " +
                    "-proxyPort " + Script.PROXY_PORT + " " +
                    "-killKey " + getSetKillKey();

        } else {
            args = "-sleep " + sleepMinutesUntilScriptRestart + " " +
                    "-killEmail " + "true" + " " +
                    "-email " + email + " " +
                    "-password " + password + " " +
                    "-world " + Worlds.getCurrent() + " " +
                    "-apiKey " + Script.API_KEY + " " +
                    "-killKey " + getSetKillKey();
        }

        command = command + args + " && exit";

        try {
            Runtime.getRuntime().exec(
                    "cmd /c start cmd.exe /K \"" + command + "\"");
        } catch (IOException e) {
            Log.severe(e);
            script.writeToErrorFile(e.toString());
            e.printStackTrace();
        }
    }

    private String getSetKillKey() {
        if (killKey == null || killKey.isEmpty() || killKey.isBlank()) {
            for (int i = 0; i < 3; i ++) {
                killKey += Script.randInt(100, 999) + "/KILL/";
            }
        }
        return  killKey;
    }
}
