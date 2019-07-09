package com.rewards.simulator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import static com.rewards.simulator.Commands.EXIT_COMMAND;
import static com.rewards.simulator.Commands.GET_ALL_REWARD_PATTERN;
import static com.rewards.simulator.Commands.GET_LEVEL_PARTNER_PATTERN;
import static com.rewards.simulator.Commands.GET_REWARD_PARTNER_PATTERN;
import static com.rewards.simulator.Commands.LOAD_CSV_FILE_PATTERN;
import static com.rewards.simulator.Commands.REGISTER_PARTNERID_PARENT_PATTERN;
import static com.rewards.simulator.Commands.REGISTER_PARTNERID_PATTERN;

@Component
@Slf4j
class RewardsSimulatorExecutor {

    private static final Map<Integer, Partner> partnerRepository = new HashMap<>();
    private static final Map<Integer, Contract> contractRepository = new HashMap<>();

    private static final String REQUEST_COMMAND_MSG = "Type your command:";
    private static final String OPERATION_SUCCESS = "Operation Successful!";
    private static final String ERROR_INVALID_COMMAND_MSG = "<< Error >>: Invalid Command!";
    private static final String PROCESSING_COMMAND_MSG = "Processing Command: ";
    private static final String PARTNER_NOT_EXIST_MSG = "Partner with the given id does not exists!";
    private static final String COMMA_DELIMITER = ", ";

    void execute() {
        log.info(REQUEST_COMMAND_MSG);
        final Scanner scanner = new Scanner(System.in);

        while (scanner.hasNext()) {

            final String command = scanner.nextLine();
            if (command.equals(EXIT_COMMAND)) {
                break;
            }
            log.info(PROCESSING_COMMAND_MSG + command);

            boolean b = Pattern.matches(REGISTER_PARTNERID_PATTERN, command);
            if (b) {
                registerPartner(command);
                continue;
            }

            b = Pattern.matches(REGISTER_PARTNERID_PARENT_PATTERN, command);
            if (b) {
                registerPartnerIdParentPartnerId(command);
                continue;
            }

            b = Pattern.matches(LOAD_CSV_FILE_PATTERN, command);
            if (b) {
                loadFile(command);
                continue;
            }

            b = Pattern.matches(GET_LEVEL_PARTNER_PATTERN, command);
            if (b) {
                levelPartnerYearQuarter(command);
                continue;
            }

            b = Pattern.matches(GET_REWARD_PARTNER_PATTERN, command);
            if (b) {
                rewardsPartnerIdYearQuarter(command);
                continue;
            }

            b = Pattern.matches(GET_ALL_REWARD_PATTERN, command);
            if (b) {
                allRewards(command);
                continue;
            }

            log.info(ERROR_INVALID_COMMAND_MSG);
        }

    }

    private void registerPartner(final String command) {
        final String[] split = command.split("\\s+");
        final int partnerId = Integer.parseInt(split[1]);
        final Partner partner = new Partner(partnerId);
        partnerRepository.put(partnerId, partner);

        log.info(OPERATION_SUCCESS);
        log.info(REQUEST_COMMAND_MSG);
    }

    private void registerPartnerIdParentPartnerId(final String command) {
        final String[] split = command.split("\\s+");
        final int partnerId = Integer.parseInt(split[1]);
        final int parentPartnerId = Integer.parseInt(split[2]);

        final Partner child;
        final Partner parent;

        // get or create child partner
        if (partnerRepository.containsKey(partnerId)) {
            child = partnerRepository.get(partnerId);
        } else {
            child = new Partner(partnerId);
            partnerRepository.put(partnerId, child);
        }

        // get or create parent partner
        if (partnerRepository.containsKey(parentPartnerId)) {
            parent = partnerRepository.get(parentPartnerId);
        } else {
            parent = new Partner(parentPartnerId);
            partnerRepository.put(parentPartnerId, child);
        }

        // set child-parent relationship
        child.setParent(parent);
        parent.addChild(child);

        log.info(OPERATION_SUCCESS);
        log.info(REQUEST_COMMAND_MSG);
    }

    private void allRewards(final String command) {
        final String[] split = command.split("\\s+");
        final int partnerId = Integer.parseInt(split[1]);
        if (partnerRepository.containsKey(partnerId)) {
            final Partner partner = partnerRepository.get(partnerId);
            final ArrayList<String> rewards = partner.getTotalRewards();
            for (final String reward : rewards) {
                log.info(reward);
            }
        } else {
            log.info(PARTNER_NOT_EXIST_MSG);
        }

        log.info(REQUEST_COMMAND_MSG);
    }

    private void rewardsPartnerIdYearQuarter(final String command) {
        final String[] split = command.split("\\s+");
        final int partnerId = Integer.parseInt(split[1]);
        final int year = Integer.parseInt(split[2]);
        final int quarter = Integer.parseInt(split[3]);

        if (partnerRepository.containsKey(partnerId)) {
            final Partner partner = partnerRepository.get(partnerId);
            final int rewardAmount = partner.getRewardsOnQuarter(year, quarter);
            log.info(String.format("%d", rewardAmount));
        } else {
            log.info(PARTNER_NOT_EXIST_MSG);
        }

        log.info(REQUEST_COMMAND_MSG);
    }

    private void levelPartnerYearQuarter(final String command) {
        final String[] split = command.split("\\s+");
        final int partnerId = Integer.parseInt(split[1]);
        final int year = Integer.parseInt(split[2]);
        final int quarter = Integer.parseInt(split[3]);

        if (partnerRepository.containsKey(partnerId)) {
            final Partner partner = partnerRepository.get(partnerId);
            final SalesLevel level = partner.getSalesLevelOnQuarter(year, quarter);
            log.info(level.toString());
        } else {
            log.info(PARTNER_NOT_EXIST_MSG);
        }

        log.info(REQUEST_COMMAND_MSG);
    }

    private void loadFile(final String command) {
        final String[] split = command.split("\\s+");
        final String filename = split[1];

        final Path path = FileSystems.getDefault().getPath("src/main/resources").toAbsolutePath();
        final Path pathname = path.resolve(filename);
        log.info(pathname.toString());

        try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(pathname.toString()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                final String[] values = line.split(COMMA_DELIMITER);

                final int partnerId = Integer.parseInt(values[0]);
                final int contractId = Integer.parseInt(values[1]);
                final String contractType = values[2];
                final String date = values[3];
                final String action = values[4];

                final Partner partner = getPartner(partnerId);

                final Contract contract = getContract(contractId, contractType);

                contract.setContractDate(action, date);

                partner.addOrUpdateSalesContract(contract);

            }
            log.info(OPERATION_SUCCESS);

        } catch (final IOException ex) {
            log.info("Exception happens" + ex);
        }

        log.info(REQUEST_COMMAND_MSG);
    }

    private Contract getContract(final int contractId, final String contractType) {
        final Contract contract;
        if (contractRepository.containsKey(contractId)) {
            contract = contractRepository.get(contractId);
        } else {
            contract = new Contract(contractId, contractType);
            contractRepository.put(contractId, contract);
        }
        return contract;
    }

    private Partner getPartner(final int partnerId) {
        final Partner partner;
        if (partnerRepository.containsKey(partnerId)) {
            partner = partnerRepository.get(partnerId);
        } else {
            partner = new Partner(partnerId);
            partnerRepository.put(partnerId, partner);
        }
        return partner;
    }

//    void testProgram() {
//        final Partner p1 = new Partner(1);
//        final Partner p2 = new Partner(2);
//        final Partner p3 = new Partner(3);
//        final Partner p4 = new Partner(4);
//
//        partnerRepository.put(1, p1);
//        partnerRepository.put(2, p2);
//        partnerRepository.put(3, p3);
//        partnerRepository.put(4, p4);
//
//        p1.addChild(p2);
//        p1.addChild(p3);
//        p2.setParent(p1);
//        p3.setParent(p1);
//        p2.addChild(p4);
//        p4.setParent(p2);
//
//
//        final String filename = "records.csv";
//        final Path path = FileSystems.getDefault().getPath("src/main/resources").toAbsolutePath();
//        final Path pathname = path.resolve(filename);
//
//        try (final BufferedReader bufferedReader = new BufferedReader(new FileReader(pathname.toString()))) {
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                final String[] values = line.split(COMMA_DELIMITER);
//
//                final int partnerId = Integer.parseInt(values[0]);
//                final int contractId = Integer.parseInt(values[1]);
//                final String contractType = values[2];
//                final String date = values[3];
//                final String action = values[4];
//
//                final Partner partner = getPartner(partnerId);
//
//                final Contract contract = getContract(contractId, contractType);
//
//                contract.setContractDate(action, date);
//
//                partner.addOrUpdateSalesContract(contract);
//
//            }
//            log.info(OPERATION_SUCCESS);
//
//        } catch (final IOException ex) {
//            log.info("Exception happens" + ex);
//        }
//
//        System.out.println(p1.getAllRewardTimes());
//        System.out.println(p1.getTotalRewards());
//
//    }
}
