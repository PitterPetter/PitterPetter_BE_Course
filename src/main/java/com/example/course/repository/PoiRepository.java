package com.example.course.repository;

import com.example.course.domain.Poi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PoiRepository extends JpaRepository<Poi, Long> {

    Optional<Poi> findByNameAndLatAndLng(String name, Double lat, Double lng);
}
