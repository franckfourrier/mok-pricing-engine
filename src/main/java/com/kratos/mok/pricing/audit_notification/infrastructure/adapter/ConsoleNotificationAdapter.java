package com.kratos.mok.pricing.audit_notification.infrastructure.adapter;

import com.kratos.mok.pricing.audit_notification.domain.Notification;
import com.kratos.mok.pricing.audit_notification.domain.repository.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ConsoleNotificationAdapter implements NotificationSender {
    @Override
    public void send(Notification n) {
        log.info("📧 [EMAIL SENT] To: {} | Subject: {} | Priority: {}",
                n.recipient(), n.subject(), n.priority());
    }
}
