package org.kdepo.solutions.mealplanner.server.repository.impl;

import org.kdepo.solutions.mealplanner.shared.model.Week;
import org.kdepo.solutions.mealplanner.shared.repository.WeeksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class WeeksRepositoryImpl implements WeeksRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeeksRepositoryImpl.class);

    private static final String SQL_ADD_WEEK = "INSERT INTO weeks (week_id, profile_id, name, order_number) VALUES (?, ?, ?, ?)";
    private static final String SQL_DELETE_WEEK = "DELETE FROM weeks WHERE week_id = ?";
    private static final String SQL_GET_ALL_WEEKS_FROM_PROFILE = "SELECT * FROM weeks WHERE profile_id = ? ORDER BY order_number ASC";
    private static final String SQL_GET_ORDER_NUMBER = "SELECT IFNULL(MAX(order_number) + 1, 1) AS order_number FROM weeks WHERE profile_id = ?";
    private static final String SQL_GET_WEEK = "SELECT * FROM weeks WHERE week_id = ?";
    private static final String SQL_IS_USED = "SELECT week_id FROM days WHERE week_id = ? LIMIT 1";
    private static final String SQL_UPDATE_WEEK = "UPDATE weeks SET profile_id = ?, name = ?, order_number = ? WHERE week_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public WeeksRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Week addWeek(Integer weekId, Integer profileId, String name, Integer orderNumber) {
        LOGGER.trace("[DBR][addWeek] Invoked with parameters: weekId={}, profileId={}, name={}, orderNumber={}",
                weekId, profileId, name, orderNumber
        );

        jdbcTemplate.update(
                SQL_ADD_WEEK,
                ps -> {
                    ps.setInt(1, weekId);
                    ps.setInt(2, profileId);
                    ps.setString(3, name);
                    ps.setInt(4, orderNumber);
                }
        );

        return getWeek(weekId);
    }

    @Override
    public void deleteWeek(Integer weekId) {
        LOGGER.trace("[DBR][deleteWeek] Invoked with parameters: weekId={}", weekId);
        jdbcTemplate.update(
                SQL_DELETE_WEEK,
                ps -> ps.setInt(1, weekId)
        );
    }

    @Override
    public List<Week> getAllWeeksFromProfile(Integer profileId) {
        LOGGER.trace("[DBR][getAllWeeksFromProfile] Invoked with parameters: profileId={}", profileId);
        return jdbcTemplate.query(
                SQL_GET_ALL_WEEKS_FROM_PROFILE,
                ps -> ps.setInt(1, profileId),
                rs -> {
                    List<Week> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(convert(rs));
                    }
                    return result;
                }
        );
    }

    @Override
    public Integer getOrderNumber(Integer profileId) {
        LOGGER.trace("[DBR][getOrderNumber] Invoked with parameters: profileId={}", profileId);
        return jdbcTemplate.query(
                SQL_GET_ORDER_NUMBER,
                ps -> ps.setInt(1, profileId),
                rs -> {
                    Integer nextVal = null;
                    if (rs.next()) {
                        nextVal = Integer.parseInt(rs.getString("order_number"));
                    }
                    return nextVal;
                }
        );
    }

    @Override
    public Week getWeek(Integer weekId) {
        LOGGER.trace("[DBR][getWeek] Invoked with parameters: weekId={}", weekId);
        return jdbcTemplate.query(
                SQL_GET_WEEK,
                ps -> ps.setInt(1, weekId),
                rs -> {
                    Week week = null;
                    if (rs.next()) {
                        week = convert(rs);
                    }
                    return week;
                }
        );
    }

    @Override
    public boolean isUsed(Integer weekId) {
        LOGGER.trace("[DBR][isUsed] Invoked with parameters: weekId={}", weekId);
        return Boolean.TRUE.equals(jdbcTemplate.query(
                SQL_IS_USED,
                ps -> ps.setInt(1, weekId),
                ResultSet::next
        ));
    }

    @Override
    public void updateWeek(Integer weekId, Integer profileId, String name, Integer orderNumber) {
        LOGGER.trace("[DBR][updateWeek] Invoked with parameters: weekId={}, profileId={}, name={}, orderNumber={}",
                weekId, profileId, name, orderNumber
        );

        jdbcTemplate.update(
                SQL_UPDATE_WEEK,
                ps -> {
                    ps.setInt(1, profileId);
                    ps.setString(2, name);
                    ps.setInt(3, orderNumber);
                    ps.setInt(4, weekId);
                }
        );
    }

    private Week convert(ResultSet rs) throws SQLException {
        Integer weekId = rs.getInt("week_id");
        Integer profileId = rs.getInt("profile_id");
        String name = rs.getString("name");
        Integer orderNumber = rs.getInt("order_number");

        Week week = new Week();
        week.setWeekId(weekId);
        week.setProfileId(profileId);
        week.setName(name);
        week.setOrderNumber(orderNumber);

        return week;
    }
}
