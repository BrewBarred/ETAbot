package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WorldManager {
    private List<World> worlds;

    public WorldManager() {
        worlds = new ArrayList<>();
        initializeWorlds();
    }

    private void initializeWorlds() {
        worlds.add(new World(301, "United States (east)", false, false, "Trade", "", 0));
        worlds.add(new World(302, "United Kingdom", true, false, "Trade", "", 0));
        worlds.add(new World(303, "Germany", true, false, "", "", 0));
        worlds.add(new World(304, "Germany", true, false, "Trouble Brewing", "", 0));
        worlds.add(new World(305, "United States (east)", true, false, "Falador Party Room", "", 0));
        worlds.add(new World(306, "United States (west)", true, false, "Barbarian Assault", "", 0));
        worlds.add(new World(307, "United States (west)", true, false, "Wintertodt", "", 0));
        worlds.add(new World(308, "United Kingdom", false, true, "PK", "", 0));
        worlds.add(new World(309, "United Kingdom", true, false, "Wintertodt", "", 0));
        worlds.add(new World(310, "United Kingdom", true, false, "Barbarian Assault", "", 0));
        worlds.add(new World(311, "Germany", true, false, "Wintertodt", "", 0));
        worlds.add(new World(312, "Germany", true, false, "Group Skilling", "", 0));
        worlds.add(new World(313, "United States (west)", true, false, "Group Skilling", "", 0));
        worlds.add(new World(314, "United States (east)", true, false, "Brimhaven Agility Arena", "", 0));
        worlds.add(new World(315, "United States (west)", true, false, "Fishing Trawler", "", 0));
        worlds.add(new World(316, "United Kingdom", false, true, "PK", "", 0));
        worlds.add(new World(317, "United Kingdom", true, false, "", "", 0));
        worlds.add(new World(318, "United Kingdom", true, true, "Bounty Hunter", "", 0));
        worlds.add(new World(320, "United States (west)", true, false, "Soul Wars", "", 0));
        worlds.add(new World(321, "United States (east)", true, false, "Sulliuscep cutting", "", 0));
        worlds.add(new World(322, "United States (east)", true, false, "Clan Wars - Free-for-all", "", 0));
        worlds.add(new World(323, "United States (west)", true, false, "Volcanic Mine", "", 0));
        worlds.add(new World(324, "United States (west)", true, false, "Group Iron", "", 0));
        worlds.add(new World(325, "United Kingdom", true, false, "Group Iron", "", 0));
        worlds.add(new World(326, "United Kingdom", false, false, "LMS Casual", "", 0));
        worlds.add(new World(327, "Germany", true, false, "Ourania Altar", "", 0));
        worlds.add(new World(328, "Germany", true, false, "Group Iron", "", 0));
        worlds.add(new World(329, "United States (east)", true, false, "Tombs of Amascut", "", 0));
        worlds.add(new World(330, "United States (east)", true, false, "House Party, Gilded Altar", "", 0));
        worlds.add(new World(331, "United States (west)", true, false, "Tombs of Amascut", "", 0));
        worlds.add(new World(332, "United States (west)", true, false, "Nex FFA", "", 0));
        worlds.add(new World(333, "United Kingdom", true, false, "Tombs of Amascut", "", 0));
        worlds.add(new World(334, "United Kingdom", true, false, "Castle Wars", "", 0));
        worlds.add(new World(335, "Germany", false, false, "Group Iron", "", 0));
        worlds.add(new World(336, "Germany", true, false, "ToA FFA", "", 0));
        worlds.add(new World(337, "United States (east)", true, false, "Nightmare of Ashihama", "", 0));
        worlds.add(new World(338, "United States (west)", true, false, "ToA FFA", "", 0));
        worlds.add(new World(339, "United States (west)", true, false, "", "", 0));
        worlds.add(new World(340, "United States (west)", true, false, "", "", 0));
        worlds.add(new World(341, "United Kingdom", true, false, "Tempoross", "", 0));
        worlds.add(new World(342, "United Kingdom", true, false, "Role-playing", "", 0));
        worlds.add(new World(343, "Germany", true, false, "", "", 0));
        worlds.add(new World(344, "Germany", true, false, "Pest Control", "", 0));
        worlds.add(new World(345, "United States (east)", true, false, "", "Deadman", 0));
        worlds.add(new World(346, "United States (east)", true, false, "Agility Training", "", 0));
        worlds.add(new World(347, "United States (west)", true, false, "", "", 0));
        worlds.add(new World(348, "United States (west)", true, false, "", "", 0));
        worlds.add(new World(349, "United Kingdom", true, false, "", "", 2000));
        worlds.add(new World(350, "United Kingdom", true, false, "Soul Wars", "", 0));
        worlds.add(new World(351, "Germany", true, false, "", "", 0));
        worlds.add(new World(352, "Germany", true, false, "Blast Furnace", "", 0));
        worlds.add(new World(353, "United States (east)", true, false, "", "", 1250));
        worlds.add(new World(354, "United States (east)", true, false, "Castle Wars", "", 0));
        worlds.add(new World(355, "United States (west)", true, false, "Blast Furnace", "", 0));
        worlds.add(new World(356, "United States (west)", true, false, "Blast Furnace", "", 0));
        worlds.add(new World(357, "United States (west)", true, false, "Blast Furnace", "", 0));
        worlds.add(new World(358, "United Kingdom", true, false, "Blast Furnace", "", 0));
        worlds.add(new World(359, "Germany", true, false, "", "", 0));
        worlds.add(new World(360, "Germany", true, false, "", "", 0));
        worlds.add(new World(361, "United States (east)", true, false, "", "", 2000));
        worlds.add(new World(362, "United States (east)", true, false, "TzHaar Fight Pit", "", 0));
        worlds.add(new World(363, "United Kingdom", true, false, "", "", 2200));
        worlds.add(new World(364, "United Kingdom", true, false, "", "", 1250));
        worlds.add(new World(365, "United Kingdom", true, true, "High Risk", "", 0));
        worlds.add(new World(366, "United Kingdom", true, false, "", "", 1500));
        worlds.add(new World(367, "Germany", true, false, "", "", 0));
        worlds.add(new World(368, "Germany", true, false, "", "", 0));
        worlds.add(new World(369, "United States (east)", true, true, "PK", "", 0));
        worlds.add(new World(370, "United States (east)", true, false, "Fishing Trawler", "", 0));
        worlds.add(new World(371, "United Kingdom", false, false, "Group Iron", "", 0));
        worlds.add(new World(372, "United Kingdom", false, false, "", "", 750));
        worlds.add(new World(373, "United Kingdom", true, false, "", "", 1750));
        worlds.add(new World(374, "United States (west)", true, false, "Theatre of Blood", "", 0));
        worlds.add(new World(375, "Germany", true, false, "Zalcano", "", 0));
        worlds.add(new World(376, "Germany", true, false, "Theatre of Blood", "", 0));
        worlds.add(new World(377, "United States (east)", true, false, "Mort'ton Temple", "", 0));
        worlds.add(new World(378, "United States (west)", true, false, "Zalcano", "", 0));
        worlds.add(new World(379, "United Kingdom", false, false, "Arena", "", 0));
        worlds.add(new World(380, "United Kingdom", false, false, "", "", 0));
        worlds.add(new World(381, "United Kingdom", false, false, "", "", 500));
        worlds.add(new World(382, "United Kingdom", false, false, "", "", 0));
        worlds.add(new World(383, "Germany", false, false, "Castle Wars", "", 0));
        worlds.add(new World(384, "Germany", false, false, "", "", 0));
        worlds.add(new World(385, "United States (east)", true, false, "Brimhaven Agility", "", 0));
        worlds.add(new World(386, "United States (east)", true, false, "Blast Furnace", "", 0));
        worlds.add(new World(387, "Australia", true, false, "Blast Furnace", "", 0));
        worlds.add(new World(388, "Australia", true, false, "Forestry", "", 0));
        worlds.add(new World(389, "Australia", true, false, "Wintertodt", "", 0));
        worlds.add(new World(390, "Australia", true, false, "LMS Competitive", "", 0));
        worlds.add(new World(391, "Australia", true, false, "", "", 1750));
        worlds.add(new World(392, "Australia", true, true, "PK", "", 0));
        worlds.add(new World(393, "United States (east)", false, false, "", "", 750));
        worlds.add(new World(394, "United States (east)", false, false, "Clan Wars", "", 0));
        worlds.add(new World(395, "Germany", true, false, "Blast Furnace", "", 0));
        worlds.add(new World(396, "Germany", true, false, "", "", 2000));
        worlds.add(new World(397, "Germany", false, false, "", "", 0));
        worlds.add(new World(398, "Germany", false, false, "Forestry", "", 0));
        worlds.add(new World(399, "Germany", false, false, "", "", 0));
        worlds.add(new World(413, "Germany", false, false, "", "", 500));
        worlds.add(new World(414, "Germany", false, false, "", "", 750));
        worlds.add(new World(415, "United States (east)", true, false, "", "", 2200));
        worlds.add(new World(416, "United States (east)", true, false, "", "", 1500));
        worlds.add(new World(417, "United States (east)", false, false, "Group Iron", "", 0));
        worlds.add(new World(418, "United States (west)", false, false, "", "", 0));
        worlds.add(new World(419, "United States (west)", false, false, "", "", 500));
        worlds.add(new World(420, "United States (west)", true, false, "", "", 1500));
        worlds.add(new World(421, "United States (west)", true, false, "", "", 0));
        worlds.add(new World(422, "United States (west)", true, false, "Tempoross", "", 0));
        worlds.add(new World(423, "United States (west)", true, false, "", "Fresh Start", 0));
        worlds.add(new World(424, "Australia", true, false, "Blast Furnace", "", 0));
        worlds.add(new World(425, "Australia", true, false, "Guardians of the Rift", "", 0));
        worlds.add(new World(426, "Australia", true, false, "House Party", "", 0));
        worlds.add(new World(427, "Australia", false, false, "", "", 500));
        worlds.add(new World(428, "United States (west)", true, false, "", "", 2000));
        worlds.add(new World(429, "United States (west)", true, false, "", "", 1250));
        worlds.add(new World(430, "United States (west)", false, false, "", "", 0));
        worlds.add(new World(431, "United States (west)", false, false, "", "", 0));
        worlds.add(new World(432, "United States (west)", false, false, "", "", 750));
        worlds.add(new World(433, "United States (west)", false, false, "", "", 0));
        worlds.add(new World(434, "United States (west)", false, false, "Forestry", "", 0));
        worlds.add(new World(435, "United States (west)", false, false, "", "", 0));
        worlds.add(new World(436, "United States (west)", false, false, "", "", 0));
        worlds.add(new World(437, "United States (west)", false, false, "", "", 0));
        worlds.add(new World(441, "United States (west)", true, false, "Guardians of the Rift", "", 0));
        worlds.add(new World(443, "United States (west)", true, false, "", "", 0));
        worlds.add(new World(444, "United States (west)", true, false, "Forestry", "", 0));
        worlds.add(new World(445, "United States (west)", true, false, "Guardians of the Rift", "", 0));
        worlds.add(new World(446, "United States (west)", true, false, "Role-playing", "", 0));
        worlds.add(new World(447, "Germany", true, false, "", "", 1250));
        worlds.add(new World(448, "Germany", true, false, "", "", 1500));
        worlds.add(new World(449, "Germany", true, false, "", "", 1750));
        worlds.add(new World(450, "Germany", true, false, "", "", 2200));
        worlds.add(new World(451, "Germany", false, false, "", "", 0));
        worlds.add(new World(452, "Germany", false, false, "", "", 0));
        worlds.add(new World(453, "Germany", false, false, "", "", 0));
        worlds.add(new World(454, "Germany", false, false, "", "", 0));
        worlds.add(new World(455, "Germany", false, false, "", "", 0));
        worlds.add(new World(456, "Germany", false, false, "", "", 0));
        worlds.add(new World(459, "Germany", true, false, "Guardians of the Rift", "", 0));
        worlds.add(new World(463, "Germany", true, false, "Tempoross", "", 0));
        worlds.add(new World(464, "Germany", true, false, "Guardians of the Rift", "", 0));
        worlds.add(new World(465, "Germany", true, false, "House Party", "", 0));
        worlds.add(new World(466, "Germany", true, false, "Blast Furnace", "", 0));
        worlds.add(new World(467, "United States (east)", true, false, "", "", 1750));
        worlds.add(new World(468, "United States (east)", false, false, "", "", 500));
        worlds.add(new World(469, "United States (east)", false, false, "LMS Casual", "", 0));
        worlds.add(new World(474, "United States (east)", true, true, "High Risk", "", 0));
        worlds.add(new World(475, "United States (east)", false, false, "", "", 0));
        worlds.add(new World(476, "United States (east)", false, false, "", "", 0));
        worlds.add(new World(477, "United States (east)", true, false, "Clan Recruitment", "", 0));
        worlds.add(new World(478, "United States (east)", true, false, "Guardians of the Rift", "", 0));
        worlds.add(new World(479, "United States (east)", true, false, "Arena", "", 0));
        worlds.add(new World(480, "United States (east)", true, false, "Ourania Altar", "", 0));
        worlds.add(new World(481, "United States (east)", true, false, "Nex FFA", "", 0));
        worlds.add(new World(482, "United States (east)", true, false, "", "", 0));
        worlds.add(new World(483, "United States (east)", false, false, "", "", 0));
        worlds.add(new World(484, "United States (east)", true, false, "", "", 0));
        worlds.add(new World(485, "United States (east)", true, false, "", "", 0));
        worlds.add(new World(486, "United States (east)", true, false, "", "", 0));
        worlds.add(new World(487, "United States (east)", true, false, "Forestry", "", 0));
        worlds.add(new World(488, "United States (east)", true, false, "", "", 0));
        worlds.add(new World(489, "United States (east)", true, false, "", "", 0));
        worlds.add(new World(490, "United States (east)", true, false, "Guardians of the Rift", "", 0));
        worlds.add(new World(491, "United States (east)", true, false, "Burthorpe Games Room", "", 0));
        worlds.add(new World(492, "United States (east)", true, false, "Guardians of the Rift", "", 0));
        worlds.add(new World(493, "United States (east)", true, false, "Pyramid Plunder", "", 0));
        worlds.add(new World(494, "United States (east)", true, false, "Blast Furnace", "", 0));
        worlds.add(new World(495, "United States (east)", true, false, "Blast Furnace", "", 0));
        worlds.add(new World(496, "United States (east)", true, false, "Blast Furnace", "", 0));
        worlds.add(new World(497, "United Kingdom", false, false, "Clan Recruitment", "", 0));
        worlds.add(new World(498, "United Kingdom", false, false, "", "", 0));
        worlds.add(new World(499, "United Kingdom", false, false, "", "", 0));
        worlds.add(new World(500, "United Kingdom", false, false, "", "", 0));
        worlds.add(new World(501, "United Kingdom", false, false, "", "", 0));
        worlds.add(new World(502, "United Kingdom", true, false, "", "Speedrun", 0));
        worlds.add(new World(505, "United Kingdom", true, false, "Nex FFA", "", 0));
        worlds.add(new World(506, "United Kingdom", true, false, "", "", 0));
        worlds.add(new World(507, "United Kingdom", true, false, "Guardians of the Rift", "", 0));
        worlds.add(new World(508, "United Kingdom", true, false, "Brimhaven Agility", "", 0));
        worlds.add(new World(509, "United Kingdom", true, false, "", "", 0));
        worlds.add(new World(510, "United Kingdom", true, false, "Forestry", "", 0));
        worlds.add(new World(511, "United Kingdom", true, false, "", "", 0));
        worlds.add(new World(512, "United Kingdom", true, false, "House Party, Gilded Altar", "", 0));
        worlds.add(new World(513, "United Kingdom", true, false, "Zeah Runecrafting", "", 0));
        worlds.add(new World(514, "United Kingdom", true, false, "Nightmare of Ashihama", "", 0));
        worlds.add(new World(515, "United Kingdom", true, false, "Blast Furnace", "", 0));
        worlds.add(new World(516, "United Kingdom", true, false, "Blast Furnace", "", 0));
        worlds.add(new World(517, "United Kingdom", true, false, "", "", 0));
        worlds.add(new World(518, "United Kingdom", true, false, "", "", 0));
        worlds.add(new World(519, "United Kingdom", true, false, "", "", 0));
        worlds.add(new World(520, "United Kingdom", true, false, "", "", 0));
        worlds.add(new World(521, "United Kingdom", true, false, "", "", 0));
        worlds.add(new World(522, "United Kingdom", true, false, "Guardians of the Rift", "", 0));
        worlds.add(new World(523, "United Kingdom", true, false, "Nex FFA", "", 0));
        worlds.add(new World(524, "United Kingdom", true, false, "", "", 0));
        worlds.add(new World(525, "United Kingdom", true, false, "", "", 0));
        worlds.add(new World(526, "Australia", true, false, "", "", 2200));
        worlds.add(new World(527, "Australia", true, false, "", "", 2000));
        worlds.add(new World(528, "Australia", true, false, "", "", 1500));
        worlds.add(new World(529, "Australia", true, false, "", "", 1250));
        worlds.add(new World(530, "Australia", false, false, "", "", 750));
        worlds.add(new World(531, "Australia", true, false, "Tombs of Amascut", "", 0));
        worlds.add(new World(532, "Australia", true, false, "Group PvM", "", 0));
        worlds.add(new World(533, "Australia", true, true, "High Risk", "", 0));
        worlds.add(new World(534, "Australia", true, false, "Guardians of the Rift", "", 0));
        worlds.add(new World(535, "Australia", true, false, "Soul Wars", "", 0));
        worlds.add(new World(537, "Australia", false, false, "", "", 0));
        worlds.add(new World(539, "United States (east)", true, true, "PvP", "", 0));
        worlds.add(new World(540, "United States (west)", true, false, "", "Speedrun", 0));
        worlds.add(new World(544, "United States (west)", false, false, "", "", 0));
        worlds.add(new World(545, "United States (west)", false, false, "", "", 0));
        worlds.add(new World(546, "United States (west)", false, false, "", "", 0));
        worlds.add(new World(547, "United States (west)", false, false, "", "", 0));
        worlds.add(new World(548, "Germany", true, true, "High Risk", "", 0));
        worlds.add(new World(549, "Germany", true, false, "", "Speedrun", 0));
        worlds.add(new World(552, "Germany", false, false, "", "", 0));
        worlds.add(new World(553, "Germany", false, false, "", "", 0));
        worlds.add(new World(554, "Germany", false, false, "", "", 0));
        worlds.add(new World(555, "Germany", false, false, "", "", 0));
        worlds.add(new World(558, "United Kingdom", true, false, "Arena", "", 0));
        worlds.add(new World(559, "United Kingdom", true, false, "LMS Competitive", "", 0));
        worlds.add(new World(565, "United Kingdom", false, false, "", "Fresh Start", 0));
        worlds.add(new World(567, "United Kingdom", true, false, "", "", 0));
        worlds.add(new World(568, "Australia", true, false, "", "Speedrun", 0));
        worlds.add(new World(569, "Australia", true, true, "Bounty Hunter", "", 0));
        worlds.add(new World(570, "Australia", true, false, "Arena", "", 0));
        worlds.add(new World(571, "Australia", false, false, "", "", 0));
        worlds.add(new World(573, "United States (east)", true, true, "Bounty Hunter", "", 0));
        worlds.add(new World(575, "United States (east)", false, false, "", "", 0));
        worlds.add(new World(576, "United States (east)", true, false, "", "Speedrun", 0));
        worlds.add(new World(577, "United States (east)", false, true, "PvP", "", 0));
        worlds.add(new World(578, "United States (east)", true, false, "Arena", "", 0));
    }

    // Existing methods remain unchanged
    public List<World> filterWorlds(WorldFilter filter) {
        return worlds.stream()
                .filter(world -> !filter.isPvP() || world.isPvP())
                .filter(world -> filter.getSpecial().isEmpty() || world.getSpecial().equals(filter.getSpecial()))
                .filter(world -> filter.isF2P() == !world.isMembers())
                .filter(world -> filter.getSkillTotal() == null || (world.getSkillTotal() != null && world.getSkillTotal() >= filter.getSkillTotal()))
                .filter(world -> filter.getRegion() == null || world.getRegion().equalsIgnoreCase(filter.getRegion()))
                .filter(world -> filter.getActivity() == null || world.getActivity().equalsIgnoreCase(filter.getActivity()))
                .filter(world -> world.isAvailable())
                .collect(Collectors.toList());
    }

    /**
     * Update the timer for a world to be available after a certain period.
     * @param worldId The world ID to update.
     * @param waitTimeInSeconds The wait time in seconds before the world can be used again.
     */
    public void updateWorldTimer(int worldId, long waitTimeInSeconds) {
        for (World world : worlds) {
            if (world.getWorldNumber() == worldId) {
                world.setNextAvailableTime(waitTimeInSeconds);
                System.out.println("Updated world " + worldId + " to be available after " + waitTimeInSeconds + " seconds.");
                break;
            }
        }
    }
}