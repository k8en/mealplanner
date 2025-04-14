package org.kdepo.solutions.mealplanner.server.dto;

import java.util.List;

public class DayDto {

    private Integer dayId;
    private String name;
    private List<MealDto> meals;

    public Integer getDayId() {
        return dayId;
    }

    public void setDayId(Integer dayId) {
        this.dayId = dayId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MealDto> getMeals() {
        return meals;
    }

    public void setMeals(List<MealDto> meals) {
        this.meals = meals;
    }
}
