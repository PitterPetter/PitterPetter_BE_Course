package com.example.course.repository;

import com.example.course.domain.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, String> {

    @Query("""
            select distinct c from Course c
            left join fetch c.poiSets ps
            left join fetch ps.poi
            where c.id = :courseId
            """)
    Optional<Course> findByIdWithPoiSets(@Param("courseId") String courseId);

    @Query("""
            select distinct c from Course c
            left join fetch c.poiSets ps
            left join fetch ps.poi
            where c.coupleId = :coupleId
            order by c.createdAt desc
            """)
    List<Course> findAllByCoupleIdWithPoiSets(@Param("coupleId") String coupleId);

    Optional<Course> findByIdAndCoupleId(String id, String coupleId);

    long deleteByIdAndCoupleId(String id, String coupleId);
}
