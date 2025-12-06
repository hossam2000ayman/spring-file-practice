package org.example.filespractice.controller;

import io.minio.GetObjectResponse;
import org.example.filespractice.service.MinIOService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@RestController
@RequestMapping("/api/minio/")
public class MinIOController {

    private final MinIOService minIOService;

    public MinIOController(MinIOService minIOService) {
        this.minIOService = minIOService;
    }

    @PostMapping("file/upload")
    public ResponseEntity<String> uploadFile(@RequestPart("file") MultipartFile file) {
        try {
            String response = minIOService.createFileObject(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Error uploading the file: \n\n" + e.getMessage());
        }
    }

    @GetMapping("file/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            GetObjectResponse fileObject = minIOService.getFileObject(fileName);
            String contentType = fileObject.headers().get(HttpHeaders.CONTENT_TYPE);
            Long contentLength = Long.valueOf(Objects.requireNonNull(fileObject.headers().get(HttpHeaders.CONTENT_LENGTH)));

            Resource resource = new InputStreamResource(fileObject);
            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.attachment()
                                    .filename(fileObject.object(), StandardCharsets.UTF_8)
                                    .build()
                                    .toString()
                    )
                    .contentType(MediaType.parseMediaType(Objects.requireNonNull(contentType)))
                    .contentLength(contentLength)
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .build();
        }
    }

    @GetMapping("file/review/{fileName}")
    public ResponseEntity<Resource> reviewFile(@PathVariable String fileName) {
        try {
            GetObjectResponse fileObject = minIOService.getFileObject(fileName);
            String contentType = fileObject.headers().get(HttpHeaders.CONTENT_TYPE);
            Long contentLength = Long.valueOf(Objects.requireNonNull(fileObject.headers().get(HttpHeaders.CONTENT_LENGTH)));

            Resource resource = new InputStreamResource(fileObject);
            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.inline()
                                    .filename(fileObject.object(), StandardCharsets.UTF_8)
                                    .build()
                                    .toString()
                    )
                    .contentType(MediaType.parseMediaType(contentType))
                    .contentLength(contentLength)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .build();
        }
    }

    @GetMapping("file/list")
    public ResponseEntity<?> listFileObjects(@RequestParam(defaultValue = "") String prefix) {
        try {
            return ResponseEntity.ok(minIOService.listFileObjects(prefix));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("file/delete/{fileName}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileName) {
        try {
            minIOService.deleteFileObject(fileName);
            return ResponseEntity.ok("Deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("file/copy")
    public ResponseEntity<?> copy(@RequestParam String source, @RequestParam String target) {
        try {
            minIOService.copyFileObject(source, target);
            return ResponseEntity.ok("Copied");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("file/move")
    public ResponseEntity<?> move(@RequestParam String source, @RequestParam String target) {
        try {
            minIOService.moveFileObject(source, target);
            return ResponseEntity.ok("Moved");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("bucket/list")
    public ResponseEntity<?> listBuckets() {
        try {
            return ResponseEntity.ok(minIOService.listBuckets());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    @PostMapping("bucket/create/{bucket}")
    public ResponseEntity<?> createBucket(@PathVariable String bucket) {
        try {
            minIOService.createBucket(bucket);
            return ResponseEntity.ok("Bucket created");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @DeleteMapping("bucket/delete/{bucket}")
    public ResponseEntity<?> deleteBucket(@PathVariable String bucket) {
        try {
            minIOService.deleteBucket(bucket);
            return ResponseEntity.ok("Bucket deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("file/share/{fileName}")
    public ResponseEntity<?> shareFile(@PathVariable String fileName) {
        try {
            String url = minIOService.generatePresignedUrl(fileName);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("file/upload-url/{fileName}")
    public ResponseEntity<?> generateUploadUrl(@PathVariable String fileName) {
        try {
            String url = minIOService.generateUploadUrl(fileName);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



}
