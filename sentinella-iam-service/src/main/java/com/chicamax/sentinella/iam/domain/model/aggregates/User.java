package com.chicamax.sentinella.iam.domain.model.aggregates;

import com.chicamax.sentinella.iam.domain.model.valueobjects.Role;
import com.chicamax.sentinella.iam.domain.model.valueobjects.RolePermissionDefaults;
import com.chicamax.sentinella.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "users", schema = "iam")
public class User extends AuditableAbstractAggregateRoot<User> {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tailing_dam_ids", columnDefinition = "uuid[]")
    private UUID[] tailingDamIds;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "last_login")
    private OffsetDateTime lastLogin;

    @Column(name = "failed_attempts", nullable = false)
    private short failedAttempts;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "permissions", columnDefinition = "text[]", nullable = false)
    private String[] permissions = new String[0];

    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Column(name = "phone", length = 30)
    private String phone;

    protected User() {
    }

    public User(UUID id, String email, String passwordHash, String fullName, Role role, UUID[] tailingDamIds) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.tailingDamIds = tailingDamIds;
        this.permissions = RolePermissionDefaults.toArray(role);
    }

    public boolean isLockedAt(OffsetDateTime when) {
        return lockedUntil != null && lockedUntil.isAfter(when);
    }

    public void registerFailedAttempt(OffsetDateTime now, int maxAttempts, int lockMinutes) {
        failedAttempts++;
        if (failedAttempts >= maxAttempts) {
            lockedUntil = now.plusMinutes(lockMinutes);
        }
    }

    public void registerSuccessfulLogin(OffsetDateTime now) {
        lastLogin = now;
        failedAttempts = 0;
        lockedUntil = null;
    }

    public void updateRole(Role newRole) {
        this.role = newRole;
    }

    public void updatePasswordHash(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void updateFullName(String name) {
        this.fullName = name;
    }

    public void updateProfileDetails(String name, String title, String phoneNumber) {
        this.fullName = name;
        this.jobTitle = title;
        this.phone = phoneNumber;
    }

    public void updatePermissions(String[] explicitPermissions) {
        this.permissions = RolePermissionDefaults.validateAndDistinct(explicitPermissions);
    }

    public String[] getEffectivePermissions() {
        return RolePermissionDefaults.normalize(permissions, role);
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public UUID[] getTailingDamIds() {
        return tailingDamIds;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getLastLogin() {
        return lastLogin;
    }

    public short getFailedAttempts() {
        return failedAttempts;
    }

    public OffsetDateTime getLockedUntil() {
        return lockedUntil;
    }

    public String[] getPermissions() {
        return permissions;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getPhone() {
        return phone;
    }
}
