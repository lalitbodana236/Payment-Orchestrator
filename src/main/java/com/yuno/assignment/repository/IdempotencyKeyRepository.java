package com.yuno.assignment.repository;

import com.yuno.assignment.entity.IdempotencyKeyEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKeyEntity, Long> {

    Optional<IdempotencyKeyEntity> findByIdempotencyKey(String idempotencyKey);
}
