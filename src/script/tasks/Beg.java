package script.tasks;

import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.input.Keyboard;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.task.Task;
import script.Beggar;

import java.awt.*;

public class Beg extends Task {

    @Override
    public boolean validate() {
        return Beggar.beg && Beggar.location.getBegArea().contains(Players.getLocal());
    }

    @Override
    public int execute() {
        Keyboard.sendText("Can someone pls double my " + Beggar.gp.getSgp() + " gold?");
        Keyboard.pressEnter();
        Beggar.walk = true;
        return 20000;
    }
}
