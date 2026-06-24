package com.chicamax.sentinella.monitoring.bootstrap;

import com.chicamax.sentinella.monitoring.application.internal.prediction.ReadingSnapshotBackfillService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(110)
public class SnapshotBackfillRunner implements ApplicationRunner {

    private final ReadingSnapshotBackfillService readingSnapshotBackfillService;

    public SnapshotBackfillRunner(ReadingSnapshotBackfillService readingSnapshotBackfillService) {
        this.readingSnapshotBackfillService = readingSnapshotBackfillService;
    }

    @Override
    public void run(ApplicationArguments args) {
        readingSnapshotBackfillService.backfillLastDays(14);
    }
}
