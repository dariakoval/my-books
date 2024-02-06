package org.example.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import org.example.service.BookService;

import static org.example.util.RequestUtil.getAction;

public class BooksServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        String action = getAction(request);

        if (action.equals("list")) {
            BookService.showBooks(request, response);
        } else {
            BookService.showBook(request, response);
        }
    }

    @Override
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        String action = getAction(request);
        switch (action) {
            case "list" -> BookService.createBook(request, response);
            case "edit" -> BookService.updateBook(request, response);
            case "delete" -> BookService.destroyBook(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
