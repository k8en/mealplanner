package org.kdepo.solutions.mealplanner.config;

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

    @Autowired
    Environment env;

    @Bean(name = "mealPlannerDataSource")
    public DataSource mealPlannerDataSource() {
        System.out.println("[DBC] Datasource configuration:");

        String driverClassName = env.getProperty("meal-planner.database.driver-class-name");
        System.out.println("[DBC] driverClassName = " + driverClassName);

        String url = env.getProperty("meal-planner.database.url");
        System.out.println("[DBC] url = " + url);

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
