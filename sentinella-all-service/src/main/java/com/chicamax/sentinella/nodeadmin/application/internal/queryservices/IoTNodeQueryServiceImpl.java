package com.chicamax.sentinella.nodeadmin.application.internal.queryservices;

import com.chicamax.sentinella.nodeadmin.domain.model.aggregates.IoTNode;
import com.chicamax.sentinella.nodeadmin.domain.model.queries.GetNodesByDamQuery;
import com.chicamax.sentinella.nodeadmin.domain.services.IoTNodeQueryService;
import com.chicamax.sentinella.nodeadmin.infrastructure.persistence.jpa.IoTNodeRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class IoTNodeQueryServiceImpl implements IoTNodeQueryService {

    private final IoTNodeRepository ioTNodeRepository;

    public IoTNodeQueryServiceImpl(IoTNodeRepository ioTNodeRepository) {
        this.ioTNodeRepository = ioTNodeRepository;
    }

    @Override
    public List<IoTNode> handle(GetNodesByDamQuery query) {
        return ioTNodeRepository.findByTailingDamId(query.tailingDamId());
    }
}
