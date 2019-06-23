package script.data;

import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.movement.position.Position;

public enum Location {

    GE_AREA_MED(Area.polygonal(
            new Position[] {
                    new Position(3154, 3500, 0),
                    new Position(3164, 3504, 0),
                    new Position(3176, 3500, 0),
                    new Position(3179, 3489, 0),
                    new Position(3175, 3479, 0),
                    new Position(3165, 3474, 0),
                    new Position(3154, 3479, 0),
                    new Position(3150, 3490, 0)
            }
    )),

    GE_AREA_LARGE(Area.polygonal(
            new Position[] {
                    new Position(3148, 3490, 0),
                    new Position(3152, 3501, 0),
                    new Position(3165, 3506, 0),
                    new Position(3176, 3502, 0),
                    new Position(3181, 3489, 0),
                    new Position(3177, 3477, 0),
                    new Position(3164, 3473, 0),
                    new Position(3152, 3477, 0)
            })),

    GE_AREA(Area.polygonal(
            new Position[] {
        new Position(3161, 3498, 0),
                new Position(3168, 3498, 0),
                new Position(3173, 3493, 0),
                new Position(3173, 3486, 0),
                new Position(3167, 3480, 0),
                new Position(3161, 3481, 0),
                new Position(3156, 3486, 0),
                new Position(3156, 3493, 0)
    }));

    private Area begArea;

    Location(Area begArea) {
        this.begArea = begArea;
    }

    public Area getBegArea() {
        return begArea;
    }
}