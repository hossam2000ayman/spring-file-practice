package org.example.filespractice.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.filespractice.service.EfficientFileService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/efficient/")
@Slf4j
public class EfficientFileController {

    private final EfficientFileService efficientFileService;

    public EfficientFileController(EfficientFileService efficientFileService) {
        this.efficientFileService = efficientFileService;
    }

    @PostMapping("file/upload")
    public void uploadFile(@RequestPart("file") MultipartFile file) throws IOException {
        efficientFileService.uploadFile(file);
    }

    @GetMapping(value = "file/download/{filePath}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filePath) throws IOException {
        Resource resource = efficientFileService.getFile(filePath);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(resource.getFilename())
                                .build()
                                .toString()
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .body(resource);
    }

    @GetMapping(value = "file/preview/{filePath}")
    public ResponseEntity<Resource> previewFile(@PathVariable String filePath) throws IOException {
        Resource resource = efficientFileService.getFile(filePath);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(resource.getFilename())
                                .build()
                                .toString()
                )
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(resource.contentLength())
                .body(resource);
    }

    @GetMapping("file/list")
    public List<Map<String, Object>> listFiles() throws IOException {
        return efficientFileService.listFilesAndFolders();
    }

}
