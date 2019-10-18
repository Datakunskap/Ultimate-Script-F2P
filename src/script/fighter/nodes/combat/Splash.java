package script.fighter.nodes.combat;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.Dialog;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Skill;
import org.rspeer.runetek.api.component.tab.Skills;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.ui.Log;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.debug.Logger;
import script.fighter.framework.Node;

public class Splash extends Node {

    private Fighter main;
    private static final int STOP_LVL = 13;
    private String status;
    private Spell spell;
    private static boolean shiftPosition;

    public Splash(Fighter main) {
        this.main = main;
    }

    @Override
    public boolean validate() {
        if (Config.getProgressive().isSplash() && Config.hasRunes()) {
            spell = Config.getProgressive().getSpell();
            return true;
        }
        return false;
    }

    @Override
    public int execute() {
        invalidateTask(main.getActive());

        if (Dialog.canContinue()) {
            Dialog.processContinue();
            int lvl = Skills.getLevel(Skill.MAGIC);
            Log.fine("Magic LVL: " + lvl);
            if (lvl >= STOP_LVL) {
                //this.setStopping(true);
            }
        }

        if (!Magic.Autocast.isEnabled() || !Magic.Autocast.isSpellSelected(spell)) {
            Log.info("Setting autocast");
            Magic.Autocast.select(Magic.Autocast.Mode.OFFENSIVE, spell);
        }

        Npc npc = Npcs.getNearest("Mugger");
        if (npc != null && Players.getLocal().getTargetIndex() == -1 && !Players.getLocal().isAnimating()) {
            Log.info("Manual cast");
            if (!Magic.cast(spell, npc) || shiftPosition) {
                Log.info("Shifting position");
                Movement.walkTo(npc.getPosition().translate(Random.nextInt(-1, 1), Random.nextInt(-1, 1)));
                shiftPosition = false;
            }
        } else if (npc == null){
            Log.severe("Cant Find Npc");
        }

        return Random.high(2000, 5000);
    }

    @Override
    public String status() {
        return status;
    }

    public void invalidateTask(Node active) {
        if (active != null && !this.equals(active)) {
            Logger.debug("Node has changed.");
            active.onInvalid();
        }
        main.setActive(this);
    }

    public static void setShiftPosition(boolean shift) {
        shiftPosition = shift;
    }

}
