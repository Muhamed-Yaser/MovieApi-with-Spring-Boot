package com.netflix.MovieAPI.service;

import com.netflix.MovieAPI.exceptions.EmptyFileException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadFile(String path, MultipartFile file) throws IOException, EmptyFileException {

        if (file == null || file.isEmpty()) {
            throw new EmptyFileException(
                    "File is empty! Please send another file!"
            );
        }

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

        Path uploadDir = Paths.get(path).toAbsolutePath().normalize();
        Files.createDirectories(uploadDir);

        Path filePath = uploadDir.resolve(fileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath);
        }

        return fileName;
    }

    @Override
    public InputStream getResourceFile(String path, String fileName)
            throws IOException {

        Path uploadDir = Paths.get(path).toAbsolutePath().normalize();
        Path filePath = uploadDir.resolve(fileName).normalize();

        if (!filePath.startsWith(uploadDir)) {
            throw new FileNotFoundException("Invalid file path!");
        }

        try {
            return Files.newInputStream(filePath);
        } catch (NoSuchFileException ex) {
            throw new FileNotFoundException(
                    "File not found: " + fileName
            );
        }
    }
}