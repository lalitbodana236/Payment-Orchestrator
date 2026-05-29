package com.yuno.assignment.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuno.assignment.config.PaymentOrchestrationProperties;
import com.yuno.assignment.dto.PaymentRequest;
import com.yuno.assignment.dto.PaymentResponse;
import com.yuno.assignment.entity.IdempotencyKeyEntity;
import com.yuno.assignment.exception.ConcurrentRequestInProgressException;
import com.yuno.assignment.exception.IdempotencyConflictException;
import com.yuno.assignment.repository.IdempotencyKeyRepository;
import com.yuno.assignment.util.HashingUtils;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final HashingUtils hashingUtils;
    private final ObjectMapper objectMapper;
    private final PaymentOrchestrationProperties properties;

    public PaymentResponse execute(String idempotencyKey, PaymentRequest request, Supplier<PaymentResponse> action) {
        String requestHash = hashingUtils.sha256(request);
        String lockKey = "idem:lock:" + idempotencyKey;
        String hashKey = "idem:hash:" + idempotencyKey;
        String responseKey = "idem:response:" + idempotencyKey;
        String lockToken = UUID.randomUUID().toString();

        PaymentResponse cachedResponse = readAndValidateCachedResponse(idempotencyKey, requestHash, hashKey, responseKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        boolean acquired = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(
                lockKey,
                lockToken,
                Duration.ofSeconds(properties.idempotency().lockTtlSeconds())));

        if (!acquired) {
            return waitForInFlightRequest(idempotencyKey, requestHash, hashKey, responseKey);
        }

        try {
            validateHash(idempotencyKey, requestHash, hashKey);

            PaymentResponse response = readAndValidateCachedResponse(idempotencyKey, requestHash, hashKey, responseKey);
            if (response != null) {
                return response;
            }

            redisTemplate.opsForValue().set(
                    hashKey,
                    requestHash,
                    Duration.ofHours(properties.idempotency().ttlHours()));

            PaymentResponse freshResponse = action.get();
            persistResponse(idempotencyKey, requestHash, responseKey, freshResponse);
            return freshResponse;
        } finally {
            releaseLock(lockKey, lockToken);
        }
    }

    private PaymentResponse readAndValidateCachedResponse(
            String idempotencyKey,
            String requestHash,
            String hashKey,
            String responseKey) {
        validateHash(idempotencyKey, requestHash, hashKey);
        String cachedPayload = redisTemplate.opsForValue().get(responseKey);
        if (cachedPayload == null || cachedPayload.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(cachedPayload, PaymentResponse.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to deserialize cached idempotent response", exception);
        }
    }

    private void persistResponse(String idempotencyKey, String requestHash, String responseKey, PaymentResponse response) {
        try {
            String payload = objectMapper.writeValueAsString(response);
            Duration ttl = Duration.ofHours(properties.idempotency().ttlHours());
            redisTemplate.opsForValue().set(responseKey, payload, ttl);

            IdempotencyKeyEntity entity = idempotencyKeyRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseGet(IdempotencyKeyEntity::new);
            entity.setIdempotencyKey(idempotencyKey);
            entity.setRequestHash(requestHash);
            entity.setResponsePayload(payload);
            idempotencyKeyRepository.save(entity);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize idempotent response", exception);
        }
    }

    private void validateHash(String idempotencyKey, String requestHash, String hashKey) {
        String existingHash = Optional.ofNullable(redisTemplate.opsForValue().get(hashKey))
                .or(() -> idempotencyKeyRepository.findByIdempotencyKey(idempotencyKey).map(IdempotencyKeyEntity::getRequestHash))
                .orElse(null);
        if (existingHash != null && !existingHash.equals(requestHash)) {
            throw new IdempotencyConflictException("Idempotency key was previously used with a different request payload");
        }
    }

    private PaymentResponse waitForInFlightRequest(
            String idempotencyKey,
            String requestHash,
            String hashKey,
            String responseKey) {
        long deadline = System.currentTimeMillis() + properties.idempotency().waitTimeoutMs();
        while (System.currentTimeMillis() < deadline) {
            PaymentResponse cached = readAndValidateCachedResponse(idempotencyKey, requestHash, hashKey, responseKey);
            if (cached != null) {
                return cached;
            }
            sleep(properties.idempotency().pollIntervalMs());
        }
        throw new ConcurrentRequestInProgressException(idempotencyKey);
    }

    private void releaseLock(String lockKey, String expectedToken) {
        String currentToken = redisTemplate.opsForValue().get(lockKey);
        if (expectedToken.equals(currentToken)) {
            redisTemplate.delete(lockKey);
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for idempotent response", exception);
        }
    }
}
