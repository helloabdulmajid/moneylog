package in.abdulmajid.moneylog.payment.controller;

import in.abdulmajid.moneylog.common.CurrentUserHelper;
import in.abdulmajid.moneylog.payment.dto.request.PaymentAccountRequest;
import in.abdulmajid.moneylog.payment.dto.request.PaymentAppRequest;
import in.abdulmajid.moneylog.payment.dto.response.PaymentAccountResponse;
import in.abdulmajid.moneylog.payment.dto.response.PaymentAppResponse;
import in.abdulmajid.moneylog.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final CurrentUserHelper currentUserHelper;

    @GetMapping("/apps")
    public ResponseEntity<List<PaymentAppResponse>> getAllPaymentApps() {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(paymentService.getAllPaymentApps(userId));
    }

    @PostMapping("/apps")
    public ResponseEntity<PaymentAppResponse> createPaymentApp(
            @Valid @RequestBody PaymentAppRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(paymentService.createPaymentApp(userId, request));
    }

    @PutMapping("/apps/{appId}")
    public ResponseEntity<PaymentAppResponse> updatePaymentApp(
            @PathVariable UUID appId,
            @Valid @RequestBody PaymentAppRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(paymentService.updatePaymentApp(userId, appId, request));
    }

    @DeleteMapping("/apps/{appId}")
    public ResponseEntity<Void> deletePaymentApp(@PathVariable UUID appId) {
        UUID userId = currentUserHelper.getCurrentUserId();
        paymentService.deletePaymentApp(userId, appId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/accounts")
    public ResponseEntity<List<PaymentAccountResponse>> getAllPaymentAccounts() {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(paymentService.getAllPaymentAccounts(userId));
    }

    @GetMapping("/accounts/active")
    public ResponseEntity<List<PaymentAccountResponse>> getActivePaymentAccounts() {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(paymentService.getActivePaymentAccounts(userId));
    }

    @PostMapping("/accounts")
    public ResponseEntity<PaymentAccountResponse> createPaymentAccount(
            @Valid @RequestBody PaymentAccountRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(paymentService.createPaymentAccount(userId, request));
    }

    @PutMapping("/accounts/{accountId}")
    public ResponseEntity<PaymentAccountResponse> updatePaymentAccount(
            @PathVariable UUID accountId,
            @Valid @RequestBody PaymentAccountRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(paymentService.updatePaymentAccount(userId, accountId, request));
    }

    @DeleteMapping("/accounts/{accountId}")
    public ResponseEntity<Void> deletePaymentAccount(@PathVariable UUID accountId) {
        UUID userId = currentUserHelper.getCurrentUserId();
        paymentService.deletePaymentAccount(userId, accountId);
        return ResponseEntity.noContent().build();
    }
}
