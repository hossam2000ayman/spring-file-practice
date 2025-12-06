package org.example.filespractice.service;

import org.example.filespractice.model.FileMetadata;
import org.example.filespractice.repository.FileMetadataFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {
    @Autowired
    FileMetadataFileRepository fileMetadataFileRepository;
    @Value("${file-storage.base-path}")
    private String basePath;


    public FileMetadata getFileMetadata(Long id) throws IOException {
        return fileMetadataFileRepository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found"));
    }


    public List<Long> uploadMultipleFiles(List<MultipartFile> files) throws IOException {
        List<Long> fileIds = new ArrayList<>();
        for (MultipartFile file : files) {
            Long id = uploadFile(file);
            fileIds.add(id);
        }
        return fileIds;
    }

    public Long uploadFile(MultipartFile inputFile) throws IOException {
        //store file content in folder structure
        String filePath = storeFile(inputFile);

        //store meta data into database
        FileMetadata fileMetadata = FileMetadata.builder()
                .name(inputFile.getOriginalFilename())
                .contentType(inputFile.getContentType())
                .path(filePath)
                .size(inputFile.getSize())
                .build();
        fileMetadata = fileMetadataFileRepository.save(fileMetadata);

        return fileMetadata.getId();
    }

    private String storeFile(MultipartFile file) throws IOException {
        Files.createDirectories(Path.of(basePath));
        Path filePath = Paths.get(basePath, file.getOriginalFilename());

        // Stream file content directly to disk (no byte[] in memory)
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return filePath.toString();
    }
}
