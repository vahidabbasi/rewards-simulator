package com.rewards.simulator;

import java.util.ArrayList;

class Partner {

    private final int partnerId;
    private final ArrayList<Contract> salesContracts = new ArrayList<>();
    private final ArrayList<Partner> childPartners = new ArrayList<>();
    private Partner parentPartner;

    Partner(final int partnerId) {
        this.partnerId = partnerId;
    }

    void setParent(final Partner parentPartner) {
        this.parentPartner = parentPartner;
    }

    void addChild(final Partner child) {
        child.setParent(this);
        this.childPartners.add(child);
    }

    void addOrUpdateSalesContract(final Contract contract) {
        int idx = -1;
        for (int saleContract = 0; saleContract < this.salesContracts.size(); saleContract++) {
            final Contract curContract = this.salesContracts.get(saleContract);
            if (curContract.getContractId() == contract.getContractId()) {
                idx = saleContract;
            }
        }
        if (idx != -1) {
            this.salesContracts.set(idx, contract);
        } else {
            this.salesContracts.add(contract);
        }
    }

    private int getSalesCount() {
        return this.salesContracts.size();
    }

    private SalesLevel getSalesLevel(final int... c) {

        final int count = c.length > 0 ? c[0] : this.getSalesCount();

        final SalesLevel salesLevel;
        if (count > 0 && count < 10) {
            salesLevel = SalesLevel.ANT;
        } else if (count >= 10 && count < 50) {
            salesLevel = SalesLevel.BEE;
        } else if (count >= 50 && count < 200) {
            salesLevel = SalesLevel.CAT;
        } else if (count >= 200 && count < 1000) {
            salesLevel = SalesLevel.DOG;
        } else if (count >= 1000) {
            salesLevel = SalesLevel.ELEPHANT;
        } else {
            salesLevel = SalesLevel.NONE;
        }
        return salesLevel;
    }

    private static int getRewardRatio(final SalesLevel level) {
        final int rewardRatio;
        if (level == SalesLevel.ANT) {
            rewardRatio = 5;
        } else if (level == SalesLevel.BEE) {
            rewardRatio = 7;
        } else if (level == SalesLevel.CAT) {
            rewardRatio = 9;
        } else if (level == SalesLevel.DOG) {
            rewardRatio = 12;
        } else if (level == SalesLevel.ELEPHANT) {
            rewardRatio = 19;
        } else {
            rewardRatio = 0;
        }
        return rewardRatio;
    }

    private int getSalesCountOnQuarter(final int year, final int quarter) {

        int salesCountOnQuarter = 0;
        for (final Contract contract : this.salesContracts) {
            if (contract.isEligibleForRewardOnQuarter(year, quarter)) {
                salesCountOnQuarter = salesCountOnQuarter + 1;
            }
        }
        return salesCountOnQuarter;
    }


    SalesLevel getSalesLevelOnQuarter(final int year, final int quarter) {
        final int allSalesCountOnQuarter = this.getAllSalesCountOnQuarter(year, quarter);
        return this.getSalesLevel(allSalesCountOnQuarter);
    }

    private int getAllSalesCountOnQuarter(final int year, final int quarter) {

        // the partner itself
        int salesCount = this.getSalesCountOnQuarter(year, quarter);

        // the childs: breadth-first visiting
        for (final Partner partner : this.childPartners) {
            salesCount = salesCount + partner.getAllSalesCountOnQuarter(year, quarter);
        }
        return salesCount;
    }

    private int getSalesBonusAmountOnQuarter(final int year, final int quarter) {
        int bonus = 0;
        for (final Contract contract : this.salesContracts) {
            if (contract.isEligibleForRewardOnQuarter(year, quarter) &&
                    contract.getContractType().equals(ContractType.RABBIT.name())) {
                bonus = bonus + Contract.BONUS_RABBIT_CONTRACTS;
            }
        }
        return bonus;
    }

    private int getChildRewardDifferences(final int year, final int quarter) {
        int difference = 0;

        /* find current partner level & ratio */
        final int nContracts = this.getAllSalesCountOnQuarter(year, quarter);
        final SalesLevel currentLevel = this.getSalesLevel(nContracts);
        final int currentRatio = Partner.getRewardRatio(currentLevel);

        for (final Partner partner : this.childPartners) {
            /* find child partner level & ratio */
            final int countChildContracts = partner.getAllSalesCountOnQuarter(year, quarter);
            final SalesLevel childLevel = partner.getSalesLevel(countChildContracts);
            final int childRatio = Partner.getRewardRatio(childLevel);
            difference = difference + (countChildContracts * (currentRatio - childRatio));
        }
        return difference;
    }

    int getRewardsOnQuarter(final int year, final int quarter) {

        final int rewardAmount;
        final int countDirectContracts = this.getSalesCountOnQuarter(year, quarter);
        final int countAllContract = this.getAllSalesCountOnQuarter(year, quarter);

        final int bonus = this.getSalesBonusAmountOnQuarter(year, quarter);
        final SalesLevel level = this.getSalesLevel(countAllContract);

        final int rewardRatio = Partner.getRewardRatio(level);
        final int childRewardDifferences = this.getChildRewardDifferences(year, quarter);

        rewardAmount = bonus + (countDirectContracts * rewardRatio) + (childRewardDifferences);
        return rewardAmount;
    }

    private ArrayList<String> getCurrentRewardTimes() {
        final ArrayList<String> rewardTimes = new ArrayList<>();

        for (final Contract contract : this.salesContracts) {
            final ArrayList<String> curRewardTimes = contract.getRewardTimes();
            for (final String curRewardTime : curRewardTimes) {
                if (!rewardTimes.contains(curRewardTime)) {
                    rewardTimes.add(curRewardTime);
                }
            }
        }
        return rewardTimes;
    }

    ArrayList<String> getAllRewardTimes() {
        final ArrayList<String> allRewardsTimes = this.getCurrentRewardTimes();

        for (final Partner p : this.childPartners) {
            final ArrayList<String> r = p.getAllRewardTimes();
            for (final String r_j : r) {
                if (!allRewardsTimes.contains(r_j)) {
                    allRewardsTimes.add(r_j);
                }
            }
        }
        return allRewardsTimes;
    }

    ArrayList<String> getTotalRewards() {
        final ArrayList<String> totalRewards = new ArrayList<>();
        final ArrayList<String> times = this.getAllRewardTimes();
        for (final String time : times) {
            final String[] split = time.split(" ");
            final int year = Integer.parseInt(split[0]);
            final int quarter = Integer.parseInt(split[1]);
            final int rewardAmount = this.getRewardsOnQuarter(year, quarter);
            totalRewards.add(String.format("%d %d %d", year, quarter, rewardAmount));
        }
        return totalRewards;
    }

}
