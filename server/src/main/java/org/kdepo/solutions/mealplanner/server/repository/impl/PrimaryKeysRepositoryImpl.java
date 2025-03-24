package org.kdepo.solutions.mealplanner.server.repository.impl;

import org.kdepo.solutions.mealplanner.shared.repository.PrimaryKeysRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PrimaryKeysRepositoryImpl implements PrimaryKeysRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrimaryKeysRepositoryImpl.class);

    private static final String SQL_GET_NEXT_VAL = "SELECT next_val FROM primary_keys WHERE name = ?";
    private static final String SQL_MOVE_NEXT_VAL = "UPDATE primary_keys SET next_val = next_val + 1 WHERE name = ?";

    private final JdbcTemplate jdbcTemplate;

    public PrimaryKeysRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Integer getNextVal(String entityPk) {
        LOGGER.trace("[DBR][getNextVal] Invoked with parameters: entityPk={}", entityPk);
        return jdbcTemplate.query(
                SQL_GET_NEXT_VAL,
                ps -> ps.setString(1, entityPk),
                rs -> {
                    Integer nextVal = null;
                    if (rs.next()) {
                        nextVal = Integer.parseInt(rs.getString("next_val"));
                    }
                    return nextVal;
                }
        );
    }

    @Override
    public void moveNextVal(String entityPk) {
        LOGGER.trace("[DBR][moveNextVal] Invoked with parameters: entityPk={}", entityPk);
        jdbcTemplate.update(
                SQL_MOVE_NEXT_VAL,
                ps -> ps.setString(1, entityPk)
        );
    }

}
