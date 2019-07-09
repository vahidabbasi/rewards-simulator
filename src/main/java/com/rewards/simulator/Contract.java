package com.rewards.simulator;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.ArrayList;

class Contract {

    private final int contractId;
    private final String contractType;
    private LocalDate startDate;
    private LocalDate endDate;

    private static final int MAX_REWARD_YEARS = 8;
    static final int BONUS_RABBIT_CONTRACTS = 50;

    Contract(final int contractId, final String contractType) {
        this.contractId = contractId;
        this.contractType = contractType;
    }

    void setContractDate(final String action, final String date) {
        if (action.equals("BEGIN")) {
            this.setStartDate(LocalDate.parse(date));
        } else {
            this.setEndDate(LocalDate.parse(date));
        }
    }

    ArrayList<String> getRewardTimes() {
        // TODO: handle the case where endDate is null

        final ArrayList<String> rewardTimes = new ArrayList<>();
        final int startQuarter = this.startDate.get(IsoFields.QUARTER_OF_YEAR);
        int startYear = this.startDate.getYear();
        final int endQuarter = this.endDate.get(IsoFields.QUARTER_OF_YEAR);
        final int endYear = this.endDate.getYear();
        rewardTimes.add(String.format("%d %d", startYear, startQuarter));


        if (endYear <= startYear) {
            return rewardTimes;
        }

        int rewarded = 1;

        while (endYear > startYear) {
            startYear = startYear + 1;

            /* check if on the last year, contract is over
            before the start of the rewarding start quarter*/
            if (endYear == startYear && endQuarter < startQuarter) {
                break;
            }

            rewardTimes.add(String.format("%d %d", startYear, startQuarter));
            rewarded = rewarded + 1;
            if (rewarded == MAX_REWARD_YEARS) {
                break;
            }
        }

        return rewardTimes;
    }

    boolean isEligibleForRewardOnQuarter(final int year, final int quarter) {
        // TODO: handle the case where endDate is null
        if (this.startDate.getYear() <= year && year <= this.endDate.getYear()) {

            if (this.startDate.getYear() == year && this.startDate.get(IsoFields.QUARTER_OF_YEAR) > quarter) {
                /* contract not started */
                return false;
            } else if (this.endDate.getYear() == year && this.endDate.get(IsoFields.QUARTER_OF_YEAR) < quarter) {
                /* contract is over */
                return false;
            } else if (this.startDate.get(IsoFields.QUARTER_OF_YEAR) != quarter) {
                /* each contract is reward only in its own quarter */
                return false;
            } else if (this.endDate.getYear() - this.startDate.getYear() > MAX_REWARD_YEARS
                    && year - this.startDate.getYear() > MAX_REWARD_YEARS) {
                /* no reward after MAX_REWARD_YEARS  */
                return false;
            } else {
                /* contract is eligible for reward */
                return true;
            }
        }
        return false;
    }

    private void setStartDate(final LocalDate startDate) {
        this.startDate = startDate;
    }

    private void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    int getContractId() {
        return contractId;
    }

    String getContractType() {
        return contractType;
    }
}
