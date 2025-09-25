package com.example.course.repository;

import com.example.course.domain.PoiReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PoiReviewRepository extends JpaRepository<PoiReview, Long> {

    Optional<PoiReview> findByPoi_IdAndUserId(Long poiId, Long userId);

    @Query("select coalesce(avg(r.rating), 0) as avg, count(r) as count " +
            "from PoiReview r where r.poi.id = :poiId")
    ReviewAggregate findAverageAndCountByPoiId(@Param("poiId") Long poiId);

    interface ReviewAggregate {
        Double getAvg();
        Long getCount();
    }
}
