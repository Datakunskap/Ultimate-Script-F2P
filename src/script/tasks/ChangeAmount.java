package script.tasks;

import org.rspeer.script.task.Task;
import script.Beggar;
import script.data.Lines;

public class ChangeAmount extends Task {

    private int index = 1;

    @Override
    public boolean validate() {
        return Beggar.changeAmount && !Beggar.trading;
    }

    @Override
    public int execute() {
        if(Beggar.iterAmount){
            iterAmount();
        } else {
            randAmount();
        }

        Beggar.reloadLines();
        Beggar.changeAmount = false;
        Beggar.walk = true;
        Beggar.beg = true;
        return 1000;
    }

    public void randAmount(){
        Beggar.gp = Beggar.gpArr.get(Beggar.randInt(0, Beggar.gpArr.size()-1));
    }

    public void iterAmount(){
        Beggar.gp = Beggar.gpArr.get(index);
        index++;
        if(index == Beggar.gpArr.size()-1){
            Beggar.iterAmount = false;
            index = 0;
        }
    }
}
