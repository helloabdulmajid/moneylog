package in.abdulmajid.moneylog.payment.service;

import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.auth.repository.UserRepository;
import in.abdulmajid.moneylog.expense.dto.response.ExpenseResponse;
import in.abdulmajid.moneylog.expense.model.Expense;
import in.abdulmajid.moneylog.expense.repository.ExpenseRepository;
import in.abdulmajid.moneylog.expense.service.ExpenseResponseMapper;
import in.abdulmajid.moneylog.payment.dto.request.CreditCardRequest;
import in.abdulmajid.moneylog.payment.dto.response.CreditCardResponse;
import in.abdulmajid.moneylog.payment.model.CreditCard;
import in.abdulmajid.moneylog.payment.model.PaymentSource;
import in.abdulmajid.moneylog.payment.repository.CreditCardRepository;
import in.abdulmajid.moneylog.payment.repository.PaymentSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final PaymentSourceRepository paymentSourceRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;

    public List<CreditCardResponse> getAllCreditCards(UUID userId) {
        return creditCardRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toCreditCardResponse)
                .collect(Collectors.toList());
    }

    public List<CreditCardResponse> getActiveCreditCards(UUID userId) {
        return creditCardRepository.findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId).stream()
                .map(this::toCreditCardResponse)
                .collect(Collectors.toList());
    }

    public CreditCardResponse getCreditCard(UUID userId, UUID cardId) {
        CreditCard card = creditCardRepository.findById(cardId)
                .filter(c -> c.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Credit card not found"));
        return toCreditCardResponse(card);
    }

    @Transactional
    public CreditCardResponse createCreditCard(UUID userId, CreditCardRequest request) {
        User user = userRepository.findById(userId).orElseThrow();

        if (creditCardRepository.existsByUserIdAndIssuerAndLastFourDigits(userId, request.getIssuer(), request.getLastFourDigits())) {
            throw new RuntimeException("Credit card already exists");
        }

        CreditCard card = CreditCard.builder()
                .user(user)
                .name(request.getName())
                .issuer(request.getIssuer())
                .lastFourDigits(request.getLastFourDigits())
                .network(request.getNetwork())
                .creditLimit(request.getCreditLimit())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        PaymentSource source = paymentSourceRepository
                .findCardSource(userId, PaymentSource.PaymentSourceType.CREDIT_CARD,
                        request.getIssuer(), request.getLastFourDigits())
                .orElseGet(() -> paymentSourceRepository.save(PaymentSource.builder()
                        .user(user)
                        .name(card.getName())
                        .type(PaymentSource.PaymentSourceType.CREDIT_CARD)
                        .bankName(request.getIssuer())
                        .lastFourDigits(request.getLastFourDigits())
                        .isActive(card.getIsActive())
                        .build()));

        source.setIsActive(card.getIsActive());
        card.setPaymentSource(paymentSourceRepository.save(source));

        return toCreditCardResponse(creditCardRepository.save(card));
    }

    @Transactional
    public CreditCardResponse updateCreditCard(UUID userId, UUID cardId, CreditCardRequest request) {
        CreditCard card = creditCardRepository.findById(cardId)
                .filter(c -> c.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Credit card not found"));

        card.setName(request.getName());
        if (request.getIssuer() != null) card.setIssuer(request.getIssuer());
        if (request.getLastFourDigits() != null) card.setLastFourDigits(request.getLastFourDigits());
        if (request.getNetwork() != null) card.setNetwork(request.getNetwork());
        if (request.getCreditLimit() != null) card.setCreditLimit(request.getCreditLimit());
        if (request.getIsActive() != null) {
            card.setIsActive(request.getIsActive());
            if (card.getPaymentSource() != null) {
                card.getPaymentSource().setIsActive(request.getIsActive());
                paymentSourceRepository.save(card.getPaymentSource());
            }
        }

        return toCreditCardResponse(creditCardRepository.save(card));
    }

    @Transactional
    public void deleteCreditCard(UUID userId, UUID cardId) {
        CreditCard card = creditCardRepository.findById(cardId)
                .filter(c -> c.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Credit card not found"));
        if (card.getPaymentSource() != null) {
            card.getPaymentSource().setIsActive(false);
            paymentSourceRepository.save(card.getPaymentSource());
        }
        creditCardRepository.delete(card);
    }

    public List<ExpenseResponse> getCardExpenses(UUID userId, UUID cardId, Pageable pageable) {
        CreditCard card = creditCardRepository.findById(cardId)
                .filter(c -> c.getUser().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("Credit card not found"));
        if (card.getPaymentSource() == null) return List.of();
        UUID sourceId = card.getPaymentSource().getId();
        return expenseRepository
                .findByUserIdAndPaymentSource_IdOrderByExpenseDateDescExpenseTimeDesc(userId, sourceId, pageable)
                .stream()
                .map(ExpenseResponseMapper::map)
                .collect(Collectors.toList());
    }

    private CreditCardResponse toCreditCardResponse(CreditCard card) {
        return CreditCardResponse.builder()
                .id(card.getId())
                .name(card.getName())
                .issuer(card.getIssuer())
                .lastFourDigits(card.getLastFourDigits())
                .network(card.getNetwork())
                .creditLimit(card.getCreditLimit())
                .isActive(card.getIsActive())
                .paymentSourceId(card.getPaymentSource() != null ? card.getPaymentSource().getId() : null)
                .build();
    }
}