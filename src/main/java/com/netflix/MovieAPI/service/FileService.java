package com.netflix.MovieAPI.service;

import com.netflix.MovieAPI.exceptions.EmptyFileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface FileService {

    String uploadFile(String path, MultipartFile file) throws IOException, EmptyFileException;

    InputStream getResourceFile(String path, String filename) throws IOException;
}