package ua.dymohlo.music_content_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ua.dymohlo.music_content_service.dto.request.NewMusicFileRequest;
import ua.dymohlo.music_content_service.dto.request.UpdateMusicFileDataRequest;
import ua.dymohlo.music_content_service.dto.security.UserAccessInfo;
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

    public MusicFile findMusicFileByName(String fileName, UserAccessInfo userAccess) {
        MusicFile musicFile = musicFileRepository.findByMusicFileNameIgnoreCase(fileName)
                .orElseThrow(() -> new MusicFileNotFoundException("File with name not found"));

        if (!hasAccessToFile(musicFile, userAccess)) {
            log.warn("User {} with subscription {} tried to access file with subscription type {}",
                    userAccess.getUserId(),
                    userAccess.getUserSubscription(),
                    musicFile.getSubscriptionType());
            throw new MusicFileNotFoundException("File not available for your subscription");
        }

        return musicFile;
    }

    public Page<MusicFile> findAllMusicFiles(UserAccessInfo userAccess, Pageable pageable) {

        if (userAccess.getAllowedSubscriptionTypes() == null ||
                userAccess.getAllowedSubscriptionTypes().isEmpty()) {
            log.debug("User {} has no allowed subscription types", userAccess.getUserId());
            return Page.empty(pageable);
        }

        Page<MusicFile> musicFiles = musicFileRepository.findBySubscriptionTypeIn(
                userAccess.getAllowedSubscriptionTypes(),
                pageable
        );

        log.debug("Found {} music files for user {} with subscription {}",
                musicFiles.getTotalElements(),
                userAccess.getUserId(),
                userAccess.getUserSubscription());

        return musicFiles;
    }

    public Page<MusicFile> findMusicFilesBySubscription(String subscriptionName,
                                                        UserAccessInfo userAccess,
                                                        Pageable pageable) {

        if (!userAccess.getAllowedSubscriptionTypes().contains(subscriptionName)) {
            log.warn("User {} with subscription {} tried to access subscription type {}",
                    userAccess.getUserId(),
                    userAccess.getUserSubscription(),
                    subscriptionName);
            return Page.empty(pageable);
        }

        return musicFileRepository.findMusicFileBySubscriptionTypeIgnoreCase(subscriptionName, pageable)
                .orElse(Page.empty(pageable));
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

    private boolean hasAccessToFile(MusicFile musicFile, UserAccessInfo userAccess) {
        if (userAccess.getAllowedSubscriptionTypes() == null) {
            return false;
        }
        return userAccess.getAllowedSubscriptionTypes().contains(musicFile.getSubscriptionType());
    }
}