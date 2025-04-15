package org.kdepo.solutions.mealplanner.server.controller;

import jakarta.validation.Valid;
import org.kdepo.solutions.mealplanner.server.dto.MealDto;
import org.kdepo.solutions.mealplanner.server.dto.RecipeDto;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/days")
public class DaysController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DaysController.class);

    private static final String PK = "day_id";

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

    @GetMapping("/{id}")
    public String showDayDetailsPage(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /days/{}", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = null;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        Day day = daysRepository.getDay(id);
        if (day == null) {
            LOGGER.warn("[WEB] Cannot show day details page: day {} was not found", id);
            return "redirect:/profiles";
        }

        if (!controlService.canReadDay(userName, day.getDayId())) {
            LOGGER.warn("[WEB] Cannot show day details page: user '{}' has no access to day {}", userName, id);
            return "redirect:/profiles";
        }

        // Prepare entities
        model.addAttribute("day", day);

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
        model.addAttribute("meals", meals);

        return "day_details";
    }

    @GetMapping("/create")
    public String showDayCreationForm(Model model,
                                      @RequestParam(value = "profile_id", required = false) Integer profileId,
                                      @RequestParam(value = "week_id", required = false) Integer weekId) {
        LOGGER.trace("[WEB] GET /days/create?profile_id={}&week_id={}", profileId, weekId);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show day creation form: anonymous users cannot create days");
            return "redirect:/profiles";
        }
        model.addAttribute("isLoggedIn", userName != null);

        // Operation availability checks
        if (!controlService.canCreateDay(userName)) {
            LOGGER.warn("[WEB] Cannot show day creation form: user '{}' cannot create days", userName);
            return "redirect:/profiles";
        }

        if (profileId == null) {
            LOGGER.warn("[WEB] Cannot show day creation form: profile is not provided for day creation");
            return "redirect:/profiles";
        }

        Profile profile = profilesRepository.getProfile(profileId);
        if (profile == null) {
            LOGGER.warn("[WEB] Cannot show day creation form: profile {} was not found", profileId);
            return "redirect:/profiles";
        }

        Week week = null;
        if (weekId != null) {
            week = weeksRepository.getWeek(weekId);
            if (week == null) {
                LOGGER.warn("[WEB] Cannot show day creation form: week {} was not found", weekId);
                return "redirect:/profiles";
            }
        }

        if (week != null && Constants.ProfileType.DAYS_WITHOUT_GROUPING.equals(profile.getProfileTypeId())) {
            LOGGER.warn("[WEB] Cannot show day creation form: profile {} type mismatch - week is provided, but this this is DAYS_WITHOUT_GROUPING", profileId);
            return "redirect:/profiles";
        }

        if (week == null && Constants.ProfileType.DAYS_GROUPED_BY_WEEKS.equals(profile.getProfileTypeId())) {
            LOGGER.warn("[WEB] Cannot show day creation form: profile {} type mismatch - week is not provided, but this is DAYS_GROUPED_BY_WEEKS", profileId);
            return "redirect:/profiles";
        }

        model.addAttribute("profile", profile);
        model.addAttribute("week", week);

        String name;
        Integer orderNumber;
        if (week == null) {
            List<Day> daysList = daysRepository.getAllDaysFromProfile(profileId);
            orderNumber = daysList.size() + 1;
            name = "День " + orderNumber;
        } else {
            List<Day> daysList = daysRepository.getAllDaysFromWeek(weekId);
            orderNumber = daysList.size() + 1;
            if (orderNumber >= 8) {
                LOGGER.warn("[WEB] Cannot show day creation form: all days for week {} are created already", weekId);
                return "redirect:/weeks/" + weekId;
            }

            if (orderNumber == 1) {
                name = "Понедельник";
            } else if (orderNumber == 2) {
                name = "Вторник";
            } else if (orderNumber == 3) {
                name = "Среда";
            } else if (orderNumber == 4) {
                name = "Четверг";
            } else if (orderNumber == 5) {
                name = "Пятница";
            } else if (orderNumber == 6) {
                name = "Суббота";
            } else {
                name = "Воскресенье";
            }
        }

        // Prepare entity with default values
        Day day = new Day();
        day.setDayId(-1);
        day.setProfileId(profileId);
        day.setWeekId(weekId);
        day.setName(name);
        day.setOrderNumber(orderNumber);

        model.addAttribute("day", day);

        return "day_create";
    }

    @PostMapping("/create")
    public String acceptDayCreationForm(@Valid Day day, BindingResult result) {
        LOGGER.trace("[WEB] POST /days/create");

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept day creation form: anonymous users cannot create days");
            return "redirect:/profiles";
        }

        // Operation availability checks
        if (!controlService.canCreateDay(userName)) {
            LOGGER.warn("[WEB] Cannot accept day creation form: user '{}' cannot create days", userName);
            return "redirect:/profiles";
        }

        // Validate that provided data is correct
        String dayName = day.getName();
        if (dayName == null || dayName.isEmpty()) {
            FieldError fieldError = new FieldError("day", "name", "Поле не может быть пустым!");
            result.addError(fieldError);
            return "day_create";
        }

        if (dayName.length() > 20) {
            FieldError fieldError = new FieldError("day", "name", "Название не может быть длиннее 20 символов!");
            result.addError(fieldError);
            return "day_create";
        }

        Integer profileId = day.getProfileId();
        Integer weekId = day.getWeekId();

        if (profileId == null) {
            LOGGER.warn("[WEB] Profile is not provided for day creation");
            return "redirect:/profiles";
        }

        Profile profile = profilesRepository.getProfile(profileId);
        if (profile == null) {
            LOGGER.warn("[WEB] Profile {} was not found", profileId);
            return "redirect:/profiles";
        }

        if (!controlService.canReadProfile(userName, profileId)) {
            LOGGER.warn("[WEB] Cannot accept day creation form: user '{}' has no access to profile {}", userName, profileId);
            return "redirect:/profiles";
        }

        int profileTypeId;
        if (weekId != null) {
            // Split by weeks
            Week week = weeksRepository.getWeek(weekId);
            if (week == null) {
                LOGGER.warn("[WEB] Cannot accept day creation form: week {} was not found", weekId);
                return "redirect:/profiles/" + profile.getProfileId();
            }
            day.setWeekId(weekId);

            profileTypeId = Constants.ProfileType.DAYS_GROUPED_BY_WEEKS;

            List<Day> daysList = daysRepository.getAllDaysFromWeek(weekId);
            int orderNumber = daysList.size() + 1;

            if (orderNumber >= 8) {
                FieldError fieldError = new FieldError("day", "name", "В неделе не может быть больше 7 дней!");
                result.addError(fieldError);
                return "day_create";
            }

            day.setOrderNumber(orderNumber);

        } else {
            // Split by days
            profileTypeId = Constants.ProfileType.DAYS_WITHOUT_GROUPING;

            List<Day> daysList = daysRepository.getAllDaysFromProfile(profileId);
            int orderNumber = daysList.size() + 1;
            day.setOrderNumber(orderNumber);

        }

        // Generate primary key for new entity
        Integer dayId = primaryKeysRepository.getNextVal(PK);
        primaryKeysRepository.moveNextVal(PK);
        day.setDayId(dayId);

        // Create entity
        Day createdDay = daysRepository.addDay(
                day.getDayId(),
                day.getProfileId(),
                day.getWeekId(),
                day.getName(),
                day.getOrderNumber()
        );

        // Register operation in system events log
        logService.registerDayCreated(userName, createdDay);

        // Check and update profile if necessary
        if (Constants.ProfileType.UNDEFINED.equals(profile.getProfileTypeId())) {
            Profile oldData = profilesRepository.getProfile(profileId);

            profile.setProfileTypeId(profileTypeId);
            profilesRepository.updateProfile(
                    profile.getProfileId(),
                    profile.getProfileTypeId(),
                    profile.getName(),
                    profile.getActive()
            );

            // Register operation in system events log
            logService.registerProfileUpdated(userName, oldData, profile);
        }

        return "redirect:/profiles/" + profile.getProfileId();
    }

    @GetMapping("/{id}/update")
    public String showDayModificationForm(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /days/{}/update", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show day modification form: anonymous user cannot modify days");
            return "redirect:/days/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Day day = daysRepository.getDay(id);
        if (day == null) {
            LOGGER.warn("[WEB] Cannot show day modification form: day {} was not found", id);
            return "redirect:/profiles";
        }

        if (!controlService.canModifyDay(userName, day.getDayId())) {
            LOGGER.warn("[WEB] Cannot show day modification form: user '{}' has no access to day {} modification", userName, id);
            return "redirect:/days/" + day.getDayId();
        }

        model.addAttribute("day", day);

        return "day_update";
    }

    @PostMapping("/{id}/update")
    public String acceptDayModificationForm(@Valid Day day, @PathVariable Integer id, BindingResult result) {
        LOGGER.trace("[WEB] POST /days/{}/update", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept day modification form: anonymous user cannot modify days");
            return "redirect:/days/" + id;
        }

        Day dayFromDb = daysRepository.getDay(day.getDayId());
        if (dayFromDb == null) {
            LOGGER.warn("[WEB] Cannot accept day modification form: day {} was not found", id);
            return "redirect:/profiles";
        }

        if (!day.getDayId().equals(id)) {
            LOGGER.warn("[WEB] Cannot accept day modification form: day id mismatch: {} and {}", day.getDayId(), id);
            return "redirect:/profiles";
        }

        if (!controlService.canModifyDay(userName, dayFromDb.getDayId())) {
            LOGGER.warn("[WEB] Cannot accept day modification form: user '{}' has no access to day {} modification", userName, id);
            return "redirect:/days/" + dayFromDb.getDayId();
        }

        // Validate that provided data is correct
        String dayName = day.getName();
        if (dayName == null || dayName.isEmpty()) {
            FieldError nameFieldError = new FieldError("day", "name", "Поле не может быть пустым!");
            result.addError(nameFieldError);
            return "day_update";
        }

        if (dayName.length() > 20) {
            FieldError nameFieldError = new FieldError("day", "name", "Название не может быть длиннее 20 символов!");
            result.addError(nameFieldError);
            return "day_update";
        }

        // Adjust not editable data
        day.setProfileId(dayFromDb.getProfileId());
        day.setWeekId(dayFromDb.getWeekId());
        day.setOrderNumber(dayFromDb.getOrderNumber());

        // Update entity
        daysRepository.updateDay(
                day.getDayId(),
                day.getProfileId(),
                day.getWeekId(),
                day.getName(),
                day.getOrderNumber()
        );

        // Register operation in system events log
        logService.registerDayUpdated(userName, dayFromDb, day);

        return "redirect:/days/" + day.getDayId();
    }

    @GetMapping("/{id}/delete")
    public String showDayDeletionForm(@PathVariable Integer id, Model model) {
        LOGGER.trace("[WEB] GET /days/{}/delete", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
            model.addAttribute("userName", userName);
        } else {
            LOGGER.warn("[WEB] Cannot show day deletion form: anonymous user cannot delete days");
            return "redirect:/days/" + id;
        }
        model.addAttribute("isLoggedIn", userName != null);

        Day day = daysRepository.getDay(id);
        if (day == null) {
            LOGGER.warn("[WEB] Cannot show day deletion form: day {} was not found", id);
            return "redirect:/profiles";
        }

        if (!controlService.canDeleteDay(userName, day.getDayId())) {
            LOGGER.warn("[WEB] Cannot show day deletion form: user '{}' has no access to day {} deletion", userName, id);
            return "redirect:/days/" + day.getDayId();
        }

        model.addAttribute("day", day);

        return "day_delete";
    }

    @PostMapping("/{id}/delete")
    public String acceptDayDeletionForm(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        LOGGER.trace("[WEB] POST /days/{}/delete", id);

        // Authentication checks
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName;
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            userName = authentication.getName();
        } else {
            LOGGER.warn("[WEB] Cannot accept day deletion form: anonymous user cannot delete days");
            return "redirect:/days/" + id;
        }

        Day dayFromDb = daysRepository.getDay(id);
        if (dayFromDb == null) {
            LOGGER.warn("[WEB] Cannot accept day deletion form: day {} was not found", id);
            return "redirect:/profiles";
        }

        if (!controlService.canDeleteDay(userName, dayFromDb.getDayId())) {
            LOGGER.warn("[WEB] Cannot accept day deletion form: user '{}' has no access to day {} deletion", userName, id);
            return "redirect:/days/" + dayFromDb.getDayId();
        }

        Profile profile = profilesRepository.getProfile(dayFromDb.getProfileId());

        // Delete dependent entities
        List<Meal> mealsList = mealsRepository.getAllMealsFromDay(dayFromDb.getDayId());
        for (Meal meal : mealsList) {
            List<Recipe> recipesList = recipesRepository.getAllRecipesFromMeal(meal.getMealId());
            for (Recipe recipe : recipesList) {
                recipesRepository.deleteRecipeFromMeal(recipe.getRecipeId(), meal.getMealId());
            }
            mealsRepository.deleteMeal(meal.getMealId());
            // TODO register operations?
        }

        // Delete entity
        daysRepository.deleteDay(dayFromDb.getDayId());

        // Register operation in system events log
        logService.registerDayDeleted(userName, dayFromDb.getDayId());

        // Update profile type if no more days
        if (Constants.ProfileType.DAYS_WITHOUT_GROUPING.equals(profile.getProfileTypeId())) {
            List<Day> daysList = daysRepository.getAllDaysFromProfile(profile.getProfileId());
            if (daysList.isEmpty()) {
                Profile oldData = profilesRepository.getProfile(dayFromDb.getProfileId());

                profile.setProfileTypeId(Constants.ProfileType.UNDEFINED);
                profilesRepository.updateProfile(
                        profile.getProfileId(),
                        profile.getProfileTypeId(),
                        profile.getName(),
                        profile.getActive()
                );

                // Register operation in system events log
                logService.registerProfileUpdated(userName, oldData, profile);
            }
        }

        // Redirect depends on profile type
        if (Constants.ProfileType.UNDEFINED.equals(profile.getProfileTypeId())
                || Constants.ProfileType.DAYS_WITHOUT_GROUPING.equals(profile.getProfileTypeId())) {
            return "redirect:/profiles/" + profile.getProfileId();
        } else {
            return "redirect:/weeks/" + dayFromDb.getWeekId();
        }
    }
}
