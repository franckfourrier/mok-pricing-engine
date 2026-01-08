package com.kratos.mok.pricing.audit_notification.domain.repository;

import com.kratos.mok.pricing.audit_notification.domain.Notification;

public interface NotificationSender {
    void send(Notification notification);
}
