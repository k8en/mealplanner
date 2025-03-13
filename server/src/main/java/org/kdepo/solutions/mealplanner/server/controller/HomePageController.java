package org.kdepo.solutions.mealplanner.server.controller;

import org.kdepo.solutions.mealplanner.shared.repository.DaysRepository;
import org.kdepo.solutions.mealplanner.shared.repository.MealsRepository;
import org.kdepo.solutions.mealplanner.shared.repository.ProfilesRepository;
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
    private ProfilesRepository profilesRepository;

    @Autowired
    private RecipesRepository recipesRepository;

    @Autowired
    private WeeksRepository weeksRepository;

    @GetMapping
    public String showHomePage() {
        return "redirect:/recipes";
    }

//    @GetMapping
//    public String showProfilesListPage(Model model,
//                                       @RequestParam("profileId") Optional<String> profileId) {
//        System.out.println("[WEB]" + " GET " + "/" + (profileId.map(s -> "?profileId=" + s).orElse("")));
//
////        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
////        if (!(authentication instanceof AnonymousAuthenticationToken)) {
////            String currentUserName = authentication.getName();
////            System.out.println("Logged as " + currentUserName);
////        } else {
////            System.out.println("Logged as ANONYMOUS");
////        }
//
//        List<Profile> profiles = profilesRepository.getAllProfiles();
//        model.addAttribute("profiles", profiles);
//
//        Integer selectedProfileId = null;
//        if (profileId.isPresent()) {
//            try {
//                selectedProfileId = Integer.parseInt(profileId.get());
//            } catch (Exception e) {
//                System.out.println("Cannot convert to profileId value: " + profileId.get());
//            }
//            model.addAttribute("profileId", selectedProfileId);
//        }
//
//        if (selectedProfileId == null && !profiles.isEmpty()) {
//            selectedProfileId = profiles.get(0).getProfileId();
//        }
//
//        Profile profile = null;
//        for (Profile p : profiles) {
//            if (p.getProfileId().equals(selectedProfileId)) {
//                profile = p;
//                break;
//            }
//        }
//
//        ProfileDto profileDto = null;
//        if (profile != null) {
//            profileDto = new ProfileDto();
//            profileDto.setProfileId(profile.getProfileId());
//            profileDto.setName(profile.getName());
//
//            List<Week> weeks = weeksRepository.getAllWeeksFromProfile(profile.getProfileId());
//            List<WeekDto> weekDtoList = new ArrayList<>();
//            for (Week week : weeks) {
//                WeekDto weekDto = new WeekDto();
//                weekDto.setWeekId(week.getWeekId());
//                weekDto.setName(week.getName());
//
//                List<Day> days = daysRepository.getAllDaysFromWeek(week.getWeekId());
//                List<DayDto> dayDtoList = new ArrayList<>();
//                for (Day day : days) {
//                    DayDto dayDto = new DayDto();
//                    dayDto.setName(day.getName());
//
//                    List<Meal> meals = mealsRepository.getAllMealsFromDay(day.getDayId());
//                    List<MealDto> mealDtoList = new ArrayList<>();
//                    for (Meal meal : meals) {
//                        MealDto mealDto = new MealDto();
//                        mealDto.setName(meal.getName());
//
//                        List<Recipe> recipes = recipesRepository.getAllRecipesFromMeal(meal.getMealId());
//                        List<RecipeDto> recipeDtoList = new ArrayList<>();
//                        for (Recipe recipe : recipes) {
//                            RecipeDto recipeDto = new RecipeDto();
//                            recipeDto.setRecipeId(recipe.getRecipeId());
//                            recipeDto.setName(recipe.getName());
//                            recipeDtoList.add(recipeDto);
//                        }
//                        mealDto.setRecipes(recipeDtoList);
//                        mealDtoList.add(mealDto);
//                    }
//                    dayDto.setMeals(mealDtoList);
//                    dayDtoList.add(dayDto);
//                }
//                weekDto.setDays(dayDtoList);
//                weekDtoList.add(weekDto);
//            }
//            profileDto.setWeeks(weekDtoList);
//
//            model.addAttribute("detailedProfile", profileDto);
//        }
//
//        return "mealplanner";
//    }

}
