package com.yuno.assignment.repository;

import com.yuno.assignment.entity.PaymentEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    Optional<PaymentEntity> findByPaymentReference(String paymentReference);
}
