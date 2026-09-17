package in.abdulmajid.moneylog.payment.dto.request;

import in.abdulmajid.moneylog.payment.model.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
public class BillPaymentRequest {

    @NotNull(message = "Credit card is required")
    private UUID creditCardId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    private LocalTime paymentTime;

    private String paymentChannel;

    private UUID paymentAppId;

    private PaymentMethod paymentMethod;

    private UUID paidFromSourceId;

    private String note;
}