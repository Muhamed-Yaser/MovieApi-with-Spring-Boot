
package com.netflix.MovieAPI.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadFile(String path, MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IOException("Cannot upload an empty file");
        }

        // Generate a unique filename to avoid collisions
        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null) {
            int lastDot = originalFilename.lastIndexOf('.');

            if (lastDot >= 0) {
                String candidate = originalFilename.substring(lastDot);

                if (candidate.matches("\\.[a-zA-Z0-9]{1,10}")) {
                    extension = candidate;
                }
            }
        }

        String fileName = UUID.randomUUID() + extension;

        // Create the upload directory if it doesn't exist
        Path uploadDir = Paths.get(path).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        // Copy the uploaded file
        Path filePath = uploadDir.resolve(fileName);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath);
        }

        return fileName;
    }

    @Override
    public InputStream getResourceFile(String path, String fileName) throws FileNotFoundException {

        Path uploadDir = Paths.get(path).toAbsolutePath().normalize();
        Path filePath = uploadDir.resolve(fileName).normalize();

        // Prevent accessing files outside the upload directory
        if (!filePath.startsWith(uploadDir)) {
            throw new FileNotFoundException("Invalid file path");
        }

        return new FileInputStream(filePath.toFile());
    }
}
