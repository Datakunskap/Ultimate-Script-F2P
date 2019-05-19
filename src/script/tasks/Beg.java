package script.tasks;

import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class Beg extends Task {

    @Override
    public boolean validate() {
        return Beggar.beg && !Beggar.trading;
    }

    @Override
    public int execute() {
        Log.info("Begging");
        Keyboard.sendText(Beggar.lines.getRandLine());
        Keyboard.pressEnter();
        Beggar.beg = false;
        Beggar.walk = false;
        if(!Beggar.iterAmount) {
            maybeAmount();
        }
        return 600;
    }

    public void maybeAmount(){
        if(Beggar.randInt(1, Beggar.amountChance) == 1){
            Beggar.changeAmount = true;
        }
    }
}
