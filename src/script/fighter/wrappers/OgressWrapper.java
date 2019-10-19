package script.fighter.wrappers;

import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;

public class OgressWrapper {

    public static final Position TOCK_QUEST_POSITION = new Position(3030, 3273, 0);

    public static final Position TOCK_BOAT_TO_COVE_POSITION = new Position(2911, 3226, 0);

    public static final Area CORSAIR_COVE_DUNGEON = Area.rectangular(1988, 9014, 2028, 8962, 1);

    public static final Area[] CORSAIR_COVE = {Area.rectangular(2508, 2878, 2604, 2831, 0),
            Area.rectangular(2508, 2878, 2604, 2831, 1)};

}
