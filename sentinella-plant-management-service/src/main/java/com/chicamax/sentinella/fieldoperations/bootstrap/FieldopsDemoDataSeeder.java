package com.chicamax.sentinella.fieldoperations.bootstrap;

import com.chicamax.sentinella.fieldoperations.domain.model.aggregates.InspectionRound;
import com.chicamax.sentinella.fieldoperations.domain.model.valueobjects.RoundStatus;
import com.chicamax.sentinella.fieldoperations.infrastructure.persistence.jpa.InspectionRoundRepository;
import com.chicamax.sentinella.shared.bootstrap.demo.SentinellaDemoIds;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@Order(100)
@ConditionalOnProperty(name = "sentinella.seed.enabled", havingValue = "true")
public class FieldopsDemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FieldopsDemoDataSeeder.class);
    private static final int DEMO_ROUNDS = 48;

    private final InspectionRoundRepository inspectionRoundRepository;
    private final TransactionTemplate transactionTemplate;
    private final boolean seedForce;

    public FieldopsDemoDataSeeder(
            InspectionRoundRepository inspectionRoundRepository,
            PlatformTransactionManager transactionManager,
            @Value("${sentinella.seed.force:false}") boolean seedForce
    ) {
        this.inspectionRoundRepository = inspectionRoundRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.seedForce = seedForce;
    }

    @Override
    public void run(ApplicationArguments args) {
        transactionTemplate.executeWithoutResult(status -> seed());
    }

    private void seed() {
        if (!seedForce && inspectionRoundRepository.count() >= 20) {
            log.info(
                    "sentinella.seed (plant-management): {} rondas demo ya presentes, se omite.",
                    inspectionRoundRepository.count()
            );
            return;
        }

        if (seedForce && inspectionRoundRepository.count() > 0) {
            inspectionRoundRepository.deleteAllInBatch();
        }

        OffsetDateTime now = OffsetDateTime.now();
        List<InspectionRound> rounds = new ArrayList<>(DEMO_ROUNDS);
        RoundStatus[] statuses = {
                RoundStatus.SYNCED,
                RoundStatus.COMPLETED,
                RoundStatus.IN_PROGRESS,
                RoundStatus.PENDING
        };

        for (int i = 0; i < DEMO_ROUNDS; i++) {
            OffsetDateTime scheduled = now.minusDays(i % 75).withHour(7 + (i % 4)).withMinute(30).withSecond(0).withNano(0);
            InspectionRound round = new InspectionRound(
                    java.util.UUID.randomUUID(),
                    i % 3 == 0 ? SentinellaDemoIds.USER_OPERATOR : SentinellaDemoIds.USER_MANAGER,
                    SentinellaDemoIds.TAILING_DAM_CHICAMA_NORTE,
                    scheduled,
                    i % 5 == 0
            );
            RoundStatus status = statuses[i % statuses.length];
            applyHistoricalState(round, status, scheduled);
            rounds.add(round);
        }

        inspectionRoundRepository.saveAll(rounds);
        log.info("sentinella.seed (plant-management): {} rondas de inspeccion demo creadas.", DEMO_ROUNDS);
    }

    private static void applyHistoricalState(InspectionRound round, RoundStatus status, OffsetDateTime scheduled) {
        try {
            setField(round, "status", status);
            if (status == RoundStatus.PENDING) {
                return;
            }
            setField(round, "startedAt", scheduled.plusHours(1));
            if (status == RoundStatus.IN_PROGRESS) {
                return;
            }
            setField(round, "completedAt", scheduled.plusHours(3));
            if (status == RoundStatus.COMPLETED) {
                return;
            }
            setField(round, "syncedAt", scheduled.plusHours(4));
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("No se pudo preparar ronda demo", ex);
        }
    }

    private static void setField(Object target, String name, Object value) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
