package com.example.course.service;

import com.example.course.domain.Poi;
import com.example.course.domain.PoiReview;
import com.example.course.repository.PoiRepository;
import com.example.course.repository.PoiReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PoiReviewService {

    private final PoiRepository poiRepository;
    private final PoiReviewRepository poiReviewRepository;

    public PoiReviewService(PoiRepository poiRepository, PoiReviewRepository poiReviewRepository) {
        this.poiRepository = poiRepository;
        this.poiReviewRepository = poiReviewRepository;
    }

    public PoiReview upsertReview(long userId, long poiId, int rating) {
        validateRating(rating);
        PoiReview existing = poiReviewRepository.findByPoi_IdAndUserId(poiId, userId).orElse(null);
        if (existing != null) {
            existing.setRating(rating);
            return existing;
        }

        Poi poi = poiRepository.findById(poiId)
                .orElseThrow(() -> new EntityNotFoundException("Poi not found for id: " + poiId));

        PoiReview review = new PoiReview();
        review.setPoi(poi);
        review.setUserId(userId);
        review.setRating(rating);
        return poiReviewRepository.save(review);
    }

    public List<PoiReview> upsertReviews(long userId, List<ReviewCommand> commands) {
        List<PoiReview> results = new ArrayList<>(commands.size());
        for (ReviewCommand command : commands) {
            PoiReview review = upsertReview(userId, command.poiId(), command.rating());
            results.add(review);
        }
        return results;
    }

    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }
    }

    public record ReviewCommand(long poiId, int rating) {
    }
}
