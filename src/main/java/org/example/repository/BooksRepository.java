package org.example.repository;

import org.example.entity.Book;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BooksRepository extends BaseRepository {
    public static List<Book> findEntities(int page, int rowsPerPage) throws SQLException {
        var offset = page * rowsPerPage;
        var sql = String.format("""
                SELECT * FROM books
                ORDER BY id LIMIT %d OFFSET %d
                """, rowsPerPage, offset);

        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            var resultSet = stmt.executeQuery();
            var books = new ArrayList<Book>();

            while (resultSet.next()) {
                var id = resultSet.getLong("id");
                var title = resultSet.getString("title");
                var author = resultSet.getString("author");
                var genreId = resultSet.getLong("genre_id");
                var createdAt = resultSet.getTimestamp("created_at");

                var genre = GenresRepository.findById(genreId).orElseThrow();
                var book = new Book(title, author, genre);
                book.setId(id);
                book.setCreatedAt(createdAt);
                books.add(book);
            }

            return books;
        }
    }

    public static List<Book> findEntitiesByAuthor(String authorName, int page, int rowsPerPage) throws SQLException {
        var offset = page * rowsPerPage;
        var sql = String.format("""
                SELECT * FROM books
                WHERE author = '%s'
                ORDER BY id LIMIT %d OFFSET %d
                """, authorName, rowsPerPage, offset);

        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            var resultSet = stmt.executeQuery();
            var books = new ArrayList<Book>();

            while (resultSet.next()) {
                var id = resultSet.getLong("id");
                var title = resultSet.getString("title");
                var genreId = resultSet.getLong("genre_id");
                var createdAt = resultSet.getTimestamp("created_at");

                var genre = GenresRepository.findById(genreId).orElseThrow();
                var book = new Book(title, authorName, genre);
                book.setId(id);
                book.setCreatedAt(createdAt);
                books.add(book);
            }

            return books;
        }
    }

    public static List<Book> findEntitiesByGenre(String genreName, int page, int rowsPerPage) throws SQLException {
        var offset = page * rowsPerPage;
        var sql = String.format("""
                SELECT * FROM books
                INNER JOIN genres
                ON books.genre_id = genres.id
                WHERE genres.name = '%s'
                ORDER BY id LIMIT %d OFFSET %d
                """, genreName, rowsPerPage, offset);

        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            var resultSet = stmt.executeQuery();
            var books = new ArrayList<Book>();

            while (resultSet.next()) {
                var id = resultSet.getLong("id");
                var title = resultSet.getString("title");
                var author = resultSet.getString("author");
                var genreId = resultSet.getLong("genre_id");
                var createdAt = resultSet.getTimestamp("created_at");

                var genre = GenresRepository.findById(genreId).orElseThrow();
                var book = new Book(title, author, genre);
                book.setId(id);
                book.setCreatedAt(createdAt);
                books.add(book);
            }

            return books;
        }
    }

    public static Optional<Book> findById(Long id) throws SQLException {
        var sql = "SELECT * FROM books WHERE id = ?";

        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            var resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                var title = resultSet.getString("title");
                var author = resultSet.getString("author");
                var genreId = resultSet.getLong("genre_id");
                var createdAt = resultSet.getTimestamp("created_at");

                var genre = GenresRepository.findById(genreId).orElseThrow();
                var book = new Book(title, author, genre);
                book.setId(id);
                book.setCreatedAt(createdAt);

                return Optional.of(book);
            }

            return Optional.empty();
        }
    }

    public static Optional<Book> findByTitle(String bookTitle) throws SQLException {
        var sql = "SELECT * FROM books WHERE title = ?";

        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bookTitle);
            var resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                var id = resultSet.getLong("id");
                var author = resultSet.getString("author");
                var genreId = resultSet.getLong("genre_id");
                var createdAt = resultSet.getTimestamp("created_at");

                var genre = GenresRepository.findById(genreId).orElseThrow();
                var book = new Book(bookTitle, author, genre);
                book.setId(id);
                book.setCreatedAt(createdAt);

                return Optional.of(book);
            }

            return Optional.empty();
        }
    }

    public static void save(Book book) throws SQLException {
        String sql = "INSERT INTO books (title, author, genre_id, created_at) VALUES (?, ?, ?, ?)";
        var datetime = new Timestamp(System.currentTimeMillis());

        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, book.getTitle());
            preparedStatement.setString(2, book.getAuthor());
            preparedStatement.setLong(3, book.getGenre().getId());
            preparedStatement.setTimestamp(4, datetime);
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();

            if (generatedKeys.next()) {
                book.setId(generatedKeys.getLong(1));
                book.setCreatedAt(datetime);
            } else {
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        }
    }

    public static void update(Book book, Long id) throws SQLException {
        String sql = "UPDATE books SET title = ?, author = ?, genre_id = ? WHERE id = ?";

        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, book.getTitle());
            preparedStatement.setString(2, book.getAuthor());
            preparedStatement.setLong(3, book.getGenre().getId());
            preparedStatement.setLong(4, id);
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();

            if (generatedKeys.next()) {
                book.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        }
    }

    public static void deleteById(Long id) throws SQLException {
        var sql = "DELETE FROM books WHERE id = ?";

        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.execute();
        }
    }

    public static void clear() throws SQLException {
        var sql = "DELETE FROM books";

        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}
