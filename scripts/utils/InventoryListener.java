package utils;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.script.Script;
import java.util.*;
import java.util.stream.Collectors;

public class InventoryListener {
    /**
     * Stores the previous inventory state
     */
    private List<Item> previousInventory = new ArrayList<>();

    /**
     * Checks for inventory changes using Item objects.
     */
    public void checkInventoryChanges(Script script) {
        // fetch the current inventory state, ignoring empty (null) item slots
        List<Item> currentInventory = Arrays.stream(script.getInventory().getItems())
                .filter(Objects::nonNull) // filter out null items
                .collect(Collectors.toList()); // Collects into a List<Item>

        // ensure a change has been made before logging changes
        if (!previousInventory.isEmpty() && !previousInventory.equals(currentInventory))
            logInventoryChanges(previousInventory, currentInventory, script);

        // Update previous inventory state
        previousInventory = new ArrayList<>(currentInventory);
    }

//    /**
//     * Retrieves a list of non-null Item objects from the inventory.
//     */
//    private List<Item> getInventoryItems(Inventory inventory) {
//        // stores the non-null inventory items
//        List<Item> currentInventory = new ArrayList<>();
//        for (Item item : inventory.getItems()) {
//            if (item != null) {
//                currentInventory.add(item);
//            }
//        }
//        return currentInventory;
//    }

    /**
     * Logs inventory additions and removals.
     */
    private void logInventoryChanges(List<Item> oldInventory, List<Item> newInventory, Script script) {
        script.log("Logging changes...");
        // convert old inventory to a set for faster iterations
        Set<Item> setA = new HashSet<>(oldInventory);
        // uses stream filter to create a list containing
        List<Item> changedItems = newInventory.stream()
                .filter(e -> !setA.contains(e)) // only keep items not in set A
                .collect(Collectors.toList()); // convert to a list
//        // for each item in the new inventory
//        for (int i = 0; i < newInventory.size(); i++) {
//            int quantityDiff = 0;
//            Item oldItem = oldInventory.get(i);
//            Item newItem = newInventory.get(i);
//
//            // if there are two items to compare
//            if (oldItem != null && newItem != null) {
//                // and if they are the same item
//                if (oldItem.getName().equals(newItem.getName())) {
//                    // calculate the quantity difference
//                    quantityDiff = newItem.getAmount() - oldItem.getAmount();
//                    // if quantities have changed
//                    if (oldItem.getAmount() != newItem.getAmount()) {
//                        // update quantities
//                        quantityDiff = newItem.getAmount() - oldItem.getAmount();
//                        // if quantity diff is positive, items were gained, else items were lost
//                        String effect = (quantityDiff > 0 ? "Added" : "Removed") + " ";
//
//                    }
//                }
//            }
//
//
//            if (!containsItem(oldInventory, newItem)) {
//                script.log(quantityDiff > 0 ? "-":"+" + " " + newItem.getName() + " x" + newItem.getAmount());
//            }
//        }
    }

    /**
     * Checks if an item list contains an equivalent item (matching name and amount).
     */
    private boolean containsItem(List<Item> list, Item item) {
        return list.stream().anyMatch(i -> i.getName().equals(item.getName()) && i.getAmount() == item.getAmount());
    }
}
