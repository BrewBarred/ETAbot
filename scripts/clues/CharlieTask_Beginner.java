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
