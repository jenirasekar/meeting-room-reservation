package com.meetingroom.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.Properties;

public class DBConnection {
    private static HikariDataSource dataSource;

    static {
        try {
            Properties props = new Properties();
            InputStream input = DBConnection.class.getClassLoader()
                    .getResourceAsStream("db.properties");

            if (input != null) {
                props.load(input);
            }

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url",
                    "jdbc:mysql://localhost:3306/meeting_room"));
            config.setUsername(props.getProperty("db.username", "root"));
            config.setPassword(props.getProperty("db.password", "0713"));
            config.setDriverClassName(props.getProperty("db.driver",
                    "com.mysql.cj.jdbc.Driver"));

            // Connection pool settings
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(10000);
            // Don't fail startup if DB is temporarily unreachable
            config.setInitializationFailTimeout(-1);

            dataSource = new HikariDataSource(config);
        } catch (Exception e) {
            System.err.println("WARNING: Failed to initialize connection pool: " + e.getMessage());
            e.printStackTrace();
            // Don't throw — allow the app to deploy; DB errors will surface at request time
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
