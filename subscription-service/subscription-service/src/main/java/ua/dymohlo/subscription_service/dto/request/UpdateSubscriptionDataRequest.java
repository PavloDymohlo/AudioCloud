package ua.dymohlo.subscription_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.dymohlo.subscription_service.models.SubscriptionStatus;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSubscriptionDataRequest {
    private String subscriptionCurrentName;
    private String subscriptionNewName;
    private BigDecimal subscriptionPrice;
    private Integer subscriptionDurationTime;
    private SubscriptionStatus subscriptionStatus;
}
