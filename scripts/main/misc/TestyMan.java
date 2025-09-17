package main.misc;

import main.BotMan;
import main.BotMenu;
import main.task.tasks.basic.Dig;
import org.osbot.rs07.api.Store;
import org.osbot.rs07.script.ScriptManifest;

@ScriptManifest(
        info = "",
        author = "ETA",
        name = "ETAs Testicle Script",
        version = 0.1,
        logo = "Never!")
public class TestyMan extends BotMan<BotMenu> {
    /**
     * Enable tutorial mode by setting this to true, and disable it by setting it to false.
     */
    private final boolean isLearning = true;

    /**
     * onLoad: Write logic here that should only run when the bot starts, such as gearing up or adjusting menu options.
     */@Override
    public boolean onLoad() {
        //
        return setStatus("Loading test script...");
    }

    /**
     * run(): This is the logic that gets executed on every main loop.
     * <p>
     * You can think of this as a fake main loop, within {@link BotMan's} fake main loop, within the real OsBot APIs
     * main loop. Confusing, yeah... but just know that this is the only loop you really need to be concerned with.
     * <p>
     * This loop will run once, then go back to the BotMan for some safety checks before continuing another loop.
     * This means that the backend stuff is all handled, you just have to worry about defining your botting script.
     *
     * @return {@link Boolean True} if the loop executed successfully, else returns {@link Boolean false}.
     */
    @Override
    public boolean run() throws InterruptedException {
        // this displays a message to the overlay manager (if you've enabled it)
        setStatus("Running!");
        // start a tutorial for new enthusiasts to learn how to use this library
        if (isLearning)
            startTutorial(true);

        return false;
    }

    /**
     * Starts a tutorial which demonstrates the capabilities of a bot for people that are unfamiliar with this library.
     */
    private boolean startTutorial(boolean isTrue) throws InterruptedException {
        if (isTrue) {
            // prove that this function writes messages and returns true (always - be careful)
            boolean test = setStatus("This returns true so you can do two-line checks w/error logging while still returning a result");
            // reverse this true value and skip over this part of the tutorial
            startTutorial(!test);
        }

        if (!isTrue) {
            boolean something = !setStatus("Unfortunately, to do the same for false statements, you need to remember to do the inverse :/ (Or just do it normally)");
            // this function sleeps for 1000ms
            sleep(1000);
            // we can use setStatus as normal output too, this is preferred over logging since it logs as well as prints to user
            setStatus("Status result should be false, result: " + something);
        }


        setStatus("ok, now that's the boring stuff, let's try something a bit more fun....");

        ///
        ///     Sleeping: is crucial in ensuring every action isn't attempted within a few seconds. Good timing is crucial
        ///               for a good script, not only for general functionality, but also for the safety of your account!
        ///

        setStatus("sleeping... (1s)");
        // there are many ways to sleep, this will sleep for 2 seconds unconditionally
        sleep(1000);
        // this won't wake you up, lol, but... if you want to return an error message to the overlay manager and the logger,
        // followed by a sleep and returning true or false, this simple function can do all of that in one hit, see below for
        // a:
        // updated status followed by a 3 second sleep and returning true
        boolean notFalse = setStatus("zzzz.....", 2000);
        setStatus("notFalse is the same as " + notFalse);
        // returns notFalse is the same as true - because setStatus()
        // always returns true, remember! Be careful with it, its the only downside to this sex bomb of a function

        setStatus("I hop your digging this tutorial so far, but if not, it's your lucky day...");
        // create a new simple action, dig.
        Dig d = new Dig();
        setStatus("you must execute tasks or they will NOT do anything, and you'll be like, ayyyyy? It's not doing anything?", 1000);
        // this calls a function (defined in main.task.tasks.basic.Dig.java) which just digs in the players current position.
        boolean diggingIt = d.execute(this);

        setStatus("Yeah.... you digging it now? Digging it = " + diggingIt);


        setStatus("Tutorial complete! Happy scripting ;)", 1000);
        throw new InterruptedException();
        //return true;
    }
}
