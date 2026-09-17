package in.abdulmajid.moneylog.payment.controller;

import in.abdulmajid.moneylog.common.CurrentUserHelper;
import in.abdulmajid.moneylog.payment.dto.request.BillPaymentRequest;
import in.abdulmajid.moneylog.payment.dto.response.BillPaymentResponse;
import in.abdulmajid.moneylog.payment.service.BillPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/bill-payments")
@RequiredArgsConstructor
public class BillPaymentController {

    private final BillPaymentService billPaymentService;
    private final CurrentUserHelper currentUserHelper;

    @GetMapping
    public ResponseEntity<Page<BillPaymentResponse>> getBillPayments(
            @RequestParam(required = false) UUID creditCardId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(billPaymentService.getBillPayments(userId, creditCardId, month, year, PageRequest.of(page, size)));
    }

    @PostMapping
    public ResponseEntity<BillPaymentResponse> createBillPayment(@Valid @RequestBody BillPaymentRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(billPaymentService.createBillPayment(userId, request));
    }

    @PutMapping("/{billPaymentId}")
    public ResponseEntity<BillPaymentResponse> updateBillPayment(
            @PathVariable UUID billPaymentId,
            @Valid @RequestBody BillPaymentRequest request) {
        UUID userId = currentUserHelper.getCurrentUserId();
        return ResponseEntity.ok(billPaymentService.updateBillPayment(userId, billPaymentId, request));
    }

    @DeleteMapping("/{billPaymentId}")
    public ResponseEntity<Void> deleteBillPayment(@PathVariable UUID billPaymentId) {
        UUID userId = currentUserHelper.getCurrentUserId();
        billPaymentService.deleteBillPayment(userId, billPaymentId);
        return ResponseEntity.noContent().build();
    }
}