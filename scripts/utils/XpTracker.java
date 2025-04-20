//package utils;
//
//import org.osbot.rs07.script.Script;
//import org.osbot.rs07.api.ui.Skill;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class XpTracker implements Runnable {
//
//    private Script script;
//    private final Map<Skill, Integer> xpCache = new ConcurrentHashMap<>();
//    private volatile boolean running = true;
//
//    public XpTracker(Script script) {
//        this.script = script;
//
//        for (Skill skill : Skill.values()) {
//            xpCache.put(skill, script.getSkills().getExperience(skill));
//        }
//    }
//
//    @Override
//    public void run() {
//        while (running) {
//            for (Skill skill : Skill.values()) {
//                int currentXp = script.getSkills().getExperience(skill);
//                int previousXp = xpCache.getOrDefault(skill, 0);
//
//                if (currentXp != previousXp) {
//                    xpCache.put(skill, currentXp);
//                    script.log(skill + " XP changed: " + previousXp + " → " + currentXp);
//                }
//            }
//
//            try {
//                Thread.sleep(1000); // check every 1s
//            } catch (InterruptedException e) {
//                running = false;
//            }
//        }
//    }
//
//    public int getCachedXp(Skill skill) {
//        return xpCache.getOrDefault(skill, 0);
//    }
//
//    public void stop() {
//        running = false;
//    }
//}
