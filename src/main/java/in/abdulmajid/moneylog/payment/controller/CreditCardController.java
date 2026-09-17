package in.abdulmajid.moneylog.payment.controller;

import in.abdulmajid.moneylog.common.CurrentUserHelper;
import in.abdulmajid.moneylog.expense.dto.response.ExpenseResponse;
import in.abdulmajid.moneylog.payment.dto.request.CreditCardRequest;
import in.abdulmajid.moneylog.payment.dto.response.BillPaymentResponse;
import in.abdulmajid.moneylog.payment.dto.response.CreditCardResponse;
import in.abdulmajid.moneylog.payment.service.BillPaymentService;
import in.abdulmajid.moneylog.payment.service.CreditCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/credit-cards")
@RequiredArgsConstructor
public class CreditCardController {

    private final CreditCardService creditCardService;
    private final BillPaymentService billPaymentService;
    private final CurrentUserHelper currentUserHelper;

    @GetMapping
    public ResponseEntity<List<CreditCardResponse>> getAllCreditCards() {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(creditCardService.getAllCreditCards(userId));
    }

    @GetMapping("/active")
    public ResponseEntity<List<CreditCardResponse>> getActiveCreditCards() {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(creditCardService.getActiveCreditCards(userId));
    }

    @PostMapping
    public ResponseEntity<CreditCardResponse> createCreditCard(@Valid @RequestBody CreditCardRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(creditCardService.createCreditCard(userId, request));
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CreditCardResponse> getCreditCard(@PathVariable UUID cardId) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(creditCardService.getCreditCard(userId, cardId));
    }

    @PutMapping("/{cardId}")
    public ResponseEntity<CreditCardResponse> updateCreditCard(
            @PathVariable UUID cardId,
            @Valid @RequestBody CreditCardRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(creditCardService.updateCreditCard(userId, cardId, request));
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCreditCard(@PathVariable UUID cardId) {
        UUID userId = currentUserHelper.getCurrentUserId();
        creditCardService.deleteCreditCard(userId, cardId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{cardId}/expenses")
    public ResponseEntity<List<ExpenseResponse>> getCardExpenses(
            @PathVariable UUID cardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(creditCardService.getCardExpenses(userId, cardId, PageRequest.of(page, size)));
    }

    @GetMapping("/{cardId}/bill-payments")
    public ResponseEntity<Page<BillPaymentResponse>> getCardBillPayments(
            @PathVariable UUID cardId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(billPaymentService.getBillPayments(userId, cardId, null, null, PageRequest.of(page, size)));
    }
}