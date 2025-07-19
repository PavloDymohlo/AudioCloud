package ua.dymohlo.music_content_service.excepton;

public class MusicFileAlreadyExistsException extends RuntimeException {
    public MusicFileAlreadyExistsException(String message) {
        super(message);
    }
}
