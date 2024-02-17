package org.example.repository;

import org.example.entity.Genre;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GenresRepository extends BaseRepository { // тут замечания такие же как в другом репозитории
    public static List<Genre> findEntities(int page, int rowsPerPage) throws SQLException {
        var offset = page * rowsPerPage;
        var sql = String.format("""
                SELECT * FROM genres
                ORDER BY id LIMIT %d OFFSET %d
                """, rowsPerPage, offset);

        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            var resultSet = stmt.executeQuery();
            var genres = new ArrayList<Genre>();

            while (resultSet.next()) {
                var id = resultSet.getLong("id");
                var name = resultSet.getString("name");
                var createdAt = resultSet.getTimestamp("created_at");
                var genre = new Genre(name);
                genre.setId(id);
                genre.setCreatedAt(createdAt);
                genres.add(genre);
            }

            return genres;
        }
    }

    public static Optional<Genre> findById(Long id) throws SQLException {
        var sql = "SELECT * FROM genres WHERE id = ?";

        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            var resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                var name = resultSet.getString("name");
                var createdAt = resultSet.getTimestamp("created_at");
                var genre = new Genre(name);
                genre.setId(id);
                genre.setCreatedAt(createdAt);

                return Optional.of(genre);
            }

            return Optional.empty();
        }
    }

    public static Optional<Genre> findByName(String genreName) throws SQLException {
        var sql = "SELECT * FROM genres WHERE name = ?";

        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, genreName);
            var resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                var id = resultSet.getLong("id");
                var createdAt = resultSet.getTimestamp("created_at");
                var genre = new Genre(genreName);
                genre.setId(id);
                genre.setCreatedAt(createdAt);

                return Optional.of(genre);
            }

            return Optional.empty();
        }
    }

    public static void save(Genre genre) throws SQLException {
        String sql = "INSERT INTO genres (name, created_at) VALUES (?, ?)";
        var datetime = new Timestamp(System.currentTimeMillis());

        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, genre.getName());
            preparedStatement.setTimestamp(2, datetime);
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();

            if (generatedKeys.next()) {
                genre.setId(generatedKeys.getLong(1));
                genre.setCreatedAt(datetime);
            } else {
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        }
    }

    public static void update(Genre genre, Long id) throws SQLException {
        String sql = "UPDATE genres SET name = ? WHERE id = ?";

        try (var conn = dataSource.getConnection();
             var preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, genre.getName());
            preparedStatement.setLong(2, id);
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();

            if (generatedKeys.next()) {
                genre.setId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("DB have not returned an id after saving an entity");
            }
        }
    }

    public static void deleteById(Long id) throws SQLException {
        var sql = "DELETE FROM genres WHERE id = ?";

        try (var conn = dataSource.getConnection();
             var stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.execute();
        }
    }

    public static void clear() throws SQLException {
        var sql = "DELETE FROM genres";

        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }
}
