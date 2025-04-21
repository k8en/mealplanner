package org.kdepo.solutions.mealplanner.server.controller;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.server.dto.DayDto;
import org.kdepo.solutions.mealplanner.server.dto.MealDto;
import org.kdepo.solutions.mealplanner.server.dto.RecipeDto;
import org.kdepo.solutions.mealplanner.server.dto.WeekDto;
import org.kdepo.solutions.mealplanner.server.service.OperationsControlService;
import org.kdepo.solutions.mealplanner.server.service.OperationsLogService;
import org.kdepo.solutions.mealplanner.shared.Constants;
import org.kdepo.solutions.mealplanner.shared.model.Day;
import org.kdepo.solutions.mealplanner.shared.model.Meal;
import org.kdepo.solutions.mealplanner.shared.model.Profile;
import org.kdepo.solutions.mealplanner.shared.model.Recipe;
import org.kdepo.solutions.mealplanner.shared.model.Week;
import org.kdepo.solutions.mealplanner.shared.repository.DaysRepository;
import org.kdepo.solutions.mealplanner.shared.repository.MealsRepository;
import org.kdepo.solutions.mealplanner.shared.repository.PrimaryKeysRepository;
import org.kdepo.solutions.mealplanner.shared.repository.ProfilesRepository;
import org.kdepo.solutions.mealplanner.shared.repository.RecipesRepository;
import org.kdepo.solutions.mealplanner.shared.repository.WeeksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/profiles")
public class ProfilesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfilesController.class);

    private static final String PK = "profile_id";

    @Autowired
    private DaysRepository daysRepository;

    @Autowired
    private MealsRepository mealsRepository;

    @Autowired
    private PrimaryKeysRepository primaryKeysRepository;

    @Autowired
    private ProfilesRepository profilesRepository;

    @Autowired
    private RecipesRepository recipesRepository;

    @Autowired
    private WeeksRepository weeksRepository;

    @Autowired
    private OperationsControlService controlService;

    @Autowired
    private OperationsLogService logService;

    @GetMapping
    public String showProfilesListPage(Model model) {
        LOGGER.trace("[WEB] GET /profiles");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show profiles list page: anonymous user cannot read profiles list");
            return "redirect:/recipes";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Prepare entities
        List<Profile> profiles = profilesRepository.getAllProfiles();
        model.addAttribute("profiles", profiles);

        return "profiles_list";
    }

    @GetMapping("/{id}")
    public String showProfileDetailsPage(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /profiles/{}", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show profile details page: anonymous user cannot read profile details");
            return "redirect:/profiles";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        Profile profile = profilesRepository.getProfile(id);
        if (profile == null) {
            LOGGER.warn("[WEB] Cannot show profile details page: profile {} was not found", id);
            return "redirect:/profiles";
        }

        if (!controlService.canReadProfile(userName, profile.getProfileId())) {
            LOGGER.warn("[WEB] Cannot show profile details page: user '{}' has no access to profile {}", userName, id);
            return "redirect:/profiles";
        }

        // Prepare entities
        model.addAttribute("profile", profile);

        if (Constants.ProfileType.DAYS_WITHOUT_GROUPING.equals(profile.getProfileTypeId())) {
            List<Day> daysList = daysRepository.getAllDaysFromProfile(profile.getProfileId());
            List<DayDto> days = new ArrayList<>();
            for (Day day : daysList) {
                DayDto dayDto = new DayDto();
                dayDto.setDayId(day.getDayId());
                dayDto.setName(day.getName());

                List<Meal> mealsList = mealsRepository.getAllMealsFromDay(day.getDayId());
                List<MealDto> meals = new ArrayList<>();
                for (Meal meal : mealsList) {
                    MealDto mealDto = new MealDto();
                    mealDto.setMealId(meal.getMealId());
                    mealDto.setName(meal.getName());

                    List<Recipe> recipesList = recipesRepository.getAllRecipesFromMeal(meal.getMealId());
                    List<RecipeDto> recipes = new ArrayList<>();
                    for (Recipe recipe : recipesList) {
                        RecipeDto recipeDto = new RecipeDto();
                        recipeDto.setRecipeId(recipe.getRecipeId());
                        recipeDto.setName(recipe.getName());

                        recipes.add(recipeDto);
                    }

                    mealDto.setRecipes(recipes);
                    meals.add(mealDto);
                }

                dayDto.setMeals(meals);
                days.add(dayDto);
            }
            model.addAttribute("days", days);

        } else if (Constants.ProfileType.DAYS_GROUPED_BY_WEEKS.equals(profile.getProfileTypeId())) {
            List<Week> weeksList = weeksRepository.getAllWeeksFromProfile(profile.getProfileId());
            List<WeekDto> weeks = new ArrayList<>();
            for (Week week : weeksList) {
                WeekDto weekDto = new WeekDto();
                weekDto.setWeekId(week.getWeekId());
                weekDto.setName(week.getName());

                List<Day> daysList = daysRepository.getAllDaysFromWeek(week.getWeekId());
                List<DayDto> days = new ArrayList<>();
                for (Day day : daysList) {
                    DayDto dayDto = new DayDto();
                    dayDto.setDayId(day.getDayId());
                    dayDto.setName(day.getName());

                    List<Meal> mealsList = mealsRepository.getAllMealsFromDay(day.getDayId());
                    List<MealDto> meals = new ArrayList<>();
                    for (Meal meal : mealsList) {
                        MealDto mealDto = new MealDto();
                        mealDto.setMealId(meal.getMealId());
                        mealDto.setName(meal.getName());

                        List<Recipe> recipesList = recipesRepository.getAllRecipesFromMeal(meal.getMealId());
                        List<RecipeDto> recipes = new ArrayList<>();
                        for (Recipe recipe : recipesList) {
                            RecipeDto recipeDto = new RecipeDto();
                            recipeDto.setRecipeId(recipe.getRecipeId());
                            recipeDto.setName(recipe.getName());

                            recipes.add(recipeDto);
                        }

                        mealDto.setRecipes(recipes);
                        meals.add(mealDto);
                    }

                    dayDto.setMeals(meals);
                    days.add(dayDto);
                }
                weekDto.setDays(days);
                weeks.add(weekDto);
            }

            model.addAttribute("weeks", weeks);
        }

        return "profile_details";
    }

    @GetMapping("/create")
    public String showProfileCreationForm(Model model) {
        LOGGER.trace("[WEB] GET /profiles/create");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show profile creation form: anonymous user cannot create profiles");
            return "redirect:/profiles";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        if (!controlService.canCreateProfile(userName)) {
            LOGGER.warn("[WEB] Cannot show profile creation form: user '{}' cannot create profiles", userName);
            return "redirect:/profiles";
        }

        // Prepare entity with default values
        Profile profile = new Profile();
        profile.setProfileId(-1);
        profile.setProfileTypeId(Constants.ProfileType.UNDEFINED);
        profile.setActive(false);

        model.addAttribute("profile", profile);

        return "profile_create";
    }

    @PostMapping("/create")
    public String acceptProfileCreationForm(@Valid Profile profile, BindingResult result) {
        LOGGER.trace("[WEB] POST /profiles/create");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept profile creation form: anonymous user cannot create profiles");
            return "redirect:/profiles";
        }

        // Operation availability checks
        if (!controlService.canCreateProfile(userName)) {
            LOGGER.warn("[WEB] Cannot accept profile creation form: user '{}' cannot create profiles", userName);
            return "redirect:/profiles";
        }

        // Validate that provided data is correct
        String profileName = profile.getName();
        if (profileName == null || profileName.isEmpty()) {
            FieldError fieldError = new FieldError("profile", "name", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "profile_create";
        }

        if (profileName.length() > 50) {
            FieldError fieldError = new FieldError("profile", "name", "Название не может быть длиннее 50 символов!");
            result.addError(fieldError);
            return "profile_create";
        }

        // Adjust profile type id
        if (!Constants.ProfileType.DAYS_WITHOUT_GROUPING.equals(profile.getProfileTypeId())
                && !Constants.ProfileType.DAYS_GROUPED_BY_WEEKS.equals(profile.getProfileTypeId())) {
            profile.setProfileTypeId(Constants.ProfileType.UNDEFINED);
        }

        // Generate primary key for new entity
        Integer profileId = primaryKeysRepository.getNextVal(PK);
        primaryKeysRepository.moveNextVal(PK);
        profile.setProfileId(profileId);

        List<Profile> profiles = profilesRepository.getAllProfiles();
        boolean active = profiles.isEmpty();

        // Create entity
        Profile createdProfile = profilesRepository.addProfile(
                profile.getProfileId(),
                profile.getProfileTypeId(),
                profile.getName(),
                active
        );

        // Register operation in system events log
        logService.registerProfileCreated(userName, createdProfile);

        return "redirect:/profiles/" + profile.getProfileId();
    }

    @GetMapping("/{id}/update")
    public String showProfileModificationForm(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /profiles/{}/update", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show profile modification form: anonymous user cannot modify profiles");
            return "redirect:/profiles/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Profile profile = profilesRepository.getProfile(id);
        if (profile == null) {
            LOGGER.warn("[WEB] Cannot show profile modification form: profile {} was not found", id);
            return "redirect:/profiles";
        }

        if (!controlService.canModifyProfile(userName, profile.getProfileId())) {
            LOGGER.warn("[WEB] Cannot show profile modification form: user '{}' has no access to profile {} modification", userName, id);
            return "redirect:/profiles/" + profile.getProfileId();
        }

        model.addAttribute("profile", profile);

        return "profile_update";
    }

    @PostMapping("/{id}/update")
    public String acceptProfileModificationForm(@Valid Profile profile, @PathVariable Integer id, BindingResult result) {
        LOGGER.trace("[WEB] POST /profiles/{}/update", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept profile modification form: anonymous user cannot modify profiles");
            return "redirect:/profiles/" + id;
        }

        Profile profileFromDb = profilesRepository.getProfile(profile.getProfileId());
        if (profileFromDb == null) {
            LOGGER.warn("[WEB] Cannot accept profile modification form: profile {} was not found", id);
            return "redirect:/profiles";
        }

        if (!controlService.canModifyProfile(userName, profileFromDb.getProfileId())) {
            LOGGER.warn("[WEB] Cannot accept profile modification form: user '{}' has no access to profile {} modification", userName, id);
            return "redirect:/profiles/" + profileFromDb.getProfileId();
        }

        // Validate that provided data is correct
        String profileName = profile.getName();
        if (profileName == null || profileName.isEmpty()) {
            FieldError nameFieldError = new FieldError("profile", "name", "Поле не может быть пустым!");
            result.addError(nameFieldError);
            return "profile_update";
        }

        if (profileName.length() > 50) {
            FieldError nameFieldError = new FieldError("profile", "name", "Название не может быть длиннее 50 символов!");
            result.addError(nameFieldError);
            return "profile_update";
        }

        profile.setActive(profileFromDb.getActive()); // Because this field is not present on the UI

        // Update entity
        profilesRepository.updateProfile(
                profile.getProfileId(),
                profileFromDb.getProfileTypeId(),
                profile.getName(),
                profile.getActive()
        );

        // Register operation in system events log
        logService.registerProfileUpdated(userName, profileFromDb, profile);

        return "redirect:/profiles/" + profile.getProfileId();
    }

    @GetMapping("/{id}/delete")
    public String showProfileDeletionForm(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /profiles/{}/delete", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show profile deletion form: anonymous user cannot delete profiles");
            return "redirect:/profiles/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Profile profile = profilesRepository.getProfile(id);
        if (profile == null) {
            LOGGER.warn("[WEB] Cannot show profile deletion form: profile {} was not found", id);
            return "redirect:/profiles";
        }

        if (!controlService.canDeleteProfile(userName, profile.getProfileId())) {
            LOGGER.warn("[WEB] Cannot show profile deletion form: user '{}' has no access to profile {} deletion", userName, id);
            return "redirect:/profiles/" + profile.getProfileId();
        }

        model.addAttribute("profile", profile);

        return "profile_delete";
    }

    @PostMapping("/{id}/delete")
    public String acceptProfileDeletionForm(@PathVariable Integer id) {
        LOGGER.trace("[WEB] POST /profiles/{}/delete", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept profile deletion form: anonymous user cannot delete profiles");
            return "redirect:/profiles/" + id;
        }

        Profile profileFromDb = profilesRepository.getProfile(id);
        if (profileFromDb == null) {
            LOGGER.warn("[WEB] Cannot accept profile deletion form: profile {} was not found", id);
            return "redirect:/profiles";
        }

        if (!controlService.canDeleteProfile(userName, profileFromDb.getProfileId())) {
            LOGGER.warn("[WEB] Cannot accept profile deletion form: user '{}' has no access to profile {} deletion", userName, id);
            return "redirect:/profiles/" + profileFromDb.getProfileId();
        }

        // Delete entity
        profilesRepository.deleteProfile(profileFromDb.getProfileId());

        if (profileFromDb.getActive()) {
            List<Profile> allProfiles = profilesRepository.getAllProfiles();
            if (!allProfiles.isEmpty()) {
                Profile nextActiveProfile = allProfiles.get(0);
                nextActiveProfile.setActive(true);
                profilesRepository.updateProfile(
                        nextActiveProfile.getProfileId(),
                        nextActiveProfile.getProfileTypeId(),
                        nextActiveProfile.getName(),
                        nextActiveProfile.getActive()
                );
            }
        }

        // Register operation in system events log
        logService.registerProfileDeleted(userName, profileFromDb.getProfileId());

        return "redirect:/profiles";
    }

    @GetMapping("/{id}/active")
    public String setProfileActive(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /profiles/{}/active", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot set profile active: anonymous user cannot modify profiles");
            return "redirect:/profiles/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Profile profile = profilesRepository.getProfile(id);
        if (profile == null) {
            LOGGER.warn("[WEB] Cannot set profile active: profile {} was not found", id);
            return "redirect:/profiles";
        }

        if (!controlService.canModifyProfile(userName, profile.getProfileId())) {
            LOGGER.warn("[WEB] Cannot set profile active: user '{}' has no access to profile {} modification", userName, id);
            return "redirect:/profiles/" + profile.getProfileId();
        }

        List<Profile> allProfiles = profilesRepository.getAllProfiles();
        List<Profile> activeProfiles = allProfiles.stream()
                .filter(Profile::getActive)
                .toList();

        for (Profile activeProfile : activeProfiles) {
            if (!profile.getProfileId().equals(activeProfile.getProfileId())) {
                activeProfile.setActive(false);
                profilesRepository.updateProfile(
                        activeProfile.getProfileId(),
                        activeProfile.getProfileTypeId(),
                        activeProfile.getName(),
                        activeProfile.getActive()
                );
            }
        }

        if (!profile.getActive()) {
            profile.setActive(true);
            profilesRepository.updateProfile(
                    profile.getProfileId(),
                    profile.getProfileTypeId(),
                    profile.getName(),
                    profile.getActive()
            );
        }

        model.addAttribute("profile", profile);

        return "redirect:/profiles/" + id;
    }
}
