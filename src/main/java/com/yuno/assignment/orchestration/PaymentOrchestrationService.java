package com.yuno.assignment.orchestration;

import com.yuno.assignment.dto.PaymentRequest;
import com.yuno.assignment.dto.PaymentResponse;
import com.yuno.assignment.entity.PaymentEntity;
import com.yuno.assignment.enums.PaymentProviderType;
import com.yuno.assignment.enums.PaymentStatus;
import com.yuno.assignment.exception.PaymentNotFoundException;
import com.yuno.assignment.exception.PaymentProcessingException;
import com.yuno.assignment.idempotency.IdempotencyService;
import com.yuno.assignment.mapper.PaymentMapper;
import com.yuno.assignment.metrics.PaymentMetricsRecorder;
import com.yuno.assignment.provider.dto.ProviderPaymentResponse;
import com.yuno.assignment.repository.PaymentRepository;
import com.yuno.assignment.routing.PaymentRoute;
import com.yuno.assignment.routing.RoutingStrategy;
import com.yuno.assignment.service.PaymentService;
import com.yuno.assignment.util.PaymentReferenceGenerator;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOrchestrationService implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final RoutingStrategy routingStrategy;
    private final ProviderExecutor providerExecutor;
    private final IdempotencyService idempotencyService;
    private final PaymentReferenceGenerator paymentReferenceGenerator;
    private final PaymentMetricsRecorder metricsRecorder;

    @Override
    public PaymentResponse createPayment(String idempotencyKey, PaymentRequest request) {
        Timer.Sample sample = metricsRecorder.startSample();
        try {
            PaymentResponse response = idempotencyService.execute(idempotencyKey, request, () -> doCreatePayment(request));
            metricsRecorder.recordApiLatency(sample, "create_payment", "success");
            return response;
        } catch (RuntimeException exception) {
            metricsRecorder.recordApiLatency(sample, "create_payment", "failure");
            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(String paymentReference) {
        Timer.Sample sample = metricsRecorder.startSample();
        try {
            PaymentResponse response = paymentMapper.toResponse(paymentRepository.findByPaymentReference(paymentReference)
                    .orElseThrow(() -> new PaymentNotFoundException(paymentReference)));
            metricsRecorder.recordApiLatency(sample, "get_payment", "success");
            return response;
        } catch (RuntimeException exception) {
            metricsRecorder.recordApiLatency(sample, "get_payment", "failure");
            throw exception;
        }
    }

    @Transactional
    protected PaymentResponse doCreatePayment(PaymentRequest request) {
        PaymentEntity payment = paymentRepository.save(PaymentEntity.builder()
                .paymentReference(paymentReferenceGenerator.nextReference())
                .amount(request.amount())
                .currency(request.currency())
                .paymentMethod(request.paymentMethod())
                .status(PaymentStatus.CREATED)
                .retryCount(0)
                .build());

        PaymentRoute route = routingStrategy.route(request.paymentMethod());
        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);

        PaymentProcessingException lastException = null;
        for (PaymentProviderType candidate : route.candidates()) {
            try {
                if (candidate != route.primaryProvider()) {
                    payment.setStatus(PaymentStatus.RETRYING);
                    paymentRepository.save(payment);
                }

                ProviderPaymentResponse providerResponse = providerExecutor.execute(payment, candidate);
                payment.setProvider(providerResponse.provider());
                payment.setStatus(providerResponse.status());
                payment.setFailureReason(null);
                paymentRepository.save(payment);
                metricsRecorder.incrementPaymentOutcome("success", payment.getProvider());
                return paymentMapper.toResponse(payment);
            } catch (RuntimeException exception) {
                lastException = new PaymentProcessingException(
                        "Provider " + candidate + " failed for payment " + payment.getPaymentReference(),
                        exception);
                payment.setProvider(candidate);
                payment.setFailureReason(exception.getMessage());
                paymentRepository.save(payment);
                metricsRecorder.incrementRetry(candidate.name(), exception.getClass().getSimpleName());
                log.warn("Provider attempt failed. paymentReference={}, provider={}, reason={}",
                        payment.getPaymentReference(), candidate, exception.getMessage());
            }
        }

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);
        metricsRecorder.incrementPaymentOutcome("failure", payment.getProvider());
        throw lastException == null
                ? new PaymentProcessingException("Payment processing failed", null)
                : lastException;
    }
}
