package script.tasks;

import org.rspeer.script.task.Task;
import org.rspeer.ui.Log;
import script.Beggar;
import script.data.Lines;

public class ChangeAmount extends Task {


    @Override
    public boolean validate() {
        return Beggar.changeAmount && !Beggar.trading;
    }

    @Override
    public int execute() {
        Log.info("Changing beg amount");
        if(Beggar.iterAmount){
            iterAmount();
        } else {
            randAmount();
        }

        Beggar.reloadLines();
        Beggar.changeAmount = false;
        Beggar.walk = true;
        Beggar.sendTrade = true;
        Beggar.beg = true;
        return 1000;
    }

    public void randAmount(){
        Beggar.gp = Beggar.gpArr.get(Beggar.randInt(0, Beggar.gpArr.size()-1));
    }

    public void iterAmount(){
        Beggar.gp = Beggar.gpArr.get(Beggar.amntIndex);
        Beggar.amntIndex++;
        if(Beggar.amntIndex >= (Beggar.gpArr.size()-1) / 2){
            Beggar.iterAmount = false;
            Beggar.amntIndex = 0;
        }
    }
}
