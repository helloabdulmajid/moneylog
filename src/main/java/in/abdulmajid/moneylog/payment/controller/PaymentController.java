package in.abdulmajid.moneylog.payment.controller;

import in.abdulmajid.moneylog.common.CurrentUserHelper;
import in.abdulmajid.moneylog.payment.dto.request.PaymentAppRequest;
import in.abdulmajid.moneylog.payment.dto.request.PaymentSourceRequest;
import in.abdulmajid.moneylog.payment.dto.response.PaymentAppResponse;
import in.abdulmajid.moneylog.payment.dto.response.PaymentSourceResponse;
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

    @GetMapping("/sources")
    public ResponseEntity<List<PaymentSourceResponse>> getAllPaymentSources() {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(paymentService.getAllPaymentSources(userId));
    }

    @GetMapping("/sources/active")
    public ResponseEntity<List<PaymentSourceResponse>> getActivePaymentSources() {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(paymentService.getActivePaymentSources(userId));
    }

    @PostMapping("/sources")
    public ResponseEntity<PaymentSourceResponse> createPaymentSource(
            @Valid @RequestBody PaymentSourceRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(paymentService.createPaymentSource(userId, request));
    }

    @PutMapping("/sources/{sourceId}")
    public ResponseEntity<PaymentSourceResponse> updatePaymentSource(
            @PathVariable UUID sourceId,
            @Valid @RequestBody PaymentSourceRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(paymentService.updatePaymentSource(userId, sourceId, request));
    }

    @DeleteMapping("/sources/{sourceId}")
    public ResponseEntity<Void> deletePaymentSource(@PathVariable UUID sourceId) {
        UUID userId = currentUserHelper.getCurrentUserId();
        paymentService.deletePaymentSource(userId, sourceId);
        return ResponseEntity.noContent().build();
    }
}