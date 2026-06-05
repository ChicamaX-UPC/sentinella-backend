package com.chicamax.sentinella.plantmanagement.domain.services;

import com.chicamax.sentinella.plantmanagement.domain.model.aggregates.Relave;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RelaveQueryService {
    List<Relave> findAll();

    Optional<Relave> findById(UUID id);
}
