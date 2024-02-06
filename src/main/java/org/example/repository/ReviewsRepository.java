package org.example.repository;

import org.example.entity.Review;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReviewsRepository extends BaseRepository {
    public static List<Review> findEntities(int page, int rowsPerPage) throws SQLException {
        var offset = page * rowsPerPage;
        var sql = String.format("""
                SELECT * FROM reviews
                ORDER BY book_id LIMIT %d OFFSET %d
                """, rowsPerPage, offset);

        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            var resultSet = stmt.executeQuery();
            var reviews = new ArrayList<Review>();

            while (resultSet.next()) {
                var bookId = resultSet.getLong("book_id");
                var content = resultSet.getString("content");
                var createdAt = resultSet.getTimestamp("created_at");

                var book = BooksRepository.findById(bookId).orElseThrow();
                var review = new Review(content, book);
                review.setId(bookId);
                review.setCreatedAt(createdAt);
                reviews.add(review);
            }

            return reviews;
        }
    }

    public static Optional<Review> findById(Long id) throws SQLException {
        var sql = "SELECT * FROM reviews WHERE book_id = ?";

        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            var resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                var content = resultSet.getString("content");
                var createdAt = resultSet.getTimestamp("created_at");

                var book = BooksRepository.findById(id).orElseThrow();
                var review = new Review(content, book);
                review.setId(id);
                review.setCreatedAt(createdAt);

                return Optional.of(review);
            }

            return Optional.empty();
        }
    }

    public static void save(Review review) throws SQLException {
        String sql = "INSERT INTO reviews (book_id, content, created_at) VALUES (?, ?, ?)";
        var datetime = new Timestamp(System.currentTimeMillis());

        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setLong(1, review.getBook().getId());
            preparedStatement.setString(2, review.getContent());
            preparedStatement.setTimestamp(3, datetime);
            preparedStatement.executeUpdate();

            review.setId(review.getBook().getId());
            review.setCreatedAt(datetime);
        }
    }

    public static void update(Review review, Long id) throws SQLException {
        String sql = "UPDATE reviews SET content = ? WHERE book_id = ?";

        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, review.getContent());
            preparedStatement.setLong(2, id);
            preparedStatement.executeUpdate();

            review.setId(review.getBook().getId());
        }
    }
}
