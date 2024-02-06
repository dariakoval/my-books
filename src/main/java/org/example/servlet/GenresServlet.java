package org.example.servlet;

import org.example.service.GenreService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.example.util.RequestUtil.getAction;

public class GenresServlet extends HttpServlet {
    @Override
    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws IOException, ServletException {

        String action = getAction(request);

        if (action.equals("list")) {
            GenreService.showGenres(request, response);
        } else {
            GenreService.showGenre(request, response);
        }
    }

    @Override
    public void doPost(HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {

        String action = getAction(request);

        switch (action) {
            case "list" -> GenreService.createGenre(request, response);
            case "edit" -> GenreService.updateGenre(request, response);
            case "delete" -> GenreService.destroyGenre(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
