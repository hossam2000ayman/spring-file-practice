package org.example.filespractice.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "files_metadata")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String name;
    String contentType;
    String path;
    Long size;
}
