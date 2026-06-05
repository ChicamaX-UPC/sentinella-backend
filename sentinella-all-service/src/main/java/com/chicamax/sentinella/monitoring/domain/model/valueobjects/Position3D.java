package com.chicamax.sentinella.monitoring.domain.model.valueobjects;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Position3D(
        @JsonProperty("x") double x,
        @JsonProperty("y") double y,
        @JsonProperty("z") double z
) {
}
