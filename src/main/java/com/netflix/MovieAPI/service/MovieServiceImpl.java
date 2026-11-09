
package com.netflix.MovieAPI.service;

import com.netflix.MovieAPI.dto.MovieDto;
import com.netflix.MovieAPI.entities.Movie;
import com.netflix.MovieAPI.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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

    public MovieServiceImpl(MovieRepository movieRepository, FileService fileService) {
        this.movieRepository = movieRepository;
        this.fileService = fileService;
    }

    @Override
    public MovieDto addMovie(MovieDto movieDto, MultipartFile file) throws IOException {

        // 1. Upload the poster
        String uploadedFileName = fileService.uploadFile(path, file);

        // 2. Set the poster filename in DTO
        movieDto.setPoster(uploadedFileName);

        // 3. Map DTO to Movie entity
        Movie movie = new Movie(
                null,
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        // 4. Save movie in database
        Movie savedMovie = movieRepository.save(movie);

        // 5. Return the saved movie as DTO
        return convertToDto(savedMovie);
    }

    @Override
    public MovieDto getMovie(Integer movieId) {

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
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
    public MovieDto updateMovie(Integer movieId, MovieDto movieDto, MultipartFile file)
            throws IOException {

        // 1. Find existing movie
        Movie existingMovie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Movie not found with id = " + movieId
                ));

        String fileName = existingMovie.getPoster();

        // 2. Upload new poster if provided
        if (file != null) {
            fileName = fileService.uploadFile(path, file);
        }

        // 3. Set poster filename
        movieDto.setPoster(fileName);

        // 4. Map DTO to Movie entity
        Movie movie = new Movie(
                existingMovie.getMovieId(),
                movieDto.getTitle(),
                movieDto.getDirector(),
                movieDto.getStudio(),
                movieDto.getMovieCast(),
                movieDto.getReleaseYear(),
                movieDto.getPoster()
        );

        // 5. Save updated movie
        Movie updatedMovie = movieRepository.save(movie);

        // 6. Delete old poster only after successful save
        if (file != null) {
            Files.deleteIfExists(Paths.get(path, existingMovie.getPoster()));
        }

        return convertToDto(updatedMovie);
    }

    @Override
    public String deleteMovie(Integer movieId) throws IOException {

        // 1. Find movie
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Movie not found with id = " + movieId
                ));

        // 2. Delete movie from database
        movieRepository.delete(movie);

        // 3. Delete associated poster
        Files.deleteIfExists(Paths.get(path, movie.getPoster()));

        return "Movie deleted with id = " + movieId;
    }

    // Convert Movie entity to MovieDto
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
