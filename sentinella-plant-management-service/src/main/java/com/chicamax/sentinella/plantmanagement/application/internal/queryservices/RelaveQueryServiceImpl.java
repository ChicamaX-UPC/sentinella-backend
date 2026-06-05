package com.chicamax.sentinella.plantmanagement.application.internal.queryservices;

import com.chicamax.sentinella.plantmanagement.domain.model.aggregates.Relave;
import com.chicamax.sentinella.plantmanagement.domain.services.RelaveQueryService;
import com.chicamax.sentinella.plantmanagement.infrastructure.persistence.jpa.RelaveRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RelaveQueryServiceImpl implements RelaveQueryService {

    private final RelaveRepository relaveRepository;

    public RelaveQueryServiceImpl(RelaveRepository relaveRepository) {
        this.relaveRepository = relaveRepository;
    }

    @Override
    public List<Relave> findAll() {
        return relaveRepository.findAll();
    }

    @Override
    public Optional<Relave> findById(UUID id) {
        return relaveRepository.findById(id);
    }
}
