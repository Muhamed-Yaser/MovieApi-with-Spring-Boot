package com.netflix.MovieAPI.repositories;

import com.netflix.MovieAPI.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Integer> {
}