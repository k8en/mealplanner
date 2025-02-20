package org.kdepo.solutions.mealplanner.repository.impl;

import org.kdepo.solutions.mealplanner.model.Week;
import org.kdepo.solutions.mealplanner.repository.MealPlannerWeeksRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MealPlannerWeeksRepositoryImpl implements MealPlannerWeeksRepository {

    private static final String SQL_ADD_WEEK = "INSERT INTO weeks (week_id, profile_id, name, order_number) VALUES (?, ?, ?, ?)";
    private static final String SQL_DELETE_WEEK = "DELETE FROM weeks WHERE week_id = ?";
    private static final String SQL_GET_ALL_WEEKS_FROM_PROFILE = "SELECT * FROM weeks WHERE profile_id = ? ORDER BY order_number ASC";
    private static final String SQL_GET_ORDER_NUMBER = "SELECT IFNULL(MAX(order_number) + 1, 1) AS order_number FROM weeks WHERE profile_id = ?";
    private static final String SQL_GET_WEEK = "SELECT * FROM weeks WHERE week_id = ?";
    private static final String SQL_IS_USED = "SELECT week_id FROM days WHERE week_id = ? LIMIT 1";
    private static final String SQL_UPDATE_WEEK = "UPDATE weeks SET profile_id = ?, name = ?, order_number = ? WHERE week_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public MealPlannerWeeksRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Week addWeek(Integer weekId, Integer profileId, String name, Integer orderNumber) {
        System.out.println("[ML][WeekDao][addWeek] Invoked with parameters:"
                + " weekId=" + weekId
                + ", profileId=" + profileId
                + ", name='" + name + "'"
                + ", orderNumber=" + orderNumber
        );
        jdbcTemplate.update(SQL_ADD_WEEK, weekId, profileId, name, orderNumber);

        return getWeek(weekId);
    }

    @Override
    public void deleteWeek(Integer weekId) {
        System.out.println("[ML][WeekDao][deleteWeek] Invoked with parameters: weekId=" + weekId);
        jdbcTemplate.update(SQL_DELETE_WEEK, weekId);
    }

    @Override
    public List<Week> getAllWeeksFromProfile(Integer profileId) {
        System.out.println("[ML][WeekDao][getAllWeeksFromProfile] Invoked with parameters: profileId=" + profileId);
        return jdbcTemplate.query(
                SQL_GET_ALL_WEEKS_FROM_PROFILE,
                (resultSet, rowNum) -> {
                    Integer weekId = resultSet.getInt("week_id");
                    //Integer profileId = resultSet.getInt("profile_id");
                    String name = resultSet.getString("name");
                    Integer orderNumber = resultSet.getInt("order_number");

                    Week week = new Week();
                    week.setWeekId(weekId);
                    week.setProfileId(profileId);
                    week.setName(name);
                    week.setOrderNumber(orderNumber);

                    return week;
                },
                profileId
        );
    }

    @Override
    public Integer getOrderNumber(Integer profileId) {
        System.out.println("[ML][WeekDao][getOrderNumber] Invoked with parameters: profileId=" + profileId);
        return jdbcTemplate.query(
                SQL_GET_ORDER_NUMBER,
                resultSet -> {
                    return resultSet.getInt("order_number");
                },
                profileId
        );
    }

    @Override
    public Week getWeek(Integer weekId) {
        System.out.println("[ML][WeekDao][getWeek] Invoked with parameters: weekId=" + weekId);
        return jdbcTemplate.query(
                SQL_GET_WEEK,
                resultSet -> {
                    //Integer weekId = resultSet.getInt("week_id");
                    Integer profileId = resultSet.getInt("profile_id");
                    String name = resultSet.getString("name");
                    Integer orderNumber = resultSet.getInt("order_number");

                    Week week = new Week();
                    week.setWeekId(weekId);
                    week.setProfileId(profileId);
                    week.setName(name);
                    week.setOrderNumber(orderNumber);

                    return week;
                },
                weekId
        );
    }

    @Override
    public boolean isUsed(Integer weekId) {
        System.out.println("[ML][WeekDao][isUsed] Invoked with parameters: weekId=" + weekId);
        Integer objectId = jdbcTemplate.query(
                SQL_IS_USED,
                resultSet -> {
                    return resultSet.getInt("week_id");
                },
                weekId
        );
        return objectId != null;
    }

    @Override
    public void updateWeek(Integer weekId, Integer profileId, String name, Integer orderNumber) {
        System.out.println("[ML][WeekDao][updateWeek] Invoked with parameters:"
                + " weekId=" + weekId
                + ", profileId=" + profileId
                + ", name='" + name + "'"
                + ", orderNumber=" + orderNumber
        );

        jdbcTemplate.update(SQL_UPDATE_WEEK, profileId, name, orderNumber, weekId);
    }
}
