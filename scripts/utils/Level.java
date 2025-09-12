package utils;

public enum Level {
    _1(1, 0), _2(2, 83), _3(3, 174),
    _4(4, 276), _5(5, 388), _6(6, 512),
    _7(7, 650), _8(8, 801), _9(9, 969),
    _10(10, 1154), _11(11, 1358), _12(12, 1584),
    _13(13, 1833), _14(14, 2107), _15(15, 2411),
    _16(16, 2746), _17(17, 3115), _18(18, 3523),
    _19(19, 3973), _20(20, 4470), _21(21, 5018),
    _22(22, 5624), _23(23, 6291), _24(24, 7028),
    _25(25, 7842), _26(26, 8740), _27(27, 9730),
    _28(28, 10824), _29(29, 12031), _30(30, 13363),
    _31(31, 14833), _32(32, 16456), _33(33, 18247),
    _34(34, 20224), _35(35, 22406), _36(36, 24815),
    _37(37, 27473), _38(38, 30408), _39(39, 33648),
    _40(40, 37224), _41(41, 41171), _42(42, 45529),
    _43(43, 50339), _44(44, 55649), _45(45, 61512),
    _46(46, 67983), _47(47, 75127), _48(48, 83014),
    _49(49, 91721), _50(50, 101333), _51(51, 111945),
    _52(52, 123660), _53(53, 136594), _54(54, 150872),
    _55(55, 166636), _56(56, 184040), _57(57, 203254),
    _58(58, 224466), _59(59, 247886), _60(60, 273742),
    _61(61, 302288), _62(62, 333804), _63(63, 368599),
    _64(64, 407015), _65(65, 449428), _66(66, 496254),
    _67(67, 547953), _68(68, 605032), _69(69, 668051),
    _70(70, 737627), _71(71, 814445), _72(72, 899257),
    _73(73, 992895), _74(74, 1096278), _75(75, 1210421),
    _76(76, 1336443), _77(77, 1475581), _78(78, 1629200),
    _79(79, 1798808), _80(80, 1986068), _81(81, 2192818),
    _82(82, 2421087), _83(83, 2673114), _84(84, 2951373),
    _85(85, 3258594), _86(86, 3597792), _87(87, 3972294),
    _88(88, 4385776), _89(89, 4842295), _90(90, 5346332),
    _91(91, 5902831), _92(92, 6517253), _93(93, 7195629),
    _94(94, 7944614), _95(95, 8771558), _96(96, 9684577),
    _97(97, 10692629), _98(98, 11805606), _99(99, 13034431),
    _100(100, 14391160), _101(101, 15889109), _102(102, 17542976),
    _103(103, 19368992), _104(104, 21385073), _105(105, 23611006),
    _106(106, 26068632), _107(107, 28782069), _108(108, 31777943),
    _109(109, 35085654), _110(110, 38737661), _111(111, 42769801),
    _112(112, 47221641), _113(113, 52136869), _114(114, 57563718),
    _115(115, 63555443), _116(116, 70170840), _117(117, 77474828),
    _118(118, 85539082), _119(119, 94442737), _120(120, 104273167);

    private final int level;
    private final int xp;

    Level(int level, int xp) {
        this.level = level;
        this.xp = xp;
    }

    public int getLevel() {
        return level;
    }

    public int getXp() {
        return xp;
    }

    /**
     * Returns the {@link Level} object associated with the passed {@link Integer level}.
     *
     * @param level An {@link Integer} value denoting the level of the desired {@link Level} object.
     * @return The {@link Level} object associated with the passed {@link Integer level}.
     */
    public static Level getLevelByLevel(int level) {
        return values()[level];
    }

    /**
     * Calculates the virtual level corresponding to a given experience total by iterating through all levels until a
     * level with a greater xp values is found, returning that level - 1.
     *
     * @param experience XP to calculate a virtual level for.
     * @return The level corresponding to the passed experience amount, as an int value, else returns -1 if no match is found.
     */
    public int getLevelByXP(int experience) {
        // iterate through all levels/xp values (starts at 0 xp)
        for (Level level : Level.values()) {
            // if this levels xp is more than the passed xp, that's the next level up
            if (level.getXp() > experience)
                // therefore, the level at this experience is this level, minus one
                return level.getLevel() - 1;
        }

        return -1;
    }
}

