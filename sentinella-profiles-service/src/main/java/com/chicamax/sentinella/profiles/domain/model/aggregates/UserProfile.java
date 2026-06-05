package com.chicamax.sentinella.profiles.domain.model.aggregates;

import com.chicamax.sentinella.profiles.domain.model.events.ProfileCreatedEvent;
import com.chicamax.sentinella.profiles.domain.model.valueobjects.ActivePlanReference;
import com.chicamax.sentinella.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_profiles", schema = "profiles")
public class UserProfile extends AuditableAbstractAggregateRoot<UserProfile> {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    private String phone;

    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "plan_type")
    private String planType;

    @Column(name = "sensor_limit")
    private Integer sensorLimit;

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String preferences;

    protected UserProfile() {
    }

    public static UserProfile create(UUID userId, String email, String fullName) {
        UserProfile profile = new UserProfile();
        profile.userId = userId;
        profile.email = email;
        profile.fullName = fullName;
        profile.registerEvent(new ProfileCreatedEvent(userId, email));
        return profile;
    }

    public void applyActivePlan(ActivePlanReference plan) {
        this.planType = plan.planType();
        this.sensorLimit = plan.sensorLimit();
        this.subscriptionId = plan.subscriptionId();
    }

    public void updateDetails(String fullName, String phone, String jobTitle, String preferencesJson) {
        if (fullName != null) {
            this.fullName = fullName;
        }
        if (phone != null) {
            this.phone = phone;
        }
        if (jobTitle != null) {
            this.jobTitle = jobTitle;
        }
        if (preferencesJson != null) {
            this.preferences = preferencesJson;
        }
    }

    public UUID getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getPlanType() {
        return planType;
    }

    public Integer getSensorLimit() {
        return sensorLimit;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public String getPreferences() {
        return preferences;
    }
}
