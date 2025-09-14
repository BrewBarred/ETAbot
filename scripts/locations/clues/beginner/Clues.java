//

//TODO: SOMEHOW IMPLEMENT THIS FUNCTION IN A CLUEHANDLER OR ENUM TO RETURN THE LOCATION OR POSSIBLE LOCATIONS OF A CLUE`
//HOT OR COLD, Chronicle Trick
//The Chronicle Trick is an easy way to determine which of the possible locations your clue is at, and is performed as follows:
//
//1. Teleport to the Champions' Guild using the Chronicle, and check the orb
//
//2. If it is Cold, the clue is on Ice Mountain.
//
//3. If it is Hot, the clue is in the Draynor wheat field
//
//4. If it is Very Hot or Incredibly Hot, the clue is in the cow field furthest north of Lumbridge Castle.
//
//5. If Warm, the clue is either north of the Al Kharid mine, or it is behind Draynor Manor. Take a step Eastward and check the orb again. If it is warmer, the clue is in Al Kharid, otherwise, it is behind the Draynor Manor.




// RECOMMENDED BEGINNER ITEMS
//
//Clue scroll	Must be in the inventory in order to receive the next step or reward. // ALWAYS
//Spade or Eastfloor spade	Digging in certain locations is required for some clues. // CONSIDER ALTERNATIVE SPADES
//Teleportation runes and items	Use these to travel over large distances. // EG SCEPTRE, TELE RUNES
//Money	Use this to buy items needed for emote clues and travel costs. // CHARTER, BUY ITEMS FOR CHARLIE, ETC.
//Chronicle for tele


// REQUIRED EMOTE ITEMS
//Bronze axe
//Gold necklace
//Gold ring
//Leather boots

// EMOTE MIN LEVEL
// GOLD RING CRAFTING 5
// GOLD NECKLACE CRAFTING 6
// MINING/SMITHING FOR GOLD BARS? // CHECK ALTERNATIVE METHODS OF GOLD BARS IN F2P?

// CHARLIE THE TRAMP F2P ITEMS
//Item	Skilling Source	Other Sources
//Iron dagger	Smithing 15 (boostable)	Iron daggers can be bought from the Varrock Swordshop just north of Charlie. There is also an item spawn up at the goblin house in Lumbridge.
//Iron ore	Mining 15 (boostable)	Iron ore can be bought from the Ore Seller at the Blast Furnace. There are also iron rocks in the nearby South-west Varrock mine.
//Leather body	Crafting 14 (boostable)	Leather bodies can be purchased from Aaron's Archery Appendages in the Ranging Guild or Thessalia's Fine Clothes north of Charlie. One also spawns nearby in a building south of Varrock East Bank.
//Leather chaps	Crafting 18 (boostable)	Leather chaps can also be purchased from Aaron's Archery Appendages or be made by the player with materials found in Lumbridge and Al Kharid.
//Pike	Cooking 20 (boostable)	Raw pike can be purchased be from Rufus' Meat Emporium in Canifis or fished at level 25 Fishing with a fishing rod and bait at the lure/bait fishing spots at Barbarian Village or Lumbridge river and then cooked.
//Raw herring	Fishing 10 (boostable)	Raw herring can be purchased at Frankie's Fishing Emporium in Port Piscarilius, or be caught by hand in Draynor Village.
//Raw trout	Fishing 20 (boostable)	Raw trout can be fished with a fly fishing rod and feathers at Barbarian Village or Lumbridge river (or purchased at Rufus' Meat Emporium in Canifis) and be cooked into Trout if needed.
//Trout	Cooking 15 (boostable)	Trout can be obtained by completing Adventurer Jon's Adventure Paths or purchased from the Warrior Guild Food Shop.

//package locations.clues;
//
// import sub-packages to easily reference clue locations
//
///**
// * Consider adding clue functionality to this clue? Such as solveClue() etc.
// */
//public class Clues {
//    public final class Clues {
//        public static HotAndCold[] beginnerHotAndCold() {
//            return HotAndCold.values();
//        }
//
//        public static MapClues[] easyMaps() {
//            return MapClues.values();
//        }
//    }
//}