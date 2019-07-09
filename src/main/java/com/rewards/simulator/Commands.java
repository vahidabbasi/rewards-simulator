package com.rewards.simulator;

final class Commands {

    private Commands() {
    }

    static final String EXIT_COMMAND = "EXIT";

    static final String REGISTER_PARTNERID_PATTERN = "^REGISTER\\s\\d+\\s*$";

    static final String REGISTER_PARTNERID_PARENT_PATTERN = "^REGISTER\\s\\d+\\s\\d+\\s*$";

    static final String LOAD_CSV_FILE_PATTERN = "^LOAD .*\\.csv\\s*$";

    static final String GET_LEVEL_PARTNER_PATTERN = "^LEVEL\\s\\d+\\s\\d+\\s\\d+\\s*$";

    static final String GET_REWARD_PARTNER_PATTERN = "^REWARDS\\s\\d+\\s\\d+\\s\\d+\\s*$";

    static final String GET_ALL_REWARD_PATTERN = "^ALL_REWARDS\\s\\d+\\s*$";
}
