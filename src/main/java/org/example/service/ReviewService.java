package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.ReviewDTO;
import org.example.entity.Review;
import org.example.mapper.ReviewMapper;
import org.example.repository.BooksRepository;
import org.example.repository.ReviewsRepository;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.example.util.RequestUtil.getId;

public class ReviewService {
    private static final int ROWS_PER_PAGE = 10;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void showReviews(HttpServletRequest request,
                                   HttpServletResponse response)
            throws IOException, ServletException {

        String currentPage = request.getParameter("page");
        int normalizedPage = currentPage == null ? 1 : Integer.parseInt(currentPage);

        List<Review> reviews;
        try {
            reviews = ReviewsRepository.findEntities(normalizedPage - 1, ROWS_PER_PAGE);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        List<ReviewDTO> reviewDTOS = new ArrayList<>(reviews).stream()
                .map(ReviewMapper::toDTO)
                .toList();
        String resultJsonString = OBJECT_MAPPER.writeValueAsString(reviewDTOS);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(resultJsonString);
        out.flush();
    }

    public static void showReview(HttpServletRequest request,
                                  HttpServletResponse response)
            throws IOException, ServletException {

        String id = getId(request);
        Long normalizedId = Long.parseLong(Objects.requireNonNull(id));

        Review review;
        try {
            review = ReviewsRepository.findById(normalizedId)
                    .orElseThrow(() -> new RuntimeException("Review not found"));
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } catch (RuntimeException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        var reviewDTO = ReviewMapper.toDTO(review);
        String resultJsonString = OBJECT_MAPPER.writeValueAsString(reviewDTO);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(resultJsonString);
        out.flush();
    }

    public static void createReview(HttpServletRequest request,
                                    HttpServletResponse response)
            throws IOException, ServletException {

        String bookTitle = request.getParameter("bookTitle");
        String content = request.getParameter("content");

        Review review;

        try {
            var book = BooksRepository.findByTitle(bookTitle)
                    .orElseThrow(() -> new RuntimeException("Book not found"));
            review = new Review(content, book);
            ReviewsRepository.save(review);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } catch (RuntimeException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        var reviewDTO = ReviewMapper.toDTO(review);
        String resultJsonString = OBJECT_MAPPER.writeValueAsString(reviewDTO);
        PrintWriter out = response.getWriter();
        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(resultJsonString);
        out.flush();
    }

    public static void updateReview(HttpServletRequest request,
                                    HttpServletResponse response)
            throws IOException, ServletException {

        String id = getId(request);
        Long normalizedId = Long.parseLong(Objects.requireNonNull(id));

        String content = request.getParameter("content");

        Review review;
        try {
            var book = BooksRepository.findById(normalizedId)
                    .orElseThrow(() -> new RuntimeException("Book not found"));
            review = new Review(content, book);
            ReviewsRepository.update(review, normalizedId);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } catch (RuntimeException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        var reviewDTO = ReviewMapper.toDTO(review);
        String resultJsonString = OBJECT_MAPPER.writeValueAsString(reviewDTO);
        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        out.print(resultJsonString);
        out.flush();
    }

    public static void destroyReview(HttpServletRequest request,
                                HttpServletResponse response)
            throws IOException, ServletException {

        String id = getId(request);
        Long normalizedId = Long.parseLong(Objects.requireNonNull(id));

        try {
            ReviewsRepository.deleteById(normalizedId);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

}
