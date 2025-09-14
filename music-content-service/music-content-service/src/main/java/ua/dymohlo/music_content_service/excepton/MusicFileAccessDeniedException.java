package ua.dymohlo.music_content_service.excepton;

public class MusicFileAccessDeniedException extends RuntimeException {
    public MusicFileAccessDeniedException(String message) {
        super(message);
    }
}