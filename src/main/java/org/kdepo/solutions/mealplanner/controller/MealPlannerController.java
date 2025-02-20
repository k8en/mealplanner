package org.kdepo.solutions.mealplanner.controller;

import org.kdepo.solutions.mealplanner.dto.DayDto;
import org.kdepo.solutions.mealplanner.dto.MealDto;
import org.kdepo.solutions.mealplanner.dto.ProfileDto;
import org.kdepo.solutions.mealplanner.dto.RecipeDto;
import org.kdepo.solutions.mealplanner.dto.WeekDto;
import org.kdepo.solutions.mealplanner.model.Day;
import org.kdepo.solutions.mealplanner.model.Meal;
import org.kdepo.solutions.mealplanner.model.Profile;
import org.kdepo.solutions.mealplanner.model.Recipe;
import org.kdepo.solutions.mealplanner.model.Week;
import org.kdepo.solutions.mealplanner.repository.MealPlannerDaysRepository;
import org.kdepo.solutions.mealplanner.repository.MealPlannerMealsRepository;
import org.kdepo.solutions.mealplanner.repository.MealPlannerProfilesRepository;
import org.kdepo.solutions.mealplanner.repository.MealPlannerRecipesRepository;
import org.kdepo.solutions.mealplanner.repository.MealPlannerWeeksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/mealplanner")
public class MealPlannerController {

    @Autowired
    private MealPlannerDaysRepository daysRepository;

    @Autowired
    private MealPlannerMealsRepository mealsRepository;

    @Autowired
    private MealPlannerProfilesRepository profilesRepository;

    @Autowired
    private MealPlannerRecipesRepository recipesRepository;

    @Autowired
    private MealPlannerWeeksRepository weeksRepository;

    @GetMapping
    public String showProfilesListPage(Model model,
                                       @RequestParam("profileId") Optional<String> profileId) {
        System.out.println("[WEB]" + " GET " + "/mealplanner"
                + (profileId.map(s -> "?profileId=" + s).orElse("")));

        List<Profile> profiles = profilesRepository.getAllProfiles();
        model.addAttribute("profiles", profiles);

        Integer selectedProfileId = null;
        if (profileId.isPresent()) {
            try {
                selectedProfileId = Integer.parseInt(profileId.get());
            } catch (Exception e) {
                System.out.println("Cannot convert to profileId value: " + profileId.get());
            }
            model.addAttribute("profileId", selectedProfileId);
        }

        if (selectedProfileId == null && !profiles.isEmpty()) {
            selectedProfileId = profiles.get(0).getProfileId();
        }

        Profile profile = null;
        for (Profile p : profiles) {
            if (p.getProfileId().equals(selectedProfileId)) {
                profile = p;
                break;
            }
        }

        ProfileDto profileDto = null;
        if (profile != null) {
            profileDto = new ProfileDto();
            profileDto.setProfileId(profile.getProfileId());
            profileDto.setName(profile.getName());

            List<Week> weeks = weeksRepository.getAllWeeksFromProfile(profile.getProfileId());
            List<WeekDto> weekDtoList = new ArrayList<>();
            for (Week week : weeks) {
                WeekDto weekDto = new WeekDto();
                weekDto.setWeekId(week.getWeekId());
                weekDto.setName(week.getName());

                List<Day> days = daysRepository.getAllDaysFromWeek(week.getWeekId());
                List<DayDto> dayDtoList = new ArrayList<>();
                for (Day day : days) {
                    DayDto dayDto = new DayDto();
                    dayDto.setName(day.getName());

                    List<Meal> meals = mealsRepository.getAllMealsFromDay(day.getDayId());
                    List<MealDto> mealDtoList = new ArrayList<>();
                    for (Meal meal : meals) {
                        MealDto mealDto = new MealDto();
                        mealDto.setName(meal.getName());

                        List<Recipe> recipes = recipesRepository.getAllRecipesFromMeal(meal.getMealId());
                        List<RecipeDto> recipeDtoList = new ArrayList<>();
                        for (Recipe recipe : recipes) {
                            RecipeDto recipeDto = new RecipeDto();
                            recipeDto.setRecipeId(recipe.getRecipeId());
                            recipeDto.setName(recipe.getName());
                            recipeDtoList.add(recipeDto);
                        }
                        mealDto.setRecipes(recipeDtoList);
                        mealDtoList.add(mealDto);
                    }
                    dayDto.setMeals(mealDtoList);
                    dayDtoList.add(dayDto);
                }
                weekDto.setDays(dayDtoList);
                weekDtoList.add(weekDto);
            }
            profileDto.setWeeks(weekDtoList);

            model.addAttribute("detailedProfile", profileDto);
        }

        return "mealplanner";
    }

}
