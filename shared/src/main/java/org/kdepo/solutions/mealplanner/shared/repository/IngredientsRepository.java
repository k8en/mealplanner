package org.kdepo.solutions.mealplanner.shared.repository;

import org.kdepo.solutions.mealplanner.shared.model.Ingredient;

import java.util.List;

public interface IngredientsRepository {

    Ingredient addIngredient(Integer ingredientId, String name, Integer recipeId, Integer productId, Integer amount, Integer unitId);

    void deleteIngredient(Integer ingredientId);

    List<Ingredient> getAllIngredientsFromRecipe(Integer recipeId);

    Ingredient getIngredient(Integer ingredientId);

    void updateIngredient(Integer ingredientId, String name, Integer recipeId, Integer productId, Integer amount, Integer unitId);

}
