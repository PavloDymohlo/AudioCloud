package ua.dymohlo.user_service.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubscriptionConstants {
    public static final String DEFAULT_SUBSCRIPTION = "FREE";
    public static final String TRIAL_SUBSCRIPTION = "TRIAL";
    public static final String AFTER_TRIAL_SUBSCRIPTION = "MAXIMUM";
}
