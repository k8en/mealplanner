package org.kdepo.solutions.mealplanner.controller;

import org.kdepo.solutions.mealplanner.model.Profile;
import org.kdepo.solutions.mealplanner.repository.MealPlannerProfilesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/profiles")
public class ProfilesController {

    @Autowired
    private MealPlannerProfilesRepository profilesRepository;

    @GetMapping
    public String showRecipesListPage(Model model) {
        System.out.println("[WEB]" + " GET " + "/profiles");

        List<Profile> profiles = profilesRepository.getAllProfiles();
        model.addAttribute("profiles", profiles);

        return "profiles_list";
    }

}
