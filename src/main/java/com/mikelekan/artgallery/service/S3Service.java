package com.mikelekan.artgallery.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.io.IOException;
import java.util.UUID;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

@Service
public class S3Service
{
    private final S3Client s3Client;

    @Value("${aws_bucket}")
    private String bucketName;

    @Value("${aws_region}")
    private String region;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file) throws IOException
    {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        // 2. Build the upload request
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        // 3. Upload the file
        s3Client.putObject(putObjectRequest,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // 4. Return the public URL of the image
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
    }

    public void deleteFile(String fileURL)
    {
        String key = fileURL.substring(fileURL.lastIndexOf("/") + 1);

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }
}
