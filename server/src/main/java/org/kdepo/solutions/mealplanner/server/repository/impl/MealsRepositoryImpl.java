package org.kdepo.solutions.mealplanner.server.repository.impl;

import org.kdepo.solutions.mealplanner.shared.model.Meal;
import org.kdepo.solutions.mealplanner.shared.repository.MealsRepository;
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
public class MealsRepositoryImpl implements MealsRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(MealsRepositoryImpl.class);

    private static final String SQL_ADD_MEAL = "INSERT INTO meals (meal_id, day_id, name, order_number) VALUES (?, ?, ?, ?)";
    private static final String SQL_DELETE_MEAL = "DELETE FROM meals WHERE meal_id = ?";
    private static final String SQL_GET_ALL_MEALS_FROM_DAY = "SELECT * FROM meals WHERE day_id = ? ORDER BY order_number ASC";
    private static final String SQL_GET_ALL_MEALS_WITH_RECIPE = "SELECT meal_id FROM meals_contents WHERE recipe_id = ?";
    private static final String SQL_GET_MEAL = "SELECT * FROM meals WHERE meal_id = ?";
    private static final String SQL_GET_ORDER_NUMBER = "SELECT IFNULL(MAX(order_number) + 1, 1) AS order_number FROM meals WHERE day_id = ?";
    private static final String SQL_IS_USED = "SELECT meal_id FROM meals_contents WHERE meal_id = ? LIMIT 1";
    private static final String SQL_UPDATE_MEAL = "UPDATE meals SET day_id = ?, name = ?, order_number = ? WHERE meal_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public MealsRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Meal addMeal(Integer mealId, Integer dayId, String name, Integer orderNumber) {
        LOGGER.trace("[DBR][addMeal] Invoked with parameters: mealId={}, dayId={}, name={}, orderNumber={}",
                mealId, dayId, name, orderNumber
        );
        jdbcTemplate.update(
                SQL_ADD_MEAL,
                ps -> {
                    ps.setInt(1, mealId);
                    ps.setInt(2, dayId);
                    ps.setString(3, name);
                    ps.setInt(4, orderNumber);
                }
        );
        return getMeal(mealId);
    }

    @Override
    public void deleteMeal(Integer mealId) {
        LOGGER.trace("[DBR][deleteMeal] Invoked with parameters: mealId={}", mealId);
        jdbcTemplate.update(
                SQL_DELETE_MEAL,
                ps -> ps.setInt(1, mealId)
        );
    }

    @Override
    public List<Meal> getAllMealsFromDay(Integer dayId) {
        LOGGER.trace("[DBR][getAllMealsFromDay] Invoked with parameters: dayId={}", dayId);
        return jdbcTemplate.query(
                SQL_GET_ALL_MEALS_FROM_DAY,
                ps -> ps.setInt(1, dayId),
                rs -> {
                    List<Meal> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(convert(rs));
                    }
                    return result;
                }
        );
    }

    @Override
    public List<Integer> getAllMealsWithRecipe(Integer recipeId) {
        LOGGER.trace("[DBR][getAllMealsWithRecipe] Invoked with parameters: recipeId={}", recipeId);
        return jdbcTemplate.query(
                SQL_GET_ALL_MEALS_WITH_RECIPE,
                ps -> ps.setInt(1, recipeId),
                rs -> {
                    List<Integer> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(rs.getInt("meal_id"));
                    }
                    return result;
                }
        );
    }

    @Override
    public Meal getMeal(Integer mealId) {
        LOGGER.trace("[DBR][getMeal] Invoked with parameters: mealId={}", mealId);
        return jdbcTemplate.query(
                SQL_GET_MEAL,
                ps -> ps.setInt(1, mealId),
                rs -> {
                    Meal meal = null;
                    if (rs.next()) {
                        meal = convert(rs);
                    }
                    return meal;
                }
        );
    }

    @Override
    public Integer getOrderNumber(Integer dayId) {
        LOGGER.trace("[DBR][getOrderNumber] Invoked with parameters: dayId={}", dayId);
        return jdbcTemplate.query(
                SQL_GET_ORDER_NUMBER,
                ps -> ps.setInt(1, dayId),
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
    public boolean isUsed(Integer mealId) {
        LOGGER.trace("[DBR][isUsed] Invoked with parameters: mealId={}", mealId);
        return Boolean.TRUE.equals(jdbcTemplate.query(
                SQL_IS_USED,
                ps -> ps.setInt(1, mealId),
                ResultSet::next
        ));
    }

    @Override
    public void updateMeal(Integer mealId, Integer dayId, String name, Integer orderNumber) {
        LOGGER.trace("[DBR][updateMeal] Invoked with parameters: mealId={}, dayId={}, name={}, orderNumber={}",
                mealId, dayId, name, orderNumber
        );
        jdbcTemplate.update(
                SQL_UPDATE_MEAL,
                ps -> {
                    ps.setInt(1, dayId);
                    ps.setString(2, name);
                    ps.setInt(3, orderNumber);
                    ps.setInt(4, mealId);
                }
        );
    }

    private Meal convert(ResultSet rs) throws SQLException {
        Integer mealId = rs.getInt("meal_id");
        Integer dayId = rs.getInt("day_id");
        String name = rs.getString("name");
        Integer orderNumber = rs.getInt("order_number");

        Meal meal = new Meal();
        meal.setMealId(mealId);
        meal.setDayId(dayId);
        meal.setName(name);
        meal.setOrderNumber(orderNumber);

        return meal;
    }
}
