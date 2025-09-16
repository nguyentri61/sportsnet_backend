package com.tlcn.sportsnet_backend.service;


import com.tlcn.sportsnet_backend.enums.MediaTypeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {
    @Value("${app.file.upload-dir}")
    private String uploadDir;

    @Value("${app.file.base-url}")
    private String baseUrl;


    public String storeFile(MultipartFile file, String subDirectory) {
        try {
            // Tạo tên file duy nhất
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID() + fileExtension;

            // Tạo thư mục nếu chưa tồn tại
            Path uploadPath = Paths.get(uploadDir, subDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Lưu file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Trả về tên file (không bao gồm đường dẫn)
            return uniqueFilename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file: " + ex.getMessage(), ex);
        }
    }

    public String getFileUrl(String filename, String subDirectory) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        return baseUrl  + subDirectory + "/" + filename;
    }

    public void deleteFile(String filename, String subDirectory) {
        try {
            Path filePath = Paths.get(uploadDir, subDirectory, filename);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file: " + ex.getMessage(), ex);
        }
    }

    public MediaTypeEnum detectMediaType(String filename) {
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        return List.of("mp4", "mov", "avi", "mkv").contains(ext)
                ? MediaTypeEnum.VIDEO
                : MediaTypeEnum.IMAGE;
    }
}

