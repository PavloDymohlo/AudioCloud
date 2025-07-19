package ua.dymohlo.music_content_service.controller;

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
import ua.dymohlo.music_content_service.entity.MusicFile;
import ua.dymohlo.music_content_service.service.MusicFileService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/music-files")
public class MusicFileController {
    private final MusicFileService musicFileService;
    private final MusicFileResponseFactory musicFileResponseFactory;

    @PostMapping()
    public MusicFileDataResponse addNewMusicFile(@RequestBody NewMusicFileRequest request) {
        MusicFile musicFile = musicFileService.addNewMusicFile(request);
        return musicFileResponseFactory.createMusicFileDataResponse(musicFile);
    }

    @GetMapping("/{name}")
    public MusicFileDataResponse findMusicFileByName(@PathVariable String name) {
        MusicFile musicFile = musicFileService.findMusicFileByName(name);
        return musicFileResponseFactory.createMusicFileDataResponse(musicFile);
    }

    @GetMapping
    public Page<MusicFileDataResponse> findAllMusicFiles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "musicFileName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirections
    ) {
        Sort sort = sortDirections.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<MusicFile> musicFiles = musicFileService.findAllMusicFiles(pageable);
        return musicFiles.map(
                musicFile -> new MusicFileDataResponse(
                        musicFile.getMusicFileName(),
                        musicFile.getSubscriptionType()
                )
        );
    }

    @GetMapping("/subscriptions/{subscription}")
    public Page<MusicFileDataResponse> findMusicFilesBySubscription(
            @PathVariable String subscription,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "musicFileName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirections
    ) {
        Sort sort = sortDirections.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<MusicFile> musicFiles = musicFileService.findMusicFilesBySubscription(subscription, pageable);
        return musicFiles.map(
                musicFile -> new MusicFileDataResponse(
                        musicFile.getMusicFileName(),
                        musicFile.getSubscriptionType()
                )
        );
    }

    @PutMapping
    public MusicFileDataResponse updateMusicFileData(@RequestBody UpdateMusicFileDataRequest request) {
        MusicFile musicFile = musicFileService.updateMusicFileData(request);
        return musicFileResponseFactory.createMusicFileDataResponse(musicFile);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<String> deleteMusicFileByName(@PathVariable String name) {
        musicFileService.deleteMusicFileByName(name);
        return ResponseEntity.noContent().build();
    }
}
