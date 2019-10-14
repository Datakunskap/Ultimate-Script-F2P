package script.fighter.nodes.combat;

import org.rspeer.runetek.adapter.scene.Npc;
import org.rspeer.runetek.api.commons.math.Random;
import org.rspeer.runetek.api.component.tab.Magic;
import org.rspeer.runetek.api.component.tab.Spell;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.scene.Npcs;
import org.rspeer.runetek.api.scene.Players;
import script.fighter.Fighter;
import script.fighter.config.Config;
import script.fighter.framework.Node;

public class Splash extends Node {

    private String status;
    private Spell spell;

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
        Npc npc = Npcs.getNearest("Mugger");
        if (npc != null && Movement.isInteractable(npc, false)) {
            status = "Splashing";
            if (!Magic.Autocast.isEnabled() || !Magic.Autocast.isSpellSelected(spell)) {
                Magic.Autocast.select(Magic.Autocast.Mode.OFFENSIVE, spell);
            }
            if (Players.getLocal().getTargetIndex() == -1 && !Players.getLocal().isAnimating()) {
                Magic.cast(spell, npc);
            }
        } else if (npc != null) {
            status = "Shifting position";
            Movement.walkTo(npc.getPosition().translate(Random.nextInt(-1, 1), Random.nextInt(-1, 1)));
        }

        return Fighter.getLoopReturn();
    }

    @Override
    public String status() {
        return status;
    }

}
