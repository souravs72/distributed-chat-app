package com.chatapp.media.service;

import com.chatapp.common.dto.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {

    @Value("${app.media.upload-dir:/tmp/uploads}")
    private String uploadDir;

    @Value("${app.media.max-file-size:10485760}") // 10MB
    private long maxFileSize;

    public BaseResponse<String> uploadFile(MultipartFile file, String userId) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return BaseResponse.error("File is empty", "EMPTY_FILE");
            }

            if (file.getSize() > maxFileSize) {
                return BaseResponse.error("File size exceeds limit", "FILE_TOO_LARGE");
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String filename = UUID.randomUUID().toString() + extension;

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, userId);
            Files.createDirectories(uploadPath);

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // Generate thumbnail for images
            if (isImageFile(extension)) {
                generateThumbnail(filePath.toString());
            }

            String fileUrl = "/api/media/files/" + userId + "/" + filename;
            return BaseResponse.success(fileUrl, "File uploaded successfully");

        } catch (IOException e) {
            log.error("Error uploading file for user: {}", userId, e);
            return BaseResponse.error("Failed to upload file", "UPLOAD_FAILED");
        }
    }

    public BaseResponse<byte[]> downloadFile(String userId, String filename) {
        try {
            Path filePath = Paths.get(uploadDir, userId, filename);
            
            if (!Files.exists(filePath)) {
                return BaseResponse.error("File not found", "FILE_NOT_FOUND");
            }

            byte[] fileContent = Files.readAllBytes(filePath);
            return BaseResponse.success(fileContent);

        } catch (IOException e) {
            log.error("Error downloading file: {}", filename, e);
            return BaseResponse.error("Failed to download file", "DOWNLOAD_FAILED");
        }
    }

    public BaseResponse<byte[]> downloadThumbnail(String userId, String filename) {
        try {
            String thumbnailName = "thumb_" + filename;
            Path thumbnailPath = Paths.get(uploadDir, userId, thumbnailName);
            
            if (!Files.exists(thumbnailPath)) {
                return BaseResponse.error("Thumbnail not found", "THUMBNAIL_NOT_FOUND");
            }

            byte[] thumbnailContent = Files.readAllBytes(thumbnailPath);
            return BaseResponse.success(thumbnailContent);

        } catch (IOException e) {
            log.error("Error downloading thumbnail: {}", filename, e);
            return BaseResponse.error("Failed to download thumbnail", "THUMBNAIL_DOWNLOAD_FAILED");
        }
    }

    private void generateThumbnail(String filePath) throws IOException {
        String thumbnailPath = filePath.replaceAll("\\.(jpg|jpeg|png|gif)$", "_thumb.$1");
        
        Thumbnails.of(filePath)
                .size(200, 200)
                .outputQuality(0.8)
                .toFile(thumbnailPath);
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : filename.substring(lastDotIndex);
    }

    private boolean isImageFile(String extension) {
        String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
        for (String ext : imageExtensions) {
            if (extension.toLowerCase().equals(ext)) {
                return true;
            }
        }
        return false;
    }
}
