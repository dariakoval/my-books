package org.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.example.component.DataInitializer;
import org.example.repository.BaseRepository;
import org.example.servlet.BooksServlet;
import org.example.servlet.GenresServlet;
import org.example.servlet.ReviewsServlet;
import org.example.servlet.WelcomeServlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;

@Slf4j
public class App {
    private static String getUser() {
        return System.getenv("USER");
    }

    private static String getPassword() {
        return System.getenv("PASSWORD");
    }

    private static String getDefaultDatabaseUrl() {
        return "jdbc:postgresql://localhost:5432/dasha?" + "user=" + getUser() + "&password=" + getPassword();
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "8000");
        return Integer.parseInt(port);
    }

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", "testing");
    }
    private static boolean isDevelopment() {
        return getMode().equals("development");
    }

    private static InputStream getFileFromResourceAsStream(String fileName) {
        ClassLoader classLoader = App.class.getClassLoader();
        return classLoader.getResourceAsStream(fileName);
    }

    private static String getContentFromStream(InputStream is) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private static void getConfiguredDatabase(String url) throws IOException, SQLException {
        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);

        var dataSource = new HikariDataSource(hikariConfig);
        String sql = getContentFromStream(getFileFromResourceAsStream("schema.sql"));

        log.info(sql);
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute(sql);
        }

        BaseRepository.dataSource = dataSource;
    }


    public static Tomcat getApp() throws IOException, SQLException {
        if (isDevelopment()) {
            getConfiguredDatabase(getDefaultDatabaseUrl());
        } else {
            getConfiguredDatabase("jdbc:h2:mem:test");
        }

        DataInitializer.run();

        int port = getPort();

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(System.getProperty("java.io.tmpdir"));
        tomcat.setPort(port);

        Context ctx = tomcat.addContext("", new File(".").getAbsolutePath());

        Tomcat.addServlet(ctx, WelcomeServlet.class.getSimpleName(), new WelcomeServlet());
        ctx.addServletMappingDecoded("", WelcomeServlet.class.getSimpleName());

        Tomcat.addServlet(ctx, BooksServlet.class.getSimpleName(), new BooksServlet());
        ctx.addServletMappingDecoded("/books/*", BooksServlet.class.getSimpleName());

        Tomcat.addServlet(ctx, GenresServlet.class.getSimpleName(), new GenresServlet());
        ctx.addServletMappingDecoded("/genres/*", GenresServlet.class.getSimpleName());

        Tomcat.addServlet(ctx, ReviewsServlet.class.getSimpleName(), new ReviewsServlet());
        ctx.addServletMappingDecoded("/reviews/*", ReviewsServlet.class.getSimpleName());

        return tomcat;
    }

    public static void main(String[] args) throws LifecycleException, SQLException, IOException {
        Tomcat app = getApp();
        app.start();
        app.getServer().await();
    }
}
