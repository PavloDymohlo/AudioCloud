package ua.dymohlo.music_content_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.music_content_service.dto.security.UserAccessInfo;
import ua.dymohlo.music_content_service.entity.MusicFile;
import ua.dymohlo.music_content_service.security.annotation.UserSubscription;
import ua.dymohlo.music_content_service.service.MusicFileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/stream")
public class MusicStreamingController {

    private final MusicFileService musicFileService;

    @GetMapping("/{name}")
    public ResponseEntity<Resource> streamMusicFile(
            @PathVariable String name,
            @UserSubscription UserAccessInfo userAccess) {

        log.info("Streaming music file: {} for user: {}", name, userAccess.getUserId());

        MusicFile musicFile = musicFileService.findMusicFileByName(name, userAccess);
        log.info("Found music file in DB: {}, path: {}", musicFile.getMusicFileName(), musicFile.getMusicFilePath());

        try {
            Path filePath = Paths.get(musicFile.getMusicFilePath());

            if (!Files.exists(filePath)) {
                log.error("Physical file not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            long fileSize = Files.size(filePath);

            log.info("Streaming file: {}, size: {} bytes", name, fileSize);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + musicFile.getMusicFileName() + "\"")
                    .header("Accept-Ranges", "bytes")
                    .contentLength(fileSize)
                    .body(resource);

        } catch (IOException e) {
            log.error("IO error while streaming music file: {}", name, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/download/{name}")
    public ResponseEntity<Resource> downloadMusicFile(
            @PathVariable String name,
            @UserSubscription UserAccessInfo userAccess) {

        log.info("Downloading music file: {} for user: {}", name, userAccess.getUserId());

        try {
            MusicFile musicFile = musicFileService.findMusicFileByName(name, userAccess);
            Path filePath = Paths.get(musicFile.getMusicFilePath());

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            long fileSize = Files.size(filePath);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "audio/mpeg")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + musicFile.getMusicFileName() + "\"")
                    .contentLength(fileSize)
                    .body(resource);

        } catch (Exception e) {
            log.error("Error downloading music file: {}", name, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}