package org.example.filespractice.controller;

import org.example.filespractice.model.FileMetadata;
import org.example.filespractice.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    FileService fileService;

    @PostMapping("/upload")
    public Long uploadFile(@RequestPart MultipartFile file) throws IOException {
        return fileService.uploadFile(file);
    }

    @PostMapping("/upload/multiple")
    public List<Long> uploadMultipleFiles(@RequestPart List<MultipartFile> files) throws IOException {
        return fileService.uploadMultipleFiles(files);
    }

    @GetMapping("/review/{id}")
    public ResponseEntity<Resource> reviewFile(@PathVariable Long id) throws IOException {
        FileMetadata storedFile = fileService.getFileMetadata(id);
        Path path = Paths.get(storedFile.getPath());
        // Streams 4KB–8KB chunks → constant memory, supports huge files.
        // Resource resource = new InputStreamResource(Files.newInputStream(path));
        // ⚡ (Advanced) If You Want Maximum Performance: FileSystemResource
        Resource resource = new FileSystemResource(path.toFile());


        return ResponseEntity.ok()
                // Controls content behavior by "HttpHeaders.CONTENT_DISPOSITION".
                // Inline Preview Instead of Forced Download
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(storedFile.getName(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                // Browser opens PDFs/images correctly.
                .contentType(MediaType.parseMediaType(storedFile.getContentType()))
                // Allows progress bar, avoids chunked transfer unless needed.
                .contentLength(storedFile.getSize())
                .body(resource);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws IOException {
        FileMetadata storedFile = fileService.getFileMetadata(id);
        Path path = Paths.get(storedFile.getPath());
        Resource resource = new FileSystemResource(path.toFile());


        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(storedFile.getName())
                                .build()
                                .toString()
                )
                .contentType(MediaType.parseMediaType(storedFile.getContentType()))
                .contentLength(storedFile.getSize())
                .body(resource);
    }
}
