package com.netflix.MovieAPI.service;

import com.netflix.MovieAPI.dto.MovieDto;
import com.netflix.MovieAPI.entities.Movie;
import com.netflix.MovieAPI.exceptions.EmptyFileException;
import com.netflix.MovieAPI.exceptions.MovieNotFoundException;
import com.netflix.MovieAPI.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final FileService fileService;

    @Value("${project.poster}")
    private String path;

    @Value("${base.url}")
    private String baseUrl;

    public MovieServiceImpl(
            MovieRepository movieRepository,
            FileService fileService) {

        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file)
            throws IOException, EmptyFileException {

        String uploadedFileName = fileService.uploadFile(path, file);

        movieDto.setPoster(uploadedFileName);

        Movie movie = new Movie(
                null,
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        Movie savedMovie = movieRepository.save(movie);

        return convertToDto(savedMovie);
    }

    @Override
    public MovieDto getMovie(Integer movieId) {

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(
                        "Movie not found with id = " + movieId
                ));

        return convertToDto(movie);
    }

    @Override
    public List<MovieDto> getAllMovies() {

        List<Movie> movies = movieRepository.findAll();
        List<MovieDto> movieDtos = new ArrayList<>();

        for (Movie movie : movies) {
            movieDtos.add(convertToDto(movie));
        }

        return movieDtos;
    }

    @Override
    public MovieDto updateMovie(
            Integer movieId,
            MovieDto movieDto,
            MultipartFile file) throws IOException, EmptyFileException {

        Movie existingMovie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(
                        "Movie not found with id = " + movieId
                ));

        String fileName = existingMovie.getPoster();

        if (file != null) {
            fileName = fileService.uploadFile(path, file);
        }

        movieDto.setPoster(fileName);

        Movie movie = new Movie(
                existingMovie.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        Movie updatedMovie = movieRepository.save(movie);

        if (file != null) {
            Files.deleteIfExists(
                    Paths.get(path, existingMovie.getPoster())
            );
        }

        return convertToDto(updatedMovie);
    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException {

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(
                        "Movie not found with id = " + movieId
                ));

        movieRepository.delete(movie);

        Files.deleteIfExists(
                Paths.get(path, movie.getPoster())
        );

        return "Movie deleted with id = " + movieId;
    }

    private MovieDto convertToDto(Movie movie) {

        String posterUrl = baseUrl + "/file/" + movie.getPoster();

        return new MovieDto(
                movie.getMovieId(),
                movie.getTitle(),
                movie.getDirector(),
                movie.getStudio(),
                movie.getMovieCast(),
                movie.getReleaseYear(),
                movie.getPoster(),
                posterUrl
        );
    }
}