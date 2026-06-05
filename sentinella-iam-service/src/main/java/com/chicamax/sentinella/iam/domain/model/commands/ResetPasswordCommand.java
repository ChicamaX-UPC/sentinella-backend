package com.chicamax.sentinella.iam.domain.model.commands;

public record ResetPasswordCommand(String token, String newPassword) {
}
