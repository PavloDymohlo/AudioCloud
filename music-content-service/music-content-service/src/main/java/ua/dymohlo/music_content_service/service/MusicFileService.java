package ua.dymohlo.music_content_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ua.dymohlo.music_content_service.dto.request.NewMusicFileRequest;
import ua.dymohlo.music_content_service.dto.request.UpdateMusicFileDataRequest;
import ua.dymohlo.music_content_service.entity.MusicFile;
import ua.dymohlo.music_content_service.excepton.MusicFileAlreadyExistsException;
import ua.dymohlo.music_content_service.excepton.MusicFileNotFoundException;
import ua.dymohlo.music_content_service.repository.MusicFileRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class MusicFileService {
    private final MusicFileRepository musicFileRepository;

    public MusicFile addNewMusicFile(NewMusicFileRequest request) {
        musicFileRepository.findByMusicFileNameIgnoreCase(request.getMusicFileName())
                .ifPresent(f -> {
                    throw new MusicFileAlreadyExistsException("File with this name already exists");
                });

        return musicFileRepository.save(
                MusicFile.builder()
                        .musicFileName(request.getMusicFileName())
                        .musicFilePath(request.getMusicFilePath())
                        .subscriptionType(request.getSubscriptionType())
                        .build()
        );
    }

    public MusicFile findMusicFileByName(String fileName) {
        return musicFileRepository.findByMusicFileNameIgnoreCase(fileName)
                .orElseThrow(() -> new MusicFileNotFoundException("File with name not found"));
    }

    public Page<MusicFile> findAllMusicFiles(Pageable pageable) {
        return musicFileRepository.findAll(pageable);
    }

    public Page<MusicFile> findMusicFilesBySubscription(String subscriptionName, Pageable pageable) {
        return musicFileRepository.findMusicFileBySubscriptionTypeIgnoreCase(subscriptionName, pageable)
                .orElse(Page.empty());
    }

    public MusicFile updateMusicFileData(UpdateMusicFileDataRequest request) {
        return musicFileRepository.findByMusicFileNameIgnoreCase(request.getMusicFileCurrentName())
                .map(existingMusicFile -> {
                    existingMusicFile.setMusicFileName(request.getMusicFileNewName());
                    existingMusicFile.setSubscriptionType(request.getSubscriptionType());
                    return existingMusicFile;
                })
                .map(musicFileRepository::save)
                .orElseThrow(() -> new MusicFileNotFoundException("File with name not found"));
    }

    public void deleteMusicFileByName(String musicFileName) {
        musicFileRepository.delete(musicFileRepository.findByMusicFileNameIgnoreCase(musicFileName)
                .orElseThrow(() -> new MusicFileNotFoundException("File with name not found")));

    }

}
