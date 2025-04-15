package utils;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.utility.ConditionalSleep;

public class Walking {

    public static void walkTo(Script script, Area area) {
        if (area.contains(script.myPlayer())) {
            script.log("Already in area, skipping walk.");
            return;
        }

        Position randomPosition = getRandomPosition(area);

        if (script.getWalking().webWalk(randomPosition)) {
            new ConditionalSleep(script.random(1500, 2500), script.random(5000)) {
                @Override
                public boolean condition() throws InterruptedException {
                    script.log("Walking... Condition: " + area.contains(script.myPlayer()));
                    return area.contains(script.myPlayer());
                }
            }.sleep();
        }
    }

    private static Position getRandomPosition(Area area) {
        Position pos = area.getRandomPosition();
        int randX = getRandomOffset();
        int randY = getRandomOffset();
        return new Position(pos.getX() + randX, pos.getY() + randY, pos.getZ());
    }

    private static int getRandomOffset() {
        return (int) (Math.random() * 3) - 1; // -1, 0, or +1
    }
}
