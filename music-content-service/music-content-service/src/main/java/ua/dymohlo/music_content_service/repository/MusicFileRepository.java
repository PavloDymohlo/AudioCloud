package ua.dymohlo.music_content_service.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.dymohlo.music_content_service.entity.MusicFile;

import java.util.List;
import java.util.Optional;

public interface MusicFileRepository extends JpaRepository<MusicFile, Long> {
    Optional<MusicFile> findByMusicFileNameIgnoreCase(String musicFileName);

    Optional<Page<MusicFile>> findMusicFileBySubscriptionTypeIgnoreCase(String subscription, Pageable pageable);

    Page<MusicFile> findBySubscriptionTypeIn(List<String> subscriptionTypes, Pageable pageable);
}