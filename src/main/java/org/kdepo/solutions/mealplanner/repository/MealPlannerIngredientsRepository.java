package org.kdepo.solutions.mealplanner.repository;

import org.kdepo.solutions.mealplanner.model.Ingredient;

import java.util.List;

public interface MealPlannerIngredientsRepository {

    Ingredient addIngredient(Integer ingredientId, String name, Integer recipeId, Integer productId, Integer amount, Integer unitId);

    void deleteIngredient(Integer ingredientId);

    List<Ingredient> getAllIngredientsFromRecipe(Integer recipeId);

    Ingredient getIngredient(Integer ingredientId);

    void updateIngredient(Integer ingredientId, String name, Integer recipeId, Integer productId, Integer amount, Integer unitId);

}
