package org.kdepo.solutions.mealplanner.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DbConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DbConfig.class);

    @Autowired
    Environment env;

    @Bean(name = "mealPlannerDataSource")
    public DataSource mealPlannerDataSource() {
        LOGGER.info("[DBC] Datasource configuration:");

        String driverClassName = env.getProperty("meal-planner.database.driver-class-name");
        if (driverClassName == null || driverClassName.isEmpty()) {
            LOGGER.error("[DBC] Error! Driver class name not found or empty!");
            throw new RuntimeException("[DBC] Error! Driver class name not found or empty!");
        } else {
            LOGGER.info("[DBC] driverClassName = {}", driverClassName);
        }

        String url = env.getProperty("meal-planner.database.url");
        if (url == null || url.isEmpty()) {
            LOGGER.error("[DBC] Error! Url to database not found or empty!");
            throw new RuntimeException("[DBC] Error! Url to database not found or empty!");
        } else {
            LOGGER.info("[DBC] url = {}", url);
        }

        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        return dataSource;
    }

    @Bean(name = "mealPlannerJdbcTemplate")
    public JdbcTemplate mealPlannerJdbcTemplate(@Qualifier("mealPlannerDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
