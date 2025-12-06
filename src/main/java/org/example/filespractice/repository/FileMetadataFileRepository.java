package org.example.filespractice.repository;

import org.example.filespractice.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataFileRepository extends JpaRepository<FileMetadata, Long> {
}
