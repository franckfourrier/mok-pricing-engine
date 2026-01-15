package com.kratos.mok.pricing.audit.infrastructure.adapter;

import com.kratos.mok.pricing.audit.domain.Notification;
import com.kratos.mok.pricing.audit.domain.repository.NotificationSender;
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
