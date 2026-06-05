package com.chicamax.sentinella.iam.domain.model.commands;

import java.util.UUID;

public record UpdateUserPermissionsCommand(UUID userId, String[] permissions) {
}
