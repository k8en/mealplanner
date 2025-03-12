package org.kdepo.solutions.mealplanner.repository.impl;

import org.kdepo.solutions.mealplanner.model.Day;
import org.kdepo.solutions.mealplanner.repository.DaysRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DaysRepositoryImpl implements DaysRepository {

    private static final String SQL_ADD_DAY = "INSERT INTO days (day_id, week_id, name, order_number) VALUES (?, ?, ?, ?)";
    private static final String SQL_DELETE_DAY = "DELETE FROM days WHERE day_id = ?";
    private static final String SQL_GET_ALL_DAYS_FROM_WEEK = "SELECT * FROM days WHERE week_id = ? ORDER BY order_number ASC";
    private static final String SQL_GET_DAY = "SELECT * FROM days WHERE day_id = ?";
    private static final String SQL_GET_ORDER_NUMBER = "SELECT IFNULL(MAX(order_number) + 1, 1) AS order_number FROM days WHERE week_id = ?";
    private static final String SQL_IS_USED = "SELECT day_id FROM meals WHERE day_id = ? LIMIT 1";
    private static final String SQL_UPDATE_DAY = "UPDATE days SET week_id = ?, name = ?, order_number = ? WHERE day_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public DaysRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Day addDay(Integer dayId, Integer weekId, String name, Integer orderNumber) {
        System.out.println("[DayDao][addDay] Invoked with parameters:"
                + " dayId=" + dayId
                + ", weekId=" + weekId
                + ", name='" + name + "'"
                + ", orderNumber=" + orderNumber
        );
        jdbcTemplate.update(SQL_ADD_DAY, dayId, weekId, name, orderNumber);

        return getDay(dayId);
    }

    @Override
    public void deleteDay(Integer dayId) {
        System.out.println("[DayDao][deleteDay] Invoked with parameters: dayId=" + dayId);
        jdbcTemplate.update(SQL_DELETE_DAY, dayId);
    }

    @Override
    public List<Day> getAllDaysFromWeek(Integer weekId) {
        System.out.println("[DayDao][getAllDaysFromWeek] Invoked with parameters: weekId=" + weekId);
        return jdbcTemplate.query(
                SQL_GET_ALL_DAYS_FROM_WEEK,
                (resultSet, rowNum) -> {
                    Integer dayId = resultSet.getInt("day_id");
                    String name = resultSet.getString("name");
                    Integer orderNumber = resultSet.getInt("order_number");

                    Day day = new Day();
                    day.setDayId(dayId);
                    day.setWeekId(weekId);
                    day.setName(name);
                    day.setOrderNumber(orderNumber);

                    return day;
                },
                weekId
        );
    }

    @Override
    public Day getDay(Integer dayId) {
        System.out.println("[DayDao][getDay] Invoked with parameters: dayId=" + dayId);
        return jdbcTemplate.query(
                SQL_GET_DAY,
                resultSet -> {
                    Integer weekId = resultSet.getInt("week_id");
                    String name = resultSet.getString("name");
                    Integer orderNumber = resultSet.getInt("order_number");

                    Day day = new Day();
                    day.setDayId(dayId);
                    day.setWeekId(weekId);
                    day.setName(name);
                    day.setOrderNumber(orderNumber);

                    return day;
                },
                dayId
        );
    }

    @Override
    public Integer getOrderNumber(Integer weekId) {
        System.out.println("[DayDao][getOrderNumber] Invoked with parameters: weekId=" + weekId);
        return jdbcTemplate.query(
                SQL_GET_ORDER_NUMBER,
                resultSet -> {
                    return resultSet.getInt("order_number");
                },
                weekId
        );
    }

    @Override
    public boolean isUsed(Integer dayId) {
        System.out.println("[DayDao][isUsed] Invoked with parameters: dayId=" + dayId);
        Integer objectId = jdbcTemplate.query(
                SQL_IS_USED,
                resultSet -> {
                    return resultSet.getInt("day_id");
                },
                dayId
        );
        return objectId != null;
    }

    @Override
    public void updateDay(Integer dayId, Integer weekId, String name, Integer orderNumber) {
        System.out.println("[DayDao][updateDay] Invoked with parameters:"
                + " dayId=" + dayId
                + ", weekId=" + weekId
                + ", name='" + name + "'"
                + ", orderNumber=" + orderNumber
        );
        jdbcTemplate.update(SQL_UPDATE_DAY, weekId, name, orderNumber, dayId);
    }
}
