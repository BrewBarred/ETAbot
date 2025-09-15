package locations.clueLocations.beginner;

// CHARLIE NOTES:

// TRIGGER CLUE HINT: "Talk to Charlie the Tramp in Varrock."

// MAP(Area area, int mapId, String description)

// IRON MEN QUICK-SOURCE:
//Iron ore can be bought from the Ore Seller at the Blast Furnace. There are also iron rocks in the nearby South-west Varrock mine.
//Iron daggers can be bought from the Varrock Swordshop just north of Charlie. There is also an item spawn up at the goblin house in Lumbridge.
//Raw herring can be purchased at Frankie's Fishing Emporium in Port Piscarilius, or be caught by hand in Draynor Village.
//Raw trout can be fished with a fly fishing rod and feathers at Barbarian Village or Lumbridge river (or purchased at Rufus' Meat Emporium in Canifis) and be cooked into Trout if needed.
//Pike similarly can be fished raw with a fishing rod and bait at lure/bait fishing spots at Barbarian Village or Lumbridge river (or purchased raw at Rufus' Meat Emporium in Canifis) and then cooked.
//Leather bodies can be purchased from Aaron's Archery Appendages in the Ranging Guild or Thessalia's Fine Clothes north of Charlie. One also spawns nearby in a building south of Varrock East Bank.
//Leather chaps can also be purchased from Aaron's Archery Appendages or be made by the player with materials found in Lumbridge and Al Kharid.

import org.osbot.rs07.api.map.Area;

// ITEM
// Iron ore
//	Iron dagger
//	Raw herring
//	Raw trout
//	Trout
//	Pike
//	Leather bodY
//	Leather chaps
public enum CharlieTheTramp {
    ///
    ///     ~ CHARLIE THE TRAMP CLUEs ~
    ///
    TROUT();
//    IRON_DAGGER("I need to give Charlie one iron dagger.", "Iron Dagger", utils.NPC.CHARLIE_THE_TRAMP.getName()), // can buy from nearby shop!
//    IRON_ORE("I need to give Charlie a piece of iron ore.", "Iron ore", utils.NPC.CHARLIE_THE_TRAMP.getName()),
//    PIKE(),
//    RAW_HERRING();

    Area area;
    String name;
    String hint;
    String description;
    String item;

    CharlieTheTramp() {}

    CharlieTheTramp(Area area, String name, String description, String hint, String item) {
        this.area = area;
        this.name = name;
        this.hint = hint;
        this.description = description;
        this.item = item;
    }
}
