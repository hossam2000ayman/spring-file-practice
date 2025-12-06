package org.example.filespractice.service;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MinIOService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String defaultBucketName;

    public MinIOService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }


    @PostConstruct
    public void init() throws Exception {
        boolean isDefaultBucketExists = bucketAlreadyExists(defaultBucketName);
        if (!isDefaultBucketExists) {
            createBucket(defaultBucketName);
        }
    }

    public String createFileObject(MultipartFile file) throws Exception {
        return createFileObject(file, defaultBucketName);
    }

    public String createFileObject(MultipartFile file, String bucketName) throws Exception {
        String fileName = file.getOriginalFilename();
        InputStream inputStream = file.getInputStream();

        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(fileName)
                .stream(inputStream, file.getSize(), -1)
                .contentType(file.getContentType())
                .build();

        ObjectWriteResponse response = minioClient.putObject(putObjectArgs);

        return response.toString();
    }

    public GetObjectResponse getFileObject(String objectName) throws Exception {
        return getFileObject(objectName, defaultBucketName);
    }


    public GetObjectResponse getFileObject(String objectName, String bucketName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    /**
     * List all objects in the bucket
     *
     */
    public Iterable<Result<Item>> listFileObjects(String bucketName) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .recursive(true)
                        .build()
        );
    }

    public Iterable<Result<Item>> listFileObjects(String prefix, String bucketName) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(prefix)   // e.g. "documents/2025/"
                        .recursive(true)
                        .build()
        );
    }


    public void deleteFileObject(String objectName) throws Exception {
        deleteFileObject(objectName, defaultBucketName);
    }

    public void deleteFileObject(String objectName, String bucketName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    public void copyFileObject(String sourceName, String targetName) throws Exception {
        copyFileObject(sourceName, targetName, defaultBucketName);
    }

    public void copyFileObject(String sourceName, String targetName, String bucketName) throws Exception {
        minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket(bucketName)
                        .object(targetName)
                        .source(
                                CopySource.builder()
                                        .bucket(bucketName)
                                        .object(sourceName)
                                        .build()
                        )
                        .build()
        );
    }

    public void moveFileObject(String sourceName, String targetName) throws Exception {
        moveFileObject(sourceName, targetName, defaultBucketName);
    }

    public void moveFileObject(String sourceName, String targetName, String bucketName) throws Exception {
        copyFileObject(sourceName, targetName, bucketName);
        deleteFileObject(sourceName, bucketName);
    }


    public void createFolder(String folderName) throws Exception {
        createFolder(folderName, defaultBucketName);
    }

    public void createFolder(String folderName, String bucketName) throws Exception {
        if (!folderName.endsWith("/")) folderName += "/";

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(folderName)
                        .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                        .build()
        );
    }

    public void deleteFolder(String prefix) throws Exception {
        deleteFolder(prefix, defaultBucketName);
    }

    public void deleteFolder(String prefix, String bucketName) throws Exception {
        Iterable<Result<Item>> results = listFileObjects(prefix, bucketName);

        for (Result<Item> result : results) {
            deleteFileObject(result.get().objectName(), bucketName);
        }
    }

    /**
     * List All Buckets
     *
     */
    public List<Bucket> listBuckets() throws Exception {
        return minioClient.listBuckets();
    }

    public boolean bucketAlreadyExists(String bucketName) throws Exception {
        return minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build()
        );
    }

    /**
     * ✅ Delete Bucket (Only if empty!)
     * ⚠️ If bucket is NOT empty → MinIO throws an exception.
     *
     */
    public void deleteBucket(String bucketName) throws Exception {
        Iterable<Result<Item>> allObjectsInsideBucket = listFileObjects(bucketName);
        List<DeleteObject> objectsToDelete = new ArrayList<>();
        for (Result<Item> result : allObjectsInsideBucket) {
            Item item = result.get();
            objectsToDelete.add(new DeleteObject(item.objectName()));
        }
        Iterable<Result<DeleteError>> errors = minioClient.removeObjects(
                RemoveObjectsArgs.builder()
                        .bucket(bucketName)
                        .objects(objectsToDelete)
                        .build()
        );

        for (Result<DeleteError> error : errors) {
            log.info("Failed to delete file object : {}", error.get());
        }

        minioClient.removeBucket(
                RemoveBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
        );
    }


    public void createBucket(String bucketName) throws Exception {
        minioClient.makeBucket(
                MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
        );
    }

    public String generatePresignedUrl(String objectName) throws Exception {
        return generatePresignedUrl(objectName, defaultBucketName);
    }
    public String generatePresignedUrl(String objectName, String bucketName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(1, TimeUnit.MINUTES)
                        .build()
        );
    }

    public String generateUploadUrl(String objectName) throws Exception {
        return generateUploadUrl(objectName, defaultBucketName);
    }
    public String generateUploadUrl(String objectName, String bucketName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.POST)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(1, TimeUnit.MINUTES)
                        .build()
        );
    }
}
