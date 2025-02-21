package org.kdepo.solutions.mealplanner.repository;

import org.kdepo.solutions.mealplanner.model.Meal;

import java.util.List;

public interface MealsRepository {

    Meal addMeal(Integer mealId, Integer dayId, String name, Integer orderNumber);

    void deleteMeal(Integer mealId);

    List<Meal> getAllMealsFromDay(Integer dayId);

    List<Integer> getAllMealsWithRecipe(Integer recipeId);

    Meal getMeal(Integer mealId);

    Integer getOrderNumber(Integer dayId);

    boolean isUsed(Integer mealId);

    void updateMeal(Integer mealId, Integer dayId, String name, Integer orderNumber);

}
