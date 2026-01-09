package com.kratos.mok.pricing.auditNotification.domain.repository;

import com.kratos.mok.pricing.auditNotification.domain.Notification;

public interface NotificationSender {
    void send(Notification notification);
}
