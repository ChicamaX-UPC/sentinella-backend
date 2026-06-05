package com.chicamax.sentinella.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UpdateUserPermissionsResource(@NotEmpty List<String> permissions) {
}
