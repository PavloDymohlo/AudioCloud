package ua.dymohlo.music_content_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "music_files", schema = "music_content_service")
public class MusicFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String musicFileName;
    private String musicFilePath;
    private String subscriptionType;
}
