package com.chicamax.sentinella.iam.bootstrap;

import com.chicamax.sentinella.iam.domain.model.aggregates.User;
import com.chicamax.sentinella.iam.domain.model.valueobjects.Role;
import com.chicamax.sentinella.iam.domain.services.HashingService;
import com.chicamax.sentinella.iam.infrastructure.persistence.jpa.UserRepository;
import com.chicamax.sentinella.shared.bootstrap.demo.SentinellaDemoIds;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Usuarios demo (misma convención que {@code SentinellaDataSeeder} del monolito).
 */
@Component
@Order(100)
@ConditionalOnProperty(name = "sentinella.seed.enabled", havingValue = "true")
public class IamDemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(IamDemoDataSeeder.class);

    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TransactionTemplate transactionTemplate;

    public IamDemoDataSeeder(
            UserRepository userRepository,
            HashingService hashingService,
            PlatformTransactionManager transactionManager
    ) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        transactionTemplate.executeWithoutResult(status -> seed());
    }

    private void seed() {
        if (userRepository.existsByEmail("admin@sentinella.demo")) {
            log.info("sentinella.seed (iam): datos demo ya presentes, se omite.");
            return;
        }

        UUID[] damScope = new UUID[]{SentinellaDemoIds.TAILING_DAM_CHICAMA_NORTE};
        String hash = hashingService.hash(SentinellaDemoIds.DEMO_PASSWORD);

        userRepository.save(new User(
                SentinellaDemoIds.USER_ADMIN,
                "admin@sentinella.demo",
                hash,
                "María Quispe — Administración TI",
                Role.SYSTEM_ADMIN,
                damScope
        ));
        userRepository.save(new User(
                SentinellaDemoIds.USER_MANAGER,
                "jefe.planta@sentinella.demo",
                hash,
                "Carlos Ríos — Jefe de planta relaves",
                Role.PLANT_MANAGER,
                damScope
        ));
        userRepository.save(new User(
                SentinellaDemoIds.USER_OPERATOR,
                "campo@sentinella.demo",
                hash,
                "Luis Huamán — Operario de relavera",
                Role.FIELD_OPERATOR,
                damScope
        ));
        userRepository.save(new User(
                SentinellaDemoIds.USER_AUDIT,
                "auditoria@sentinella.demo",
                hash,
                "Ana Márquez — Solo lectura OEFA",
                Role.READ_ONLY,
                damScope
        ));

        log.info(
                "sentinella.seed (iam): usuarios demo creados (contraseña '{}').",
                SentinellaDemoIds.DEMO_PASSWORD
        );
    }
}
