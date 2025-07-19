package ua.dymohlo.music_content_service.excepton;

public class MusicFileNotFoundException extends RuntimeException {
    public MusicFileNotFoundException(String message) {
        super(message);
    }
}
