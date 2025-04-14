package org.kdepo.solutions.mealplanner.server.dto;

import java.util.List;

public class MealDto {

    private Integer mealId;
    private String name;
    private List<RecipeDto> recipes;

    public Integer getMealId() {
        return mealId;
    }

    public void setMealId(Integer mealId) {
        this.mealId = mealId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RecipeDto> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<RecipeDto> recipes) {
        this.recipes = recipes;
    }
}
