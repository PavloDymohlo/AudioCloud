package ua.dymohlo.music_content_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ua.dymohlo.music_content_service.dto.request.NewMusicFileRequest;
import ua.dymohlo.music_content_service.dto.request.UpdateMusicFileDataRequest;
import ua.dymohlo.music_content_service.dto.response.MusicFileDataResponse;
import ua.dymohlo.music_content_service.dto.response.MusicFileResponseFactory;
import ua.dymohlo.music_content_service.dto.security.UserAccessInfo;
import ua.dymohlo.music_content_service.entity.MusicFile;
import ua.dymohlo.music_content_service.security.annotation.UserSubscription;
import ua.dymohlo.music_content_service.service.MusicFileService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/music-files")
@Tag(name = "Music Files", description = "Music file management and access operations")
public class MusicFileController {
    private final MusicFileService musicFileService;
    private final MusicFileResponseFactory musicFileResponseFactory;

    @PostMapping()
    @Operation(
            summary = "Add new music file",
            description = "Adds a new music file to the system with subscription-based access control. Admin role required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Music file added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or file already exists"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public MusicFileDataResponse addNewMusicFile(@RequestBody NewMusicFileRequest request) {
        MusicFile musicFile = musicFileService.addNewMusicFile(request);
        return musicFileResponseFactory.createMusicFileDataResponse(musicFile);
    }

    @GetMapping("/{name}")
    @Operation(
            summary = "Find music file by name",
            description = "Retrieves a specific music file by name with subscription access validation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Music file found and accessible"),
            @ApiResponse(responseCode = "403", description = "File not available for user's subscription"),
            @ApiResponse(responseCode = "404", description = "Music file not found"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public MusicFileDataResponse findMusicFileByName(
            @PathVariable String name,
            @UserSubscription UserAccessInfo userAccess) {

        log.debug("Finding music file by name: {} for user: {}", name, userAccess.getUserId());
        MusicFile musicFile = musicFileService.findMusicFileByName(name, userAccess);
        return musicFileResponseFactory.createMusicFileDataResponse(musicFile);
    }

    @GetMapping
    @Operation(
            summary = "Get all accessible music files",
            description = "Retrieves paginated list of music files based on user's subscription level"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Music files retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Page<MusicFileDataResponse> findAllMusicFiles(
            @UserSubscription UserAccessInfo userAccess,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "musicFileName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirections
    ) {
        log.debug("Finding all music files for user: {} with subscription: {}",
                userAccess.getUserId(), userAccess.getUserSubscription());

        Sort sort = sortDirections.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<MusicFile> musicFiles = musicFileService.findAllMusicFiles(userAccess, pageable);

        return musicFiles.map(musicFileResponseFactory::createMusicFileDataResponse);
    }

    @GetMapping("/subscriptions/{subscription}")
    @Operation(
            summary = "Find music files by subscription type",
            description = "Retrieves music files filtered by specific subscription type with access validation"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Music files retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied for requested subscription type"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public Page<MusicFileDataResponse> findMusicFilesBySubscription(
            @PathVariable String subscription,
            @UserSubscription UserAccessInfo userAccess,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "musicFileName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirections
    ) {
        log.debug("Finding music files by subscription: {} for user: {}",
                subscription, userAccess.getUserId());

        Sort sort = sortDirections.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<MusicFile> musicFiles = musicFileService.findMusicFilesBySubscription(
                subscription, userAccess, pageable);

        return musicFiles.map(musicFileResponseFactory::createMusicFileDataResponse);
    }

    @PutMapping
    @Operation(
            summary = "Update music file data",
            description = "Updates existing music file information. Admin role required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Music file updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Music file not found")
    })
    public MusicFileDataResponse updateMusicFileData(@RequestBody UpdateMusicFileDataRequest request) {
        MusicFile musicFile = musicFileService.updateMusicFileData(request);
        return musicFileResponseFactory.createMusicFileDataResponse(musicFile);
    }

    @DeleteMapping("/{name}")
    @Operation(
            summary = "Delete music file",
            description = "Deletes a music file from the system. Admin role required."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Music file deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @ApiResponse(responseCode = "404", description = "Music file not found")
    })
    public ResponseEntity<String> deleteMusicFileByName(@PathVariable String name) {
        musicFileService.deleteMusicFileByName(name);
        return ResponseEntity.noContent().build();
    }
}