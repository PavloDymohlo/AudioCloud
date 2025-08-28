package ua.dymohlo.music_content_service.dto.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAccessInfo {
    private UUID userId;
    private String userSubscription;
    private List<String> allowedSubscriptionTypes;
}
