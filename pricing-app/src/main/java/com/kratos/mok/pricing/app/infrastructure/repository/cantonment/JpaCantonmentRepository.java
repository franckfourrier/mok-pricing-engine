package com.kratos.mok.pricing.app.infrastructure.repository.cantonment;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaCantonmentRepository extends JpaRepository<CantonmentEntity, String> {
    boolean existsByPaymentReference(String paymentReference);
    Optional<CantonmentEntity> findByPaymentReference(String paymentReference);
}
