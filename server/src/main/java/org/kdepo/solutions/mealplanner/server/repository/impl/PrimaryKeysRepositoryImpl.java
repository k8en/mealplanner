package org.kdepo.solutions.mealplanner.server.repository.impl;

import org.kdepo.solutions.mealplanner.shared.repository.PrimaryKeysRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PrimaryKeysRepositoryImpl implements PrimaryKeysRepository {

    private static final String SQL_GET_NEXT_VAL = "SELECT next_val FROM primary_keys WHERE name = ?";
    private static final String SQL_MOVE_NEXT_VAL = "UPDATE primary_keys SET next_val = next_val + 1 WHERE name = ?";

    private final JdbcTemplate jdbcTemplate;

    public PrimaryKeysRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Integer getNextVal(String entityPk) {
        return jdbcTemplate.query(
                SQL_GET_NEXT_VAL,
                resultSet -> {
                    String nextValStr = resultSet.getString("next_val");
                    return Integer.parseInt(nextValStr);
                },
                entityPk
        );
    }

    @Override
    public void moveNextVal(String entityPk) {
        jdbcTemplate.update(SQL_MOVE_NEXT_VAL, entityPk);
    }

}
