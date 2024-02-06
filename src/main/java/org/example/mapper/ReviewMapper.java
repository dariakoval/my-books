package org.example.mapper;

import org.example.dto.ReviewDTO;
import org.example.entity.Review;

public class ReviewMapper {
    public static ReviewDTO toDTO(Review review) {
        var reviewDTO = new ReviewDTO();
        reviewDTO.setId(review.getId());
        reviewDTO.setBookTitle(review.getBook().getTitle());
        reviewDTO.setBookAuthor(review.getBook().getAuthor());
        reviewDTO.setContent(review.getContent());

        return reviewDTO;
    }
}
