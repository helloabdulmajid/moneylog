package in.abdulmajid.moneylog.payment.service;

import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.auth.repository.UserRepository;
import in.abdulmajid.moneylog.payment.dto.request.BillPaymentRequest;
import in.abdulmajid.moneylog.payment.dto.response.BillPaymentResponse;
import in.abdulmajid.moneylog.payment.dto.response.CreditCardResponse;
import in.abdulmajid.moneylog.payment.dto.response.PaymentAppResponse;
import in.abdulmajid.moneylog.payment.dto.response.PaymentSourceResponse;
import in.abdulmajid.moneylog.payment.model.BillPayment;
import in.abdulmajid.moneylog.payment.model.CreditCard;
import in.abdulmajid.moneylog.payment.model.PaymentApp;
import in.abdulmajid.moneylog.payment.model.PaymentSource;
import in.abdulmajid.moneylog.payment.repository.BillPaymentRepository;
import in.abdulmajid.moneylog.payment.repository.CreditCardRepository;
import in.abdulmajid.moneylog.payment.repository.PaymentAppRepository;
import in.abdulmajid.moneylog.payment.repository.PaymentSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillPaymentService {

    private final BillPaymentRepository billPaymentRepository;
    private final CreditCardRepository creditCardRepository;
    private final PaymentAppRepository paymentAppRepository;
    private final PaymentSourceRepository paymentSourceRepository;
    private final UserRepository userRepository;

    public Page<BillPaymentResponse> getBillPayments(UUID userId, UUID creditCardId, Integer month, Integer year, Pageable pageable) {
        Page<BillPayment> page;
        if (creditCardId != null && month != null && year != null) {
            LocalDate start = LocalDate.of(year, month, 1);
            page = billPaymentRepository.findByUserIdAndCreditCardIdAndPaymentDateBetweenOrderByPaymentDateDescPaymentTimeDesc(
                    userId, creditCardId, start, start.withDayOfMonth(start.lengthOfMonth()), pageable);
        } else if (creditCardId != null) {
            page = billPaymentRepository.findByUserIdAndCreditCardIdOrderByPaymentDateDescPaymentTimeDesc(userId, creditCardId, pageable);
        } else if (month != null && year != null) {
            LocalDate start = LocalDate.of(year, month, 1);
            page = billPaymentRepository.findByUserIdAndPaymentDateBetweenOrderByPaymentDateDescPaymentTimeDesc(
                    userId, start, start.withDayOfMonth(start.lengthOfMonth()), pageable);
        } else {
            page = billPaymentRepository.findByUserIdOrderByPaymentDateDescPaymentTimeDesc(userId, pageable);
        }
        return page.map(this::toBillPaymentResponse);
    }

    @Transactional
    public BillPaymentResponse createBillPayment(UUID userId, BillPaymentRequest request) {
        User user = userRepository.findById(userId).orElseThrow();

        CreditCard creditCard = creditCardRepository.findById(request.getCreditCardId())
                .filter(c -> c.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Credit card not found"));

        BillPayment billPayment = BillPayment.builder()
                .user(user)
                .creditCard(creditCard)
                .amount(request.getAmount())
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now())
                .paymentTime(request.getPaymentTime())
                .paymentChannel(request.getPaymentChannel())
                .paymentMethod(request.getPaymentMethod())
                .note(request.getNote())
                .build();

        if (request.getPaymentAppId() != null) {
            PaymentApp app = paymentAppRepository.findById(request.getPaymentAppId())
                    .filter(a -> a.getUser().getId().equals(userId))
                    .orElseThrow(() -> new RuntimeException("Payment app not found"));
            billPayment.setPaymentApp(app);
        }

        if (request.getPaidFromSourceId() != null) {
            PaymentSource source = paymentSourceRepository.findById(request.getPaidFromSourceId())
                    .filter(s -> s.getUser().getId().equals(userId))
                    .orElseThrow(() -> new RuntimeException("Payment source not found"));
            billPayment.setPaidFromSource(source);
        }

        return toBillPaymentResponse(billPaymentRepository.save(billPayment));
    }

    @Transactional
    public BillPaymentResponse updateBillPayment(UUID userId, UUID billPaymentId, BillPaymentRequest request) {
        BillPayment billPayment = findOwned(userId, billPaymentId);

        billPayment.setAmount(request.getAmount());
        billPayment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : billPayment.getPaymentDate());
        billPayment.setPaymentTime(request.getPaymentTime());
        billPayment.setPaymentChannel(request.getPaymentChannel());
        billPayment.setPaymentMethod(request.getPaymentMethod());
        billPayment.setNote(request.getNote());

        if (request.getCreditCardId() != null) {
            CreditCard creditCard = creditCardRepository.findById(request.getCreditCardId())
                    .filter(c -> c.getUser().getId().equals(userId))
                    .orElseThrow(() -> new RuntimeException("Credit card not found"));
            billPayment.setCreditCard(creditCard);
        }

        if (request.getPaymentAppId() != null) {
            PaymentApp app = paymentAppRepository.findById(request.getPaymentAppId())
                    .filter(a -> a.getUser().getId().equals(userId))
                    .orElseThrow(() -> new RuntimeException("Payment app not found"));
            billPayment.setPaymentApp(app);
        } else {
            billPayment.setPaymentApp(null);
        }

        if (request.getPaidFromSourceId() != null) {
            PaymentSource source = paymentSourceRepository.findById(request.getPaidFromSourceId())
                    .filter(s -> s.getUser().getId().equals(userId))
                    .orElseThrow(() -> new RuntimeException("Payment source not found"));
            billPayment.setPaidFromSource(source);
        } else {
            billPayment.setPaidFromSource(null);
        }

        return toBillPaymentResponse(billPaymentRepository.save(billPayment));
    }

    @Transactional
    public void deleteBillPayment(UUID userId, UUID billPaymentId) {
        BillPayment billPayment = findOwned(userId, billPaymentId);
        billPaymentRepository.delete(billPayment);
    }

    private BillPayment findOwned(UUID userId, UUID billPaymentId) {
        return billPaymentRepository.findById(billPaymentId)
                .filter(b -> b.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Bill payment not found"));
    }

    private BillPaymentResponse toBillPaymentResponse(BillPayment billPayment) {
        return BillPaymentResponse.builder()
                .id(billPayment.getId())
                .creditCard(CreditCardResponse.builder()
                        .id(billPayment.getCreditCard().getId())
                        .name(billPayment.getCreditCard().getName())
                        .issuer(billPayment.getCreditCard().getIssuer())
                        .lastFourDigits(billPayment.getCreditCard().getLastFourDigits())
                        .network(billPayment.getCreditCard().getNetwork())
                        .creditLimit(billPayment.getCreditCard().getCreditLimit())
                        .isActive(billPayment.getCreditCard().getIsActive())
                        .build())
                .amount(billPayment.getAmount())
                .paymentDate(billPayment.getPaymentDate())
                .paymentTime(billPayment.getPaymentTime() != null ? billPayment.getPaymentTime() : LocalTime.of(0, 0))
                .paymentChannel(billPayment.getPaymentChannel())
                .paymentApp(billPayment.getPaymentApp() != null ? PaymentAppResponse.builder()
                        .id(billPayment.getPaymentApp().getId())
                        .name(billPayment.getPaymentApp().getName())
                        .type(billPayment.getPaymentApp().getType())
                        .build() : null)
                .paymentMethod(billPayment.getPaymentMethod())
                .paidFromSource(billPayment.getPaidFromSource() != null ? PaymentSourceResponse.builder()
                        .id(billPayment.getPaidFromSource().getId())
                        .name(billPayment.getPaidFromSource().getName())
                        .type(billPayment.getPaidFromSource().getType())
                        .bankName(billPayment.getPaidFromSource().getBankName())
                        .lastFourDigits(billPayment.getPaidFromSource().getLastFourDigits())
                        .isActive(billPayment.getPaidFromSource().getIsActive())
                        .build() : null)
                .note(billPayment.getNote())
                .build();
    }
}