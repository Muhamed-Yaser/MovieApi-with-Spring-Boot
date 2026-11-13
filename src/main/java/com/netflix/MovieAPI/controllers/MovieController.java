
package com.netflix.MovieAPI.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.MovieAPI.dto.MovieDto;
import com.netflix.MovieAPI.dto.MoviePageResponse;
import com.netflix.MovieAPI.exceptions.EmptyFileException;
import com.netflix.MovieAPI.service.MovieService;
import com.netflix.utils.AppConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movie")
@CrossOrigin(origins = "*")
public class MovieController {

    private final MovieService movieService;
    private final ObjectMapper objectMapper;

    public MovieController(
            MovieService movieService,
            ObjectMapper objectMapper) {

        this.movieService = movieService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/add-movie")
    public ResponseEntity<MovieDto> addMovieHandler(
            @RequestPart("file") MultipartFile file,
            @RequestPart("movieDto") String movieDto)
            throws IOException, EmptyFileException {

        MovieDto dto = convertToMovieDto(movieDto);

        return new ResponseEntity<>(
                movieService.addMovie(dto, file),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieDto> getMovieHandler(
            @PathVariable Integer movieId) {

        return ResponseEntity.ok(
                movieService.getMovie(movieId)
        );
    }

    @GetMapping("/all")
    public ResponseEntity<List<MovieDto>> getAllMoviesHandler() {

        return ResponseEntity.ok(
                movieService.getAllMovies()
        );
    }

    @PutMapping("/update/{movieId}")
    public ResponseEntity<MovieDto> updateMovieHandler(
            @PathVariable Integer movieId,
            @RequestPart(value = "file", required = false)
            MultipartFile file,
            @RequestPart("movieDto") String movieDtoObj)
            throws IOException, EmptyFileException {

        MovieDto movieDto = convertToMovieDto(movieDtoObj);

        return ResponseEntity.ok(
                movieService.updateMovie(movieId, movieDto, file)
        );
    }

    @DeleteMapping("/delete/{movieId}")
    public ResponseEntity<String> deleteMovieHandler(
            @PathVariable Integer movieId) throws IOException {

        return ResponseEntity.ok(
                movieService.deleteMovie(movieId)
        );
    }

    @GetMapping("/allMoviesPage")
    public ResponseEntity<MoviePageResponse> getMoviesWithPagination(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false)
            Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false)
            Integer pageSize) {

        return ResponseEntity.ok(
                movieService.getAllMoviesWithPagination(pageNumber, pageSize)
        );
    }

    @GetMapping("/allMoviesPageSort")
    public ResponseEntity<MoviePageResponse> getMoviesWithPaginationAndSorting(
            @RequestParam(defaultValue = AppConstants.PAGE_NUMBER, required = false)
            Integer pageNumber,
            @RequestParam(defaultValue = AppConstants.PAGE_SIZE, required = false)
            Integer pageSize,
            @RequestParam(defaultValue = AppConstants.SORT_BY, required = false)
            String sortBy,
            @RequestParam(defaultValue = AppConstants.SORT_DIR, required = false)
            String dir) {

        return ResponseEntity.ok(
                movieService.getAllMoviesWithPaginationAndSorting(
                        pageNumber, pageSize, sortBy, dir
                )
        );
    }

    private MovieDto convertToMovieDto(String movieDtoObj)
            throws JsonProcessingException {

        return objectMapper.readValue(movieDtoObj, MovieDto.class);
    }
}