package org.kdepo.solutions.mealplanner.server.controller;

import org.kdepo.solutions.mealplanner.shared.repository.DaysRepository;
import org.kdepo.solutions.mealplanner.shared.repository.MealsRepository;
import org.kdepo.solutions.mealplanner.shared.repository.MenusRepository;
import org.kdepo.solutions.mealplanner.shared.repository.RecipesRepository;
import org.kdepo.solutions.mealplanner.shared.repository.WeeksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomePageController {

    @Autowired
    private DaysRepository daysRepository;

    @Autowired
    private MealsRepository mealsRepository;

    @Autowired
    private MenusRepository menusRepository;

    @Autowired
    private RecipesRepository recipesRepository;

    @Autowired
    private WeeksRepository weeksRepository;

    @GetMapping
    public String showHomePage() {
        return "redirect:/recipes";
    }

}
