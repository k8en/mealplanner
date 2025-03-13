package org.kdepo.solutions.mealplanner.server.dto;

import java.util.List;

public class MealDto {

    private String name;
    private List<RecipeDto> recipes;

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
