package com.kratos.mok.pricing.audit.domain;

import com.kratos.mok.pricing.audit.domain.enums.Priority;

import java.util.Map;

public final class NotificationPolicy {

    private static final Map<String, Priority> ACTION_TO_PRIORITY = Map.ofEntries(
            Map.entry("CONFIGURATION_BLOCKED", Priority.CRITICAL),

            Map.entry("REJECTED", Priority.WARNING),
            Map.entry("SUSPENDED", Priority.WARNING),

            Map.entry("CREATED", Priority.INFO),
            Map.entry("SUBMITTED_FOR_APPROVAL", Priority.INFO),
            Map.entry("APPROVED", Priority.INFO),
            Map.entry("UPDATED", Priority.INFO),
            Map.entry("ARCHIVED", Priority.INFO)
    );

    private NotificationPolicy() {}

    public static Priority resolve(String action) {
        return ACTION_TO_PRIORITY.getOrDefault(action, Priority.INFO);
    }
}
