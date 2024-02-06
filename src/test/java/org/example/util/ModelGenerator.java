package org.example.util;

import org.example.entity.Book;
import org.example.entity.Genre;
import org.example.entity.Review;
import org.instancio.Instancio;
import org.instancio.Model;
import org.instancio.Select;

public class ModelGenerator {
    public static Model<Genre> genreModel;
    public static Model<Book> bookModel;
    public static Model<Review> reviewModel;

    public static void init() {
        genreModel = Instancio.of(Genre.class)
                .ignore(Select.field(Genre::getId))
                .supply(Select.field(Genre::getName), () -> "Test Genre")
                .toModel();

        bookModel = Instancio.of(Book.class)
                .ignore(Select.field(Book::getId))
                .supply(Select.field(Book::getTitle), () -> "Test Title")
                .supply(Select.field(Book::getAuthor), () -> "Test Author")
                .toModel();

        reviewModel = Instancio.of(Review.class)
                .ignore(Select.field(Review::getId))
                .supply(Select.field(Review::getContent), () -> "Test Review!")
                .toModel();
    }
}
