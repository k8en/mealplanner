package org.kdepo.solutions.mealplanner.server.repository.impl;

import org.kdepo.solutions.mealplanner.shared.model.Meal;
import org.kdepo.solutions.mealplanner.shared.repository.MealsRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MealsRepositoryImpl implements MealsRepository {

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
        System.out.println("[MealDao][addMeal] Invoked with parameters:"
                + " mealId=" + mealId
                + ", dayId=" + dayId
                + ", name='" + name + "'"
                + ", orderNumber=" + orderNumber
        );
        jdbcTemplate.update(SQL_ADD_MEAL, mealId, dayId, name, orderNumber);

        return getMeal(mealId);
    }

    @Override
    public void deleteMeal(Integer mealId) {
        System.out.println("[MealDao][deleteMeal] Invoked with parameters: mealId=" + mealId);
        jdbcTemplate.update(SQL_DELETE_MEAL, mealId);
    }

    @Override
    public List<Meal> getAllMealsFromDay(Integer dayId) {
        System.out.println("[MealDao][getAllMealsFromDay] Invoked with parameters: dayId=" + dayId);
        return jdbcTemplate.query(
                SQL_GET_ALL_MEALS_FROM_DAY,
                (resultSet, rowNum) -> {
                    Integer mealId = resultSet.getInt("meal_id");
                    //Integer dayId = resultSet.getInt("day_id");
                    String name = resultSet.getString("name");
                    Integer orderNumber = resultSet.getInt("order_number");

                    Meal meal = new Meal();
                    meal.setMealId(mealId);
                    meal.setDayId(dayId);
                    meal.setName(name);
                    meal.setOrderNumber(orderNumber);

                    return meal;
                },
                dayId
        );
    }

    @Override
    public List<Integer> getAllMealsWithRecipe(Integer recipeId) {
        System.out.println("[MealDao][getAllMealsWithRecipe] Invoked with parameters: recipeId=" + recipeId);
        return jdbcTemplate.query(
                SQL_GET_ALL_MEALS_WITH_RECIPE,
                (resultSet, rowNum) -> {
                    return resultSet.getInt("meal_id");
                },
                recipeId
        );
    }

    @Override
    public Meal getMeal(Integer mealId) {
        System.out.println("[MealDao][getMeal] Invoked with parameters: mealId=" + mealId);
        return jdbcTemplate.query(
                SQL_GET_MEAL,
                resultSet -> {
                    //Integer mealId = resultSet.getInt("meal_id");
                    Integer dayId = resultSet.getInt("day_id");
                    String name = resultSet.getString("name");
                    Integer orderNumber = resultSet.getInt("order_number");

                    Meal meal = new Meal();
                    meal.setMealId(mealId);
                    meal.setDayId(dayId);
                    meal.setName(name);
                    meal.setOrderNumber(orderNumber);

                    return meal;
                },
                mealId
        );
    }

    @Override
    public Integer getOrderNumber(Integer dayId) {
        System.out.println("[MealDao][getOrderNumber] Invoked with parameters: dayId=" + dayId);
        return jdbcTemplate.query(
                SQL_GET_ORDER_NUMBER,
                resultSet -> {
                    return resultSet.getInt("order_number");
                },
                dayId
        );
    }

    @Override
    public boolean isUsed(Integer mealId) {
        System.out.println("[MealDao][isUsed] Invoked with parameters: mealId=" + mealId);
        Integer objectId = jdbcTemplate.query(
                SQL_IS_USED,
                resultSet -> {
                    return resultSet.getInt("meal_id");
                },
                mealId
        );
        return objectId != null;
    }

    @Override
    public void updateMeal(Integer mealId, Integer dayId, String name, Integer orderNumber) {
        System.out.println("[MealDao][updateMeal] Invoked with parameters:"
                + " mealId=" + mealId
                + ", dayId=" + dayId
                + ", name='" + name + "'"
                + ", orderNumber=" + orderNumber
        );
        jdbcTemplate.update(SQL_UPDATE_MEAL, dayId, name, orderNumber, mealId);
    }
}
