package com.rublin.rublinmart.listener;

import com.rublin.rublinmart.dao.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.h2.tools.RunScript;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

@WebListener
public class DatabaseContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initializing RublinMart Web Application Context & Database...");
        try {
            DBConnection.init();
            executeSqlScript("db/schema.sql");
            executeSqlScript("db/seed.sql");
            logger.info("Database schema and seed scripts executed successfully.");
        } catch (Exception e) {
            logger.error("Error initializing database context", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void executeSqlScript(String resourcePath) throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalStateException("SQL resource not found: " + resourcePath);
            }

            try (Connection conn = DBConnection.getConnection();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                RunScript.execute(conn, reader);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Shutting down RublinMart Web Application Context...");
        DBConnection.close();
    }
}
