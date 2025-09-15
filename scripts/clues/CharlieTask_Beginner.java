//// Gypsy Aris – Blow Raspberry
//new BeginnerEmote(
//    () -> new Area(3201, 3425, 3204, 3423),
//    "Blow a raspberry at Gypsy Aris",
//Emote.RASPBERRY
//),
//
//// Brugsen Bursen – Bow
//        new BeginnerEmote(
//    () -> new Area(3163, 3477, 3166, 3475),
//    "Bow to Brugsen Bursen",
//Emote.BOW
//),
//
//// Iffie Nitter – Cheer
//        new BeginnerEmote(
//    () -> new Area(3203, 3417, 3205, 3415),
//    "Cheer at Iffie Nitter",
//Emote.CHEER
//),
//
//// Al'Kharid Mine – Panic
//        new BeginnerEmote(
//    () -> new Area(3296, 3275, 3300, 3279),
//    "Panic at Al'Kharid mine",
//Emote.PANIC
//),
//
//// Flynn’s Mace Shop – Spin
//        new BeginnerEmote(
//    () -> new Area(2949, 3386, 2951, 3387),
//    "Spin at Flynn's mace shop",
//Emote.SPIN
//)









//package clues;
//
//import task.ClueTask;
//
//import java.util.Objects;
//
//public class CharlieTask {
//    public enum Beginner {
//        IRON_ORE("I need to give Charlie a piece of iron ore.", "Iron ore"),
//        RAW_HERRING("I need to give Charlie a raw herring.", "Raw herring");
//
//        /**
//         * The hint provided by the clue scroll.
//         */
//        String hint;
//        /**
//         * The name of the item requested by Charlie the Tramp.
//         */
//        String requiredItem;
//
//        CharlieTask(String hint, String requiredItem) {
//            this.hint = hint;
//            this.requiredItem = requiredItem;
//        }
//
//        public String getHint() {
//            return hint;
//        }
//
//        public String getReqItem() {
//            return requiredItem;
//        }
//
//        public ClueTask findTaskByHint(String hint) {
//            return tasks.stream()
//                    .filter(t -> t.getHint().equals(hint))
//                    .findFirst()
//                    .orElse(null);
//        }
//
//        /**
//         * Fetches the required item for a beginner Charlie task based on the passed clue scroll hint.
//         *
//         * @param hint The hint provided by the beginner clue scroll.
//         * @return The name of the required item to complete this {}
//         */
//        public String getReqItem(String hint) {
//            // for each c3harlie clue item in the enum
//            for (CharlieTask_Beginner clue : values()) {
//                // if the passed hint matches a loaded clue
//                if (Objects.equals(clue.getHint(), getHint())) {
//                    // if a match is found, return the clue item - this item needs to be coded in
//                    return clue.getReqItem();
//                }
//            }
//        }
//    }
//}
