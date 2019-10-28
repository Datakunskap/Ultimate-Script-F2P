import org.rspeer.script.ScriptMeta;
import org.rspeer.script.task.Task;
import org.rspeer.script.task.TaskScript;
import tasks.doBonding;

@ScriptMeta(developer = "Streagrem", name = "MortFungus", desc = "MortFungus")
public class Main extends TaskScript {

    private static final Task[] TASKS = {
            new doBonding()
    };

    @Override
    public void onStart() {
        submit(TASKS);
    }
}
