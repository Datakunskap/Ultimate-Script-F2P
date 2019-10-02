package dax.api.game;

import dax.api.utils.DaxListener;

public interface TickEventListener extends DaxListener<TickEvent> {
    @Override
    void trigger(TickEvent event);
}
