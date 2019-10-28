package script.beg;

import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Script;

public class Beg extends Task {

    private Script main;

    public Beg(Script script){
        main = script;
    }

    @Override
    public boolean validate() {
        return main.beg && !main.trading;
    }

    @Override
    public int execute() {
        /*if (main.isBadInstanceTime())
            new StartupChecks(main).instanceCheck();*/

        Log.info("Begging");
        Keyboard.sendText(main.lines.getRandLine());
        Keyboard.pressEnter();
        main.numBegs ++;
        main.beg = false;
        main.walk = false;
        if(!main.iterAmount) {
            maybeAmount();
        }
        return 600;
    }

    private void maybeAmount(){
        if(Script.randInt(1, main.amountChance) == 1){
            main.changeAmount = true;
        }
    }
}
