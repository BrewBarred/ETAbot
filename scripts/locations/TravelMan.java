package locations;

import com.sun.istack.internal.NotNull;
import locations.cities.AlKharidLocation;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.MagicSpell;
import utils.BotMan;
import utils.Rand;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * The travel manager which forces all locations to implement a travel function (because why have a location listed if
 * we can't go there? right?)
 * <p>
 * This also ensures consistency between {@link Locations locations}. For example, this interface will encourage
 * inheritors to always have an {@link Area area} and {@link String description} of the listed location for easier
 * script management when walking to various location and outputting task descriptions.
 */
public interface TravelMan {
    ///
    ///     ~TRAVEL MANAGER~
    ///
    //
    static final String OPTIONAL_CONSTANT = "This can be used to force the same property across multiple enums and child interfaces when they inherit TravelMan";

    /**
     * @return The {@link Area area} of this {@link TravelMan location}.
     */
    Area getArea();

    /**
     * @return The name of this location, for display purposes.
     */
    String getName();

    /**
     * @return A detailed description of this location. This will later be used to feed an AI bot some information.
     */
    String getDescription();

    //TODO: Consider adding features here to collect data while a bot walks

    /**
     * Enables the simple retrieval and grouping of different {@link Locations location} types.
     * <p>
     * Usage: City[] allCities = City.values()
     *
     * @param values Implicitly converts the collection of enum values and returns it as a stream object when values()
     *               is called for easier manipulation.
     * @return A stream containing the enums collection of values.
     * @param <E> A generic type which allows enum items to be displayed as a {@link AlKharidLocation} for
     *           example, as opposed to everything being a {@link TravelMan} and requiring explicit conversion.
     */
    default <E extends Enum<E> & TravelMan> Stream<E> stream(E[] values) {
        return Arrays.stream(values);
    }
    /**
     * Provides a default and centralized method of walking to a location.
     * <p>
     * By centralizing this logic, it is easy to adjust how a player walks to a location (e.g., add preferred paths or
     * realism to the walk event, prompt bot to catch passed implings on the way etc.
     * <p>
     * Also, doing this way allows everything to be accessed from one root class for simpler scripting, imports and
     * future-proofing for AI upgrades later.
     *
     * @param bot The {@link BotMan bot} being controlled.
     * @return {@link Boolean True} if the bot successfully walks to this location, else returns false.
     */
    default boolean walkTo(BotMan<?> bot) throws InterruptedException {
        // cant run if you got no legs!
        if (bot == null)
            return false;

        // no point in trying to walk somewhere if you're already there!
        if (getArea().contains(bot.myPlayer()))
                return true;

        //TODO: consider implementing attempt counter here or setting up task manager and feeding all tasks through task man for the attempt count

        // attempt to walk
        try {
            bot.setStatus("Attempting to walk to " + getArea() + "...");
            bot.getWalking().webWalk(getArea().getRandomPosition());
        } catch (Exception e) {
            //TODO: change this logic to use alternative locations where possible? Just not sure on the best way to
            // decide which alternative location to use or when. Consider using the Task attempts perhaps?
            // Make task return the attempt count!! Then use that attempt count as the alternative option.
            bot.getWalking().webWalk(getArea().getCentralPosition());
            return getArea().contains(bot.myPlayer());
        }
        return bot.getWalking().webWalk(getArea().getRandomPosition());
    }

    default boolean TeleTo(BotMan<?> bot, MagicSpell teleport) throws InterruptedException {
        // write me a script here to teleport to getArea();
        bot.setStatus("Attempting to teleport to " + getArea() + "...", true);

        //TODO: Consider moving this logic to bot man since it can be used for any spells, and referencing it here

        // ensure the player meets the correct magic level, runes and spellbook for the passed spell?
        if (bot.magic.canCast(teleport)) {
            // casts the spell
            bot.magic.castSpell(teleport);
            BotMan.sleep(Rand.getRandShortDelayInt());
            return true;
        }

        bot.setStatus("Failed to teleport to " + getArea() + "...");
        return false;
    }

//    /**
//     * Calculate and return the approximate distance from the passed {@link Position position} as an
//     * {@link Integer integer} value
//     *
//     * @param pos The {@link Position position} used to calculate the distance from this city (current position recommended)
//     * @return An {@link Integer integer} value equal to the distance between the passed {@link Position} 'pos' and this {@link Locations location}.
//     */
//    default int distanceTo(Position pos) {
//        return pos.distance(getArea().getCentralPosition());
//    }

    /**
     * Return the distance from the passed position to this location.
     *
     * @param pos The {@link Position position} to calculate the distance from.
     * @return An {@link Integer} value representing the distance from this location to the passed {@link Position}.
     */
    default int distanceFrom(Position pos) {
        return getArea().getCentralPosition().distance(pos);
    }

    default TravelMan asTravelMan() {
        return this;
    }

    /**
     * Check if the passed Position is inside any of the passed {@link TravelMan} areas.
     */
    static boolean validatePosition(@NotNull BotMan<?> bot, @NotNull TravelMan... locations) {
        // iterate through each of the passed locations and check if your player is contained in any of them ;)
        for (TravelMan location : locations)
            if (location.getArea().contains(bot.myPlayer()))
                return true;
        return false;
    }
}