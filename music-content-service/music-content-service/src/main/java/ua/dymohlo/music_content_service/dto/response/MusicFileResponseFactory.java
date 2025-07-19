package ua.dymohlo.music_content_service.dto.response;

import org.springframework.stereotype.Component;
import ua.dymohlo.music_content_service.entity.MusicFile;

import java.util.List;

@Component
public class MusicFileResponseFactory {

    public MusicFileDataResponse createMusicFileDataResponse(MusicFile musicFile) {
        return MusicFileDataResponse.builder()
                .musicFileName(musicFile.getMusicFileName())
                .subscriptionType(musicFile.getSubscriptionType()).build();
    }

    public List<MusicFileDataResponse> createMusicFileDataResponseList(List<MusicFile> musicFiles) {
        return musicFiles.stream()
                .map(musicFile -> new MusicFileDataResponse(
                        musicFile.getMusicFileName(),
                        musicFile.getSubscriptionType()
                ))
                .toList();
    }
}
