package utils;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.script.Script;

public class InventoryTracker {
    private static final int MAX_ITEM_ID = 20_000; // Covers all OSRS items
    private int[] lastInventorySnapshot = new int[MAX_ITEM_ID]; // Direct memory snapshot

    public void updateAndCompareInventory(Script script) {
        int[] currentSnapshot = new int[MAX_ITEM_ID]; // New snapshot for this tick

        // Snapshot the inventory (O(28) = constant time)
        for (Item item : script.getInventory().getItems()) {
            if (item != null) {
                currentSnapshot[item.getId()] = item.getAmount(); // Store item ID → amount
            }
        }

        // Compare snapshots in a **single loop**
        StringBuilder changes = new StringBuilder("Inventory changed: ");

        boolean hasChanges = false;
        for (int i = 0; i < MAX_ITEM_ID; i++) {
            if (lastInventorySnapshot[i] != currentSnapshot[i]) {
                int diff = currentSnapshot[i] - lastInventorySnapshot[i];
                changes.append("[ItemID: ").append(i).append(", Change: ").append(diff).append("] ");
                hasChanges = true;
            }
        }

        if (hasChanges) {
            script.log(changes.toString()); // Log only if something changed
        }

        // **Replace old snapshot with the new one (fast memory swap)**
        lastInventorySnapshot = currentSnapshot;
    }
}
