package org.example.component;

import org.example.entity.Book;
import org.example.entity.Genre;
import org.example.repository.BooksRepository;
import org.example.repository.GenresRepository;

import java.sql.SQLException;
import java.util.List;

public class DataInitializer {
    private static void addDefaultGenres() {
        List<String> defaultGenres = List.of(
                "Popular science", "Historical fiction", "Mystery", "Young adult", "Adventure",
                "Graphic novel", "History", "Religious", "Science fiction", "Fantasy", "Memoir",
                "Dystopian", "Short story", "Magical Realism", "Humor", "Paranormal", "Horror",
                "Romance", "Contemporary", "Thriller", "Classics", "Suspense", "Poetry", "Satire"
        );

        defaultGenres.forEach(genreName -> {
            Genre genre = new Genre(genreName);

            try {
                GenresRepository.save(genre);
            } catch (SQLException e) {
                System.err.println(e.getErrorCode());
            }
        });
    }

    private static void addDefaultBooks() {
        List<List<String>> data = List.of(
                List.of("Surely You're Joking, Mr. Feynman!", "Richard Phillips Feynman", "Humor"),
                List.of("Harry Potter and the Sorcerer's Stone", "Joanne Rowling", "Fantasy"),
                List.of("Code: The Hidden Language of Computer Hardware and Software",
                        "Charles Petzold", "Popular science")
        );

        data.forEach(list -> {
            var title = list.get(0);
            var author = list.get(1);
            var genreName = list.get(2);

            try {
                var genre = GenresRepository.findByName(genreName)
                        .orElseThrow(() -> new RuntimeException("Genre not found"));
                var book = new Book(title, author, genre);
                BooksRepository.save(book);
            } catch (SQLException e) {
                System.err.println(e.getErrorCode());
            }
        });
    }

    public static void run() {
        addDefaultGenres();
        addDefaultBooks();
    }
}
