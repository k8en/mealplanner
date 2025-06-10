package org.kdepo.solutions.mealplanner.server.repository.impl;

import org.kdepo.solutions.mealplanner.shared.model.Day;
import org.kdepo.solutions.mealplanner.shared.repository.DaysRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DaysRepositoryImpl implements DaysRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DaysRepositoryImpl.class);

    private static final String SQL_ADD_DAY = "INSERT INTO days (day_id, menu_id, week_id, name, order_number) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_DELETE_DAY = "DELETE FROM days WHERE day_id = ?";
    private static final String SQL_GET_ALL_DAYS_FROM_MENU = "SELECT * FROM days WHERE menu_id = ? ORDER BY order_number ASC";
    private static final String SQL_GET_ALL_DAYS_FROM_WEEK = "SELECT * FROM days WHERE week_id = ? ORDER BY order_number ASC";
    private static final String SQL_GET_DAY = "SELECT * FROM days WHERE day_id = ?";
    private static final String SQL_GET_ORDER_NUMBER = "SELECT IFNULL(MAX(order_number) + 1, 1) AS order_number FROM days WHERE week_id = ?";
    private static final String SQL_IS_USED = "SELECT day_id FROM meals WHERE day_id = ? LIMIT 1";
    private static final String SQL_UPDATE_DAY = "UPDATE days SET menu_id = ?, week_id = ?, name = ?, order_number = ? WHERE day_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public DaysRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Day addDay(Integer dayId, Integer menuId, Integer weekId, String name, Integer orderNumber) {
        LOGGER.trace("[DBR][addDay] Invoked with parameters: dayId={}, menuId={}, weekId={}, name={}, orderNumber={}",
                dayId, menuId, weekId, name, orderNumber
        );

        jdbcTemplate.update(
                SQL_ADD_DAY,
                ps -> {
                    ps.setInt(1, dayId);
                    ps.setInt(2, menuId);
                    ps.setObject(3, weekId, Types.INTEGER);
                    ps.setString(4, name);
                    ps.setInt(5, orderNumber);
                }
        );

        return getDay(dayId);
    }

    @Override
    public void deleteDay(Integer dayId) {
        LOGGER.trace("[DBR][deleteDay] Invoked with parameters: dayId={}", dayId);
        jdbcTemplate.update(
                SQL_DELETE_DAY,
                ps -> ps.setInt(1, dayId)
        );
    }

    @Override
    public List<Day> getAllDaysFromMenu(Integer menuId) {
        LOGGER.trace("[DBR][getAllDaysFromMenu] Invoked with parameters: menuId={}", menuId);
        return jdbcTemplate.query(
                SQL_GET_ALL_DAYS_FROM_MENU,
                ps -> ps.setInt(1, menuId),
                rs -> {
                    List<Day> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(convert(rs));
                    }
                    return result;
                }
        );
    }

    @Override
    public List<Day> getAllDaysFromWeek(Integer weekId) {
        LOGGER.trace("[DBR][getAllDaysFromWeek] Invoked with parameters: weekId={}", weekId);
        return jdbcTemplate.query(
                SQL_GET_ALL_DAYS_FROM_WEEK,
                ps -> ps.setInt(1, weekId),
                rs -> {
                    List<Day> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(convert(rs));
                    }
                    return result;
                }
        );
    }

    @Override
    public Day getDay(Integer dayId) {
        LOGGER.trace("[DBR][getDay] Invoked with parameters: dayId={}", dayId);
        return jdbcTemplate.query(
                SQL_GET_DAY,
                ps -> ps.setInt(1, dayId),
                rs -> {
                    Day day = null;
                    if (rs.next()) {
                        day = convert(rs);
                    }
                    return day;
                }
        );
    }

    @Override
    public Integer getOrderNumber(Integer weekId) {
        LOGGER.trace("[DBR][getOrderNumber] Invoked with parameters: weekId={}", weekId);
        return jdbcTemplate.query(
                SQL_GET_ORDER_NUMBER,
                ps -> ps.setInt(1, weekId),
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
    public boolean isUsed(Integer dayId) {
        LOGGER.trace("[DBR][isUsed] Invoked with parameters: dayId={}", dayId);
        return Boolean.TRUE.equals(jdbcTemplate.query(
                SQL_IS_USED,
                ps -> ps.setInt(1, dayId),
                ResultSet::next
        ));
    }

    @Override
    public void updateDay(Integer dayId, Integer menuId, Integer weekId, String name, Integer orderNumber) {
        LOGGER.trace("[DBR][updateDay] Invoked with parameters: dayId={}, menuId={}, weekId={}, name={}, orderNumber={}",
                dayId, menuId, weekId, name, orderNumber
        );

        jdbcTemplate.update(
                SQL_UPDATE_DAY,
                ps -> {
                    ps.setInt(1, menuId);
                    ps.setObject(2, weekId, Types.INTEGER);
                    ps.setString(3, name);
                    ps.setInt(4, orderNumber);
                    ps.setInt(5, dayId);
                }
        );
    }

    private Day convert(ResultSet rs) throws SQLException {
        Integer dayId = rs.getInt("day_id");
        Integer menuId = rs.getInt("menu_id");

        Integer weekId = rs.getInt("week_id");
        if (rs.wasNull()) {
            weekId = null;
        }

        String name = rs.getString("name");
        Integer orderNumber = rs.getInt("order_number");

        Day day = new Day();
        day.setDayId(dayId);
        day.setMenuId(menuId);
        day.setWeekId(weekId);
        day.setName(name);
        day.setOrderNumber(orderNumber);

        return day;
    }
}
