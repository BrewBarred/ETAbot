//package fishing;
//
//import org.osbot.rs07.api.map.Area;
//import utils.Rand;
//import org.osbot.rs07.script.ScriptManifest;
//
//import java.time.Instant;
//
//import static org.apache.commons.lang3.time.DurationFormatUtils.formatDuration;
//
//@ScriptManifest(
//        name = "F2P Karamja Fisherman",
//        author = "E.T.A.",
//        version = 2.0,
//        info = "Fishes, cooks and sells lobsters/swordfish in Karamja utilizing a new framework by ETA (Free-to-Play)",
//        logo = ""
//)
//public class F2PKaramja extends FishingMan {
//    /**
//     * The amount of gold points required before the bot will head to Karamja to start fishing
//     */
//    private final int FULL_GP_REQ = 60;
//    private final double VERSION = 2.0;
//    private static final Area PORT_SARIM_COOKING_RANGE = new Area(3015, 3240, 3019, 3236);
//    private final Area PORT_SARIM_DEPOSIT_BOX_AREA = new Area(3043, 3237, 3049, 3234);
//    private static final Area PORT_SARIM_FISHING_SHOP = new Area(3011, 3225, 3016, 3222);
//    private boolean isCooking = false;
//
////    @Override
////    protected void paintScriptOverlay(Graphics2D g) {
////        int x = 10, y = 30, w = 220, h = 90;
////        g.setColor(new Color(0, 0, 0, 120));
////        g.fillRoundRect(x - 5, y - 18, w, h, 10, 10);
////        g.setColor(Color.WHITE);
////        g.setFont(new Font("Consolas", Font.PLAIN, 12));
////
////
////        long xpGained = getExperienceTracker().getGainedXP(Skill.WOODCUTTING);
////        long xpHr = getExperienceTracker().getGainedXPPerHour(Skill.WOODCUTTING);
////        long ttl = getExperienceTracker().getTimeToLevel(Skill.WOODCUTTING);
////
////
////        g.drawString("Woodcutter99 v1.0", x, y);
////        g.drawString("Status: " + status, x, y + 16);
////        g.drawString("WC Lv: " + getSkills().getStatic(Skill.WOODCUTTING) + " (" + getSkills().experienceToLevel(Skill.WOODCUTTING) + " xp to)" , x, y + 32);
////        g.drawString("XP: " + xpGained + " (" + xpHr + "/h)", x, y + 48);
////        //g.drawString("Runtime: " + formatDuration(Duration.between(startTime, Instant.now())), x, y + 80);
////    }
//
//    @Override
//    protected void onLoad() {
//        setStatus("Starting " + this.getName() + " script...");
//        // sets bot to harpoon fish at karamja docks (musa point)
//        this.setFishingStyle((FishingStyle) botMenu.selectionFishingStyle.getSelectedItem());
//        this.setFishingArea((FishingArea) botMenu.selectionFishingArea.getSelectedItem());
//        // pause bot to allow users to set menu options
//        this.pause();
//    }
//
//    //TODO: Fix bug with being in mems world on f2p and logged out cancelling script
//
//    @Override
//    protected boolean runBot() throws InterruptedException {
//        if (this.isAFK) {
//            setStatus("Thinking...", false);
//        } else {
//            setStatus("Settings mode has been enabled! Pausing script...");
//            return true;
//        }
//
//        //TODO: Fix/implement inventory tracker
//        // track inventory changes
//        //inventoryListener.checkInventoryChanges(this);
//
//        // if the player has no fishing gear
//        if (!hasReqFishingGear()) {
//            //TODO: Implement logic to determine and fetch required fishing gear based on GUI settings
//            log("Unable to find the required fishing equipment... Exiting script...");
//            this.onExit();
//        }
//
//        if (!hasReqCharterFare()) {
//            //TODO: Implement logic to fetch/collect enough coins for the required charter (e.g., bananas or bank)
//            log("Insufficient GP found! Please upgrade to PRO for the GP fetching feature!");
//            this.onExit();
//        }
//
//        // if the player currently has a full inventory
//        if (isFullInv()) {
//            // check if there is any food to cook
//            if(hasRawFood()) {
//                // if the player has chosen to cook their catch
//                if (isCooking) {
//                    // and the player is near the port sarim cooking range
//                    if (PORT_SARIM_COOKING_RANGE.contains(myPlayer())) {
//                        // cook food
//                        cookFish();
//                    } else {
//                        // else walk to port sarim cooking range
//                        walkTo(PORT_SARIM_COOKING_RANGE, "Port Sarim cooking range");
//                        // return short delay to prevent player getting stuck in random afk at each door
//                        return true;
//                    }
//                    // else if player does not wish to cook their catch
//                } else {
//                    // sell food
//                    sellFood();
//                }
//            } else {
//                depositFood();
//            }
//        } else {
//            setStatus("Checking valid fishing location...", false);
//            if (isAtFishingArea()) {
//                //TODO: Revise logic to reference hash map in hasFishingGear function + add GUI functionality
//                if (hasHarpoon())
//                    fishHarpoon();
//                if (hasCage())
//                    fishCage();
//            } else {
//                FishingArea spot = FishingArea.MUSA_POINT;
//                return walkTo(spot.getArea(), spot.toString());
//            }
//        }
//
//        return false;
//    }
//
//}