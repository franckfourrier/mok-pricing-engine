package com.kratos.mok.pricing.app.infrastructure.repository.cantonment;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaCantonmentCreditRepository extends JpaRepository<CantonmentCreditEntity, String> {
    boolean existsByPaymentReference(String paymentReference);
    Optional<CantonmentCreditEntity> findByPaymentReference(String paymentReference);
}
