package com.rublin.rublinmart.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);
    private static HikariDataSource dataSource;

    private DBConnection() {}

    public static synchronized void init(DataSource customDs) {
        if (customDs instanceof HikariDataSource) {
            dataSource = (HikariDataSource) customDs;
        }
    }

    public static synchronized void init() {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        try {
            Properties props = new Properties();
            try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("application.properties")) {
                if (is != null) {
                    props.load(is);
                }
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url", "jdbc:h2:file:./data/rublinmartdb;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE"));
            config.setUsername(props.getProperty("db.username", "sa"));
            config.setPassword(props.getProperty("db.password", ""));
            config.setDriverClassName(props.getProperty("db.driver", "org.h2.Driver"));

            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("hikari.maximum-pool-size", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("hikari.minimum-idle", "2")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("hikari.idle-timeout", "300000")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("hikari.connection-timeout", "20000")));

            dataSource = new HikariDataSource(config);
            logger.info("HikariCP DataSource initialized successfully with URL: {}", config.getJdbcUrl());
        } catch (Exception e) {
            logger.error("Failed to initialize HikariCP DataSource", e);
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            init();
        }
        return dataSource.getConnection();
    }

    public static DataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            init();
        }
        return dataSource;
    }

    public static synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP DataSource closed.");
        }
    }
}
