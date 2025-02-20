package org.kdepo.solutions.mealplanner.controller;

import org.kdepo.solutions.mealplanner.model.Day;
import org.kdepo.solutions.mealplanner.model.Meal;
import org.kdepo.solutions.mealplanner.model.Recipe;
import org.kdepo.solutions.mealplanner.repository.impl.MealPlannerDaysRepositoryImpl;
import org.kdepo.solutions.mealplanner.repository.impl.MealPlannerMealsRepositoryImpl;
import org.kdepo.solutions.mealplanner.repository.impl.MealPlannerRecipesRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/shopping")
public class ShoppingController {

    @Autowired
    private MealPlannerDaysRepositoryImpl daysRepository;

    @Autowired
    private MealPlannerRecipesRepositoryImpl recipesRepository;

    @Autowired
    private MealPlannerMealsRepositoryImpl mealsRepository;

    @GetMapping()
    public String showDeviceDetailsPage(@RequestParam("weekId") Optional<String> weekId,
                                        @RequestParam("portions") Optional<String> portions) {
        System.out.println("[WEB]" + " GET " + "/shopping?weekId=" + weekId + "&portions=" + portions);

        if (weekId.isEmpty() || portions.isEmpty()) {
            return "redirect:/home";
        }

        int weekIdInt = Integer.parseInt(weekId.get());

        // Calculations
        List<Recipe> allRecipes = new ArrayList<>();
        for (Day day : daysRepository.getAllDaysFromWeek(weekIdInt)) {
            for (Meal meal : mealsRepository.getAllMealsFromDay(day.getDayId())) {
                allRecipes.addAll(recipesRepository.getAllRecipesFromMeal(meal.getMealId()));
            }
        }

        return "shopping";
    }
}
