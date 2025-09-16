package utils;

import com.sun.istack.internal.NotNull;
import org.osbot.rs07.api.NPCS;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.NPC;
import org.osbot.rs07.api.model.Entity;

/**
 * Toon is a custom wrapper around OSBot's {@link NPC}.
 * It exposes all the standard NPC functionality, while also
 * allowing you to add your own custom methods and properties.
 *
 * This avoids the limitation that {@link NPC} cannot be subclassed,
 * by delegating (forwarding) all calls to the original NPC object.
 */
public class Toon {
    /** The underlying NPC object provided by the OSBot API */
    private final String name;
    private final Area location;
    private NPC npc;//TODO: adjust enum to suit

    public Toon(@NotNull NPC npc) {
        this.name = npc.getName();
        this.location = npc.getArea(1);
        this.npc = npc;
    }

    /**
     * Construct a new Toon wrapper for an existing NPC or just use known default values to make locating/interacting
     * with NPCs a little easier.
     *
     * @param name The name of this NPC
     * @param spawn The typical location of this NPC.
     * @param npc The NPC object to wrap.
     */
    public Toon(String name, Area spawn, NPC npc) {
        this.name = name;
        this.location = spawn;
        this.npc = npc;
    }

    /**
     * Create a toon by searching nearby NPCs (The NPC must be nearby for this to work)
     *
     * @param bot The {@link BotMan botting} instance used to find nearby NPCs.
     * @param name The name of the NPC to find.
     */
    public Toon(BotMan<?> bot, String name, Area spawn) {
        this.name = name;
        this.npc = bot.getNpcs().closest(name);
        this.location = npc.getArea(1);
    }

    // -------------------------------
    // 🔹 NPC STANDARD METHODS
    // -------------------------------

    /** @return NPC name, or null if unavailable */
    public String getName() {
        return npc.getName();
    }

    /** @return NPC unique ID */
    public int getId() {
        return npc.getId();
    }

    /** @return NPC current area (can be null if undefined) */
    public Area getArea(int radius) {
        return npc.getArea(radius);
    }

    /** @return NPC current area (can be null if undefined) */
    public Area getArea() {
        return npc.getArea(1);
    }

    public boolean isValid() {
        return npc != null && npc.exists();
    }

    public NPC getNpc(BotMan<?> bot) {
        if (!isValid()) {
            // refresh from world using name + area
            npc = bot.getNpcs().closest(n ->
                    n.getName().equals(name) &&
                            (location == null || location.contains(n))
            );
        }
        return npc;
    }

    /**
     * Returns a random tile within the {@link Area area} listed for this npc.
     *
     * @return
     */
    public Position getRoughPosition(BotMan<?> bot) {
        NPC active = getNpc(bot);
        if (active != null) return active.getPosition();
        // fallback: walk to static spawn area
        return location != null ? location.getRandomPosition() : null;
    }

    /** @return NPC's current health percent (0–100) */
    public int getHealthPercent() {
        return npc.getHealthPercent();
    }

    /** @return True if NPC is still valid in the game world */
    public boolean exists() {
        return npc.exists();
    }

    /**
     * Interact with this NPC using the passed action.
     *
     * @param action The interaction string (e.g., "Talk-to", "Attack").
     * @return True if the action was successfully triggered.
     */
    public boolean interact(BotMan<?> bot, String action) {
        NPC active = getNpc(bot);
        return active != null && active.interact(action);
    }

    /** @return The current entity this NPC is interacting with (can be player or another NPC) */
    public Entity getInteracting() {
        return npc.getInteracting();
    }

    /** @return True if NPC is currently under combat */
    public boolean isUnderAttack() {
        return npc.isUnderAttack();
    }

    /** @return True if NPC is currently animating (e.g., fighting, chopping, etc.) */
    public boolean isAnimating() {
        return npc.isAnimating();
    }

            // -------------------------------
            // 🔹 CUSTOM TOON EXTENSIONS
            // -------------------------------

    public boolean talkTo(BotMan<?> bot, String... options) throws InterruptedException {
        return bot.talkTo(this, options);
    }

    /**
     * Find the closest NPC matching multiple possible names and wrap it in a Toon.
     *
     * @param npcs   The OSBot NPC API handle (from Script#getNpcs()).
     * @param names  Array of possible names (e.g., {"Guard", "Man"}).
     * @return A Toon wrapping the NPC, or null if none found.
     */
    public static Toon createFromNames(NPCS npcs, String... names) {
        NPC npc = npcs.closest(names);
        return npc != null ? new Toon(npc) : null;
    }


    /**
     * Walk to this NPC then talk to them using the passed chat options where possible.
     *
     * @param bot The {@link BotMan bot} instance for script api.
     * @param options A {@link String[]} of chat dialogue options that will be used when conversing with this NPC.
     * @return {@link Boolean True} if the travelling or conversation loop is not prematurely broken.
     */
    public boolean walkAndTalk(BotMan<?> bot, String... options) throws InterruptedException {
        // self reference to walk and talk to npcs using this object
        if (!bot.walkTo(this))
            return !bot.setStatus("Unable to travel to \"" + getName() + "\"");
        return talkTo(bot, options);
    }

    /**
     * Walk to this NPC then perform the passed emote(s).
     *
     * @param bot The {@link BotMan bot} instance for script api.
     * @param emotes A {@link String[]} of chat dialogue options that will be used when conversing with this NPC.
     * @return {@link Boolean True} if this task completes successfully.
     */
    public boolean walkAndEmote(BotMan<?> bot, EmoteMan... emotes) throws InterruptedException {
        if (!bot.walkTo(this))
            return !bot.setStatus("Unable to travel to \"" + getName() + "\"");

        if (!bot.lookAt(this.getArea()))
            for (EmoteMan emote : emotes)
               if (!emote.perform(bot))
                   return true;

        return true;
    }

    /**
     * Check if this Toon is aggressive toward the player.
     *
     * @return True if the NPC is targeting the player.
     */
    public boolean isAggressiveToPlayer(BotMan<?> bot) {
        return npc != null && npc.getInteracting() != null
                && npc.getInteracting().equals(bot.myPlayer());
    }

    /**
     * Example custom method: check if this Toon is within a safe area.
     *
     * @param safeZone The area considered "safe".
     * @return True if NPC is inside the safe zone.
     */
    public boolean isInSafeZone(Area safeZone) {
        return npc != null && safeZone.contains(npc);
    }

    /**
     * @return The raw underlying OSBot NPC object.
     * Useful if you need direct API access.
     */
    public NPC unwrap() {
        return npc;
    }

    public NPC getNpc() {
        return npc;
    }
}




///
///     Filter through this stuff and see if we want to keep anything in the baove class:
///
///     custom toons and functions to implement later?
///

//package utils;
//
//import locations.TravelMan;
//import locations.cityLocations.VarrockLocation;
//import org.osbot.rs07.api.map.Area;
//import org.osbot.rs07.api.model.NPC;
//
//public enum Toon implements TravelMan {
//    CHARLIE_THE_TRAMP(VarrockLocation.BLACK_ARMS_GANG_ALLEY.getArea(), "Charlie the Tramp"),
//    HANS("Hans"),
//    GERTRUDE("Gertrude"),
//    URI("Uri");
//
//    Area area;
//    String name;
//    String description;
//    String[] fastDialogueOptions;
//
//    Toon(String name, String... fastDialogueOptions) {
//
//    }
//
//    Toon(Area area, String name, String... fastDialogueOptions) {
//
//    }
//
//    Toon(Area area, String name, String description, String... fastDialogueOptions) {
//
//    }
//
//    @Override
//    public Area getArea() {
//        return area;
//    }
//
//    @Override
//    public String getName() {return name;}
//
//    @Override
//    public String getDescription() {
//        return description;
//    }
//
//    public String[] getFastDialogueOptions() {
//        return fastDialogueOptions;
//    }
//
//    public NPC getNearest(BotMan<?> bot) {
//            return bot.getNpcs().closest(n -> n != null
//                    && n.getName() != null);
//    }
//
//    public boolean isVisible(BotMan<?> bot) {
//        return bot.getNpcs().closest(n -> n != null
//                && n.getName() != null
//                && n.getName().equalsIgnoreCase(getName())
//                && n.isVisible()) != null;
//    }
//

//
//}
