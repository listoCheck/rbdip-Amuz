package com.rbdip.bookstore.review;

import com.rbdip.bookstore.review.api.PurchaseHistory;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final PurchaseHistory purchaseHistory;

    public ReviewService(ReviewRepository reviewRepository, PurchaseHistory purchaseHistory) {
        this.reviewRepository = reviewRepository;
        this.purchaseHistory = purchaseHistory;
    }

    @Transactional
    public Review addReview(Long productId, String authorName, Integer rating, String comment) {
        String author = authorName == null ? "anonymous" : authorName;
        boolean verified = purchaseHistory.hasPurchased(productId, author);
        return reviewRepository.save(new Review(productId, author, rating, comment, verified));
    }

    public List<Review> listReviews(Long productId) {
        return reviewRepository.findByProductId(productId);
    }
}
