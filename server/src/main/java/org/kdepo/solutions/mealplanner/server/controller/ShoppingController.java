package org.kdepo.solutions.mealplanner.server.controller;

import org.kdepo.solutions.mealplanner.server.repository.impl.DaysRepositoryImpl;
import org.kdepo.solutions.mealplanner.server.repository.impl.MealsRepositoryImpl;
import org.kdepo.solutions.mealplanner.server.repository.impl.RecipesRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/shopping")
public class ShoppingController {

    @Autowired
    private DaysRepositoryImpl daysRepository;

    @Autowired
    private RecipesRepositoryImpl recipesRepository;

    @Autowired
    private MealsRepositoryImpl mealsRepository;

    @GetMapping()
    public String showDeviceDetailsPage(@RequestParam("weekId") Optional<String> weekId,
                                        @RequestParam("portions") Optional<String> portions) {
        System.out.println("[WEB]" + " GET " + "/shopping?weekId=" + weekId + "&portions=" + portions + " -> redirect to recipes");

//        if (weekId.isEmpty() || portions.isEmpty()) {
//            return "redirect:/home";
//        }
//
//        int weekIdInt = Integer.parseInt(weekId.get());
//
//        // Calculations
//        List<Recipe> allRecipes = new ArrayList<>();
//        for (Day day : daysRepository.getAllDaysFromWeek(weekIdInt)) {
//            for (Meal meal : mealsRepository.getAllMealsFromDay(day.getDayId())) {
//                allRecipes.addAll(recipesRepository.getAllRecipesFromMeal(meal.getMealId()));
//            }
//        }
//
//        return "shopping";

        return "redirect:/recipes";
    }
}
