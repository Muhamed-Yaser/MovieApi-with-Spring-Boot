package com.netflix.MovieAPI.service;

import com.netflix.MovieAPI.dto.MovieDto;
import com.netflix.MovieAPI.exceptions.EmptyFileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MovieService {

    MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException, EmptyFileException;

    MovieDto getMovie(Integer movieId);

    List<MovieDto> getAllMovies();

    MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file) throws IOException, EmptyFileException;

    String deleteMovie(Integer movieId) throws IOException;
}
