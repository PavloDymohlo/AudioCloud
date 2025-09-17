package ua.dymohlo.music_content_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusicFileDataResponse {
    private String musicFileName;
    private String subscriptionType;
    private String streamUrl;
    private String downloadUrl;
    private Long fileSize;
    private String duration;
}