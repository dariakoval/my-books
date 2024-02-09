package org.example.servlet;

import org.example.service.ReviewService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.example.util.RequestUtil.getAction;

public class ReviewsServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        String action = getAction(request);

        if (action.equals("list")) {
            ReviewService.showReviews(request, response);
        } else {
            ReviewService.showReview(request, response);
        }
    }

    @Override
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        String action = getAction(request);

        switch (action) {
            case "list" -> ReviewService.createReview(request, response);
            case "edit" -> ReviewService.updateReview(request, response);
            case "delete" -> ReviewService.destroyReview(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
