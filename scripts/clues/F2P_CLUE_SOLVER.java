
//
//        // else, if the clue is not a map-type, read the clue text
//        String text = readClue();
//        // log text for debugging to easily add more clues
//        log("Text = " + text);
//
//        // try solve the clue using the text
//        if (decipherClueText(text)) {
//            return Rand.getRand(1243);
//        } else {
//            // if none of the above or a charlie clue, it must be a digging one
//            ClueLocation location = readMap();
//            if (location != null) {
//                solveClue(location);
//                sleep(1243);
//            }
//        }
//
//        // else, there must be an unsolvable clue or bug... exit script until I can work on a fix for it :)
//        onExit("Unable to process clue scroll! Exiting script...");
//        return Rand.getRand(0);
//    }
//

//
//    /**
//     * Attempts to solve a Hot n Cold clue
//     * @return
//     */
//    protected boolean solveClue() throws InterruptedException {
//        setStatus("Attempting to solve hot and cold clue...", true);
//        // define required items to solve a hot n cold clue, item/quantity (set quantity -1 to withdraw all)
//        HashMap<String, Integer> REQUIRED_ITEMS = new HashMap<String, Integer>() {{
//            put("Spade", 1); // cant dig without a spade
//            put("Strange Device", 1); // dunno where to dig without this thing
//            put("Clue scroll (beginner)", 1); // no point in digging without this thing
////            put("Law rune", 30); // enough laws to tele around a few times
////            put("Air rune", 1000); // enough airs to tele
////            put("Earth rune", 1000); // enough earths to tele a few times
////            put("Coins", 20000); // enough coins to charter
//        }};
//
//        // fetch the required items from the bank if needed
//        if (!fetchFromBank(REQUIRED_ITEMS))
//            return setStatus("Error fetching required hot and cold items from the bank!", true);
//
//        setStatus("Attempting to read device...", true);
//        // feel device and read hint
//        String hint = feelStrangeDevice();
//        if (hint == null) {
//            return !setStatus("Error operating strange device...", true);
//        }
//
//        log("Hot and cold hint: " + hint);
//        switch (hint) {
//            case "The strange device doesn't seem to work here.":
//                setStatus("Correcting invalid zone error...");
//                walkTo(myPosition().getArea(1).setPlane(0), "Valid Clue Zone");
//                // sleep until the player is in the correct plane
//                sleep(() -> myPosition().getArea(1).getPlane() == 0);
//                // read the device again
//                String heat = feelStrangeDevice();
//                return true;
//
//            default:
//                // default case, just try all locations
//                //TODO: complete above logic
//                return false;
//        }
////
////            case FREEZING:
////            case COLD:
////            case WARM:
////            case HOT:
////            case VERY_HOT:
////            case BURNING:
////                // TODO: use parseDirection(hint) to pick a heading and step a few tiles,
////                // then loop: feelDevice() -> re-read hint -> adjust.
////                log("Hint: " + hint);
////                return true;
////
////            case UNKNOWN:
////            default:
////                log("Unrecognized hint: " + hint);
////                return false;
////        }
//        return true;
//    }
//
//    public String feelStrangeDevice() {
//        setStatus("Feeling strange device...", true);
//        // Try to click the Strange device
//        final String STRANGE_DEVICE = "Strange device";
//        if (getInventory().interact("Feel", STRANGE_DEVICE)) {
//            sleep(Rand.getRandReallyShortDelayInt());
//            // Grab the latest chat message
//            String lastMessage = getChatbox().getMessages(Chatbox.MessageType.GAME).stream()
//                    .findFirst()
//                    .filter(f -> f.startsWith("The device is") || f.contains("strange device"))
//                    .orElse(null);
//
//            if (lastMessage != null) {
//                log("Strange device says: " + lastMessage);
//                return lastMessage;
//            }
//        }
//        return null;
//    }
//
//
//
//    protected boolean completeCharlieTask(String scrollText) throws InterruptedException {
//        setStatus("Attempting to complete charlie task...", true);
//        String item = getCharlieItem(scrollText);
//
//        setStatus("Attempting to fetch " + item + "...", true);
//        sleep(random(400, 600));
//        // return false if no task item could be found
//        if (item == null)
//            return false;
//
//        // goto bank to fetch item
//        Area VARROCK_WEST_BANK = new Area(3184, 3436, 3185, 3435);
//        walkTo(VARROCK_WEST_BANK, "Varrock West Bank");
//        sleep(random(400, 600));
//
//        // fetch the required item for this task
//        withdrawItem(item, 1);
//        sleep(random(400, 600));
//
//        // check players invetory for the item to confirm withdrawal
//        if (!inventory.contains(item))
//            return false;
//
//        findNPC("Charlie the Tramp", ClueLocation.VARROCK_SOUTH_GATE);
//        dialogues.completeDialogue("Click here to continue",
//                "Click here to continue",
//                "Click here to continue",
//                "Click here to continue");
//        return true;
//    }
//
//    protected String getCharlieItem(String scrollText) throws InterruptedException {
//        switch (scrollText) {
//            case "I need to give Charlie a piece of iron ore.":
//                return "Iron ore";
//
//            case "I need to give Charlie a raw herring.":
//                return "Raw herring";
//
//            case "I need to give Charlie a cooked pike.":
//                return "Pike";
//
//            default:
//                setStatus("Unable to retrieve Charlie's requested item... script will now exit.");
//                onExit();
//                return null;
//        }
//    }

//
