package com.rbdip.bookstore.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rbdip.bookstore.review.api.PurchaseHistory;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReviewServiceTest {
    private final ReviewRepository repository = mock(ReviewRepository.class);
    private final PurchaseHistory purchases = mock(PurchaseHistory.class);
    private final ReviewService service = new ReviewService(repository, purchases);

    @Test
    void recordsVerificationFromThePublicPort() {
        when(purchases.hasPurchased(1L, "Anna Smith")).thenReturn(true);
        when(repository.save(any(Review.class))).thenAnswer(call -> call.getArgument(0));
        Review review = service.addReview(1L, "Anna Smith", 5, "Good");
        assertThat(review.isVerifiedPurchase()).isTrue();
        assertThat(review.getProductId()).isEqualTo(1L);
        assertThat(review.getAuthorName()).isEqualTo("Anna Smith");
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getComment()).isEqualTo("Good");
        verify(purchases).hasPurchased(1L, "Anna Smith");
    }

    @Test
    void anonymousReviewRemainsUnverifiedAndCanBeListed() {
        when(repository.save(any(Review.class))).thenAnswer(call -> call.getArgument(0));
        Review review = service.addReview(2L, null, 3, null);
        when(repository.findByProductId(2L)).thenReturn(List.of(review));
        assertThat(review.getAuthorName()).isEqualTo("anonymous");
        assertThat(review.isVerifiedPurchase()).isFalse();
        assertThat(service.listReviews(2L)).containsExactly(review);
    }
}
