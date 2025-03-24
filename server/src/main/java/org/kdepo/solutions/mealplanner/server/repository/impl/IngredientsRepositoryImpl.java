package org.kdepo.solutions.mealplanner.server.repository.impl;

import org.kdepo.solutions.mealplanner.shared.model.Ingredient;
import org.kdepo.solutions.mealplanner.shared.repository.IngredientsRepository;
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
public class IngredientsRepositoryImpl implements IngredientsRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngredientsRepositoryImpl.class);

    private static final String SQL_ADD_INGREDIENT = "INSERT INTO ingredients (ingredient_id, name, recipe_id, product_id, amount, unit_id) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_DELETE_INGREDIENT = "DELETE FROM ingredients WHERE ingredient_id = ?";
    private static final String SQL_GET_ALL_INGREDIENTS_FROM_RECIPE = "SELECT * FROM ingredients WHERE recipe_id = ?";
    private static final String SQL_GET_INGREDIENT = "SELECT * FROM ingredients WHERE ingredient_id = ?";
    private static final String SQL_UPDATE_INGREDIENT = "UPDATE ingredients SET name = ?, recipe_id = ?, product_id = ?, amount = ?, unit_id = ? WHERE ingredient_id = ?";

    private final JdbcTemplate jdbcTemplate;

    public IngredientsRepositoryImpl(@Qualifier("mealPlannerJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Ingredient addIngredient(Integer ingredientId, String name, Integer recipeId, Integer productId, Integer amount, Integer unitId) {
        LOGGER.trace("[DBR][addIngredient] Invoked with parameters: ingredientId={}, name={}, recipeId={}, productId={}, amount={}, unitId={}",
                ingredientId, name, recipeId, productId, amount, unitId
        );
        jdbcTemplate.update(
                SQL_ADD_INGREDIENT,
                ps -> {
                    ps.setInt(1, ingredientId);
                    ps.setString(2, name);
                    ps.setInt(3, recipeId);
                    ps.setInt(4, productId);
                    ps.setInt(5, amount);
                    ps.setInt(6, unitId);
                }
        );
        return getIngredient(ingredientId);
    }

    @Override
    public void deleteIngredient(Integer ingredientId) {
        LOGGER.trace("[DBR][deleteIngredient] Invoked with parameters: ingredientId={}", ingredientId);
        jdbcTemplate.update(
                SQL_DELETE_INGREDIENT,
                ps -> ps.setInt(1, ingredientId)
        );
    }

    @Override
    public List<Ingredient> getAllIngredientsFromRecipe(Integer recipeId) {
        LOGGER.trace("[DBR][getAllIngredientsFromRecipe] Invoked with parameters: recipeId={}", recipeId);
        return jdbcTemplate.query(
                SQL_GET_ALL_INGREDIENTS_FROM_RECIPE,
                rs -> {
                    List<Ingredient> result = new ArrayList<>();
                    while (rs.next()) {
                        result.add(convert(rs));
                    }
                    return result;
                }
        );
    }

    @Override
    public Ingredient getIngredient(Integer ingredientId) {
        LOGGER.trace("[DBR][getIngredient] Invoked with parameters: ingredientId={}", ingredientId);
        return jdbcTemplate.query(
                SQL_GET_INGREDIENT,
                rs -> {
                    Ingredient ingredient = null;
                    if (rs.next()) {
                        ingredient = convert(rs);
                    }
                    return ingredient;
                }
        );
    }

    @Override
    public void updateIngredient(Integer ingredientId, String name, Integer recipeId, Integer productId, Integer amount, Integer unitId) {
        LOGGER.trace("[DBR][addIngredient] Invoked with parameters: ingredientId={}, name={}, recipeId={}, productId={}, amount={}, unitId={}",
                ingredientId, name, recipeId, productId, amount, unitId
        );
        jdbcTemplate.update(
                SQL_UPDATE_INGREDIENT,
                ps -> {
                    ps.setString(1, name);
                    ps.setInt(2, recipeId);
                    ps.setInt(3, productId);
                    ps.setInt(4, amount);
                    ps.setInt(5, unitId);
                    ps.setInt(6, ingredientId);
                }
        );
    }

    private Ingredient convert(ResultSet rs) throws SQLException {
        Integer ingredientId = rs.getInt("ingredient_id");
        String name = rs.getString("name");
        Integer recipeId = rs.getInt("recipe_id");
        Integer productId = rs.getInt("product_id");
        Integer amount = rs.getInt("amount");
        Integer unitId = rs.getInt("unit_id");

        Ingredient ingredient = new Ingredient();
        ingredient.setIngredientId(ingredientId);
        ingredient.setName(name);
        ingredient.setRecipeId(recipeId);
        ingredient.setProductId(productId);
        ingredient.setAmount(amount);
        ingredient.setUnitId(unitId);

        return ingredient;
    }
}
