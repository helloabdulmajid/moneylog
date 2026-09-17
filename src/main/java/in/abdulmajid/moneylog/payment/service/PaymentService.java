package in.abdulmajid.moneylog.payment.service;

import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.auth.repository.UserRepository;
import in.abdulmajid.moneylog.payment.dto.request.PaymentAppRequest;
import in.abdulmajid.moneylog.payment.dto.request.PaymentSourceRequest;
import in.abdulmajid.moneylog.payment.dto.response.PaymentAppResponse;
import in.abdulmajid.moneylog.payment.dto.response.PaymentSourceResponse;
import in.abdulmajid.moneylog.payment.model.PaymentApp;
import in.abdulmajid.moneylog.payment.model.PaymentSource;
import in.abdulmajid.moneylog.payment.repository.PaymentAppRepository;
import in.abdulmajid.moneylog.payment.repository.PaymentSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentAppRepository paymentAppRepository;
    private final PaymentSourceRepository paymentSourceRepository;
    private final UserRepository userRepository;

    public List<PaymentAppResponse> getAllPaymentApps(UUID userId) {
        return paymentAppRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(this::toPaymentAppResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentAppResponse createPaymentApp(UUID userId, PaymentAppRequest request) {
        User user = userRepository.findById(userId).orElseThrow();

        if (paymentAppRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new RuntimeException("Payment app already exists");
        }

        PaymentApp paymentApp = PaymentApp.builder()
                .user(user)
                .name(request.getName())
                .type(request.getType())
                .build();

        return toPaymentAppResponse(paymentAppRepository.save(paymentApp));
    }

    @Transactional
    public PaymentAppResponse updatePaymentApp(UUID userId, UUID appId, PaymentAppRequest request) {
        PaymentApp paymentApp = paymentAppRepository.findById(appId)
                .filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Payment app not found"));

        paymentApp.setName(request.getName());
        paymentApp.setType(request.getType());

        return toPaymentAppResponse(paymentAppRepository.save(paymentApp));
    }

    @Transactional
    public void deletePaymentApp(UUID userId, UUID appId) {
        PaymentApp paymentApp = paymentAppRepository.findById(appId)
                .filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Payment app not found"));
        paymentAppRepository.delete(paymentApp);
    }

    public List<PaymentSourceResponse> getAllPaymentSources(UUID userId) {
        return paymentSourceRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(this::toPaymentSourceResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentSourceResponse> getActivePaymentSources(UUID userId) {
        return paymentSourceRepository.findByUserIdAndIsActiveTrueOrderByNameAsc(userId).stream()
                .map(this::toPaymentSourceResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentSourceResponse createPaymentSource(UUID userId, PaymentSourceRequest request) {
        User user = userRepository.findById(userId).orElseThrow();

        if (paymentSourceRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new RuntimeException("Payment source already exists");
        }

        PaymentSource paymentSource = PaymentSource.builder()
                .user(user)
                .name(request.getName())
                .type(request.getType())
                .bankName(request.getBankName())
                .lastFourDigits(request.getLastFourDigits())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        return toPaymentSourceResponse(paymentSourceRepository.save(paymentSource));
    }

    @Transactional
    public PaymentSourceResponse updatePaymentSource(UUID userId, UUID sourceId, PaymentSourceRequest request) {
        PaymentSource paymentSource = paymentSourceRepository.findById(sourceId)
                .filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Payment source not found"));

        paymentSource.setName(request.getName());
        paymentSource.setType(request.getType());
        paymentSource.setBankName(request.getBankName());
        paymentSource.setLastFourDigits(request.getLastFourDigits());
        if (request.getIsActive() != null) {
            paymentSource.setIsActive(request.getIsActive());
        }

        return toPaymentSourceResponse(paymentSourceRepository.save(paymentSource));
    }

    @Transactional
    public void deletePaymentSource(UUID userId, UUID sourceId) {
        PaymentSource paymentSource = paymentSourceRepository.findById(sourceId)
                .filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Payment source not found"));
        paymentSourceRepository.delete(paymentSource);
    }

    private PaymentAppResponse toPaymentAppResponse(PaymentApp paymentApp) {
        return PaymentAppResponse.builder()
                .id(paymentApp.getId())
                .name(paymentApp.getName())
                .type(paymentApp.getType())
                .build();
    }

    private PaymentSourceResponse toPaymentSourceResponse(PaymentSource paymentSource) {
        return PaymentSourceResponse.builder()
                .id(paymentSource.getId())
                .name(paymentSource.getName())
                .type(paymentSource.getType())
                .bankName(paymentSource.getBankName())
                .lastFourDigits(paymentSource.getLastFourDigits())
                .isActive(paymentSource.getIsActive())
                .build();
    }
}