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
        BooksRepository.deleteById(testBook.getId());
        GenresRepository.deleteById(testGenre.getId());
    }

    @SneakyThrows
    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @SneakyThrows
    @Test
    void testWelcome() {
        var response = Unirest.get(baseUrl).asString();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getBody()).contains("Welcome to online notepad My Books");
    }

    @SneakyThrows
    @Test
    void testShowBooks() {
        var responseWithoutPage = Unirest.get(baseUrl + "/books").asString();
        assertThat(responseWithoutPage.getStatus()).isEqualTo(200);
        assertThatJson(responseWithoutPage.getBody()).isArray();

        var actualBooks = BooksRepository.findEntities(0, 10);
        for (var book: actualBooks) {
            assertThat(responseWithoutPage.getBody()).contains(String.valueOf(book.getId()));
            assertThat(responseWithoutPage.getBody()).contains(book.getTitle());
            assertThat(responseWithoutPage.getBody()).contains(book.getAuthor());
            assertThat(responseWithoutPage.getBody()).contains(book.getGenre().getName());
        }

        var responseWithPage = Unirest.get(baseUrl + "/books?page=2").asString();
        assertThat(responseWithPage.getStatus()).isEqualTo(200);
        assertThatJson(responseWithPage.getBody()).isArray();

        var responseWithAction = Unirest.get(baseUrl + "/books/list").asString();
        assertThat(responseWithAction.getStatus()).isEqualTo(200);
        assertThatJson(responseWithAction.getBody()).isArray();

        var responseWithActionWithPage = Unirest.get(baseUrl + "/books/list?page=2").asString();
        assertThat(responseWithActionWithPage.getStatus()).isEqualTo(200);
        assertThatJson(responseWithActionWithPage.getBody()).isArray();
    }

    @SneakyThrows
    @Test
    void testShowBooksByAuthor() {
        var author = testBook.getAuthor();
        var response = Unirest.get(baseUrl + "/books?author=" + author).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThatJson(response.getBody()).isArray();
        assertThat(response.getBody()).contains(author);
    }

    @SneakyThrows
    @Test
    void testShowBooksByGenre() {
        var genre = testBook.getGenre().getName();
        var response = Unirest.get(baseUrl + "/books?genre=" + genre).asString();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThatJson(response.getBody()).isArray();
        assertThat(response.getBody()).contains(genre);
    }

    @SneakyThrows
    @Test
    void testShowBook() {
        var response = Unirest.get(baseUrl + "/books/" + testBook.getId()).asString();
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
    void testShowBookNotFound() {
        Long id = 999L;
        BooksRepository.deleteById(id);

        var response = Unirest.get(baseUrl + "/books/" + id).asString();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @SneakyThrows
    @Test
    void testCreateBook() {
        var genre = new Genre("New test genre");
        GenresRepository.save(genre);

        var title = "New title";
        var author = "New author";
        var genreName = genre.getName();

        var response = Unirest.post(baseUrl + "/books/list?title=" + title
                + "&author=" + author + "&genreName=" + genreName).asEmpty();
        assertThat(response.getStatus()).isEqualTo(201);

        var actualBook = BooksRepository.findByTitle(title).orElse(null);
        assertThat(actualBook).isNotNull();
        assertThat(actualBook.getTitle()).isEqualTo(title);
        assertThat(actualBook.getAuthor()).isEqualTo(author);
        assertThat(actualBook.getGenre().getName()).isEqualTo(genreName);
    }

    @SneakyThrows
    @Test
    void testCreateBookWithoutUniqueTitle() {
        var response = Unirest.post(baseUrl + "/books/list?title=" + testBook.getTitle()
                + "&author=" + testBook.getAuthor() + "&genreName=" + testGenre.getName()).asEmpty();
        assertThat(response.getStatus()).isEqualTo(500);
    }

    @SneakyThrows
    @Test
    void testUpdateBook() {
        var genre = new Genre("Genre for update");
        GenresRepository.save(genre);

        var title = "Updated title";
        var author = "Updated author";
        var genreName = genre.getName();

        var response = Unirest.post(baseUrl + "/books/" + testBook.getId()
                + "/edit?title=" + title + "&author=" + author + "&genreName=" + genreName).asEmpty();
        assertThat(response.getStatus()).isEqualTo(200);

        var updatedTestBook = BooksRepository.findById(testBook.getId()).orElse(null);
        assertThat(updatedTestBook).isNotNull();
        assertThat(updatedTestBook.getTitle()).isEqualTo(title);
        assertThat(updatedTestBook.getAuthor()).isEqualTo(author);
        assertThat(updatedTestBook.getGenre().getName()).isEqualTo(genreName);
    }

    @SneakyThrows
    @Test
    void testDestroyBook() {
        var response = Unirest.post(baseUrl + "/books/" + testBook.getId() + "/delete").asEmpty();
        assertThat(response.getStatus()).isEqualTo(204);

        var deletedBook = BooksRepository.findById(testBook.getId()).orElse(null);
        assertThat(deletedBook).isNull();

        var reviewRelatedToBook = ReviewsRepository.findById(testBook.getId()).orElse(null);
        assertThat(reviewRelatedToBook).isNull();
    }

    @SneakyThrows
    @Test
    void testShowGenres() {
        var responseWithoutPage = Unirest.get(baseUrl + "/genres").asString();
        assertThat(responseWithoutPage.getStatus()).isEqualTo(200);
        assertThatJson(responseWithoutPage.getBody()).isArray();

        var actualGenres = GenresRepository.findEntities(0, 10);
        for (var genre: actualGenres) {
            assertThat(responseWithoutPage.getBody()).contains(String.valueOf(genre.getId()));
            assertThat(responseWithoutPage.getBody()).contains(genre.getName());
        }

        var responseWithPage = Unirest.get(baseUrl + "/genres?page=2").asString();
        assertThat(responseWithPage.getStatus()).isEqualTo(200);
        assertThatJson(responseWithPage.getBody()).isArray();

        var responseWithAction = Unirest.get(baseUrl + "/genres/list").asString();
        assertThat(responseWithAction.getStatus()).isEqualTo(200);
        assertThatJson(responseWithAction.getBody()).isArray();

        var responseWithActionWithPage = Unirest.get(baseUrl + "/genres/list?page=2").asString();
        assertThat(responseWithActionWithPage.getStatus()).isEqualTo(200);
        assertThatJson(responseWithActionWithPage.getBody()).isArray();
    }

    @SneakyThrows
    @Test
    void testShowGenre() {
        var response = Unirest.get(baseUrl + "/genres/" + testGenre.getId()).asString();
        assertThat(response.getStatus()).isEqualTo(200);

        assertThatJson(response.getBody()).and(
                v -> v.node("id").isEqualTo(testGenre.getId()),
                v -> v.node("name").isEqualTo(testGenre.getName())
        );
    }

    @SneakyThrows
    @Test
    void testShowGenreNotFound() {
        Long id = 999L;
        GenresRepository.deleteById(id);

        var response = Unirest.get(baseUrl + "/genres/" + id).asString();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @SneakyThrows
    @Test
    void testCreateGenre() {
        var name = "New name";
        var response = Unirest.post(baseUrl + "/genres/list?name=" + name).asEmpty();
        assertThat(response.getStatus()).isEqualTo(201);

        var actualGenre = GenresRepository.findByName(name).orElse(null);
        assertThat(actualGenre).isNotNull();
        assertThat(actualGenre.getName()).isEqualTo(name);
    }

    @SneakyThrows
    @Test
    void testCreateGenreWithoutUniqueName() {
        var response = Unirest.post(baseUrl + "/genres/list?name=" + testGenre.getName()).asEmpty();
        assertThat(response.getStatus()).isEqualTo(500);
    }

    @SneakyThrows
    @Test
    void testUpdateGenre() {
        var name = "Updated name";
        var response = Unirest.post(baseUrl + "/genres/" + testGenre.getId() + "/edit?name=" + name).asEmpty();
        assertThat(response.getStatus()).isEqualTo(200);

        var updatedTestGenre = GenresRepository.findById(testGenre.getId()).orElse(null);
        assertThat(updatedTestGenre).isNotNull();
        assertThat(updatedTestGenre.getName()).isEqualTo(name);
    }

    @SneakyThrows
    @Test
    void testDestroyGenreRelatedToBook() {
        var response = Unirest.post(baseUrl + "/genres/" + testGenre.getId() + "/delete").asEmpty();
        assertThat(response.getStatus()).isEqualTo(500);
    }

    @SneakyThrows
    @Test
    void testDestroyGenreNotRelated() {
        var genre = new Genre("Name");
        GenresRepository.save(genre);

        var response = Unirest.post(baseUrl + "/genres/" + genre.getId() + "/delete").asEmpty();
        assertThat(response.getStatus()).isEqualTo(204);

        var deletedGenre = GenresRepository.findById(genre.getId()).orElse(null);
        assertThat(deletedGenre).isNull();
    }

    @SneakyThrows
    @Test
    void testShowReviews() {
        var responseWithoutPage = Unirest.get(baseUrl + "/reviews").asString();
        assertThat(responseWithoutPage.getStatus()).isEqualTo(200);
        assertThatJson(responseWithoutPage.getBody()).isArray();

        var actualReviews = ReviewsRepository.findEntities(0, 10);
        for (var review: actualReviews) {
            assertThat(responseWithoutPage.getBody()).contains(String.valueOf(review.getId()));
            assertThat(responseWithoutPage.getBody()).contains(review.getBook().getTitle());
            assertThat(responseWithoutPage.getBody()).contains(review.getBook().getAuthor());
            assertThat(responseWithoutPage.getBody()).contains(review.getContent());
        }

        var responseWithPage = Unirest.get(baseUrl + "/reviews?page=2").asString();
        assertThat(responseWithPage.getStatus()).isEqualTo(200);
        assertThatJson(responseWithPage.getBody()).isArray();

        var responseWithAction = Unirest.get(baseUrl + "/reviews/list").asString();
        assertThat(responseWithAction.getStatus()).isEqualTo(200);
        assertThatJson(responseWithAction.getBody()).isArray();

        var responseWithActionWithPage = Unirest.get(baseUrl + "/reviews/list?page=2").asString();
        assertThat(responseWithActionWithPage.getStatus()).isEqualTo(200);
        assertThatJson(responseWithActionWithPage.getBody()).isArray();
    }

    @SneakyThrows
    @Test
    void testShowReview() {
        var response = Unirest.get(baseUrl + "/reviews/" + testReview.getId()).asString();
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
    void testUpdateReview() {
        var content = "Updated content";
        var response = Unirest.post(baseUrl + "/reviews/" + testReview.getId() + "/edit?content=" + content)
                .asEmpty();
        assertThat(response.getStatus()).isEqualTo(200);

        var updatedTestReview = ReviewsRepository.findById(testReview.getId()).orElse(null);
        assertThat(updatedTestReview).isNotNull();
        assertThat(updatedTestReview.getContent()).isEqualTo(content);
    }
}
