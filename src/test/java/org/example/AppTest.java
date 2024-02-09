package org.example;

import kong.unirest.Unirest;
import lombok.SneakyThrows;
import org.apache.catalina.startup.Tomcat;
import org.example.entity.Book;
import org.example.entity.Genre;
import org.example.entity.Review;
import org.example.repository.BooksRepository;
import org.example.repository.GenresRepository;
import org.example.repository.ReviewsRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.example.util.ModelGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;


public class AppTest {
    private static Tomcat app;
    private static String baseUrl;
    private Genre testGenre;
    private Book testBook;
    private Review testReview;

    private static void prepareData() {
        ModelGenerator.init();
    }

    @SneakyThrows
    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start();
        var port = app.getConnector().getLocalPort();
        baseUrl = "http://localhost:" + port;
    }

    @SneakyThrows
    @BeforeEach
    public void setUp() {
        prepareData();

        testGenre = Instancio.of(ModelGenerator.genreModel).create();
        GenresRepository.save(testGenre);

        testBook = Instancio.of(ModelGenerator.bookModel).create();
        testBook.setGenre(testGenre);
        BooksRepository.save(testBook);

        testReview = Instancio.of(ModelGenerator.reviewModel).create();
        testReview.setBook(testBook);
        ReviewsRepository.save(testReview);
    }

    @SneakyThrows
    @AfterEach
    public void cleanUp() {
        BooksRepository.clear();
        GenresRepository.clear();
    }

    @SneakyThrows
    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @SneakyThrows
    @Test
    void handleGetWelcomeWhenRequestIsExecutedThenReturnsValidResponseEntity() {
        var requestUrl = baseUrl;

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("Welcome to online notepad My Books");
    }

    @SneakyThrows
    @Test
    void handleGetAllBooksWhenRequestWithoutQueryParamThenReturnsValidResponseEntity() {
        for (int i = 1; i <= 12; i++) {
            BooksRepository.save(new Book(i + "Title", i + "Author", testGenre));
        }
        var requestUrl = baseUrl + "/books";

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThatJson(response.getBody()).isArray();
        var actualBooks = BooksRepository.findEntities(0, 10);
        for (var book: actualBooks) {
            assertThat(response.getBody()).contains(String.valueOf(book.getId()));
            assertThat(response.getBody()).contains(book.getTitle());
            assertThat(response.getBody()).contains(book.getAuthor());
            assertThat(response.getBody()).contains(book.getGenre().getName());
        }
    }

    @SneakyThrows
    @Test
    void handleGetAllBooksWhenRequestWithQueryParamPageThenReturnsValidResponseEntity() {
        for (int i = 1; i <= 24; i++) {
            BooksRepository.save(new Book(i + "Title", i + "Author", testGenre));
        }
        var page = 2;
        var requestUrl = baseUrl + "/books?page=" + page;

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThatJson(response.getBody()).isArray();
        var actualBooks = BooksRepository.findEntities(page - 1, 10);
        for (var book: actualBooks) {
            assertThat(response.getBody()).contains(String.valueOf(book.getId()));
            assertThat(response.getBody()).contains(book.getTitle());
            assertThat(response.getBody()).contains(book.getAuthor());
            assertThat(response.getBody()).contains(book.getGenre().getName());
        }
    }

    @SneakyThrows
    @Test
    void handleGetAllBooksWhenRequestWithPathParamListThenReturnsValidResponseEntity() {
        var requestUrl = baseUrl + "/books/list";

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThatJson(response.getBody()).isArray();
    }

    @SneakyThrows
    @Test
    void handleGetAllBooksWhenRequestWithQueryParamAuthorThenReturnsValidResponseEntity() {
        var author = testBook.getAuthor();
        var requestUrl = baseUrl + "/books?author=" + author;

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThatJson(response.getBody()).isArray();
        assertThat(response.getBody()).contains(author);
    }

    @SneakyThrows
    @Test
    void handleGetAllBooksWhenRequestWithQueryParamGenreThenReturnsValidResponseEntity() {
        var genre = testBook.getGenre().getName();
        var requestUrl = baseUrl + "/books?genre=" + genre;

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThatJson(response.getBody()).isArray();
        assertThat(response.getBody()).contains(genre);
    }

    @SneakyThrows
    @Test
    void handleGetExistingBookWhenRequestWithPathParamExistingBookIdThenReturnsValidResponseEntity() {
        var requestUrl = baseUrl + "/books/" + testBook.getId();

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThatJson(response.getBody()).and(
                v -> v.node("id").isEqualTo(testBook.getId()),
                v -> v.node("title").isEqualTo(testBook.getTitle()),
                v -> v.node("author").isEqualTo(testBook.getAuthor()),
                v -> v.node("genreName").isEqualTo(testBook.getGenre().getName())
        );
    }

    @SneakyThrows
    @Test
    void handleGetNonExistentBookWhenRequestWithPathParamNonExistentBookIdThenReturnsNotFoundResponse() {
        Long id = 999L;
        BooksRepository.deleteById(id);
        var requestUrl = baseUrl + "/books/" + id;

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @SneakyThrows
    @Test
    void handleCreateNewBookWhenRequestWithUniqueQueryParamTitleThenReturnsValidResponseEntity() {
        var title = "New title";
        var author = "New author";
        var genreName = testGenre.getName();
        var requestUrl = baseUrl + "/books/list?title=" + title + "&author=" + author + "&genreName=" + genreName;

        var response = Unirest.post(requestUrl).asEmpty();

        assertThat(response.getStatus()).isEqualTo(201);
        var actualBook = BooksRepository.findByTitle(title).orElse(null);
        assertThat(actualBook).isNotNull();
        assertThat(actualBook.getTitle()).isEqualTo(title);
        assertThat(actualBook.getAuthor()).isEqualTo(author);
        assertThat(actualBook.getGenre().getName()).isEqualTo(genreName);
    }

    @SneakyThrows
    @Test
    void handleCreateNewBookWhenRequestWithoutUniqueQueryParamTitleThenReturnsInternalServerErrorResponse() {
        var title = testBook.getTitle();
        var author = "New author";
        var genreName = testGenre.getName();
        var requestUrl = baseUrl + "/books/list?title=" + title + "&author=" + author + "&genreName=" + genreName;

        var response = Unirest.post(requestUrl).asEmpty();

        assertThat(response.getStatus()).isEqualTo(500);
    }

    @SneakyThrows
    @Test
    void handleUpdateBookWhenPayloadIsValidThenReturnsValidResponseEntity() {
        var title = "Updated title";
        var author = "Updated author";
        var genreName = "Genre for update";
        GenresRepository.save(new Genre(genreName));
        var requestUrl = baseUrl + "/books/" + testBook.getId()
                + "/edit?title=" + title + "&author=" + author + "&genreName=" + genreName;

        var response = Unirest.post(requestUrl).asEmpty();

        assertThat(response.getStatus()).isEqualTo(200);
        var updatedTestBook = BooksRepository.findById(testBook.getId()).orElse(null);
        assertThat(updatedTestBook).isNotNull();
        assertThat(updatedTestBook.getTitle()).isEqualTo(title);
        assertThat(updatedTestBook.getAuthor()).isEqualTo(author);
        assertThat(updatedTestBook.getGenre().getName()).isEqualTo(genreName);
    }

    @SneakyThrows
    @Test
    void handleDestroyBookWhenRequestWithPathParamExistingBookIdThenReturnsValidResponseEntity() {
        var id = testBook.getId();
        var requestUrl = baseUrl + "/books/" + id + "/delete";

        var response = Unirest.post(requestUrl).asEmpty();

        assertThat(response.getStatus()).isEqualTo(204);
        var deletedBook = BooksRepository.findById(testBook.getId()).orElse(null);
        var reviewRelatedToBook = ReviewsRepository.findById(testBook.getId()).orElse(null);
        assertThat(deletedBook).isNull();
        assertThat(reviewRelatedToBook).isNull();
    }

    @SneakyThrows
    @Test
    void handleGetAllGenresWhenRequestWithoutQueryParamThenReturnsValidResponseEntity() {
        for (int i = 1; i <= 12; i++) {
            GenresRepository.save(new Genre(i + "Genre name"));
        }
        var requestUrl = baseUrl + "/genres";

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThatJson(response.getBody()).isArray();
        var actualGenres = GenresRepository.findEntities(0, 10);
        for (var genre: actualGenres) {
            assertThat(response.getBody()).contains(String.valueOf(genre.getId()));
            assertThat(response.getBody()).contains(genre.getName());
        }
    }

    @SneakyThrows
    @Test
    void handleGetAllGenresWhenRequestWithQueryParamPageThenReturnsValidResponseEntity() {
        for (int i = 1; i <= 24; i++) {
            GenresRepository.save(new Genre(i + "Genre name"));
        }
        var page = 2;
        var requestUrl = baseUrl + "/genres?page=" + page;

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThatJson(response.getBody()).isArray();
        var actualGenres = GenresRepository.findEntities(page - 1, 10);
        for (var genre: actualGenres) {
            assertThat(response.getBody()).contains(String.valueOf(genre.getId()));
            assertThat(response.getBody()).contains(genre.getName());
        }
    }

    @SneakyThrows
    @Test
    void handleGetAllGenresWhenRequestWithPathParamListThenReturnsValidResponseEntity() {
        var requestUrl = baseUrl + "/genres/list";

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThatJson(response.getBody()).isArray();
    }

    @SneakyThrows
    @Test
    void handleGetExistingGenreWhenRequestWithPathParamExistingGenreIdThenReturnsValidResponseEntity() {
        var requestUrl = baseUrl + "/genres/" + testGenre.getId();

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThatJson(response.getBody()).and(
                v -> v.node("id").isEqualTo(testGenre.getId()),
                v -> v.node("name").isEqualTo(testGenre.getName())
        );
    }


    @SneakyThrows
    @Test
    void handleGetNonExistentGenreWhenRequestWithPathParamNonExistentGenreIdThenReturnsNotFoundResponse() {
        Long id = 999L;
        GenresRepository.deleteById(id);
        var requestUrl = baseUrl + "/genres/" + id;

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @SneakyThrows
    @Test
    void handleCreateNewGenreWhenRequestWithUniqueQueryParamNameThenReturnsValidResponseEntity() {
        var name = "New name";
        var requestUrl = baseUrl + "/genres/list?name=" + name;

        var response = Unirest.post(requestUrl).asEmpty();

        assertThat(response.getStatus()).isEqualTo(201);
        var actualGenre = GenresRepository.findByName(name).orElse(null);
        assertThat(actualGenre).isNotNull();
        assertThat(actualGenre.getName()).isEqualTo(name);
    }

    @SneakyThrows
    @Test
    void handleCreateNewGenreWhenRequestWithoutUniqueQueryParamNameThenReturnsInternalServerErrorResponse() {
        var name = testGenre.getName();
        var requestUrl = baseUrl + "/genres/list?name=" + name;

        var response = Unirest.post(requestUrl).asEmpty();

        assertThat(response.getStatus()).isEqualTo(500);
    }

    @SneakyThrows
    @Test
    void handleUpdateGenreWhenPayloadIsValidThenReturnsValidResponseEntity() {
        var name = "Updated name";
        var requestUrl = baseUrl + "/genres/" + testGenre.getId() + "/edit?name=" + name;

        var response = Unirest.post(requestUrl).asEmpty();

        assertThat(response.getStatus()).isEqualTo(200);
        var updatedTestGenre = GenresRepository.findById(testGenre.getId()).orElse(null);
        assertThat(updatedTestGenre).isNotNull();
        assertThat(updatedTestGenre.getName()).isEqualTo(name);
    }

    @SneakyThrows
    @Test
    void handleDestroyGenreWhenRequestWithPathParamGenreIdRelatedToBookThenReturnsInternalServerErrorResponse() {
        var id = testGenre.getId();
        var requestUrl = baseUrl + "/genres/" + id + "/delete";

        var response = Unirest.post(requestUrl).asEmpty();

        assertThat(response.getStatus()).isEqualTo(500);
    }

    @SneakyThrows
    @Test
    void handleDestroyGenreWhenRequestWithPathParamGenreIdNotRelatedToBookThenReturnsValidResponseEntity() {
        var genre = new Genre("Name");
        GenresRepository.save(genre);
        var requestUrl = baseUrl + "/genres/" + genre.getId() + "/delete";

        var response = Unirest.post(requestUrl).asEmpty();

        assertThat(response.getStatus()).isEqualTo(204);
        var deletedGenre = GenresRepository.findById(genre.getId()).orElse(null);
        assertThat(deletedGenre).isNull();
    }

    @SneakyThrows
    @Test
    void handleGetAllReviewsWhenRequestWithoutQueryParamThenReturnsValidResponseEntity() {
        for (int i = 1; i <= 12; i++) {
            var book = new Book(i + "Title", i + "Author", testGenre);
            BooksRepository.save(book);
            ReviewsRepository.save(new Review(i + "Content", book));
        }
        var requestUrl = baseUrl + "/reviews";

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThatJson(response.getBody()).isArray();
        var actualReviews = ReviewsRepository.findEntities(0, 10);
        for (var review: actualReviews) {
            assertThat(response.getBody()).contains(String.valueOf(review.getId()));
            assertThat(response.getBody()).contains(review.getBook().getTitle());
            assertThat(response.getBody()).contains(review.getBook().getAuthor());
            assertThat(response.getBody()).contains(review.getContent());
        }
    }

    @SneakyThrows
    @Test
    void handleGetAllReviewsWhenRequestWithQueryParamPageThenReturnsValidResponseEntity() {
        for (int i = 1; i <= 24; i++) {
            var book = new Book(i + "Title", i + "Author", testGenre);
            BooksRepository.save(book);
            ReviewsRepository.save(new Review(i + "Content", book));
        }
        var page = 2;
        var requestUrl = baseUrl + "/reviews?page=" + page;

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThatJson(response.getBody()).isArray();
        var actualReviews = ReviewsRepository.findEntities(page - 1, 10);
        for (var review: actualReviews) {
            assertThat(response.getBody()).contains(String.valueOf(review.getId()));
            assertThat(response.getBody()).contains(review.getBook().getTitle());
            assertThat(response.getBody()).contains(review.getBook().getAuthor());
            assertThat(response.getBody()).contains(review.getContent());
        }
    }

    @SneakyThrows
    @Test
    void handleGetAllReviewsWhenRequestWithPathParamListThenReturnsValidResponseEntity() {
        var requestUrl = baseUrl + "/reviews/list";

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThatJson(response.getBody()).isArray();
    }

    @SneakyThrows
    @Test
    void handleGetExistingReviewWhenRequestWithPathParamExistingReviewIdThenReturnsValidResponseEntity() {
        var requestUrl = baseUrl + "/reviews/" + testReview.getId();

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThatJson(response.getBody()).and(
                v -> v.node("id").isEqualTo(testReview.getId()),
                v -> v.node("bookTitle").isEqualTo(testReview.getBook().getTitle()),
                v -> v.node("bookAuthor").isEqualTo(testReview.getBook().getAuthor()),
                v -> v.node("content").isEqualTo(testReview.getContent())
        );
    }

    @SneakyThrows
    @Test
    void handleGetNonExistentReviewWhenRequestWithPathParamNonExistentReviewIdThenReturnsNotFoundResponse() {
        Long id = 999L;
        BooksRepository.deleteById(id);
        var requestUrl = baseUrl + "/reviews/" + id;

        var response = Unirest.get(requestUrl).asString();

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @SneakyThrows
    @Test
    void handleCreateNewReviewWhenPayloadIsValidThenReturnsValidResponseEntity() {
        var book = new Book("Title to create review", "Author to create review", testGenre);
        BooksRepository.save(book);
        var content = "Content to create review";
        var requestUrl = baseUrl + "/reviews/list?bookTitle=" + book.getTitle() + "&content=" + content;

        var response = Unirest.post(requestUrl).asEmpty();

        assertThat(response.getStatus()).isEqualTo(201);
        var actualReview = ReviewsRepository.findById(book.getId()).orElse(null);
        assertThat(actualReview).isNotNull();
        assertThat(actualReview.getBook().getTitle()).isEqualTo(book.getTitle());
        assertThat(actualReview.getBook().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(actualReview.getContent()).isEqualTo(content);
    }

    @SneakyThrows
    @Test
    void handleUpdateReviewWhenPayloadIsValidThenReturnsValidResponseEntity() {
        var content = "Updated content";
        var requestUrl = baseUrl + "/reviews/" + testReview.getId() + "/edit?content=" + content;

        var response = Unirest.post(requestUrl).asEmpty();

        assertThat(response.getStatus()).isEqualTo(200);
        var updatedTestReview = ReviewsRepository.findById(testReview.getId()).orElse(null);
        assertThat(updatedTestReview).isNotNull();
        assertThat(updatedTestReview.getContent()).isEqualTo(content);
    }
}
