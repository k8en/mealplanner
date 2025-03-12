package org.kdepo.solutions.mealplanner.repository;

import org.kdepo.solutions.mealplanner.model.Recipe;

import java.math.BigDecimal;
import java.util.List;

public interface RecipesRepository {

    Recipe addRecipe(Integer recipeId, String name, String description, String source, Integer portions, BigDecimal weight, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs);

    void addRecipeToMeal(Integer recipeId, Integer mealId, Integer orderNumber);

    void deleteRecipe(Integer recipeId);

    void deleteRecipeFromMeal(Integer recipeId, Integer mealId);

    List<Recipe> getAllRecipes();

    List<Recipe> getAllRecipes(List<Integer> products, List<Integer> tags);

    List<Recipe> getAllRecipesFromMeal(Integer mealId);

    Integer getOrderNumber(Integer mealId);

    Recipe getRecipe(Integer recipeId);

    boolean isUsed(Integer recipeId);

    void updateMealsContents(Integer mealId, Integer recipeId, Integer orderNumber);

    void updateRecipe(Integer recipeId, String name, String description, String source, Integer portions, BigDecimal weight, BigDecimal calories, BigDecimal proteins, BigDecimal fats, BigDecimal carbs);


}
