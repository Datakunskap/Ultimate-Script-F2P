package com.dax.walker.models;

import com.allatori.annotations.DoNotRename;

@DoNotRename
public class PathRequestPair {
    @DoNotRename
    private Point3D start;
    @DoNotRename
    private Point3D end;

    public PathRequestPair(Point3D start, Point3D end) {
        this.start = start;
        this.end = end;
    }

    public Point3D getStart() {
        return start;
    }

    public Point3D getEnd() {
        return end;
    }
}
