package in.abdulmajid.moneylog.payment.service;

import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.auth.repository.UserRepository;
import in.abdulmajid.moneylog.payment.dto.request.PaymentAccountRequest;
import in.abdulmajid.moneylog.payment.dto.request.PaymentAppRequest;
import in.abdulmajid.moneylog.payment.dto.response.PaymentAccountResponse;
import in.abdulmajid.moneylog.payment.dto.response.PaymentAppResponse;
import in.abdulmajid.moneylog.payment.model.PaymentAccount;
import in.abdulmajid.moneylog.payment.model.PaymentApp;
import in.abdulmajid.moneylog.payment.repository.PaymentAccountRepository;
import in.abdulmajid.moneylog.payment.repository.PaymentAppRepository;
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
    private final PaymentAccountRepository paymentAccountRepository;
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

    public List<PaymentAccountResponse> getAllPaymentAccounts(UUID userId) {
        return paymentAccountRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(this::toPaymentAccountResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentAccountResponse> getActivePaymentAccounts(UUID userId) {
        return paymentAccountRepository.findByUserIdAndIsActiveTrueOrderByNameAsc(userId).stream()
                .map(this::toPaymentAccountResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public PaymentAccountResponse createPaymentAccount(UUID userId, PaymentAccountRequest request) {
        User user = userRepository.findById(userId).orElseThrow();

        if (paymentAccountRepository.existsByUserIdAndName(userId, request.getName())) {
            throw new RuntimeException("Payment account already exists");
        }

        PaymentAccount paymentAccount = PaymentAccount.builder()
                .user(user)
                .name(request.getName())
                .type(request.getType())
                .bankName(request.getBankName())
                .lastFourDigits(request.getLastFourDigits())
                .isActive(true)
                .build();

        return toPaymentAccountResponse(paymentAccountRepository.save(paymentAccount));
    }

    @Transactional
    public PaymentAccountResponse updatePaymentAccount(UUID userId, UUID accountId, PaymentAccountRequest request) {
        PaymentAccount paymentAccount = paymentAccountRepository.findById(accountId)
                .filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Payment account not found"));

        paymentAccount.setName(request.getName());
        paymentAccount.setType(request.getType());
        paymentAccount.setBankName(request.getBankName());
        paymentAccount.setLastFourDigits(request.getLastFourDigits());

        return toPaymentAccountResponse(paymentAccountRepository.save(paymentAccount));
    }

    @Transactional
    public void deletePaymentAccount(UUID userId, UUID accountId) {
        PaymentAccount paymentAccount = paymentAccountRepository.findById(accountId)
                .filter(a -> a.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Payment account not found"));
        paymentAccountRepository.delete(paymentAccount);
    }

    private PaymentAppResponse toPaymentAppResponse(PaymentApp paymentApp) {
        return PaymentAppResponse.builder()
                .id(paymentApp.getId())
                .name(paymentApp.getName())
                .type(paymentApp.getType())
                .build();
    }

    private PaymentAccountResponse toPaymentAccountResponse(PaymentAccount paymentAccount) {
        return PaymentAccountResponse.builder()
                .id(paymentAccount.getId())
                .name(paymentAccount.getName())
                .type(paymentAccount.getType())
                .bankName(paymentAccount.getBankName())
                .lastFourDigits(paymentAccount.getLastFourDigits())
                .isActive(paymentAccount.getIsActive())
                .build();
    }
}
