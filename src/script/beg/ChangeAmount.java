package script.beg;

import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;

public class ChangeAmount extends Task {

    private Beggar main;

    public ChangeAmount(Beggar beggar){
        main = beggar;
    }

    @Override
    public boolean validate() {
        return main.changeAmount && !main.trading;
    }

    @Override
    public int execute() {
        Log.info("Changing beg amount");
        if(main.iterAmount){
            iterAmount();
        } else {
            randAmount();
        }

        main.reloadLines();
        main.changeAmount = false;
        main.walk = true;
        main.sendTrade = true;
        main.beg = true;
        return 1000;
    }

    private void randAmount(){
        main.gp = main.gpArr.get(main.randInt(0, main.gpArr.size()-1));
    }

    private void iterAmount(){
        main.gp = main.gpArr.get(main.amntIndex);
        main.amntIndex++;
        if(main.amntIndex >= (main.gpArr.size()-1) / 2){
            main.iterAmount = false;
            main.amntIndex = 0;
        }
    }
}
