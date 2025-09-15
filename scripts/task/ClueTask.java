package task;

import org.osbot.rs07.script.Script;

/**
 * A Task that requires the player to have (or obtain) a specific item.
 */
public abstract class ClueTask extends Task {
    /** A descriptive hint for the task (e.g., from the clue scroll). */
    private final String hint;

    /**
     * Constructs a Task with the given hint.
     *
     * @param hint A textual hint that describes this task.
     */
    public ClueTask(String hint) {
        super();
        this.hint = hint;
    }

    /**
     * Gets the descriptive hint associated with this task.
     *
     * @return The task hint as a String.
     */
    public String getHint() {
        return hint;
    }

    /**
     * Executes the task's specific logic.
     * Subclasses must implement their own execution rules.
     *
     * @param script The OSBot script instance used to perform API actions.
     * @return true if the task executed successfully, false otherwise.
     * @throws InterruptedException If execution is interrupted.
     */
    public abstract boolean execute(Script script)
            throws InterruptedException;
}

