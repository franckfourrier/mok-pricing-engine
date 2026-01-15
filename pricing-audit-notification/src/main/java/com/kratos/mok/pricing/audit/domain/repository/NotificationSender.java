package com.kratos.mok.pricing.audit.domain.repository;

import com.kratos.mok.pricing.audit.domain.Notification;

public interface NotificationSender {
    void send(Notification notification);
}
