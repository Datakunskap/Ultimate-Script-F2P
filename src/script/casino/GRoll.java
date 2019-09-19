/*
package script.casino;

import org.fighter.runetek.api.commons.Time;
import org.fighter.runetek.api.input.Keyboard;
import org.fighter.script.task.Task;
import script.Beggar;

public class GRoll extends Task {

    @Override
    public boolean validate() {
        return Beggar.roll;
    }

    @Override
    public int execute() {
        Keyboard.sendText("Rolling.............");
        Keyboard.pressEnter();
        Time.sleep(Beggar.randInt(2000, 8000));

        int roll = Beggar.randInt(1, 100);

        String line;
        if (roll > 55) {
            line = Beggar.gamblerName + " rolled " + roll + "Winner!";
            Beggar.tradeGambler = true;
        } else {
            line = Beggar.gamblerName + " rolled " + "Im sorry, but you lost.";
        }
        Keyboard.sendText(line);
        Keyboard.pressEnter();
        Beggar.roll = false;
        return 5000;
    }
}
*/
