package dax.api.combat.status;

import dax.api.utils.DaxListener;

public interface CombatStatusListener extends DaxListener<AttackEvent> {
    @Override
    void trigger(AttackEvent event);
}
