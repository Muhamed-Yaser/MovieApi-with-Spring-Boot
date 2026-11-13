package com.netflix.MovieAPI.service;

import com.netflix.MovieAPI.dto.MovieDto;
import com.netflix.MovieAPI.dto.MoviePageResponse;
import com.netflix.MovieAPI.entities.Movie;
import com.netflix.MovieAPI.exceptions.EmptyFileException;
import com.netflix.MovieAPI.exceptions.MovieNotFoundException;
import com.netflix.MovieAPI.repositories.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    //Pagination
    @Override
    public MoviePageResponse getAllMoviesWithPagination(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Movie> moviePages = movieRepository.findAll(pageable);
        List<Movie> movies = moviePages.getContent();

        List<MovieDto> movieDtos = new ArrayList<>();

        // 2. iterate through the list, generate posterUrl for each movie obj,
        // and map to MovieDto obj
        for(Movie movie : movies) {
            String posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }


        return new MoviePageResponse(movieDtos, pageNumber, pageSize,
                moviePages.getTotalElements(),
                moviePages.getTotalPages(),
                moviePages.isLast());
    }

    @Override
    public MoviePageResponse getAllMoviesWithPaginationAndSorting(Integer pageNumber, Integer pageSize,
                                                                  String sortBy, String dir) {
        Sort sort = dir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Movie> moviePages = movieRepository.findAll(pageable);
        List<Movie> movies = moviePages.getContent();

        List<MovieDto> movieDtos = new ArrayList<>();

        // 2. iterate through the list, generate posterUrl for each movie obj,
        // and map to MovieDto obj
        for(Movie movie : movies) {
            String posterUrl = baseUrl + "/file/" + movie.getPoster();
            MovieDto movieDto = new MovieDto(
                    movie.getMovieId(),
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getStudio(),
                    movie.getMovieCast(),
                    movie.getReleaseYear(),
                    movie.getPoster(),
                    posterUrl
            );
            movieDtos.add(movieDto);
        }


        return new MoviePageResponse(movieDtos, pageNumber, pageSize,
                moviePages.getTotalElements(),
                moviePages.getTotalPages(),
                moviePages.isLast());
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