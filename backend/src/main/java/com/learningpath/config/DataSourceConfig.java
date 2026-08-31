package com.learningpath.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Bean
    @Primary
    @Profile({"prod", "postgres"})
    public DataSource prodDataSource(
            @Value("${spring.datasource.url:${DATABASE_URL:}}") String rawUrl,
            @Value("${spring.datasource.username:${DATABASE_USERNAME:postgres}}") String username,
            @Value("${spring.datasource.password:${DATABASE_PASSWORD:postgrespassword}}") String password
    ) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            throw new IllegalStateException(
                    "CRITICAL: Production PostgreSQL URL is missing! Set SPRING_DATASOURCE_URL (e.g. jdbc:postgresql://host:port/database) in Render environment variables."
            );
        }

        String jdbcUrl = rawUrl.trim();
        if (jdbcUrl.startsWith("postgres://")) {
            jdbcUrl = "jdbc:postgresql://" + jdbcUrl.substring("postgres://".length());
        } else if (jdbcUrl.startsWith("postgresql://") && !jdbcUrl.startsWith("jdbc:postgresql://")) {
            jdbcUrl = "jdbc:" + jdbcUrl;
        }

        log.info("Initializing PRODUCTION PostgreSQL DataSource using org.postgresql.Driver for URL: {}", jdbcUrl);

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setPoolName("HikariPool-PostgreSQL-Prod");
        dataSource.setMaximumPoolSize(10);
        dataSource.setMinimumIdle(2);
        dataSource.setConnectionTimeout(30000);
        dataSource.setIdleTimeout(600000);
        dataSource.setMaxLifetime(1800000);

        return dataSource;
    }

    @Bean
    @Primary
    @Profile({"dev", "default"})
    public DataSource devDataSource() {
        log.info("Initializing DEVELOPMENT H2 in-memory DataSource using org.h2.Driver");

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setJdbcUrl("jdbc:h2:mem:learningpathdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        dataSource.setPoolName("HikariPool-H2-Dev");

        return dataSource;
    }
}
