package org.example.filespractice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class EfficientFileService {

    @Value("${file-storage.base-path}")
    private String basePath;


    public void uploadFile(MultipartFile file) throws IOException {
        Path path = Files.createFile(Path.of(basePath, Objects.requireNonNull(file.getOriginalFilename())));

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public Resource getFile(String fileName) {
        Path inputPath = Path.of(basePath, Objects.requireNonNull(fileName));
        if (!Objects.equals(inputPath.getParent(), Path.of(basePath))) {
            throw new SecurityException("Unsupported fileName");
        }
        return new FileSystemResource(inputPath);
    }

    // List folders + files inside basePath
    public List<Map<String, Object>> listFilesAndFolders() throws IOException {
        Path root = Path.of(basePath);

        List<Map<String, Object>> result = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
            for (Path p : stream) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", p.getFileName().toString());
                item.put("directory", Files.isDirectory(p));
                item.put("size", Files.isDirectory(p) ? null : Files.size(p));
                result.add(item);
            }
        }
        return result;
    }
}
