package ua.dymohlo.music_content_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewMusicFileRequest {
    private String musicFileName;
    private String musicFilePath;
    private String subscriptionType;
}
