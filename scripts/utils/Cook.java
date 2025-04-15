package utils;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;

public enum Cook {
    VARROCK_EAST(new Position(3253, 3420, 0), new Position(3238, 3409, 0), new Area(3250, 3424, 3257, 3416), new Area(3235, 3416, 3241, 3402), new Area(3232, 3432, 3263, 3396)),
    FALADOR(new Position(3012, 3355, 0), new Position(2989, 3365, 0), new Area(3009, 3358, 3021, 3353), new Area(2988, 3367, 2991, 3363), new Area(2969, 3379, 3022, 3352)),
    ;

    private Position bankTile, entityTile;
    private Area bankZone, cookZone, pathZone;

    Cook(Position bankPos, Position entityPos, Area bankArea, Area cookArea, Area pathArea) {
        this.bankTile = bankPos;
        this.entityTile = entityPos;
        this.bankZone = bankArea;
        this.cookZone = cookArea;
        this.pathZone = pathArea;
    }

    public Position getBankTile() {
        return bankTile;
    }

    public Position getEntityTile() {
        return entityTile;
    }

    public Area getBankZone() {
        return bankZone;
    }

    public Area getCookZone() {
        return cookZone;
    }

    public Area getPathZone() {
        return pathZone;
    }

}