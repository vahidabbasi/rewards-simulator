package com.rewards.simulator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Objects;


@Component
@Slf4j
@ComponentScan(basePackages = "com.rewards.simulator")
public class RewardsSimulatorApplication {

    private final RewardsSimulatorExecutor rewardsSimulatorExecutor;

    @Inject
    public RewardsSimulatorApplication(final RewardsSimulatorExecutor rewardsSimulatorExecutor) {
        Objects.requireNonNull(rewardsSimulatorExecutor, "rewardsSimulatorExecutor was null when injected");
        this.rewardsSimulatorExecutor = rewardsSimulatorExecutor;
    }

    public static void main(final String[] args) {
        log.info("Started RewardsSimulatorApplication...");
        final ApplicationContext context = new AnnotationConfigApplicationContext(RewardsSimulatorApplication.class);

        final RewardsSimulatorApplication app = context.getBean(RewardsSimulatorApplication.class);
        app.run();
        log.info("Finished RewardsSimulatorApplication.");
    }

    void run() {
        rewardsSimulatorExecutor.execute();

        // Uncomment and run the test program
        // rewardsSimulatorExecutor.testProgram();
    }

}
