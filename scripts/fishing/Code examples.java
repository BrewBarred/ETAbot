
//
//    /**
//     * Check if the player possess any kind of axe for cutting trees down.
//     *
//     * @return A boolean value that is true if the player has an item ending with " axe",
//     * equipped or in their inventory, else returns false.
//     */
//    private boolean hasAxe() {
//        // check inventory for any kind of axe
//        Item axe = getInventory().getItem(item -> item.getName().endsWith(" axe"));
//
//        // check worn equipment for any kind of axe
//        if (axe == null)
//            axe = getEquipment().getItem(item -> item.getName().endsWith(" axe"));
//
//        return axe == null;
//    }
//
//    /**
//     * Check if the player has a tinderbox in their inventory.
//     *
//     * @return A boolean value that is true if the player has a tinderbox in their inventory, else returns false.
//     */
//    private boolean hasTinderbox(Item log) {
//        Item tinderbox = getInventory().getItem(item -> item.getName().equals("Tinderbox"));
//        return tinderbox == null;
//    }
//}
