package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class TaskQueue {
    private final List<Callable<Boolean>> tasks = new ArrayList<>();
    private int index = 0;

    public boolean hasNext() {
        return index < tasks.size();
    }

    public Callable<Boolean> getCurrent() {
        return hasNext() ? tasks.get(index) : null;
    }

    public void completeCurrent() {
        index++;
    }

    public void add(Callable<Boolean> task) {
        tasks.add(task);
    }

    public void insert(int atIndex, Callable<Boolean> task) {
        tasks.add(atIndex, task);
        if (atIndex <= index) {
            index++; // shift forward if needed
        }
    }

    public void remove(int atIndex) {
        if (atIndex >= 0 && atIndex < tasks.size()) {
            tasks.remove(atIndex);
            if (atIndex < index) {
                index--; // shift back
            } else if (atIndex == index && index == tasks.size()) {
                index--; // handle edge case when removing current
            }
        }
    }

    public void jumpTo(int toIndex) {
        if (toIndex >= 0 && toIndex < tasks.size()) {
            index = toIndex;
        }
    }

    public void reset() {
        tasks.clear();
        index = 0;
    }

    public int size() {
        return tasks.size();
    }

    public int getIndex() {
        return index;
    }

    public boolean hasTasks() { return !tasks.isEmpty(); }
}
