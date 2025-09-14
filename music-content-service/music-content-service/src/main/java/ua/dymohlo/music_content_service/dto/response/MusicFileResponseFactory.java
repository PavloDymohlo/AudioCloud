package ua.dymohlo.music_content_service.dto.response;

import org.springframework.stereotype.Component;
import ua.dymohlo.music_content_service.entity.MusicFile;

import java.io.File;

@Component
public class MusicFileResponseFactory {

    public MusicFileDataResponse createMusicFileDataResponse(MusicFile musicFile) {
        return MusicFileDataResponse.builder()
                .musicFileName(musicFile.getMusicFileName())
                .subscriptionType(musicFile.getSubscriptionType())
                .streamUrl("/api/v1/stream/" + musicFile.getMusicFileName())
                .downloadUrl("/api/v1/stream/download/" + musicFile.getMusicFileName())
                .fileSize(getFileSize(musicFile.getMusicFilePath()))
                .build();
    }

    private Long getFileSize(String filePath) {
        try {
            File file = new File(filePath);
            return file.exists() ? file.length() : null;
        } catch (Exception e) {
            return null;
        }
    }
}