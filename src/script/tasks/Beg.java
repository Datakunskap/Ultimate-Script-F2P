package script.tasks;

import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class Beg extends Task {

    private Beggar main;

    public Beg(Beggar beggar){
        main = beggar;
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
        main.beg = false;
        main.walk = false;
        if(!main.iterAmount) {
            maybeAmount();
        }
        return 600;
    }

    private void maybeAmount(){
        if(Beggar.randInt(1, main.amountChance) == 1){
            main.changeAmount = true;
        }
    }
}
