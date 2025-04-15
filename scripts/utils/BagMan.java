package utils;

import org.osbot.rs07.script.Script;

public class BagMan {
    Script script;

    public BagMan(Script script) {
        this.script = script;
        script.log("Successfully initialized inventory manager!");
    }
}
