package in.abdulmajid.moneylog.payment.dto.response;

import in.abdulmajid.moneylog.payment.model.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
public class BillPaymentResponse {
    private UUID id;
    private CreditCardResponse creditCard;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private LocalTime paymentTime;
    private String paymentChannel;
    private PaymentAppResponse paymentApp;
    private PaymentMethod paymentMethod;
    private PaymentSourceResponse paidFromSource;
    private String note;
}