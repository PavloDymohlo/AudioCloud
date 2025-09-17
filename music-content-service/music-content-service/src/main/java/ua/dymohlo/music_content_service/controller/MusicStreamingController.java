package ua.dymohlo.music_content_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
@Tag(name = "Music Streaming", description = "Music file streaming and download operations")
public class MusicStreamingController {

    private final MusicFileService musicFileService;

    @GetMapping("/{name}")
    @Operation(
            summary = "Stream music file",
            description = "Streams a music file for inline playback with subscription-based access control. Returns audio content with appropriate headers for browser playback."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Music file streamed successfully"),
            @ApiResponse(responseCode = "403", description = "File not available for user's subscription"),
            @ApiResponse(responseCode = "404", description = "Music file not found or physical file missing"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "500", description = "Internal server error during streaming")
    })
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
    @Operation(
            summary = "Download music file",
            description = "Downloads a music file as an attachment with subscription-based access control. Forces download in browser."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Music file download started successfully"),
            @ApiResponse(responseCode = "403", description = "File not available for user's subscription"),
            @ApiResponse(responseCode = "404", description = "Music file not found or physical file missing"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "500", description = "Internal server error during download")
    })
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