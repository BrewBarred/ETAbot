package locations;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.MagicSpell;
import utils.BotMan;
import utils.Rand;

/**
 * The travel manager which forces all locations to implement a travel function (because why have a location listed if
 * we can't go there? right?)
 * <p>
 * This also ensures consistency between {@link Location locations}. For example, this interface will encourage
 * inheritors to always have an {@link Area area} and {@link String description} of the listed location for easier
 * script management when walking to various location and outputting task descriptions.
 */
public interface TravelMan {
    ///
    ///     ~TRAVEL MANAGER~
    ///
    //
    static final String OPTIONAL_CONSTANT = "This can be used to force the same property across multiple enums and child interfaces when they inherit TravelMan";

    Area getArea();
    String getName();
    String getDescription();
    //TODO: Consider adding features here to collect data while a bot walks

    // default logic saves needing to override and write the same code in every inheriting enum
    default boolean walkTo(BotMan<?> bot) throws InterruptedException {
        if (getArea().contains(bot.myPlayer()))
                return true;

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

        // ensure the player meets the correct magic level, runes and spellbook for the passed spell
        if (bot.magic.canCast(teleport)) {
            // casts the spell
            bot.magic.castSpell(teleport);
            BotMan.sleep(Rand.getRandShortDelayInt());
            return true;
        }

        bot.setStatus("Failed to teleport to " + getArea() + "...");
        return false;
    }

    /**
     * Calculate and return the approximate distance from the passed {@link Position position} as an
     * {@link Integer integer} value
     *
     * @param pos The {@link Position position} used to calculate the distance from this city (current position recommended)
     * @return An {@link Integer integer} value equal to the distance between the passed {@link Position} 'pos' and this {@link Location location}.
     */
    default int distanceTo(Position pos) {
        return pos.distance(getArea().getCentralPosition());
    }

    default TravelMan asTravelMan() {
        return this;
    }
}