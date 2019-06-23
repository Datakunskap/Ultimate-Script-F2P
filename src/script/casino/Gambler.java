package script.casino;

import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class Gambler extends Task {

    @Override
    public boolean validate() {
        return Beggar.beg;
    }

    @Override
    public int execute() {
        Log.info("Advertising");

        Keyboard.sendText("1-100 Hosting 55x2 Max (50% CashStack)");
        Keyboard.pressEnter();
        Beggar.beg = false;

        return 1000;
    }
}
