package org.kdepo.solutions.mealplanner.repository.impl;

import org.kdepo.solutions.mealplanner.model.Ingredient;
import org.kdepo.solutions.mealplanner.repository.IngredientsRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IngredientsRepositoryImpl implements IngredientsRepository {

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
        System.out.println("[IngredientDao][addIngredient] Invoked with parameters:"
                + " ingredientId=" + ingredientId
                + ", name='" + name + "'"
                + ", recipeId=" + recipeId
                + ", productId=" + productId
                + ", amount=" + amount
                + ", unitId=" + unitId
        );
        jdbcTemplate.update(SQL_ADD_INGREDIENT, ingredientId, name, recipeId, productId, amount, unitId);

        return getIngredient(ingredientId);
    }

    @Override
    public void deleteIngredient(Integer ingredientId) {
        System.out.println("[IngredientDao][deleteIngredient] Invoked with parameters: ingredientId=" + ingredientId);
        jdbcTemplate.update(SQL_DELETE_INGREDIENT, ingredientId);
    }

    @Override
    public List<Ingredient> getAllIngredientsFromRecipe(Integer recipeId) {
        System.out.println("[IngredientDao][getAllIngredientsFromRecipe] Invoked with parameters: recipeId=" + recipeId);
        return jdbcTemplate.query(
                SQL_GET_ALL_INGREDIENTS_FROM_RECIPE,
                (resultSet, rowNum) -> {
                    Integer ingredientId = resultSet.getInt("ingredient_id");
                    String name = resultSet.getString("name");
                    //Integer recipeId = resultSet.getInt("recipe_id");
                    Integer productId = resultSet.getInt("product_id");
                    Integer amount = resultSet.getInt("amount");
                    Integer unitId = resultSet.getInt("unit_id");

                    Ingredient ingredient = new Ingredient();
                    ingredient.setIngredientId(ingredientId);
                    ingredient.setName(name);
                    ingredient.setRecipeId(recipeId);
                    ingredient.setProductId(productId);
                    ingredient.setAmount(amount);
                    ingredient.setUnitId(unitId);

                    return ingredient;
                },
                recipeId
        );
    }

    @Override
    public Ingredient getIngredient(Integer ingredientId) {
        System.out.println("[IngredientDao][getIngredient] Invoked with parameters: ingredientId=" + ingredientId);
        return jdbcTemplate.query(
                SQL_GET_INGREDIENT,
                resultSet -> {
                    //Integer ingredientId = resultSet.getInt("ingredient_id");
                    String name = resultSet.getString("name");
                    Integer recipeId = resultSet.getInt("recipe_id");
                    Integer productId = resultSet.getInt("product_id");
                    Integer amount = resultSet.getInt("amount");
                    Integer unitId = resultSet.getInt("unit_id");

                    Ingredient ingredient = new Ingredient();
                    ingredient.setIngredientId(ingredientId);
                    ingredient.setName(name);
                    ingredient.setRecipeId(recipeId);
                    ingredient.setProductId(productId);
                    ingredient.setAmount(amount);
                    ingredient.setUnitId(unitId);

                    return ingredient;
                },
                ingredientId
        );
    }

    @Override
    public void updateIngredient(Integer ingredientId, String name, Integer recipeId, Integer productId, Integer amount, Integer unitId) {
        System.out.println("[IngredientDao][updateIngredient] Invoked with parameters:"
                + " ingredientId=" + ingredientId
                + ", name='" + name + "'"
                + ", recipeId=" + recipeId
                + ", productId=" + productId
                + ", amount=" + amount
                + ", unitId=" + unitId
        );
        jdbcTemplate.update(SQL_UPDATE_INGREDIENT, name, recipeId, productId, amount, unitId, ingredientId);
    }
}
