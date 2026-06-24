package com.chicamax.sentinella.iam.domain.model.commands;

public record SignUpCommand(String email, String password, String fullName, String companyName) {
}
